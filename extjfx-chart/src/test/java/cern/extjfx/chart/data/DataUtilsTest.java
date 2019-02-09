/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.extjfx.chart.data.ArrayData;
import cern.extjfx.chart.data.ChartData;
import cern.extjfx.chart.data.DataUtils;
import cern.extjfx.chart.data.ListData;
import cern.extjfx.test.FxJUnit4Runner;
import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart.Data;

@RunWith(FxJUnit4Runner.class)
public class DataUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testNullList() {
        DataUtils.insertionIndex(null, 0);
    }

    @Test
    public void testCompareNumbers() {
        ChartData<Number, Number> data = ArrayData.of(new int[] {0,1,2}, new int[] {0,1,2});

        assertThat(DataUtils.insertionIndex(data, -1), is(0));
        assertThat(DataUtils.insertionIndex(data, 0), is(0));
        assertThat(DataUtils.insertionIndex(data, 1), is(1));
        assertThat(DataUtils.insertionIndex(data, 1.5), is(2));
        assertThat(DataUtils.insertionIndex(data, 2), is(2));
        assertThat(DataUtils.insertionIndex(data, 3), is(3));
    }

    @Test
    public void testCompareListNumbers() {
        List<Data<Number, Number>> rawData = Arrays.asList(new Data<>(0, 0), new Data<>(1, 1), new Data<>(2, 2));
        ChartData<Number, Number> data = new ListData<>(FXCollections.observableArrayList(rawData));

        assertThat(DataUtils.insertionIndex(data, -1), is(0));
        assertThat(DataUtils.insertionIndex(data, 0), is(0));
        assertThat(DataUtils.insertionIndex(data, 1), is(1));
        assertThat(DataUtils.insertionIndex(data, 1.5), is(2));
        assertThat(DataUtils.insertionIndex(data, 2), is(2));
        assertThat(DataUtils.insertionIndex(data, 3), is(3));
    }
}
