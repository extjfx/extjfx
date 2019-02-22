/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import javafx.beans.property.DoubleProperty;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.ValueAxis;

/**
 * Static utility methods related to instances of {@link Axis} class.
 */
public final class Axes {

    private Axes() {
        //
    }

    /**
     * Returns {@code true} if given axis is an instance of {@link ValueAxis}.
     *
     * @param axis the axis to test
     * @return {@code true} if given axis is an instance of {@code ValueAxis}.
     */
    public static boolean isValueAxis(Axis<?> axis) {
        return axis instanceof ValueAxis<?>;
    }

    /**
     * Returns {@code true} if given axis is an instance of {@link CategoryAxis}.
     *
     * @param axis the axis to test
     * @return {@code true} if given axis is an instance of {@code CategoryAxis}.
     */
    public static boolean isCategoryAxis(Axis<?> axis) {
        return axis instanceof CategoryAxis;
    }

    /**
     * Casts given axis to {@link ValueAxis}.
     *
     * @param axis the axis to be cast
     * @param <T> axis value type
     * @return given axis
     * @throws IllegalArgumentException if the given axis is not an instance of ValueAxis
     */
    @SuppressWarnings("unchecked")
    public static <T extends Number> ValueAxis<T> toValueAxis(Axis<?> axis) {
        if (isValueAxis(axis)) {
            return (ValueAxis<T>) axis;
        }
        throw new IllegalArgumentException("Expected an instance of ValueAxis");
    }

    /**
     * Indicates if the given axis {@link ValueAxis#lowerBoundProperty() lower} and/or
     * {@link ValueAxis#upperBoundProperty() upper} bound property {@link DoubleProperty#isBound() is bound}.
     *
     * @param axis the tested axis
     * @return {@code true} if either lower or upper bound is bound
     */
    public static boolean hasBoundedRange(ValueAxis<?> axis) {
        return axis.lowerBoundProperty().isBound() || axis.upperBoundProperty().isBound();
    }

    /**
     * Binds lower and upper bound of given axis to the specified {@code observable} axis.
     *
     * @param axis axis whose lower/upper bound should be bound
     * @param observable the target axis
     * @throws NullPointerException if either axis is {@code null}
     */
    public static void bindBounds(ValueAxis<?> axis, ValueAxis<?> observable) {
        axis.lowerBoundProperty().bind(observable.lowerBoundProperty());
        axis.upperBoundProperty().bind(observable.upperBoundProperty());
    }

    /**
     * {@link DoubleProperty#unbind() Unbinds} lower and upper bound of given axis.
     *
     * @param axis axis whose lower and upper bound should be unbound
     * @throws NullPointerException if the given axis is {@code null}
     */
    public static void unbindBounds(ValueAxis<?> axis) {
        axis.lowerBoundProperty().unbind();
        axis.upperBoundProperty().unbind();
    }
}
