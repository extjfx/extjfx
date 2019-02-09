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
 * A horizontal line drawn on the plot area, indicating specified Y value, with an optional {@link #textProperty() text
 * label} describing the value.
 * <p>
 * Style Classes (from least to most specific):
 * <ul>
 * <li><b>Label:</b> {@code value-indicator-label, y-value-indicator-label, y-value-indicator-label[index]}</li>
 * <li><b>Line:</b> {@code value-indicator-line, y-value-indicator-line, y-value-indicator-line[index]}</li>
 * </ul>
 * where {@code [index]} corresponds to the index (zero based) of this indicator instance added to the
 * {@code XYChartPane}. For example class {@code y-value-indicator-label1} can be used to style label of the second
 * instance of this indicator added to the chart pane.
 * </p>
 * 
 * @author mhrabia
 * @param <X> type of X values of the {@link XYChartPane} that the plug-in will be applied to
 */
public class YValueIndicator<X> extends AbstractSingleValueIndicator<X, Number> {

    private final ValueAxis<Number> axis;

    /**
     * Creates a new instance indicating given Y value of the {@link XYChartPane#getChart() base chart}.
     * 
     * @param value a value to be marked
     */
    public YValueIndicator(double value) {
        this(value, null, null);
    }

    /**
     * Creates a new instance indicating given Y value of the {@link XYChartPane#getChart() base chart}, with the
     * specified label.
     * 
     * @param value a value to be marked
     * @param text the text to be shown by the label. Value of {@link #textProperty()}.
     */
    public YValueIndicator(double value, String text) {
        this(value, text, null);
    }

    /**
     * Creates a new instance indicating given Y value belonging to the specified {@code yAxis}.
     * 
     * @param value a value to be marked
     * @param yAxis Y-axis on which the value will be marked
     */
    public YValueIndicator(double value, ValueAxis<Number> yAxis) {
        this(value, null, yAxis);
    }

    /**
     * Creates a new instance indicating given Y value belonging to the specified {@code yAxis}, with the specified
     * {@link #textProperty() label}.
     * 
     * @param value a value to be marked
     * @param text the text to be shown by the label. Value of {@link #textProperty()}.
     * @param yAxis Y-axis on which the value will be marked
     */
    public YValueIndicator(double value, String text, ValueAxis<Number> yAxis) {
        super(value, text);
        this.axis = yAxis;
    }

    @Override
    void updateStyleClass() {
        setStyleClasses(label, "y-", STYLE_CLASS_LABEL);
        setStyleClasses(line, "y-", STYLE_CLASS_LINE);
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

        double yPos = minY + getYAxis().getDisplayPosition(getValue());

        if (yPos < minY || yPos > maxY) {
            getChartChildren().clear();
        } else {
            layoutLine(minX, yPos, maxX, yPos);
            layoutLabel(new BoundingBox(minX, yPos, maxX - minX, 0), getLabelPosition(), MIDDLE_POSITION);
        }
    }

    private Axis<Number> getYAxis() {
        return getValueAxis(getChartPane());
    }

    @Override
    protected ValueAxis<Number> getValueAxis(XYChartPane<X, Number> chartPane) {
        return axis == null ? Axes.toValueAxis(chartPane.getChart().getYAxis()) : axis;
    }
}
