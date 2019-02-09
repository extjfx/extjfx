/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javafx.application.Platform;

/**
 * Indicates that given method should be executed within {@link Platform#isFxApplicationThread() FX Application Thread}.
 * Will work only the test class runs with {@link FxJUnit4Runner}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface RunInFxThread {
    //
}
