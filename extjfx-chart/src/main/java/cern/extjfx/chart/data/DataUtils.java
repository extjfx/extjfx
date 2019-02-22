/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */
package cern.extjfx.chart.data;

import java.util.Arrays;

/**
 * Utility methods related to chart data.
 */
public final class DataUtils {
    private DataUtils() {
        // No instances
    }

    /**
     * Returns insertion index of specified X coordinate within given chart data.
     * 
     * @param <X> X value type
     * @param <Y> Y value type
     * @param data the searched data
     * @param x X coordinate whose insertion index should be returned
     * @return index at which a point with given X coordinate would be inserted in specified data.
     */
    public static <X extends Number, Y extends Number> int insertionIndex(ChartData<X, Y> data, double x) {
        int index = binarySearch(data, x);
        return index >= 0 ? index : -index - 1;
    }

    /**
     * Searches the specified {@code ChartData} for the data point with specified X coordinate using the binary search
     * algorithm. Points in the given {@code ChartData} must be sorted. This method is an equivalent of
     * {@link Arrays#binarySearch(double[], double)} but dedicated to {@code ChartData}.
     * 
     * @param <X> X value type
     * @param <Y> Y value type
     * @param data the searched data
     * @param x X coordinate whose index should be returned
     * @return index of the search X value, if it is contained in the ChartData; otherwise, (-(insertion point) - 1).
     *         The insertion point is defined as the point at which the key would be inserted into the data: the index
     *         of the first element greater than the X, or data.size() if all elements in the array are less than the
     *         specified X. Note that this guarantees that the return value will be &gt;= 0 if and only if the X is found.
     * @see Arrays#binarySearch(double[], double)
     */
    public static <X extends Number, Y extends Number> int binarySearch(ChartData<X, Y> data, double x) {
        int low = 0;
        int high = data.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVal = data.getXAsDouble(mid);

            if (midVal < x)
                low = mid + 1;
            else if (midVal > x)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
    }
}
