/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.fxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cern.extjfx.fxml.DefaultControllerFactory;
import javafx.geometry.Side;

public class DefaultControllerFactoryTest {
    DefaultControllerFactory factory;
    
    @Before
    public void clear() {
        factory = new DefaultControllerFactory();
    }
    
    @Test
    public void testPrototypeController() {
        assertTrue(factory.createController(TestController.class) != factory.createController(TestController.class));
    }

    @Test
    public void testSingletonDependencies() {
        TestController controller1 = factory.createController(TestController.class);
        TestController controller2 = factory.createController(TestController.class);

        assertTrue(controller1.model == controller2.model);
        assertTrue(controller1.service == controller2.service);
    }

    @Test
    public void testSystemPropertyInjection() {
        System.setProperty("fieldInitializedFromSystemProperties", "sysProperty");

        TestController controller = factory.createController(TestController.class);
        assertEquals("sysProperty", controller.fieldInitializedFromSystemProperties);
        assertNotNull(controller.userHomeDirectory);
        
        System.getProperties().remove("fieldInitializedFromSystemProperties");
    }
    
    @Test
    public void testPropertiesProviderInjectionWithTypeConversion() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("myIntField", Long.valueOf(7));
        properties.put("toolBarSide", "TOP");
        factory.setPropertiesProvider(properties::get);

        TestController controller = factory.createController(TestController.class);
        assertEquals(7, controller.intField);
        assertEquals(Side.TOP, controller.model.toolBarSide);
    }
    
    @Test
    public void testUsageOfProvidedDependency() {
        TestModel model = new TestModel();
        factory.setDependency(TestModel.class, model);
        
        TestController controller = factory.createController(TestController.class);
        assertTrue(model == controller.model);
    }

    @Test
    public void testUsageOfProvidedDependencyForEnumAndPrimitive() {
        factory.setDependency(Side.class, Side.BOTTOM);
        factory.setDependency(int.class, 6);

        TestController controller = factory.createController(TestController.class);
        assertEquals(6, controller.intField);
        assertEquals(Side.BOTTOM, controller.model.toolBarSide);
    }
}
