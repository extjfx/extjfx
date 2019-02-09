/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.plugins;

import cern.extjfx.chart.Axes;
import cern.extjfx.chart.XYChartPane;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ValueAxis;

/**
 * A rectangle drawn on the plot area, covering specified range of X values, with an optional {@link #textProperty()
 * text label} describing the range.
 * <p>
 * Style Classes (from least to most specific):
 * <ul>
 * <li><b>Label:</b> {@code range-indicator-label, x-range-indicator-label, x-range-indicator-label[index]}</li>
 * <li><b>Rectangle:</b> {@code range-indicator-rect, x-range-indicator-rect, x-range-indicator-rect[index]}</li>
 * </ul>
 * where {@code [index]} corresponds to the index (zero based) of this indicator instance added to the
 * {@code XYChartPane}. For example class {@code x-range-indicator-label1} can be used to style label of the second
 * instance of this indicator added to the chart pane.
 * </p>
 * 
 * @author mhrabia
 * @param <Y> type of Y values
 */
public class XRangeIndicator<Y> extends AbstractRangeValueIndicator<Number, Y> {

    /**
     * Creates a new instance of the indicator.
     * 
     * @param lowerBound lower bound (min value) of the range
     * @param upperBound upper bound (max value) of the range
     */
    public XRangeIndicator(double lowerBound, double upperBound) {
        this(lowerBound, upperBound, null);
    }

    /**
     * Creates a new instance of the indicator.
     * 
     * @param lowerBound lower bound (min value) of the range
     * @param upperBound upper bound (max value) of the range
     * @param text the text to be shown by the label. Value of {@link #textProperty()}.
     */
    public XRangeIndicator(double lowerBound, double upperBound, String text) {
        super(lowerBound, upperBound, text);
    }

    @Override
    void updateStyleClass() {
        setStyleClasses(label, "x-", STYLE_CLASS_LABEL);
        setStyleClasses(rectangle, "x-", STYLE_CLASS_RECT);
    }

    @Override
    public void layoutChildren() {
        if (getChartPane() == null) {
            return;
        }
        Bounds plotAreaBounds = getChartPane().getPlotAreaBounds();
        double minX = plotAreaBounds.getMinX();
        double maxX = plotAreaBounds.getMaxX();
        double minY = plotAreaBounds.getMinY();
        double maxY = plotAreaBounds.getMaxY();

        Axis<Number> xAxis = getChartPane().getChart().getXAxis();

        double startX = Math.max(minX, minX + xAxis.getDisplayPosition(getLowerBound()));
        double endX = Math.min(maxX, minX + xAxis.getDisplayPosition(getUpperBound()));

        layout(new BoundingBox(startX, minY, endX - startX, maxY - minY));
    }

    @Override
    protected ValueAxis<?> getValueAxis(XYChartPane<Number, Y> chartPane) {
        return Axes.toValueAxis(chartPane.getChart().getXAxis());
    }
}
