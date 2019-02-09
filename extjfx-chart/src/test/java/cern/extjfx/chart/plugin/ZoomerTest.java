/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.plugin;

import static cern.extjfx.chart.XYChartPaneShim.localToChartPane;
import static cern.extjfx.chart.XYChartPaneTestHelper.PRECISION;
import static cern.extjfx.chart.XYChartPaneTestHelper.createTestChartPaneWithOverlayCharts;
import static cern.extjfx.chart.XYChartPaneTestHelper.resizeRelocateLayout;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.extjfx.chart.Axes;
import cern.extjfx.chart.XYChartPane;
import cern.extjfx.chart.XYChartPaneTestHelper;
import cern.extjfx.chart.plugins.Zoomer;
import cern.extjfx.test.FxJUnit4Runner;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

@RunWith(FxJUnit4Runner.class)
public class ZoomerTest {

    private static final double RATIO_MIN_X = 0.2;
    private static final double RATIO_MAX_X = 0.8;
    private static final double RATIO_MIN_Y = 0.4;
    private static final double RATIO_MAX_Y = 0.6;

    @Test
    public void testZoomIn() {
        XYChartPane<Number, Number> chartPane = createTestChartPaneWithOverlayCharts();
        chartPane.getPlugins().add(new Zoomer());
        resizeRelocateLayout(chartPane);

        Map<ValueAxis<?>, ValueAxisRange> axesRangesBeforeZoom = getAxesRanges(chartPane);
        zoomIn(chartPane);

        // Verify bounds on all axes
        Map<ValueAxis<?>, ValueAxisRange> axesRangesAfterZoom = getAxesRanges(chartPane);
        for (XYChart<Number, Number> chart : XYChartPaneTestHelper.getCharts(chartPane)) {
            ValueAxisRange xRangeBeforeZoom = axesRangesBeforeZoom.get(chart.getXAxis());
            ValueAxisRange xRangeAfterZoom = axesRangesAfterZoom.get(chart.getXAxis());
            ValueAxisRange yRangeBeforeZoom = axesRangesBeforeZoom.get(chart.getYAxis());
            ValueAxisRange yRangeAfterZoom = axesRangesAfterZoom.get(chart.getYAxis());

            assertEquals(getPercentRangeValue(xRangeBeforeZoom, RATIO_MIN_X), xRangeAfterZoom.lowerBound, PRECISION);
            assertEquals(getPercentRangeValue(xRangeBeforeZoom, RATIO_MAX_X), xRangeAfterZoom.upperBound, PRECISION);
            assertEquals(getPercentRangeValue(yRangeBeforeZoom, RATIO_MIN_Y), yRangeAfterZoom.lowerBound, PRECISION);
            assertEquals(getPercentRangeValue(yRangeBeforeZoom, RATIO_MAX_Y), yRangeAfterZoom.upperBound, PRECISION);
        }
    }

    @Test
    public void testZoomOut() {
        XYChartPane<Number, Number> chartPane = createTestChartPaneWithOverlayCharts();
        chartPane.getPlugins().add(new Zoomer());
        resizeRelocateLayout(chartPane);

        zoomIn(chartPane);
        Map<ValueAxis<?>, ValueAxisRange> axesRangesBeforeSecondZoom = getAxesRanges(chartPane);
        zoomIn(chartPane);
        zoomOut(chartPane);

        Map<ValueAxis<?>, ValueAxisRange> axesRangesAfterZoomOut = getAxesRanges(chartPane);
        for (XYChart<Number, Number> chart : chartPane.getOverlayCharts()) {
            ValueAxisRange xRangeBeforeZoom = axesRangesBeforeSecondZoom.get(chart.getXAxis());
            ValueAxisRange xRangeAfterZoom = axesRangesAfterZoomOut.get(chart.getXAxis());
            ValueAxisRange yRangeBeforeZoom = axesRangesBeforeSecondZoom.get(chart.getYAxis());
            ValueAxisRange yRangeAfterZoom = axesRangesAfterZoomOut.get(chart.getYAxis());

            assertEquals(xRangeBeforeZoom, xRangeAfterZoom);
            assertEquals(yRangeBeforeZoom, yRangeAfterZoom);
        }
    }

    @Test
    public void testZoomOrigin() {
        XYChartPane<Number, Number> chartPane = createTestChartPaneWithOverlayCharts();
        chartPane.getPlugins().add(new Zoomer());
        resizeRelocateLayout(chartPane);

        Map<ValueAxis<?>, ValueAxisRange> axesRangesBeforeZoom = getAxesRanges(chartPane);
        zoomIn(chartPane);
        zoomIn(chartPane);
        zoomIn(chartPane);
        zoomOrigin(chartPane);

        Map<ValueAxis<?>, ValueAxisRange> axesRangesAfterZoomOut = getAxesRanges(chartPane);
        for (XYChart<Number, Number> chart : chartPane.getOverlayCharts()) {
            ValueAxisRange xRangeBeforeZoom = axesRangesBeforeZoom.get(chart.getXAxis());
            ValueAxisRange xRangeAfterZoom = axesRangesAfterZoomOut.get(chart.getXAxis());
            ValueAxisRange yRangeBeforeZoom = axesRangesBeforeZoom.get(chart.getYAxis());
            ValueAxisRange yRangeAfterZoom = axesRangesAfterZoomOut.get(chart.getYAxis());

            assertEquals(xRangeBeforeZoom, xRangeAfterZoom);
            assertEquals(yRangeBeforeZoom, yRangeAfterZoom);
        }
    }

