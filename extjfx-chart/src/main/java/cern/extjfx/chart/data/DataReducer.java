/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import java.util.List;

import javafx.scene.chart.XYChart.Data;

/**
 * Strategy reducing the number of data points.
 * 
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public interface DataReducer<X extends Number, Y extends Number> {

    /**
     * Reduces the number of data points to be equal or less than specified {@code maxPointsCount}.
     * 
     * @param data data to be reduced
     * @param dataRange range of data (lowerBound and upperBound) from which given data should be reduced
     * @param maxPointsCount the maximum number of points that the reduced list should contain
     * @return list containing reduced data, with maximum {@code maxPointsCount} elements
     */
    List<Data<X, Y>> reduce(ChartData<X, Y> data, Range<Double> dataRange, int maxPointsCount);
}
