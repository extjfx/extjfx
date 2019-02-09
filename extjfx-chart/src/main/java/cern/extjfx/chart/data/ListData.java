/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import static java.util.Objects.requireNonNull;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;

/**
 * An implementation of {@link ChartData} backed up by an {@code ObservableList} of {@link Data} objects.
 * 
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public class ListData<X extends Number, Y extends Number> implements ChartData<X, Y> {

    private final ObservableList<Data<X, Y>> data;

    /**
     * Creates a new instance.
     * 
     * @param data list containing data to be reduced
     */
    public ListData(ObservableList<Data<X, Y>> data) {
        this.data = requireNonNull(data, "The source list must not be null");
    }

    @Override
    public void addListener(InvalidationListener listener) {
        data.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        data.removeListener(listener);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public double getXAsDouble(int index) {
        return data.get(index).getXValue().doubleValue();
    }

    @Override
    public double getYAsDouble(int index) {
        return data.get(index).getYValue().doubleValue();
    }

    @Override
    public Data<X, Y> get(int index) {
        return data.get(index);
    }
}
