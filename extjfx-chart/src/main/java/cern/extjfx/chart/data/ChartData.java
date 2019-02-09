/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import javafx.beans.Observable;
import javafx.scene.chart.XYChart.Data;

/**
 * Holder for data (X and Y) that can be displayed in chart. The primary purpose of this interface is data reduction by
 * the {@link DataReducingObservableList}.
 * 
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public interface ChartData<X extends Number, Y extends Number> extends Observable {

    /**
     * Returns the number of {@link Data} elements.
     * 
     * @return the number of data elements
     */
    int size();

    /**
     * Returns the X coordinate of the data at specified position.
     * 
     * @param index index of the X coordinate to be returned
     */
    double getXAsDouble(int index);

    /**
     * Returns the Y coordinate of the data at specified position.
     * 
     * @param index index of the Y coordinate to be returned
     */
    double getYAsDouble(int index);

    /**
     * Returns data element at specified position.
     * 
     * @return element at specified position
     */
    Data<X, Y> get(int index);
}
