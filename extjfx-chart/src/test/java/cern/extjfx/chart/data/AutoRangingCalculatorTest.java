/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import cern.extjfx.chart.data.ArrayData;
import cern.extjfx.chart.data.AutoRangingCalculator;
import cern.extjfx.chart.data.ChartData;
import cern.extjfx.chart.data.ListData;
import cern.extjfx.chart.data.Range;
import javafx.collections.FXCollections;

public class AutoRangingCalculatorTest {
    private static final double ERROR = 0.0001;

    private AutoRangingCalculator<Number, Number> rangeCalculator;
    private ChartData<Number, Number> newData;
    private double[] arrays = new double[] { 1.0, 2.0, 3.0 };

    @Before
    public void setUp() {
        newData = ArrayData.builder().x(arrays).y(arrays).build();
        rangeCalculator = new AutoRangingCalculator<>();
    }

    @Test
    public void getRangeShouldReturnZeroForNullData() {
        rangeCalculator.updateRange(null);

        Range<Double> range = rangeCalculator.getRange();
        assertEquals(0, range.getLowerBound(), ERROR);
        assertEquals(0, range.getUpperBound(), ERROR);
    }

    @Test
    public void getRangeShouldReturnZeroForEmptyData() {
        ListData<Number, Number> data = new ListData<>(FXCollections.emptyObservableList());
        rangeCalculator.updateRange(data);

        Range<Double> range = rangeCalculator.getRange();
        assertEquals(0, range.getLowerBound(), ERROR);
        assertEquals(0, range.getUpperBound(), ERROR);
    }

    @Test
    public void getRangeShouldReturnFirstValueOfProvidedData() {
        rangeCalculator.updateRange(newData);

        Range<Double> range = rangeCalculator.getRange();

        assertEquals(1.0, range.getLowerBound(), ERROR);
    }

    @Test
    public void getRangeShouldReturnLastValueOfProvidedData() {
        rangeCalculator.updateRange(newData);

        Range<Double> range = rangeCalculator.getRange();

        assertEquals(3.0, range.getUpperBound(), ERROR);
    }

}
