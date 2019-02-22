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
 * A rectangle drawn on the plot area, covering specified range of Y values, with an optional {@link #textProperty()
 * text label} describing the range.
 * <p>
 * Style Classes (from least to most specific):
 * <ul>
 * <li><b>Label:</b> {@code range-indicator-label, y-range-indicator-label, y-range-indicator-label[index]}</li>
 * <li><b>Rectangle:</b> {@code range-indicator-rect, y-range-indicator-rect, y-range-indicator-rect[index]}</li>
 * </ul>
 * where {@code [index]} corresponds to the index (zero based) of this indicator instance added to the
 * {@code XYChartPane}. For example class {@code y-range-indicator-label1} can be used to style label of the second
 * instance of this indicator added to the chart pane.
 * 
 * @author mhrabia
 * @param <X> type of X values of the {@link XYChartPane} that the plug-in will be applied to
 */
public class YRangeIndicator<X> extends AbstractRangeValueIndicator<X, Number> {

    private final ValueAxis<Number> axis;

    /**
     * Creates a new instance that indicates given Y range of the {@link XYChartPane#getChart() base chart}.
     * 
     * @param lowerBound lower bound (min value) of the range
     * @param upperBound upper bound (max value) of the range
     */
    public YRangeIndicator(double lowerBound, double upperBound) {
        this(lowerBound, upperBound, null, null);
    }

    /**
     * Creates a new instance that indicates given Y range of the {@link XYChartPane#getChart() base chart}.
     * 
     * @param lowerBound lower bound (min value) of the range
     * @param upperBound upper bound (max value) of the range
     * @param text the indicator's {@link #textProperty() label's text}
     */
    public YRangeIndicator(double lowerBound, double upperBound, String text) {
        this(lowerBound, upperBound, text, null);
    }

    /**
     * Creates a new instance that indicates given Y range of the specified Y axis.
     * 
     * @param lowerBound lower bound (min value) of the range
     * @param upperBound upper bound (max value) of the range
     * @param yAxis Y-axis whose values should be indicated
     */
    public YRangeIndicator(double lowerBound, double upperBound, ValueAxis<Number> yAxis) {
        this(lowerBound, upperBound, null, yAxis);
    }
    
    private YRangeIndicator(double lowerBound, double upperBound, String text, ValueAxis<Number> yAxis) {
        super(lowerBound, upperBound, text);
        this.axis = yAxis;
    }

    @Override
    void updateStyleClass() {
        setStyleClasses(label, "y-", STYLE_CLASS_LABEL);
        setStyleClasses(rectangle, "y-", STYLE_CLASS_RECT);
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

        Axis<Number> yAxis = getValueAxis(getChartPane());

        double startY = Math.max(minY, minY + yAxis.getDisplayPosition(getUpperBound()));
        double endY = Math.min(maxY, minY + yAxis.getDisplayPosition(getLowerBound()));

        layout(new BoundingBox(minX, startY, maxX - minX, endY - startY));
    }

    @Override
    protected ValueAxis<Number> getValueAxis(XYChartPane<X, Number> chartPane) {
        return axis == null ? Axes.toValueAxis(chartPane.getChart().getYAxis()) : axis;
    }
}
