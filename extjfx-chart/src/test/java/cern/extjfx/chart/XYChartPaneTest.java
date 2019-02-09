/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import static cern.extjfx.chart.Utils.getChartContent;
import static cern.extjfx.chart.Utils.getLocationX;
import static cern.extjfx.chart.XYChartPaneShim.getLocationInChartPane;
import static cern.extjfx.chart.XYChartPaneTestHelper.PRECISION;
import static cern.extjfx.chart.XYChartPaneTestHelper.assertLocationEquals;
import static cern.extjfx.chart.XYChartPaneTestHelper.assertLocationYEquals;
import static cern.extjfx.chart.XYChartPaneTestHelper.createTestChartPaneWithOverlayCharts;
import static cern.extjfx.chart.XYChartPaneTestHelper.resizeRelocateLayout;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.extjfx.chart.XYChartPane;
import cern.extjfx.test.FxJUnit4Runner;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.XYChart;

@RunWith(FxJUnit4Runner.class)
public class XYChartPaneTest {

    @Test
    public void testLocationWithIndividualYAxes() {
        XYChartPane<Number, Number> chartPane = createTestChartPaneWithOverlayCharts();
        ObservableList<XYChart<Number, Number>> overlayCharts = chartPane.getOverlayCharts();
        overlayCharts.get(0).getYAxis().setSide(Side.LEFT);
        overlayCharts.get(1).getYAxis().setSide(Side.RIGHT);
        overlayCharts.get(2).getYAxis().setSide(Side.RIGHT);

        resizeRelocateLayout(chartPane);

        XYChart<?, ?> baseChart = chartPane.getChart();
        assertEquals(0, getLocationX(baseChart), PRECISION);
        assertEquals(0, getLocationX(baseChart), PRECISION);

        for (XYChart<?, ?> overlayChart : overlayCharts) {
            assertLocationEquals(chartPane, baseChart, overlayChart);

            assertLocationEquals(chartPane, baseChart.getXAxis(), overlayChart.getXAxis());
            assertEquals(baseChart.getXAxis().getWidth(), overlayChart.getXAxis().getWidth(), PRECISION);
            assertEquals(baseChart.getXAxis().getHeight(), overlayChart.getXAxis().getHeight(), PRECISION);

            assertLocationYEquals(chartPane, baseChart.getYAxis(), overlayChart.getYAxis());
            assertEquals(baseChart.getYAxis().getHeight(), overlayChart.getYAxis().getHeight(), PRECISION);
        }

        XYChart<Number, Number> chart0 = overlayCharts.get(0);
        XYChart<Number, Number> chart1 = overlayCharts.get(1);
        XYChart<Number, Number> chart2 = overlayCharts.get(2);
        double baseChartXAxisLocationX = getLocationInChartPane(chartPane, baseChart.getXAxis()).getX();
        double baseChartYAxisLocationX = getLocationInChartPane(chartPane, baseChart.getYAxis()).getX();
        double expectedBaseChartYAxisLocationX = baseChartXAxisLocationX - baseChart.getYAxis().getWidth();
        double chart0YAxisLocationX = getLocationInChartPane(chartPane, chart0.getYAxis()).getX();
        double expectedChart0YAxisLocationX = baseChartYAxisLocationX - chart0.getYAxis().getWidth()
                - getChartContent(chart0).snappedLeftInset();
        double chart1YAxisLocationX = getLocationInChartPane(chartPane, chart1.getYAxis()).getX();
        double expectedChart1YAxisLocationX = baseChartXAxisLocationX + baseChart.getXAxis().getWidth();
        double chart2YAxisLocationX = getLocationInChartPane(chartPane, chart2.getYAxis()).getX();
        double expectedChart2YAxisLocationX = chart1YAxisLocationX + chart1.getYAxis().getWidth();

        assertEquals(expectedBaseChartYAxisLocationX, baseChartYAxisLocationX, PRECISION);
        assertEquals(expectedChart0YAxisLocationX, chart0YAxisLocationX, PRECISION);
        assertEquals(expectedChart1YAxisLocationX, chart1YAxisLocationX, PRECISION);
        assertEquals(expectedChart2YAxisLocationX, chart2YAxisLocationX, PRECISION);
    }

    @Test
    public void testLocationWithCommonYAxis() {
        XYChartPane<Number, Number> chartPane = createTestChartPaneWithOverlayCharts();
        chartPane.setCommonYAxis(true);
        resizeRelocateLayout(chartPane);

        XYChart<?, ?> baseChart = chartPane.getChart();
        assertEquals(0, getLocationX(baseChart), PRECISION);
        assertEquals(0, getLocationX(baseChart), PRECISION);

        ObservableList<XYChart<Number, Number>> overlayCharts = chartPane.getOverlayCharts();
        for (XYChart<?, ?> overlayChart : overlayCharts) {
            assertLocationEquals(chartPane, baseChart, overlayChart);

            assertLocationEquals(chartPane, baseChart.getXAxis(), overlayChart.getXAxis());
            assertEquals(baseChart.getXAxis().getWidth(), overlayChart.getXAxis().getWidth(), PRECISION);
            assertEquals(baseChart.getXAxis().getHeight(), overlayChart.getXAxis().getHeight(), PRECISION);

            assertLocationEquals(chartPane, baseChart.getYAxis(), overlayChart.getYAxis());
            assertEquals(baseChart.getYAxis().getWidth(), overlayChart.getYAxis().getWidth(), PRECISION);
            assertEquals(baseChart.getYAxis().getHeight(), overlayChart.getYAxis().getHeight(), PRECISION);
        }
    }
}
