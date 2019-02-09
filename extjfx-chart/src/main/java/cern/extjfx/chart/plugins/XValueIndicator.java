/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.plugins;

import cern.extjfx.chart.Axes;
import cern.extjfx.chart.XYChartPane;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.chart.ValueAxis;

/**
 * A vertical line drawn on the plot area, indicating specified X value, with an optional {@link #textProperty() text
 * label} describing the value.
 * <p>
 * Style Classes (from least to most specific):
 * <ul>
 * <li><b>Label:</b> {@code value-indicator-label, x-value-indicator-label, x-value-indicator-label[index]}</li>
 * <li><b>Line:</b> {@code value-indicator-line, x-value-indicator-line, x-value-indicator-line[index]}</li>
 * </ul>
 * where {@code [index]} corresponds to the index (zero based) of this indicator instance added to the
 * {@code XYChartPane}. For example class {@code x-value-indicator-label1} can be used to style label of the second
 * instance of this indicator added to the chart pane.
 * </p>
 * 
 * @author mhrabia
 * @param <Y> type of Y values
 */
public class XValueIndicator<Y> extends AbstractSingleValueIndicator<Number, Y> {

    /**
     * Creates a new instance of the indicator.
     * 
     * @param value a X value to be indicated
     */
    public XValueIndicator(double value) {
        this(value, null);
    }

    /**
     * Creates a new instance of the indicator.
     * 
     * @param value a X value to be indicated
     * @param text the text to be shown by the label. Value of {@link #textProperty()}.
     */
    public XValueIndicator(double value, String text) {
        super(value, text);
    }
    
    @Override
    void updateStyleClass() {
        setStyleClasses(label, "x-", STYLE_CLASS_LABEL);
        setStyleClasses(line, "x-", STYLE_CLASS_LINE);
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

        double xPos = minX + getChartPane().getChart().getXAxis().getDisplayPosition(getValue());

        if (xPos < minX || xPos > maxX) {
            getChartChildren().clear();
        } else {
            layoutLine(xPos, minY, xPos, maxY);
            layoutLabel(new BoundingBox(xPos, minY, 0, maxY - minY), MIDDLE_POSITION, getLabelPosition());
        }
    }

    @Override
    protected ValueAxis<?> getValueAxis(XYChartPane<Number, Y> chartPane) {
        return Axes.toValueAxis(chartPane.getChart().getXAxis());
    }
}
