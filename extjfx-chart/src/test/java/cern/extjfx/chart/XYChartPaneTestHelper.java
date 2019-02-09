/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import static cern.extjfx.chart.XYChartPaneShim.getLocationInChartPane;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.XYChartPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

public final class XYChartPaneTestHelper {
    public static final double PRECISION = 1;

    public static <X, Y> List<XYChart<X, Y>> getCharts(XYChartPane<X, Y> chartPane) {
        List<XYChart<X, Y>> charts = new LinkedList<>();
        charts.add(chartPane.getChart());
        charts.addAll(chartPane.getOverlayCharts());
        return charts;
    }

    public static void assertLocationEquals(XYChartPane<?, ?> chartPane, Node node1, Node node2) {
        assertLocationXEquals(chartPane, node1, node2);
        assertLocationYEquals(chartPane, node1, node2);
    }

    public static void assertLocationXEquals(XYChartPane<?, ?> chartPane, Node node1, Node node2) {
        double xLocation1 = getLocationInChartPane(chartPane, node1).getX();
        double xLocation2 = getLocationInChartPane(chartPane, node2).getX();
        assertEquals(xLocation1, xLocation2, PRECISION);
    }

    public static void assertLocationYEquals(XYChartPane<?, ?> chartPane, Node node1, Node node2) {
        double yLocation1 = getLocationInChartPane(chartPane, node1).getY();
        double yLocation2 = getLocationInChartPane(chartPane, node2).getY();
        assertEquals(yLocation1, yLocation2, PRECISION);
    }

    @SuppressWarnings("unchecked")
    public static XYChartPane<Number, Number> createTestChartPaneWithOverlayCharts() {
        ScatterChart<Number, Number> chart0 = createTestScatterChart(4);
        ScatterChart<Number, Number> chart1 = createTestScatterChart(3);
        ScatterChart<Number, Number> chart2 = createTestScatterChart(2);
        ScatterChart<Number, Number> chart3 = createTestScatterChart(1);

        XYChartPane<Number, Number> chartPane = new XYChartPane<>(chart0);
        chartPane.getOverlayCharts().addAll(chart1, chart2, chart3);
        return chartPane;
    }

    public static void resizeRelocateLayout(XYChartPane<?, ?> chartPane) {
        chartPane.resizeRelocate(0, 0, 1000, 1000);
        chartPane.requestLayout();
        chartPane.layout();
    }

    private static ScatterChart<Number, Number> createTestScatterChart(int n) {
        ScatterChart<Number, Number> chart = new ScatterChart<>(createAxis(), createAxis());
        chart.setAnimated(false);
        chart.getYAxis().setLabel("Y");
        chart.getData().add(new Series<>("series" + n, createTestData(n)));
        return chart;
    }

    private static ValueAxis<Number> createAxis() {
        NumericAxis xAxis = new NumericAxis();
        xAxis.setAnimated(false);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRangeRounding(false);
        return xAxis;
    }

    private static ObservableList<Data<Number, Number>> createTestData(double factor) {
        List<Data<Number, Number>> data = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            data.add(new Data<>(i, i * i * factor));
        }
        return FXCollections.observableArrayList(data);
    }
}
