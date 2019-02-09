/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import static org.junit.Assert.*;

import org.junit.Test;

import cern.extjfx.chart.data.ArrayData;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;

public class ArrayDataTest {

    @Test
    public void testGeneratedX() {
        ObservableList<Data<Number, Number>> data = ArrayData.of(new float[] {2, 1}).toObservableList();
        assertEquals(2, data.size());
        assertTrue(data.get(0).getXValue() instanceof Integer);
        assertTrue(data.get(0).getYValue() instanceof Float);
        assertEquals(1, data.get(1).getXValue());
        assertEquals(2, data.get(0).getYValue().intValue());
    }
    
    @Test
    public void testOfDoubles() {
        ObservableList<Data<Number, Number>> data = ArrayData.of(new double[] {2, 3}, new double[] {4, 5}).toObservableList();
        assertEquals(2, data.size());
        assertTrue(data.get(0).getXValue() instanceof Double);
        assertTrue(data.get(0).getYValue() instanceof Double);
        assertEquals(3.0, data.get(1).getXValue());
        assertEquals(4, data.get(0).getYValue().intValue());
    }
    
    @Test
    public void testBuilder() {
        ObservableList<Data<Number, Number>> data = ArrayData.builder().x(new short[] {2, 3}).y(new long[] {4, 5}).build().toObservableList();
        assertEquals(2, data.size());
        assertTrue(data.get(0).getXValue() instanceof Short);
        assertTrue(data.get(0).getYValue() instanceof Long);
        assertEquals((short) 3, data.get(1).getXValue());
        assertEquals(4L, data.get(0).getYValue().intValue());
    }

}
