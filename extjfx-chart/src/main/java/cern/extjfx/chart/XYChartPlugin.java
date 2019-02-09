/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

/**
 * Represents an add-on to a XYChart that can either annotate/decorate the chart or perform some interactions with it.
 * <p>
 * Concrete plugin implementations may add custom nodes to the chart via {@link #getChartChildren()} which returns an
 * observable and modifiable list of nodes that will be added to the {@code XYChartPane} on top of charts.
 * <p>
 * Plugins may also listen and react to events (e.g. mouse events) generated on the {@code XYChartPane} via
 * {@link #registerMouseEventHandler(EventType, EventHandler)} method.
 * <p>
 * When plugin is added to the {@code XYChartPane}, it is bound to it via {@link #chartPaneProperty()}, therefore plugin
 * implementations may observe it and be notified when they are added or removed from the {@code XYChartPane}.
 *
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public abstract class XYChartPlugin<X, Y> {

    private final ObservableList<Node> chartChildren = FXCollections.observableArrayList();
    private final List<Pair<EventType<MouseEvent>, EventHandler<MouseEvent>>> mouseEventHandlers = new LinkedList<>();

    private final ObjectProperty<XYChartPane<X, Y>> chartPane = new SimpleObjectProperty<>(this, "chartPane");

    /**
     * The associated {@link XYChartPane}. Initialized when the plugin is added to the ChartPane, set to {@code null}
     * when removed.
     *
     * @return the chartPane property
     */
    public final ObjectProperty<XYChartPane<X, Y>> chartPaneProperty() {
        return chartPane;
    }

    /**
     * Returns the value of the {@link #chartPaneProperty()}.
     *
     * @return the associated ChartPane or {@code null}
     */
    public final XYChartPane<X, Y> getChartPane() {
        return chartPaneProperty().get();
    }

    /**
     * Called by the {@link XYChartPane} when the plugin is added to it.
     *
     * @param chartPane the chart pane
     */
    public final void setChartPane(XYChartPane<X, Y> chartPane) {
        chartPaneProperty().set(chartPane);
    }

    /**
     * Creates a new instance of the ChartPlugin.
     */
    protected XYChartPlugin() {
        chartPaneProperty().addListener((obs, oldChartPane, newChartPane) -> {
            removeEventHanlders(oldChartPane);
            addEventHandlers(newChartPane);
        });
    }

    private void removeEventHanlders(Node node) {
        if (node == null) {
            return;
        }
        for (Pair<EventType<MouseEvent>, EventHandler<MouseEvent>> pair : mouseEventHandlers) {
            node.removeEventHandler(pair.getKey(), pair.getValue());
        }
    }

    private void addEventHandlers(Node node) {
        if (node == null) {
            return;
        }
        for (Pair<EventType<MouseEvent>, EventHandler<MouseEvent>> pair : mouseEventHandlers) {
            node.addEventHandler(pair.getKey(), pair.getValue());
        }
    }

    /**
     * Registers event handlers that should be added to the {@code XYChartPane} node when the plugin is added to the
     * pane and are removed once the plugin is removed from the pane.
     *
     * @param eventType the event type on which the handler should be called
     * @param handler the event handler to be added to the chart
     */
    protected final void registerMouseEventHandler(EventType<MouseEvent> eventType, EventHandler<MouseEvent> handler) {
        mouseEventHandlers.add(new Pair<>(eventType, handler));
    }

    /**
     * Convenience method returning a list containing the {@link XYChartPane#getChart() chart pane base chart} and all
     * {@link XYChartPane#getOverlayCharts() overlay charts}.
     */
    protected final List<XYChart<X, Y>> getCharts() {
        if (getChartPane() == null) {
            return Collections.emptyList();
        }
        List<XYChart<X, Y>> charts = new LinkedList<>();
        charts.add(getChartPane().getChart());
        charts.addAll(getChartPane().getOverlayCharts());
        return charts;
    }

    /**
     * Returns a list containing nodes that should be added to the list of child nodes of the associated XYChart's plot
     * area children.
     * <p>
     * The method should be used by concrete implementations to add nodes that should be added to the chart area.
     *
     * @return non-null list of nodes to be added to the chart's plot area
     */
    public final ObservableList<Node> getChartChildren() {
        return chartChildren;
    }

    /**
     * Optional method that allows the plug-in to react in case the size of the {@link XYChartPane} that it belongs to
     * has changed.
     */
    public void layoutChildren() {
        // empty by default
    }

    /**
     * Converts mouse location within the scene to the location relative to the plot area.
     *
     * @param event mouse event
     * @return location within the plot area
     */
    protected final Point2D getLocationInPlotArea(MouseEvent event) {
        Point2D mouseLocationInScene = new Point2D(event.getSceneX(), event.getSceneY());
        double xInAxis = getChartPane().getChart().getXAxis().sceneToLocal(mouseLocationInScene).getX();
        double yInAxis = getChartPane().getChart().getYAxis().sceneToLocal(mouseLocationInScene).getY();
        return new Point2D(xInAxis, yInAxis);
    }

    /**
     * Converts given point in data coordinates to a point in display coordinates.
     *
     * @param dataPoint data point to be converted
     * @return corresponding display point within the plot area
     */
    protected final Point2D toDisplayPoint(Axis<Y> yAxis, Data<X, Y> dataPoint) {
        return new Point2D(getChartPane().getChart().getXAxis().getDisplayPosition(dataPoint.getXValue()),
                yAxis.getDisplayPosition(dataPoint.getYValue()));
    }

    /**
     * Converts given display point within the plot area coordinates to the corresponding data point within data
     * coordinates.
     *
     * @param displayPoint the display point to be converted
     * @return the data point
     */
    protected final Data<X, Y> toDataPoint(Axis<Y> yAxis, Point2D displayPoint) {
        return new Data<>(getChartPane().getChart().getXAxis().getValueForDisplay(displayPoint.getX()),
                yAxis.getValueForDisplay(displayPoint.getY()));
    }
}
