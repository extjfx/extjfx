/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.fxml;

import static java.util.Objects.requireNonNull;
import static javafx.fxml.FXMLLoader.CONTROLLER_SUFFIX;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * A view class that for a given controller (class or instance) loads corresponding FXML file and applies associated CSS
 * file (if present). The FXML and CSS files are searched in the same package as controller's class and are expected to
 * have the same conventional name i.e. for MainController, they should be called Main.fxml and Main.css respectively.
 * <p>
 * Note that despite the conventional names, it is necessary to define {@code fx:controller} attribute on the root node
 * of all FXML files (that have a corresponding controller).
 * </p>
 * <p>
 * The class supports also loading corresponding resource bundle file (if present) that is expected to follow the same
 * naming convention e.g. Main_en_US.properties. The package structure would look in the following way:
 *
 * <pre>
 * com.mycompany.myapp.MainController.java
 * com.mycompany.myapp.Main.fxml
 * com.mycompany.myapp.Main.css
 * com.mycompany.myapp.Main_en_US.properties
 * </pre>
 * </p>
 * <p>
 * Example usage:
 *
 * <pre>
 * FxmlView mainView = new FxmlView(MainController.class);
 * Scene scene = new Scene(mainView.getRootNode(), 400, 400);
 * // ...
 * MainController mainController = mainView.getController();
 * mainController.doSomething();
 * </pre>
 * </p>
 * <p>
 * Controllers are instantiated using {@link #setControllerFactory(Function) controller factory} that by default is
 * initialized to {@link DefaultControllerFactory#createController(Class)} which supports basic Dependency Injection -
 * see doc of this class for details.<br/>
 * A custom factory can be specified to supply controllers e.g. from an external DI framework.
 * </p>
 * <p>
 * Alternatively to the {@link #setControllerFactory(Function) controller factory}, the controller objects to be used by
 * a particular instance of {@code FxmlView} can be also given directly to the
 * {@link FxmlView#FxmlView(Class, Object...) constructor}.
 * </p>
 * <p>
 * The class has been inspired by Adam Bien's {@code FXMLView} from
 * <a href="http://afterburner.adam-bien.com">afterburner.fx</a> framework with the difference that it doesn't require a
 * separate view class per FXML.
 * </p>
 *
 * @author Olivier Alves
 * @author Grzegorz Kruk
 */
public class FxmlView {
    private static Function<Class<?>, Object> controllerFactory = new DefaultControllerFactory()::createController;

    private final Map<Class<?>, Object> controllerInstances;
    private final Class<?> controllerClass;
    private FXMLLoader fxmlLoader;

    /**
     * Creates a new instance of the {@code FxmlView} for the specified {@code controllerClass} and optional controller
     * instances. The {@code controllerClass} is used to locate and load the corresponding FXML and optional CSS and
     * resource bundle property files.
     * <p>
     * The optional {@code controllers} argument allows specifying directly the controller instances to be used instead
     * of, or in addition to the ones provided by the {@link #getControllerFactory() controller factory} i.e. the
     * controller factory is consulted only for classes whose instances are not provided in the constructor. <br/>
     * <p>
     * Example:
     *
     * <pre>
     * Main.fxml:
     *
     * ...
     * &lt;fx:include fx:id="header" source="Header.fxml"/&gt;
     * ...
     * &lt;fx:include fx:id="subView1" source="SubView.fxml"/&gt;
     * &lt;fx:include fx:id="subView2" source="SubView.fxml"/&gt;
     * ...
     *
     * MainController mainController = ...;
     * HeaderController headerController = ...;
     *
     * // The mainController and headerController instances will be used directly by the FxmlView,
     * // however two distinct instances of SubViewController (corresponding to subView1 and subView2) will be created
     * // using static controller factory
     * FxmlView view = new FxmlView(MainController.class, mainController, headerController);
     * </pre>
     * </p>
     *
     * @param controllerClass class of the controller for which FXML should be loaded, together with an optional CSS and
     *            resource bundle file
     * @param controllers optional controller instances to be used
     * @see FXMLLoader#setController(Object)
     * @see FXMLLoader#setControllerFactory(javafx.util.Callback)
     */
    public FxmlView(Class<?> controllerClass, Object... controllers) {
        this.controllerClass = requireNonNull(controllerClass, "Controller class must not be null");
        this.controllerInstances = toMap(controllers);
    }

    private Map<Class<?>, Object> toMap(Object... controllers) {
        if (controllers == null) {
            return Collections.emptyMap();
        }
        return Arrays.stream(controllers).collect(Collectors.toMap(Object::getClass, Function.identity()));
    }

    /**
     * Returns the main controller of the view.
     *
     * @return controller associated with this view
     * @see FXMLLoader#getController()
     */
    public <C> C getController() {
        ensureViewLoaded();
        return fxmlLoader.getController();
    }

    private void ensureViewLoaded() {
        if (fxmlLoader == null) {
            loadView();
        }
    }

    /**
     * Returns the root node loaded from the FXML.
     *
     * @return the root node
     * @see FXMLLoader#getRoot()
     */
    public <T extends Parent> T getRootNode() {
        ensureViewLoaded();
        return fxmlLoader.getRoot();
    }

    private void loadView() {
        loadFxml();
        addCssIfPresent();
    }

    private void loadFxml() {
        String fxmlName = conventionalName() + ".fxml";
        URL fxmlUrl = controllerClass.getResource(fxmlName);
        if (fxmlUrl == null) {
            throw new IllegalStateException(
                    "FXML " + fxmlName + " not found for controller " + controllerClass.getName());
        }
        try {
            fxmlLoader = new FXMLLoader(fxmlUrl, getResourceBundle());
            fxmlLoader.setControllerFactory(this::provideController);
            fxmlLoader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load FXML " + fxmlUrl, ex);
        }
    }

    String conventionalName() {
        return controllerClass.getSimpleName().replaceAll(CONTROLLER_SUFFIX + "$", "");
    }

    ResourceBundle getResourceBundle() {
        try {
            return ResourceBundle.getBundle(getBundleName());
        } catch (MissingResourceException mre) {
            return null;
        }
    }

    String getBundleName() {
        Package pckg = controllerClass.getPackage();
        if (pckg == null) {
            return conventionalName();
        }
        return pckg.getName() + "." + conventionalName();
    }

    Object provideController(Class<?> clazz) {
        Object controller = controllerInstances.get(clazz);
        if (controller == null) {
            controller = controllerFactory.apply(clazz);

            if (controller == null || !clazz.isAssignableFrom(controller.getClass())) {
                throw new IllegalStateException("ControllerFactory [" + controllerFactory.getClass().getName()
                        + "] returned a controller [" + controller
                        + "] that is not compatible with the requested type [" + clazz.getName() + "]");
            }
        }
        return controller;
    }

    void addCssIfPresent() {
        URL cssUrl = controllerClass.getResource(conventionalName() + ".css");
        if (cssUrl != null) {
            getRootNode().getStylesheets().add(cssUrl.toExternalForm());
        }
    }

    /**
     * Returns the controller factory used to instantiate controllers.
     *
     * @return controller factory
     */
    public static Function<Class<?>, Object> getControllerFactory() {
        return controllerFactory;
    }

    /**
     * Sets the controller factory used to instantiate controllers. By default it is initialized to
     * {@link DefaultControllerFactory#createController(Class)}.
     *
     * @param controllerFactory the controller factory
     * @throws NullPointerException if the factory is {@code null}
     */
    public static void setControllerFactory(Function<Class<?>, Object> controllerFactory) {
        FxmlView.controllerFactory = requireNonNull(controllerFactory, "Controller factory must not be null");
    }
}
