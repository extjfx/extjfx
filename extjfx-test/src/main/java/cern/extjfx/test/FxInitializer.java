/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import javafx.embed.swing.JFXPanel;

/**
 * Initializer of JavaFX environment. To be used by JUnit tests that work with graphical components.
 */
public final class FxInitializer {
    
    private static AtomicBoolean initialized = new AtomicBoolean(false);

    private FxInitializer() {
        // No instances
    }
    
    /**
     * Initializes the JavaFX toolkit. The initialization is performed on the first call on the method. Any further
     * calls have no effect.
     */
    public static void initialize() {
        if (initialized.compareAndSet(false, true)) {
            initializeToolkit();
        }
    }

    private static void initializeToolkit() {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // initializes JavaFX environment
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException ie) {
            throw new RuntimeException("Exception while initializing JavaFX environment", ie);
        }
    }
}
