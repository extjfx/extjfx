/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.plugins;

import static cern.extjfx.chart.Axes.isValueAxis;
import static cern.extjfx.chart.Axes.toValueAxis;

import cern.extjfx.chart.AbstractNumericAxis;
import cern.extjfx.chart.XYChartPlugin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.util.StringConverter;

/**
 * An abstract plugin with associated formatters for X and Y value of the data. For details see
 * {@link AbstractDataFormattingPlugin#formatData(Axis, Data)} method.
 *
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public abstract class AbstractDataFormattingPlugin<X, Y> extends XYChartPlugin<X, Y> {

    /**
     * Creates a new instance of AbstractDataIndicator.
     */
    protected AbstractDataFormattingPlugin() {
        chartPaneProperty().addListener((obs, oldChartPane, newChartPane) -> {
            if (newChartPane != null) {
                defaultXValueFormatter = createDefaultFormatter(newChartPane.getChart().getXAxis());
                defaultYValueFormatter = createDefaultFormatter(newChartPane.getChart().getYAxis());
            }
        });
    }

    private final ObjectProperty<StringConverter<X>> xValueFormatter = new SimpleObjectProperty<>(this,
            "xValueFormatter");

    /**
     * StringConverter used to format X values. If {@code null} a default will be used.
     *
     * @return the X value formatter property
     */
    public final ObjectProperty<StringConverter<X>> xValueFormatterProperty() {
        return xValueFormatter;
    }

    /**
     * Returns the value of the {@link #xValueFormatterProperty()}.
     *
     * @return the X value formatter
     */
    public final StringConverter<X> getXValueFormatter() {
        return xValueFormatterProperty().get();
    }

    /**
     * Sets the value of the {@link #xValueFormatterProperty()}.
     *
     * @param formatter the X value formatter
     */
    public final void setXValueFormatter(StringConverter<X> formatter) {
        xValueFormatterProperty().set(formatter);
    }

    private final ObjectProperty<StringConverter<Y>> yValueFormatter = new SimpleObjectProperty<>(this,
            "yValueFormatter");

    /**
     * StringConverter used to format Y values. If {@code null} a default will be used.
     *
     * @return the Y value formatter property
     */
    public final ObjectProperty<StringConverter<Y>> yValueFormatterProperty() {
        return yValueFormatter;
    }

    /**
     * Returns the value of the {@link #xValueFormatterProperty()}.
     *
     * @return the X value formatter
     */
    public final StringConverter<Y> getYValueFormatter() {
        return yValueFormatterProperty().get();
    }

    /**
     * Sets the value of the {@link #xValueFormatterProperty()}.
     *
     * @param formatter the X value formatter
     */
    public final void setYValueFormatter(StringConverter<Y> formatter) {
        yValueFormatterProperty().set(formatter);
    }

    private StringConverter<X> defaultXValueFormatter;
    private StringConverter<Y> defaultYValueFormatter;

    @SuppressWarnings("unchecked")
    private static <T> StringConverter<T> createDefaultFormatter(Axis<T> axis) {
        if (axis instanceof AbstractNumericAxis) {
            return (StringConverter<T>) new AbstractNumericAxis.DefaultFormatter((AbstractNumericAxis) axis);
        }
        if (axis instanceof NumberAxis) {
            return (StringConverter<T>) new NumberAxis.DefaultFormatter((NumberAxis) axis);
        }
        return new DefaultFormatter<>();
    }

    /**
     * Formats the data to be displayed by this plugin. Uses the specified {@link #xValueFormatterProperty()} and
     * {@link #yValueFormatterProperty()} to obtain the corresponding formatters. If it is {@code null} and the axis is
     * a {@code ValueAxis} - the method will use {@link ValueAxis#getTickLabelFormatter() tick label formatter}. If this
     * one is also not initialized - a default formatter is used.
     * <p>
     * Can be overridden to modify formatting of the data.
     * 
     * @param yAxis axis for which formatting should be done
     * @param data the data point to be formatted
     * @return formatted data
     */
    protected String formatData(Axis<Y> yAxis, Data<X, Y> data) {
        return getXValueFormatter(getChartPane().getChart().getXAxis()).toString(data.getXValue()) + ", "
                + getYValueFormatter(yAxis).toString(data.getYValue());
    }

    private StringConverter<X> getXValueFormatter(Axis<X> xAxis) {
        return getValueFormatter(xAxis, getXValueFormatter(), defaultXValueFormatter);
    }

    private StringConverter<Y> getYValueFormatter(Axis<Y> yAxis) {
        return getValueFormatter(yAxis, getYValueFormatter(), defaultYValueFormatter);
    }

    @SuppressWarnings("unchecked")
    private <T> StringConverter<T> getValueFormatter(Axis<T> axis, StringConverter<T> formatter,
            StringConverter<T> defaultFormatter) {
        StringConverter<T> valueFormatter = formatter;
        if (valueFormatter == null && isValueAxis(axis)) {
            valueFormatter = (StringConverter<T>) toValueAxis(axis).getTickLabelFormatter();
        }
        if (valueFormatter == null) {
            valueFormatter = defaultFormatter;
        }
        return valueFormatter;
    }

    private static class DefaultFormatter<T> extends StringConverter<T> {
        @Override
        public String toString(T value) {
            return String.valueOf(value);
        }

        @Override
        public final T fromString(String string) {
            throw new UnsupportedOperationException();
        }
    }
}
