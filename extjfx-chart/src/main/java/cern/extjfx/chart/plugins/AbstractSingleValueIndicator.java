/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.plugins;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.shape.Line;

/**
 * Plugin indicating a specific X or Y value as a line drawn on the plot area, with an optional {@link #textProperty()
 * text label} describing the value.
 * 
 * @param <X> type of X values
 * @param <Y> type of Y values
 * @author mhrabia
 */
public abstract class AbstractSingleValueIndicator<X, Y> extends AbstractValueIndicator<X, Y> {
    static final double MIDDLE_POSITION = 0.5;
    static final String STYLE_CLASS_LABEL = "value-indicator-label";
    static final String STYLE_CLASS_LINE = "value-indicator-line";

    /**
     * Line indicating the value.
     */
    protected final Line line = new Line();

    /**
     * Creates a new instance of AbstractSingleValueIndicator.
     * 
     * @param value a X value to be indicated
     * @param text the text to be shown by the label. Value of {@link #textProperty()}.
     */
    protected AbstractSingleValueIndicator(double value, String text) {
        super(text);
        setValue(value);
        
        line.setMouseTransparent(true);

        // Need to add them so that at initialization of the stage the CCS is applied and we can calculate label's
        // width and height
        getChartChildren().addAll(line, label);
    }

    private final DoubleProperty value = new SimpleDoubleProperty(this, "value") {
        @Override
        protected void invalidated() {
            layoutChildren();
        }
    };

    /**
     * Value indicated by this plugin.
     * 
     * @return value property
     */
    public final DoubleProperty valueProperty() {
        return value;
    }

    /**
     * Returns the indicated value.
     * 
     * @return indicated value
     */
    public final double getValue() {
        return valueProperty().get();
    }

    /**
     * Sets the value that should be indicated.
     * 
     * @param newValue value to be indicated
     */
    public final void setValue(double newValue) {
        valueProperty().set(newValue);
    }

    private final DoubleProperty labelPosition = new SimpleDoubleProperty(this, "labelPosition", 0.5) {
        @Override
        protected void invalidated() {
            if (get() < 0 || get() > 1) {
                throw new IllegalArgumentException("labelPosition must be in rage [0,1]");
            }
            layoutChildren();
        }
    };

    /**
     * Relative position, between 0.0 (left, bottom) and 1.0 (right, top) of the description {@link #textProperty()
     * label} in the plot area.
     * <p>
     * <b>Default value: 0.5</b>
     * </p>
     * 
     * @return labelPosition property
     */
    public final DoubleProperty labelPositionProperty() {
        return labelPosition;
    }

    /**
     * Returns the value of the {@link #labelPositionProperty()}.
     * 
     * @return the relative position of the {@link #textProperty() text label}
     */
    public final double getLabelPosition() {
        return labelPositionProperty().get();
    }

    /**
     * Sets the new value of the {@link #labelPositionProperty()}.
     * 
     * @param value the label position, between 0.0 and 1.0 (both inclusive)
     */
    public final void setLabelPosition(double value) {
        labelPositionProperty().set(value);
    }

    /**
     * Sets the line coordinates.
     * @param startX line starting X coordinate
     * @param startY line starting Y coordinate
     * @param endX line ending X coordinate
     * @param endY line ending Y coordinate
     */
    protected void layoutLine(double startX, double startY, double endX, double endY) {
        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);

        addChildNodeIfNotPresent(line);
    }
}
