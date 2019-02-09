/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */
package cern.extjfx.test;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import javafx.application.Platform;

/**
 * Initializes the JavaFX environment and runs tests annotated with {@link RunInFxThread} in
 * {@link Platform#isFxApplicationThread() FX application thread}.
 */
public class FxJUnit4Runner extends BlockJUnit4ClassRunner {

    /**
     * Creates a new JUnit runner that initializes the JavaFX environment.
     *
     * @param clazz the class under test
     * @throws InitializationError if the test class is malformed
     * @see FxInitializer#initialize()
     */
    public FxJUnit4Runner(Class<?> clazz) throws InitializationError {
        super(clazz);
        FxInitializer.initialize();
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        final CountDownLatch latch = new CountDownLatch(1);

        if (shouldRunInFxThread(method)) {
            assertTimeoutNotDefined(method);

            Platform.runLater(() -> {
                FxJUnit4Runner.super.runChild(method, notifier);
                latch.countDown();
            });
        } else {
            FxJUnit4Runner.super.runChild(method, notifier);
            latch.countDown();
        }

        try {
            latch.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean shouldRunInFxThread(FrameworkMethod method) {
        return method.getAnnotation(RunInFxThread.class) != null
                || method.getDeclaringClass().getAnnotation(RunInFxThread.class) != null;
    }

    private static void assertTimeoutNotDefined(final FrameworkMethod method) {
        Test annotation = method.getAnnotation(Test.class);
        long timeout = annotation.timeout();

        if (timeout > 0) {
            throw new UnsupportedOperationException(
                    "@Test(timeout) argument is not supported with " + RunInFxThread.class.getSimpleName());
        }
    }
}