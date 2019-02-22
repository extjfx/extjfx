/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.fxml;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * A default factory for FXML controllers.
 * <p>
 * This class supports a basic field injection using {@link Inject @Inject} annotation. It can be used to initialize and
 * configure controller, model or service instances, and bind them together. <br>
 * For every call of {@link #createController(Class)} method - a new controller instance is created, but all the
 * injected dependencies (models, services, etc) are instantiated only once, stored in cache and used as singletons.
 * <p>
 * The factory first makes an attempt to match field to be injected with System properties and then with optional
 * {@link #setPropertiesProvider(Function) properties provider}. The matching is done using {@link Named @Named}
 * annotation if present, otherwise using field's name.<br>
 * If no matching property is found, the factory consults dependencies cache trying find a match with the field by
 * <b>exact</b> type. Otherwise it requests {@link #setInstanceProvider(Function) instance provider} to supply a new
 * instance of given type, injects all its dependencies and puts into the cache.
 * <p>
 * For primitive and {@link Enum enum} fields, the class makes a conversion when necessary e.g. when an integer field's
 * value is injected from a System property (String). <br>
 * Example usage:
 *
 * <pre>
 * class PersonModel {
 *
 *     {@literal @}Inject
 *     {@literal @}Named("person.age.visible")
 *     boolean defaultShowAge;
 *
 *     {@literal @}Inject
 *     Side personDetailsPaneSide;
 * }
 *
 * class PersonService {
 *     Person findByName(String name) {
 *         // ...
 *     }
 * }
 *
 * class PersonController {
 *     {@literal @}Inject
 *     PersonModel model;
 *
 *     {@literal @}Inject
 *     PersonService service;
 *
 *     {@literal @}FXML
 *     void initialize() {
 *         // Initialize GUI with data from service, bind controls with model, etc.
 *     }
 * }
 * </pre>
 * <p>
 * The class is not synchronized. If multiple threads access it concurrently, it must be synchronized externally.
 * </p>
 */
public class DefaultControllerFactory {
    
    /**
     * Calls {@link Class#newInstance()} without any further checks.
     */
    private static final Function<Class<?>, Object> DEFAULT_INSTANCE_PROVIDER = clazz -> {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalStateException("Failed to instantiate " + clazz, ex);
        }
    };

    private Function<Class<?>, Object> instanceProvider = DEFAULT_INSTANCE_PROVIDER;
    private Function<String, Object> propertiesProvider;
    private final Map<Class<?>, Object> dependencies = new WeakHashMap<>();
    private Consumer<String> logger;
    
    // For easier debugging
    private int nestingLevel = 0;

    /**
     * Creates a new instance of the given controller class.
     *
     * @param <T> controller type
     * @param controllerClass class of the FXML controller used to locate the FXML file
     * @return the instance of the controller
     */
    public <T> T createController(Class<T> controllerClass) {
        try {
            return instanciateAndInjectDependencies(controllerClass);
        } finally {
            nestingLevel = 0;
        }
    }

    <T> T instanciateAndInjectDependencies(Class<T> clazz) {
        Objects.requireNonNull(clazz, "Controller class must not be null");

        log("Creating instance of " + clazz + " using " + instanceProvider);
        T controller = clazz.cast(instanceProvider.apply(clazz));
        nestingLevel++;
        injectDependencies(controller);
        nestingLevel--;
        return controller;
    }

    void injectDependencies(Object instance) {
        for (Field field : findInjectableFields(instance.getClass())) {
            injectFieldValue(field, instance);
        }
    }

    void injectFieldValue(Field field, Object instance) {
        log("Field to inject: " + field.getName() + " [" + field.getType().getName() + "]");
        nestingLevel++;
        Object fieldValue = getOrCreateFieldValue(field);
        if (fieldValue != null) {
            fieldValue = convertToFieldTypeIfNeeded(field, fieldValue);
            log("Injecting into " + instance + ": " + field.getName() + "=" + fieldValue);
            setField(field, instance, fieldValue);
        }
        nestingLevel--;
    }

    Object getOrCreateFieldValue(Field field) {
        String propertyName = getPropertyName(field);
        Object fieldValue = System.getProperty(propertyName);
        if (fieldValue != null) {
            log("Field value specified as system property [" + propertyName + "=" + fieldValue + "]");
            return fieldValue;
        }

        if (propertiesProvider != null) {
            fieldValue = propertiesProvider.apply(propertyName);
            if (fieldValue != null) {
                log("Field value [" + propertyName + "=" + fieldValue + "] provided by " + propertiesProvider);
                return fieldValue;
            }
        }

        Class<?> fieldType = field.getType();
        fieldValue = dependencies.get(fieldType);
        if (fieldValue != null) {
            log("Found dependency value: " + fieldValue);
            return fieldValue;
        }

        log("Dependency not found - trying to instantiate");
        if (fieldType.isPrimitive() || fieldType.equals(String.class) || fieldType.isEnum()) {
            log("Field type is primitive, String or Enum - skipping");
        } else {
            fieldValue = instanciateAndInjectDependencies(fieldType);
            setDependency(fieldType, fieldValue);
        }
        return fieldValue;
    }

    static String getPropertyName(Field field) {
        Named nameAnnotation = field.getAnnotation(Named.class);
        if (nameAnnotation == null || "".equals(nameAnnotation.value())) {
            return field.getName();
        }
        return nameAnnotation.value();
    }

    /**
     * Stores given dependency in a cache.
     *
     * @param type class of the dependency used for matching with injectable fields
     * @param dependency the instance to be injected into fields of specified type
     * @throws NullPointerException if the {@code type} is {@code null}
     */
    public void setDependency(Class<?> type, Object dependency) {
        log("Cache dependency: " + type.getName() + " -> " + dependency);
        dependencies.put(type, dependency);
    }

    /**
     * Clears dependencies cache.
     */
    public void clearDependencies() {
        log("clearDependencies() called");
        dependencies.clear();
    }

    /**
     * Returns the currently used instance provider.
     *
     * @return instance provider
     */
    public Function<Class<?>, Object> getInstanceProvider() {
        return instanceProvider;
    }

    /**
     * Sets the specified instance provider to be used by the factory to retrieve instances of necessary types.
     * <p>
     * By default it's initialized to {@link #DEFAULT_INSTANCE_PROVIDER}.
     * </p>
     *
     * @param instanceProvider the instance provider
     * @throws NullPointerException if given provider is {@code null}
     */
    public void setInstanceProvider(Function<Class<?>, Object> instanceProvider) {
        this.instanceProvider = requireNonNull(instanceProvider, "Instance provider must not be null");
    }

    /**
     * Returns currently used properties provider.
     *
     * @return properties provider
     */
    public Function<String, Object> getPropertiesProvider() {
        return propertiesProvider;
    }

    /**
     * Sets properties provider to be used.
     * <p>
     * Example:
     *
     * <pre>{@code 
     * Map<String, Object> props = new HashMap<>();
     * props.put("pool.size", 5);
     * setPropertiesProvider(props::get);
     * }</pre>
     *
     * @param propertiesProvider provider of properties (to be injected)
     */
    public void setPropertiesProvider(Function<String, Object> propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }
    
    /**
     * Installs logger receiving debug information.
     * 
     * @param logger consumer of debug logs
     */
    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    private void log(String msg) {
        if (logger != null) {
            logger.accept(nestingLevelIndentation() + msg);
        }
    }

    private String nestingLevelIndentation() {
        return Stream.generate(() -> "   ").limit(nestingLevel).collect(Collectors.joining());
    }

    // REFLECTION METHODS

    private static Set<Field> findInjectableFields(Class<?> clazz) {
        if (!injectAnnotationPresentOnClasspath()) {
            return Collections.emptySet();
        }
        Set<Field> annotatedFields = new LinkedHashSet<>();
        Class<?> classToSearch = clazz;
        while (classToSearch != null && classToSearch != Object.class) {
            Field[] fields = classToSearch.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    annotatedFields.add(field);
                }
            }
            classToSearch = classToSearch.getSuperclass();
        }
        return annotatedFields;
    }

    /**
     * The Inject annotation is optional. If client app uses it - it should add it to the classpath, otherwise we should
     * not require its presence.
     */
    private static boolean injectAnnotationPresentOnClasspath() {
        try {
            Class.forName("javax.inject.Inject");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private static void setField(Field field, Object instance, Object fieldValue) {
        try {
            field.setAccessible(true);
            field.set(instance, fieldValue);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            String fieldValueType = fieldValue == null ? null : fieldValue.getClass().getName();
            throw new IllegalStateException("Failed to set " + fieldValue + " [" + fieldValueType + "] to " + field
                    + " on instance " + instance, ex);
        }
    }

    private static Object convertToFieldTypeIfNeeded(Field field, Object fieldValue) {
        if (fieldValue == null) {
            return fieldValue;
        }
        Class<?> fieldType = wrapIfPrimitive(field.getType());
        Class<?> valueType = fieldValue.getClass();
        if (fieldType.isAssignableFrom(valueType)) {
            return fieldValue;
        }

        if (field.getType().isPrimitive() || field.getType().isEnum()) {
            return valueOf(field, fieldType, fieldValue);
        }
        // fall back - will fail on set
        return fieldValue;
    }

    private static Object valueOf(Field field, Class<?> fieldType, Object fieldValue) {
        try {
            Method valueOf = fieldType.getMethod("valueOf", String.class);
            return valueOf.invoke(null, fieldValue.toString());
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to convert " + fieldValue + " [" + fieldValue.getClass().getName() + "] to " + field, ex);
        }
    }

    // Found nothing in JDK and don't want to pull Guava just for this
    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<>();

    static {
        PRIMITIVES_TO_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVES_TO_WRAPPERS.put(byte.class, Byte.class);
        PRIMITIVES_TO_WRAPPERS.put(char.class, Character.class);
        PRIMITIVES_TO_WRAPPERS.put(double.class, Double.class);
        PRIMITIVES_TO_WRAPPERS.put(float.class, Float.class);
        PRIMITIVES_TO_WRAPPERS.put(int.class, Integer.class);
        PRIMITIVES_TO_WRAPPERS.put(long.class, Long.class);
        PRIMITIVES_TO_WRAPPERS.put(short.class, Short.class);
    }

    static Class<?> wrapIfPrimitive(Class<?> type) {
        if (type.isPrimitive()) {
            return PRIMITIVES_TO_WRAPPERS.get(type);
        }
        return type;
    }
}
