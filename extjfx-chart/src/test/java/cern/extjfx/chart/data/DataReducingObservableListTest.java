/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.extjfx.chart.data.ChartData;
import cern.extjfx.chart.data.DataReducingObservableList;
import cern.extjfx.chart.data.ListData;
import cern.extjfx.test.FxJUnit4Runner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;

@RunWith(FxJUnit4Runner.class)
public class DataReducingObservableListTest {
    
    @SuppressWarnings("unused")
    @Test(expected=NullPointerException.class)
    public void testNullAxis() {
        new DataReducingObservableList<>(null);
    }

    @Test
    public void testReduceData() {
        ChartData<Number, Number> data = createTestData();
        DataReducingObservableList<Number, Number> list = createTestList(data, 2);

        assertThat(list.size(), is(2));
        assertThat(list.get(0).getXValue(), is(data.get(0).getXValue()));
        assertThat(list.get(1).getXValue(), is(data.get(data.size() - 1).getXValue()));
    }

    @Test
    public void testAxisBoundsChanged() {
        ChartData<Number, Number> data = createTestData();
        NumberAxis xAxis = new NumberAxis();
        DataReducingObservableList<Number, Number> list = createTestList(xAxis, data, 3);

        assertThat(list.size(), is(3));
        assertThat(list.get(0).getXValue(), is(data.get(0).getXValue()));
        assertThat(list.get(2).getXValue(), is(data.get(data.size() - 1).getXValue()));

        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(1.5);

        assertThat(list.size(), is(3));
        assertThat(list.get(0).getXValue(), is(data.get(0).getXValue()));
        assertThat(list.get(1).getXValue(), is(data.get(1).getXValue()));
        assertThat(list.get(2).getXValue(), is(data.get(2).getXValue()));
    }

    private static DataReducingObservableList<Number, Number> createTestList(ChartData<Number, Number> data, int maxPointCount) {
        return createTestList(new NumberAxis(), data, maxPointCount);
    }

    private static DataReducingObservableList<Number, Number> createTestList(ValueAxis<Number> axis, ChartData<Number, Number> data, int maxPointCount) {
        DataReducingObservableList<Number, Number> list = new DataReducingObservableList<>(axis, data);
        list.immediateReduce = true;
        list.setMaxPointsCount(maxPointCount);
        return list;
    }

    private static ChartData<Number, Number> createTestData() {
        ObservableList<Data<Number, Number>> data = FXCollections.observableArrayList();
        for (int i = 0; i < 10; i++) {
            data.add(new Data<>(i, i * i));
        }
        return new ListData<>(data);
    }

}
