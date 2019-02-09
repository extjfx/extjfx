/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import cern.extjfx.chart.data.ArrayData.NumberArray.ArrayType;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

/**
 * Immutable holder of chart data represented by two primitive arrays (X and Y). Instances of the class can be created
 * using dedicated {@link #builder()} class that allows specifying X and Y coordinates as on the following example:
 *
 * <pre>
 * long[] xValues = ...;
 * float[] yValues = ...;
 * ArrayData<Number, Number> arrayData = ArrayData.builder().x(xValues).y(yValues).build();
 * //Convert to ObservableList that can be given directly to Chart Series
 * ObservableList<Data<Number, Number>> data = arrayData.toObservableList();
 * </pre>
 * 
 * If both arrays are of the same type one of the {@code of(..)} methods can be used instead e.g.:
 * 
 * <pre>
 * double[] xValues = ...;
 * double[] yValues = ...;
 * ObservableList<Data<Number, Number>> data = ArrayData.of(xValues, yValues).toObservableList();
 * 
 * Series<Number, Number> series = ...;
 * series.setData(data);
 * </pre>
 *
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public class ArrayData<X extends Number, Y extends Number> implements ChartData<X, Y> {

    private final NumberArray<X> xData;
    private final NumberArray<Y> yData;

    private ArrayData(NumberArray<X> xData, NumberArray<Y> yData) {
        this.xData = xData;
        this.yData = yData;
    }

    @Override
    public int size() {
        return xData.length();
    }

    @Override
    public double getXAsDouble(int index) {
        return xData.getDouble(index);
    }

    @Override
    public double getYAsDouble(int index) {
        return yData.getDouble(index);
    }

    @Override
    public Data<X, Y> get(int index) {
        return new Data<>(xData.get(index), yData.get(index));
    }

    @Override
    public void addListener(InvalidationListener listener) {
        // Ignore - ArrayData is immutable
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        // Ignore - ArrayData is immutable
    }

    /**
     * Converts the {@code ArrayData} to an ObservableList of {@link Data} points that can be added directly to the
     * chart {@link Series}.
     * 
     * @return ObservableList of data points
     */
    public ObservableList<Data<X, Y>> toObservableList() {
        int size = size();
        List<Data<X, Y>> data = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            data.add(get(i));
        }
        return FXCollections.observableArrayList(data);
    }

    /**
     * Returns {@code ArrayData} with given Y coordinates. X coordinates are generated automatically as indices of Y
     * coordinates i.e. 0 to y.length - 1.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(byte[] y) {
        Builder<X, Y> builder = builder();
        return builder.y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given X and Y coordinates.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(byte[] x, byte[] y) {
        Builder<X, Y> builder = builder();
        return builder.x(x).y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given Y coordinates. X coordinates are generated automatically as indices of Y
     * coordinates i.e. 0 to y.length - 1.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(short[] y) {
        Builder<X, Y> builder = builder();
        return builder.y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given X and Y coordinates.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(short[] x, short[] y) {
        Builder<X, Y> builder = builder();
        return builder.x(x).y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given Y coordinates. X coordinates are generated automatically as indices of Y
     * coordinates i.e. 0 to y.length - 1.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(int[] y) {
        Builder<X, Y> builder = builder();
        return builder.y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given X and Y coordinates.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(int[] x, int[] y) {
        Builder<X, Y> builder = builder();
        return builder.x(x).y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given Y coordinates. X coordinates are generated automatically as indices of Y
     * coordinates i.e. 0 to y.length - 1.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(long[] y) {
        Builder<X, Y> builder = builder();
        return builder.y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given X and Y coordinates.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(long[] x, long[] y) {
        Builder<X, Y> builder = builder();
        return builder.x(x).y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given Y coordinates. X coordinates are generated automatically as indices of Y
     * coordinates i.e. 0 to y.length - 1.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(float[] y) {
        Builder<X, Y> builder = builder();
        return builder.y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given X and Y coordinates.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(float[] x, float[] y) {
        Builder<X, Y> builder = builder();
        return builder.x(x).y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given Y coordinates. X coordinates are generated automatically as indices of Y
     * coordinates i.e. 0 to y.length - 1.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(double[] y) {
        Builder<X, Y> builder = builder();
        return builder.y(y).build();
    }

    /**
     * Returns {@code ArrayData} with given X and Y coordinates.
     * 
     * @return {@code ArrayData} built from given arrays
     */
    public static <X extends Number, Y extends Number> ArrayData<X, Y> of(double[] x, double[] y) {
        Builder<X, Y> builder = builder();
        return builder.x(x).y(y).build();
    }

    /**
     * Creates a new builder of the {@link ArrayData}.
     *
     * @return a new builder instance
     */
    public static <X extends Number, Y extends Number> Builder<X, Y> builder() {
        return new Builder<>();
    }

    /**
     * Builder of {@link ArrayData} instances.
     *
     * @param <X> type of X values
     * @param <Y> type of Y values
     */
    public static class Builder<X extends Number, Y extends Number> {
        NumberArray<X> xArray;
        NumberArray<Y> yArray;

        private Builder() {
        }

        /**
         * Initializes X values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> x(byte[] array) {
            xArray = new NumberArray<>(ArrayType.BYTE, array);
            return this;
        }

        /**
         * Initializes X values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> x(short[] array) {
            xArray = new NumberArray<>(ArrayType.SHORT, array);
            return this;
        }

        /**
         * Initializes X values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> x(int[] array) {
            xArray = new NumberArray<>(ArrayType.INT, array);
            return this;
        }

        /**
         * Initializes X values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> x(long[] array) {
            xArray = new NumberArray<>(ArrayType.LONG, array);
            return this;
        }

        /**
         * Initializes X values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> x(float[] array) {
            xArray = new NumberArray<>(ArrayType.FLOAT, array);
            return this;
        }

        /**
         * Initializes X values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> x(double[] array) {
            xArray = new NumberArray<>(ArrayType.DOUBLE, array);
            return this;
        }

        /**
         * Initializes Y values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> y(byte[] array) {
            yArray = new NumberArray<>(ArrayType.BYTE, array);
            return this;
        }

        /**
         * Initializes Y values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> y(short[] array) {
            yArray = new NumberArray<>(ArrayType.SHORT, array);
            return this;
        }

        /**
         * Initializes Y values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> y(int[] array) {
            yArray = new NumberArray<>(ArrayType.INT, array);
            return this;
        }

        /**
         * Initializes Y values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> y(long[] array) {
            yArray = new NumberArray<>(ArrayType.LONG, array);
            return this;
        }

        /**
         * Initializes Y values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> y(float[] array) {
            yArray = new NumberArray<>(ArrayType.FLOAT, array);
            return this;
        }

        /**
         * Initializes Y values with specified array.
         *
         * @param array the array of values
         * @return this builder
         */
        public Builder<X, Y> y(double[] array) {
            yArray = new NumberArray<>(ArrayType.DOUBLE, array);
            return this;
        }

        /**
         * Builds {@code ArrayData} with given X and Y coordinates. If X coordinates are not specified, the method
         * generates X coordinates as point indices i.e. the X coordinates go from 0 to Y.length - 1.
         * 
         * @throws NullPointerException if the Y coordinates are not provided
         * @throws IllegalArgumentException if given coordinates have different lengths
         */
        public ArrayData<X, Y> build() {
            Objects.requireNonNull(yArray, "The array with Y coordinates is required");
            if (xArray == null) {
                xArray = new NumberArray<>(ArrayType.INT, IntStream.range(0, yArray.length()).toArray());
            }
            if (xArray.length() != yArray.length()) {
                throw new IllegalArgumentException(
                        "X length [" + xArray.length() + "] != Y length [" + yArray.length() + "]");
            }
            return new ArrayData<>(xArray, yArray);
        }
    }

    static class NumberArray<T extends Number> {
        enum ArrayType {
            BYTE,
            SHORT,
            INT,
            LONG,
            FLOAT,
            DOUBLE
        }

        final ArrayType type;
        final Object array;

        protected NumberArray(ArrayType type, Object array) {
            this.type = type;
            this.array = requireNonNull(array, "The array must not be null");
        }

        int length() {
            return Array.getLength(array);
        }

        @SuppressWarnings("unchecked")
        T get(int index) {
            switch (type) {
            case BYTE:
                return (T) Byte.valueOf(((byte[]) array)[index]);
            case SHORT:
                return (T) Short.valueOf(((short[]) array)[index]);
            case INT:
                return (T) Integer.valueOf(((int[]) array)[index]);
            case LONG:
                return (T) Long.valueOf(((long[]) array)[index]);
            case FLOAT:
                return (T) Float.valueOf(((float[]) array)[index]);
            case DOUBLE:
                return (T) Double.valueOf(((double[]) array)[index]);
            }
            throw new IllegalStateException();
        }

        double getDouble(int index) {
            switch (type) {
            case BYTE:
                return ((byte[]) array)[index];
            case SHORT:
                return ((short[]) array)[index];
            case INT:
                return ((int[]) array)[index];
            case LONG:
                return ((long[]) array)[index];
            case FLOAT:
                return ((float[]) array)[index];
            case DOUBLE:
                return ((double[]) array)[index];
            }
            throw new IllegalStateException();
        }
    }
}
