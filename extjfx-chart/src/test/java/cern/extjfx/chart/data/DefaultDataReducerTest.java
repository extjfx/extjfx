/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cern.extjfx.chart.data.DefaultDataReducer;
import cern.extjfx.chart.data.ListData;
import cern.extjfx.chart.data.Range;
import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart.Data;

public class DefaultDataReducerTest {
    private final DefaultDataReducer<Number, Number> reducer = new DefaultDataReducer<>();

    @Test(expected = NullPointerException.class)
    public void testReduceWithNullPointsList() {
        reducer.reduce(null, null, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReduceWithTargetPointsCountLessThanTwo() {
        reducer.reduce(new ListData<>(FXCollections.observableArrayList(asList(new Data<Number, Number>(0, 0)))), new Range<>(0., 0.), 1);
    }

    @Test
    public void testReduceWithEmptyPointsList() {
        assertTrue(reducer.reduce(new ListData<>(FXCollections.observableArrayList(emptyList())), new Range<>(0., 0.), 3).isEmpty());
    }

    @Test
    public void testReduceWithOnePoint() {
        Data<Number, Number> point = new Data<>(0, 0);
        List<Data<Number, Number>> reduced = reducer.reduce(new ListData<>(FXCollections.observableArrayList(asList(point))), new Range<>(0., 0.), 3);
        assertEquals(1, reduced.size());
        assertEquals(point, reduced.get(0));
    }

    @Test
    public void testArgumentListNotReturned() {
        List<Data<Number, Number>> points = asList(new Data<Number, Number>(0, 0));
        List<Data<Number, Number>> reduced = reducer.reduce(new ListData<>(FXCollections.observableArrayList(points)), new Range<>(0., 0.), 3);
        assertFalse(points == reduced);
    }

    @Test
    public void testReduceFlatLineWithThreePoints() {
        List<Data<Number, Number>> points = new ArrayList<>();
        points.add(new Data<Number, Number>(0, 0));
        points.add(new Data<Number, Number>(1, 0));
        points.add(new Data<Number, Number>(2, 0));

        List<Data<Number, Number>> reduced = reducer.reduce(new ListData<>(FXCollections.observableArrayList(points)), new Range<>(0., 2.), 2);
        assertEquals(2, reduced.size());
        assertEquals(points.get(0), reduced.get(0));
        assertEquals(points.get(2), reduced.get(1));

    }
}
