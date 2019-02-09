/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.plugins;

import cern.extjfx.chart.XYChartPane;
import cern.extjfx.chart.XYChartPlugin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.chart.ValueAxis;
import javafx.scene.control.Label;

/**
 * Base class for plugins indicating a specific value or range of values on a {@link XYChartPane} with an optional
 * {@link #textProperty() text label} description.
 * 
 * @param <X> type of X values
 * @param <Y> type of Y values
 * @author mhrabia
 */
public abstract class AbstractValueIndicator<X, Y> extends XYChartPlugin<X, Y> {

    private final ChangeListener<? super Number> axisBoundsListener = (obs, oldVal, newVal) -> layoutChildren();

    private final ListChangeListener<? super XYChartPlugin<?, ?>> pluginsListListener = (
            Change<? extends XYChartPlugin<?, ?>> change) -> updateStyleClass();

    protected final Label label = new Label();

    /**
     * Creates a new instance of the indicator.
     */
    protected AbstractValueIndicator() {
        this(null);
    }

    /**
     * Creates a new instance of the indicator.
     * 
     * @param text the text to be shown by the label. Value of {@link #textProperty()}.
     */
    protected AbstractValueIndicator(String text) {
        setText(text);
        label.setMouseTransparent(true);

        chartPaneProperty().addListener((obs, oldChartPane, newChartPane) -> {
            if (oldChartPane != null) {
                removeAxisListener(oldChartPane);
                removePluginsListListener(oldChartPane);
            }
            if (newChartPane != null) {
                addAxisListener(newChartPane);
                addPluginsListListener(newChartPane);
            }
        });

        textProperty().addListener((obs, oldText, newText) -> layoutChildren());
    }

    private void addAxisListener(XYChartPane<X, Y> chartPane) {
        ValueAxis<?> valueAxis = getValueAxis(chartPane);
        valueAxis.lowerBoundProperty().addListener(axisBoundsListener);
        valueAxis.upperBoundProperty().addListener(axisBoundsListener);
    }

    private void removeAxisListener(XYChartPane<X, Y> chartPane) {
        ValueAxis<?> valueAxis = getValueAxis(chartPane);
        valueAxis.lowerBoundProperty().removeListener(axisBoundsListener);
        valueAxis.upperBoundProperty().removeListener(axisBoundsListener);
    }

    private void addPluginsListListener(XYChartPane<X, Y> chartPane) {
        chartPane.getPlugins().addListener(pluginsListListener);
        updateStyleClass();
    }

    private void removePluginsListListener(XYChartPane<X, Y> chartPane) {
        chartPane.getPlugins().removeListener(pluginsListListener);
    }

    /**
     * There might be several instances of a given indicator class. If one wants to specify different CSS for each
     * instance - we need a unique class name for each, so whenever the list of plugins changes, this method should
     * update name of it's CSS class.
     */
    abstract void updateStyleClass();

    void setStyleClasses(Node node, String prefix, String root) {
        node.getStyleClass().setAll(root, prefix + root, prefix + root + getIndicatorInstanceIndex());
    }
    
    private int getIndicatorInstanceIndex() {
        if (getChartPane() == null) {
            return 0;
        }
        Class<?> thisClass = getClass();
        int instanceIndex = -1;
        for (XYChartPlugin<X, Y> plugin : getChartPane().getPlugins()) {
            if (plugin.getClass().equals(thisClass)) {
                instanceIndex++;
            }
            if (plugin == this) {
                break;
            }
        }
        return instanceIndex < 0 ? 0 : instanceIndex;
    }

    /**
     * Returns the ValueAxis that this indicator is associated with.
     * 
     * @return associated ValueAxis
     */
    protected abstract ValueAxis<?> getValueAxis(XYChartPane<X, Y> chartPane);

    /**
     * Text to be displayed by the label. If set to {@code null}, the label is not shown.
     * 
     * @return text of the indicator's label
     */
    public final StringProperty textProperty() {
        return label.textProperty();
    }

    /**
     * Returns the value of the {@link #textProperty()}.
     * 
     * @return text displayed within or next to the indicator
     */
    public final String getText() {
        return textProperty().get();
    }

    /**
     * Sets the value of the {@link #textProperty()}.
     * 
     * @param text the new text. If {@code null}, the label will be hidden.
     */
    public final void setText(String text) {
        textProperty().set(text);
    }

