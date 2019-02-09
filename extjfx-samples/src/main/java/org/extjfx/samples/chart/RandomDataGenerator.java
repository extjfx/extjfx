/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.extjfx.samples.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;

class RandomDataGenerator {

    private RandomDataGenerator() {
    }

    /**
     * Generates a list with random Y values. The X values are always from 0 to size-1. Y values are calculated based on
     * the given first value and variance multiplied by a random number from range [0, 1].
     * 
     * @param firstValue Y value of the first data point
     * @param variance scale of the Y variations
     * @param size number of data points to be returned
     * @return data with random Y values
     */
    public static ObservableList<Data<Number, Number>> generateData(double firstValue, double variance, int size) {
        List<Data<Number, Number>> data = new ArrayList<>(size);
        Random rnd = new Random();
        data.add(new Data<>(0, firstValue));

        for (int x = 1; x < size; x++) {
            int sign = rnd.nextBoolean() ? 1 : -1;
            double y = data.get(x - 1).getYValue().doubleValue() + variance * rnd.nextDouble() * sign;
            data.add(new Data<>(x, y));
        }
        return FXCollections.observableArrayList(data);
    }
}