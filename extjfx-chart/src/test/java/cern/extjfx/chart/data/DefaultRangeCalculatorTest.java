/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.data.ArrayData;
import cern.extjfx.chart.data.AxisBoundRangeCalculator;
import cern.extjfx.chart.data.ChartData;
import cern.extjfx.chart.data.Range;
import cern.extjfx.test.FxJUnit4Runner;
import javafx.scene.chart.ValueAxis;

@RunWith(FxJUnit4Runner.class)
public class DefaultRangeCalculatorTest {
    private static final double ERROR = 0.0001;
    private AxisBoundRangeCalculator<Number, Number> rangeCalculator;
    private ValueAxis<Number> xAxis;

    private ChartData<Number, Number> newData;
    private double[] arrays = new double[] { 1.0, 2.0, 3.0 };

    @Before
    public void setUp() {
        xAxis = new NumericAxis();
        newData = ArrayData.builder().x(arrays).y(arrays).build();
        rangeCalculator = new AxisBoundRangeCalculator<>(xAxis);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateRangeShouldDoNothing() {
        ValueAxis<Number> axisMock = mock(ValueAxis.class);
        AxisBoundRangeCalculator<Number, Number> _rangeCalculator = new AxisBoundRangeCalculator<>(axisMock);
        _rangeCalculator.updateRange(newData);

        verifyZeroInteractions(axisMock);
    }

    @Test
    public void getRangeShouldReturnProvidedAxisLowerBound() {
        double lowerBound = 1.0;
        xAxis.setLowerBound(lowerBound);

        rangeCalculator.updateRange(newData);

        Range<Double> range = rangeCalculator.getRange();
        assertEquals(lowerBound, range.getLowerBound().doubleValue(), ERROR);
    }

    @Test
    public void getRangeShouldReturnProvidedAxisUpperBound() {
        double upperBound = 111.0;
        xAxis.setUpperBound(upperBound);

        rangeCalculator.updateRange(newData);

        Range<Double> range = rangeCalculator.getRange();
        assertEquals(upperBound, range.getUpperBound().doubleValue(), ERROR);
    }
}