    private final ObjectProperty<HPos> labelHorizontalAnchor = new SimpleObjectProperty<HPos>(this,
            "labelHorizontalAnchor", HPos.CENTER) {
        @Override
        protected void invalidated() {
            layoutChildren();
        }
    };

    /**
     * Specifies anchor of the {@link #textProperty() text label} with respect to the horizontal label position i.e. it
     * describes whether the position is related to the LEFT, CENTER or RIGHT side of the label. The position itself
     * should be specified by the extending classes.
     * <p>
     * <b>Default value: {@link HPos#CENTER}</b>
     * </p>
     * 
     * @return labelHorizontalAnchor property
     */
    public final ObjectProperty<HPos> labelHorizontalAnchorProperty() {
        return labelHorizontalAnchor;
    }

    /**
     * Returns the value of the {@link #labelHorizontalAnchorProperty()}.
     * 
     * @return value of the labelHorizontalAnchor property
     */
    public final HPos getLabelHorizontalAnchor() {
        return labelHorizontalAnchorProperty().get();
    }

    /**
     * Sets the value of the {@link #labelHorizontalAnchorProperty()}.
     * 
     * @param anchor new anchor
     */
    public final void setLabelHorizontalAnchor(HPos anchor) {
        labelHorizontalAnchorProperty().set(anchor);
    }

    private final ObjectProperty<VPos> labelVerticalAnchor = new SimpleObjectProperty<VPos>(this, "labelVerticalAnchor",
            VPos.CENTER) {
        @Override
        protected void invalidated() {
            layoutChildren();
        }
    };

    /**
     * Specifies anchor of the {@link #textProperty() text label} with respect to the vertical label position i.e. it
     * describes whether the position is related to the TOP, CENTER, BASELINE or BOTTOM of of the label. The position
     * itself should be specified by the extending classes.
     * <p>
     * <b>Default value: {@link VPos#CENTER}</b>
     * </p>
     * 
     * @return labelVerticalAnchor property
     */
    public final ObjectProperty<VPos> labelVerticalAnchorProperty() {
        return labelVerticalAnchor;
    }

    /**
     * Returns the value of the {@link #labelVerticalAnchorProperty()}.
     * 
     * @return value of the labelVerticalAnchor property
     */
    public final VPos getLabelVerticalAnchor() {
        return labelVerticalAnchorProperty().get();
    }

    /**
     * Sets the value of the {@link #labelVerticalAnchorProperty()}.
     * 
     * @param anchor new anchor
     */
    public final void setLabelVerticalAnchor(VPos anchor) {
        labelVerticalAnchorProperty().set(anchor);
    }

    /**
     * Layouts the label within specified bounds and given horizontal and vertical position, taking into account
     * {@link #labelHorizontalAnchorProperty() horizontal} and {@link #labelVerticalAnchorProperty() vertical} anchor.
     * 
     * @param bounds the bounding rectangle with respect to which the label should be positioned
     * @param hPos relative [0, 1] horizontal position of the label within the bounds
     * @param vPos relative [0, 1] vertical position of the label within the bounds
     */
    protected final void layoutLabel(Bounds bounds, double hPos, double vPos) {
        if (label.getText() == null || label.getText().isEmpty()) {
            getChartChildren().remove(label);
            return;
        }

        double xPos = bounds.getMinX() + bounds.getWidth() * hPos;
        double yPos = bounds.getMinY() + bounds.getHeight() * (1 - vPos);

        double width = label.prefWidth(-1);
        double height = label.prefHeight(width);

        if (getLabelHorizontalAnchor() == HPos.CENTER) {
            xPos -= width / 2;
        } else if (getLabelHorizontalAnchor() == HPos.RIGHT) {
            xPos -= width;
        }

        if (getLabelVerticalAnchor() == VPos.CENTER) {
            yPos -= height / 2;
        } else if (getLabelVerticalAnchor() == VPos.BASELINE) {
            yPos -= label.getBaselineOffset();
        } else if (getLabelVerticalAnchor() == VPos.BOTTOM) {
            yPos -= height;
        }
        label.resizeRelocate(xPos, yPos, width, height);
        addChildNodeIfNotPresent(label);
    }

    void addChildNodeIfNotPresent(Node node) {
        if (!getChartChildren().contains(node)) {
            getChartChildren().add(node);
        }
    }
}
