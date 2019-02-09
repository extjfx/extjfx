/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import cern.extjfx.chart.skin.SeriesTableViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;

/**
 * TableView displaying {@link Series} data in columns.
 * <p>
 * CSS class name: {@value #STYLE_CLASS}.
 * 
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public class SeriesTableView<X, Y> extends Control {

    /**
     * Name of the CCS class of this control.
     */
    public static final String STYLE_CLASS = "series-table-view";

    private final ObjectProperty<ObservableList<Series<X, Y>>> data = new SimpleObjectProperty<>(this, "data",
            FXCollections.observableList(new LinkedList<>()));

    /**
     * ObservableList of series to be displayed in the TableViSew.
     * 
     * @return the property containing list of series to be displayed
     */
    public final ObjectProperty<ObservableList<Series<X, Y>>> dataProperty() {
        return data;
    }

    /**
     * Returns the value of the {@link #dataProperty()}.
     * 
     * @return list of series to be displayed
     */
    public final ObservableList<Series<X, Y>> getData() {
        return dataProperty().get();
    }

    /**
     * Sets the value of the {@link #dataProperty()}.
     * 
     * @param value of series to be displayed
     */
    public final void setData(ObservableList<Series<X, Y>> value) {
        dataProperty().set(value);
    }

    private final Map<Series<X, Y>, StringConverter<X>> xFormatters = new WeakHashMap<>();
    private final Map<Series<X, Y>, StringConverter<Y>> yFormatters = new WeakHashMap<>();

    /**
     * Creates a new instance
     */
    public SeriesTableView() {
        getStyleClass().setAll(STYLE_CLASS);
    }

    /**
     * Creates a new instance of SeriesTableView displaying specified series.
     * 
     * @param series the series to be displayed
     */
    public SeriesTableView(Series<X, Y> series) {
        this();
        getData().add(series);
    }

    /**
     * Creates a new instance of SeriesTableView displaying specified series.
     * <p>
     * Changes of content of the specified list will be reflected in the table.
     * 
     * @param seriesList the list of series to be displayed
     */
    public SeriesTableView(ObservableList<Series<X, Y>> seriesList) {
        this();
        setData(seriesList);
    }

    /**
     * Creates a new instance of SeriesTableView displaying list of series from the given observable value, binding the
     * {@link #dataProperty()} to given observable value.
     * <p>
     * This constructor can be used be used to bind content of this control with series rendered by an {@link XYChart}:
     * 
     * <pre>
     * LineChart<Number, Number> lineChart = ...;
     * SeriesTableView<Number, Number> chartTable = new SeriesTableView<>(lineChart.dataProperty());
     * </pre>
     * 
     * In such case any changes of data rendered by the chart will be reflected in the table view.
     * 
     * @param value an observable value containing list of series to be displayed
     */
    public SeriesTableView(ObservableValue<ObservableList<Series<X, Y>>> value) {
        this();
        setData(value.getValue());
        dataProperty().bind(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SeriesTableViewSkin<>(this);
    }

    /**
     * Sets a default formatter to be used for X coordinates.
     * 
     * @param formatter the formatter of X coordinates
     */
    public void setXFormatter(StringConverter<X> formatter) {
        setXFormatter(formatter, null);
    }

    /**
     * Returns the default X coordinates formatter.
     * 
     * @return default X formatter
     */
    public StringConverter<X> getXFormatter() {
        return getXFormatter(null);
    }

    /**
     * Sets a formatter to be used for X coordinates of given series.
     * 
     * @param formatter the formatter of X coordinates
     * @param series the series for which given X formatter should be used
     */
    public void setXFormatter(StringConverter<X> formatter, Series<X, Y> series) {
        xFormatters.put(series, formatter);
    }

    /**
     * Returns a formatter to be used for X coordinates of given series.
     * 
     * @param series the series for which X formatter should be returned
     * @return formatter to be used for X coordinates of given series
     */
    public StringConverter<X> getXFormatter(Series<X, Y> series) {
        return xFormatters.get(series);
    }

    /**
     * Sets a default formatter to be used for Y coordinates.
     * 
     * @param formatter the formatter of Y coordinates
     */
    public void setYFormatter(StringConverter<Y> formatter) {
        setYFormatter(formatter, null);
    }

    /**
     * Returns the default formatter to be used for Y coordinates.
     * 
     * @return formatter to be used for Y coordinates of given series
     */
    public StringConverter<Y> getYFormatter() {
        return getYFormatter(null);
    }

    /**
     * Sets a formatter to be used for Y coordinates of given series.
     * 
     * @param formatter the formatter of Y coordinates
     * @param series the series for which given Y formatter should be used
     */
    public void setYFormatter(StringConverter<Y> formatter, Series<X, Y> series) {
        yFormatters.put(series, formatter);
    }

    /**
     * Returns a formatter to be used for Y coordinates of given series.
     * 
     * @param series the series for which Y formatter should be returned
     * @return formatter to be used for Y coordinates of given series
     */
    public StringConverter<Y> getYFormatter(Series<X, Y> series) {
        return yFormatters.get(series);
    }
}