    private static void zoomIn(XYChartPane<Number, Number> chartPane) {
        XYChart<Number, Number> baseChart = chartPane.getChart();
        ValueAxis<Number> xAxis = Axes.toValueAxis(baseChart.getXAxis());
        ValueAxis<Number> yAxis = Axes.toValueAxis(baseChart.getYAxis());
        double zoomDataMinX = getPercentRangeValue(new ValueAxisRange(xAxis), RATIO_MIN_X);
        double zoomDataMaxX = getPercentRangeValue(new ValueAxisRange(xAxis), RATIO_MAX_X);
        double zoomDataMinY = getPercentRangeValue(new ValueAxisRange(yAxis), RATIO_MIN_Y);
        double zoomDataMaxY = getPercentRangeValue(new ValueAxisRange(yAxis), RATIO_MAX_Y);

        double zoomRectMinX = localToChartPane(chartPane, xAxis, new Point2D(xAxis.getDisplayPosition(zoomDataMinX), 0)).getX();
        double zoomRectMaxX = localToChartPane(chartPane, xAxis, new Point2D(xAxis.getDisplayPosition(zoomDataMaxX), 0)).getX();
        double zoomRectMinY = localToChartPane(chartPane, yAxis, new Point2D(0, yAxis.getDisplayPosition(zoomDataMinY))).getY();
        double zoomRectMaxY = localToChartPane(chartPane, yAxis, new Point2D(0, yAxis.getDisplayPosition(zoomDataMaxY))).getY();

        MouseEvent press = mouseEvent(MOUSE_PRESSED, MouseButton.PRIMARY, zoomRectMinX, zoomRectMinY, true, false);
        MouseEvent drag = mouseEvent(MOUSE_DRAGGED, MouseButton.PRIMARY, zoomRectMaxX, zoomRectMaxY, true, false);
        MouseEvent release = mouseEvent(MOUSE_RELEASED, MouseButton.PRIMARY, zoomRectMaxX, zoomRectMaxY, false, false);
        Event.fireEvent(chartPane, press);
        Event.fireEvent(chartPane, drag);
        Event.fireEvent(chartPane, release);
    }

    private static void zoomOut(XYChartPane<Number, Number> chartPane) {
        fireZoomOutEvent(chartPane, false);
    }

    private static void zoomOrigin(XYChartPane<Number, Number> chartPane) {
        fireZoomOutEvent(chartPane, true);
    }

    private static void fireZoomOutEvent(XYChartPane<Number, Number> chartPane, boolean ctrlDown) {
        XYChart<Number, Number> baseChart = chartPane.getChart();
        ValueAxis<?> xAxis = Axes.toValueAxis(baseChart.getXAxis());
        ValueAxis<?> yAxis = Axes.toValueAxis(baseChart.getYAxis());
        double zoomOutX = getPercentRangeValue(new ValueAxisRange(xAxis), 0.5);
        double zoomOutY = getPercentRangeValue(new ValueAxisRange(yAxis), 0.5);

        MouseEvent rightClick = mouseEvent(MOUSE_CLICKED, MouseButton.SECONDARY, zoomOutX, zoomOutY, false, ctrlDown);
        Event.fireEvent(chartPane, rightClick);
    }

    private static double getPercentRangeValue(ValueAxisRange range, double ratio) {
        return range.lowerBound + (range.upperBound - range.lowerBound) * ratio;
    }

    private static Map<ValueAxis<?>, ValueAxisRange> getAxesRanges(XYChartPane<Number, Number> chartPane) {
        Map<ValueAxis<?>, ValueAxisRange> ranges = new HashMap<>();
        for (XYChart<Number, Number> chart : XYChartPaneTestHelper.getCharts(chartPane)) {
            ValueAxis<?> xValueAxis = Axes.toValueAxis(chart.getXAxis());
            ValueAxis<?> yValueAxis = Axes.toValueAxis(chart.getYAxis());
            ranges.put(xValueAxis, new ValueAxisRange(xValueAxis));
            ranges.put(yValueAxis, new ValueAxisRange(yValueAxis));
        }
        return ranges;
    }

    private static MouseEvent mouseEvent(EventType<MouseEvent> eventType, MouseButton button, double x, double y,
            boolean primaryButtonDown, boolean ctrlDown) {
        return new MouseEvent(eventType, x, y, x, y, button, 1, false, ctrlDown, false, false, primaryButtonDown, false,
                false, false, false, false, null);
    }

    private static class ValueAxisRange {
        final double lowerBound, upperBound;

        ValueAxisRange(ValueAxis<?> axis) {
            lowerBound = axis.getLowerBound();
            upperBound = axis.getUpperBound();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ValueAxisRange)) {
                return false;
            }
            ValueAxisRange other = (ValueAxisRange) obj;
            return Math.abs(lowerBound - other.lowerBound) < PRECISION
                    && Math.abs(upperBound - other.upperBound) < PRECISION;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(lowerBound);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(upperBound);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "[" + lowerBound + ", " + upperBound + "]";
        }
    }
}
