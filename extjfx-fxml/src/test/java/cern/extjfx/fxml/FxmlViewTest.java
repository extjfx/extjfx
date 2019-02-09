/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.fxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import cern.extjfx.test.FxJUnit4Runner;

@RunWith(FxJUnit4Runner.class)
public class FxmlViewTest {

    @After
    public void resetControllerFactory() {
        FxmlView.setControllerFactory(new DefaultControllerFactory()::createController);
    }

    @Test
    public void testNewControllerInstancePerView() {
        assertTrue(new FxmlView(TestController.class).getController() != new FxmlView(TestController.class)
                .getController());
    }

    @Test
    public void testResourceBundleLoaded() {
        FxmlView view = new FxmlView(TestController.class);
        TestController controller = view.getController();
        assertEquals("hi", controller.resourceBundle.getObject("label"));
    }

    @Test
    public void testCssAdded() {
        List<String> styleSheets = new FxmlView(TestController.class).getRootNode().getStylesheets();
        URL cssUrl = TestController.class.getResource("Test.css");
        assertTrue(styleSheets.get(styleSheets.size() - 1).equals(cssUrl.toExternalForm()));
    }

    @Test
    public void testControllerInstanceUsage() {
        TestController testController = new TestController();
        HeaderSubViewController headerSubViewController = new HeaderSubViewController();

        FxmlView view = new FxmlView(TestController.class, testController, headerSubViewController);
        TestController mainController = view.getController();

        assertSame(testController, mainController);
        assertSame(headerSubViewController,  mainController.headerController.headerSubViewController);
        assertFalse(mainController.subView1Controller == mainController.subView2Controller);
    }

    @Test
    public void testCustomControllerFactoryUsage() {
        TestController controller = new TestController();
        FxmlView.setControllerFactory(clazz -> {
            if (clazz == TestController.class) {
                return controller;
            }
            return new DefaultControllerFactory().createController(clazz);
        });

        FxmlView view = new FxmlView(TestController.class);
        assertSame(controller, view.getController());
    }

    @Test
    public void testIncludedFxml() {
        FxmlView view = new FxmlView(TestController.class);
        assertTrue(view.getRootNode().getChildrenUnmodifiable().size() == 4);

        TestController controller = view.getController();
        assertNotNull(controller.subView1Controller);
    }
}
