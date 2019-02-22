/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.EnumConverter;

import cern.extjfx.chart.plugins.Panner;
import cern.extjfx.chart.plugins.Zoomer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

/**
 * A pane for {@link XYChart}s that allows adding custom {@link #getPlugins() chart plugins} and
 * {@link #getOverlayCharts() overlay} different chart types. The basic usage of the pane is following:
 *
 * <pre>{@code 
 * NumericAxis xAxis = new NumericAxis();
 * xAxis.setAnimated(false);
 *
 * NumericAxis yAxis = new NumericAxis();
 * yAxis.setAnimated(false);
 * yAxis.setAutoRangePadding(0.1);
 *
 * LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
 * XYChartPane<Number, Number> chartPane = new XYChartPane<>(lineChart);
 * chartPane.getPlugins().addAll(new Zoomer(), new Panner(), new DataPointTooltip<>());
 * }</pre>
 *
 * <b>Note that certain plugin implementations such as {@link Zoomer} or {@link Panner} work properly only if both X and
 * Y axis are instances of {@link NumericAxis}.</b>
 * <p>
 * The overlay charts can be added on top of the base chart:
 *
 * <pre>{@code 
 * ScatterChart<Number, Number> overlayChart1 = ...;
 * ScatterChart<Number, Number> overlayChart2 = ...;
 * chartPane.getOverlayCharts().addAll(overlayChart1, overlayChart2);
 * }</pre>
 *
 * Constraints concerning overlay charts:
 * <ul>
 * <li>Chart background and content fill is set to {@code null} (to make it transparent and to not intercept mouse
 * events).</li>
 * <li>Grid lines, title and legend are hidden. The same applies to horizontal and vertical zero line.</li>
 * <li>The X axis opacity is set to 0 and the {@link Axis#autoRangingProperty() auto-ranging} is set to {@code false}
 * (to follow the range of the base chart).</li>
 * <li>The X axis lower and upper bound follow (are bound) to the bounds of the X axis of the base chart.</li>
 * <li>The {@link #commonYAxisProperty()} can be used to configure a single (common) Y axis or independent axes for each
 * chart. The Y axis {@link Axis#getSide() side} is respected for each overlay chart. If the {@code commonYAxis}
 * property is value is {@code true} the Y axis range follows the range of the base chart's Y axis.</li>
 * <li>The {@link Chart#titleProperty() title} of individual charts added to the pane are set to {@code null}. Use
 * {@link #titleProperty()} instead.</li>
 * <li>Legends of individual charts are hidden and therefore setting their {@link Chart#legendVisibleProperty()
 * visibility} or {@link Chart#legendSideProperty() side} will have no effect. Instead set legend's visibility and side
 * using XYChartPane properties with the same names.</li>
 * </ul>
 *
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public class XYChartPane<X, Y> extends Region {
    private static final String CHART_CSS = XYChartPane.class.getResource("chart.css").toExternalForm();
    private static final String OVERLAY_CHART_CSS = XYChartPane.class.getResource("overlay-chart.css").toExternalForm();

    private final Label titleLabel = new Label();
    private final Legend legend = new Legend();

    private final XYChart<X, Y> baseChart;
    private final Group overlayChartsArea = createChildGroup();
    private final Group pluginsArea = createChildGroup();
    private final Map<XYChartPlugin<X, Y>, Group> pluginGroups = new HashMap<>();
    private final ObservableList<XYChart<X, Y>> overlayCharts = FXCollections.observableList(new LinkedList<>());
    private final ObservableList<XYChartPlugin<X, Y>> plugins = FXCollections.observableList(new LinkedList<>());

    // Layout request for the base chart or any of the overlay charts (e.g. when
    // data is changed) must be propagated to the XYChartPane
    boolean layoutOngoing = false;
    private final ChangeListener<Boolean> layoutRequestListener = (obs, old, needsLayout) -> {
        if (needsLayout && !layoutOngoing) {
            requestLayout();
        }
    };

    /**
     * Creates a new instance of {@code ChartPane} with given chart.
     *
     * @param chart non-null chart
     */
    public XYChartPane(XYChart<X, Y> chart) {
        baseChart = requireNonNull(chart, "The chart must not be null");
        Utils.getChartContent(baseChart).needsLayoutProperty().addListener(layoutRequestListener);

        getStyleClass().add("chart-pane");
        overlayCharts.addListener(overlayChartsChanged);
        plugins.addListener(pluginsChanged);

        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.focusTraversableProperty().bind(Platform.accessibilityActiveProperty());
        titleLabel.getStyleClass().add("chart-title");

        legend.visibleProperty().bind(legendVisible);

        getChildren().addAll(baseChart, overlayChartsArea, pluginsArea, titleLabel, legend);
    }

    @Override
    public String getUserAgentStylesheet() {
        return CHART_CSS;
    }

    private static Group createChildGroup() {
        Group group = new Group();
        group.setManaged(false);
        group.setAutoSizeChildren(false);
        group.relocate(0, 0);
        return group;
    }

    /**
     * Returns the base chart associated with this pane.
     *
     * @return the base chart
     */
    public final XYChart<X, Y> getChart() {
        return baseChart;
    }

    /**
     * Returns a list of overlay charts rendered on top of the {@link #getChart() base chart}.
     *
     * @return a modifiable list of overlay charts
     */
    public final ObservableList<XYChart<X, Y>> getOverlayCharts() {
        return overlayCharts;
    }

    /**
     * Returns a list of pluggins added to this chart pane.
     *
     * @return a modifiable list of plugins
     */
    public final ObservableList<XYChartPlugin<X, Y>> getPlugins() {
        return plugins;
    }

    /**
     * Returns bounds of the plot area within the coordinates of the {@code ChartPane}.
     *
     * @return plot area bounds
     */
    public final Bounds getPlotAreaBounds() {
        Axis<X> xAxis = baseChart.getXAxis();
        Axis<Y> yAxis = baseChart.getYAxis();

        Point2D xAxisLoc = getLocationInChartPane(xAxis);
        Point2D yAxisLoc = getLocationInChartPane(yAxis);
        return new BoundingBox(xAxisLoc.getX(), yAxisLoc.getY(), xAxis.getWidth(), yAxis.getHeight());
    }

    Point2D getLocationInChartPane(Node node) {
        return localToChartPane(node, new Point2D(0, 0));
    }

    Point2D localToChartPane(Node node, Point2D point) {
        if (this.equals(node) || node.getParent() == null) {
            return point;
        }
        return localToChartPane(node.getParent(), node.localToParent(point));
    }

    /**
     * Translates point from ChartPane coordinates to the plot area coordinates.
     *
     * @param point the point to translate
     * @return point in plot area coordinates
     */
    public final Point2D toPlotArea(Point2D point) {
        return toPlotArea(point.getX(), point.getY());
    }

    /**
     * Translates point from chart pane coordinates to the plot area coordinates.
     *
     * @param xCoord the x coordinate within ChartPane coordinates system
     * @param yCoord the y coordinate within ChartPane coordinates system
     * @return point in plot area coordinates
     */
    public final Point2D toPlotArea(double xCoord, double yCoord) {
        Bounds plotAreaBounds = getPlotAreaBounds();
        return new Point2D(xCoord - plotAreaBounds.getMinX(), yCoord - plotAreaBounds.getMinY());
    }

    /**
     * Translates bounds from chart pane coordinates to the plot area coordinates.
     *
     * @param bounds the bounds within ChartPane coordinates system
     * @return bounds in plot area coordinates
     */
    public final Bounds toPlotArea(Bounds bounds) {
        Bounds plotAreaBounds = getPlotAreaBounds();
        return new BoundingBox(bounds.getMinX() - plotAreaBounds.getMinX(), bounds.getMinY() - plotAreaBounds.getMinY(),
                bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Enables {@link Axis#autoRangingProperty() auto-ranging} property on both X and Y axis by calling
     * {@link #enableXAxisAutoRanging()} and {@link #enableYAxisAutoRanging()}.
     */
    public final void enableAxisAutoRanging() {
        enableXAxisAutoRanging();
        enableYAxisAutoRanging();
    }

    /**
     * Enables {@link Axis#autoRangingProperty() auto-ranging} property on the {@link #getChart() base chart} X axis.
     * Note that {@link #getOverlayCharts() overlay chart} X axes should have always the auto-ranging property set to
     * {@code false} as their range should follow the range of the base chart X axis.
     */
    public final void enableXAxisAutoRanging() {
        getChart().getXAxis().setAutoRanging(true);
    }

    /**
     * Enables {@link Axis#autoRangingProperty() auto-ranging} property on the {@link #getChart() base chart} Y axis and
     * possibly on {@link #getOverlayCharts() overlay charts} Y axes. <br>
     * If there are overlay charts added to the pane, their Y axis auto-ranging property is set to {@code true} only if
     * {@link #commonYAxisProperty()} is {@code false}. In other words if there is a single Y axis, the auto-ranging of
     * all overlay chart Y axes should be disabled and follow the Y range of the base chart.
     */
    public final void enableYAxisAutoRanging() {
        getChart().getYAxis().setAutoRanging(true);
        if (!isCommonYAxis()) {
            getOverlayCharts().forEach(chart -> chart.getYAxis().setAutoRanging(true));
        }
    }

    @Override
    protected void layoutChildren() {
        layoutOngoing = true;
        new ChartPaneLayoutManager().layout();
        layoutPluginsChildren();
        layoutOngoing = false;
    }

    private void layoutPluginsChildren() {
        for (XYChartPlugin<X, Y> plugin : plugins) {
            plugin.layoutChildren();
        }
    }

    private final BooleanProperty commonYAxis = new SimpleBooleanProperty(this, "commonYAxis", false) {
        @Override
        protected void invalidated() {
            overlayCharts.stream().map(XYChart::getYAxis).forEach(XYChartPane.this::configureOverlayYAxis);
            requestLayout();
        }
    };

    /**
     * Indicates whether {@link #getOverlayCharts() overlay charts} should share Y axis with the {@link #getChart() base
     * chart} or axis of each chart should be rendered separately.
     * <p>
     * <b>Default Value: {@code false}</b>
     * </p>
     *
     * @return the commonYAxis property
     */
    public final BooleanProperty commonYAxisProperty() {
        return commonYAxis;
    }

    /**
     * Sets the value of the {@link #commonYAxisProperty()}.
     *
     * @param shareYAxis if {@code true} all charts will share the Y axis
     */
    public final void setCommonYAxis(boolean shareYAxis) {
        commonYAxisProperty().set(shareYAxis);
    }

    /**
     * Returns the value of the {@link #commonYAxisProperty()}.
     *
     * @return value of the {@link #commonYAxisProperty()}
     */
    public final boolean isCommonYAxis() {
        return commonYAxisProperty().get();
    }

    private final ListChangeListener<XYChart<X, Y>> overlayChartsChanged = (change) -> {
        while (change.next()) {
            change.getAddedSubList().forEach(this::overlayChartAdded);
            change.getRemoved().forEach(this::overlayChartRemoved);
        }
        overlayChartsArea.getChildren().setAll(overlayCharts);
    };

    private void overlayChartAdded(XYChart<X, Y> chart) {
        applyOverlayStyle(chart);
        makeTransparentToMouseEventsExceptPlotContent(chart);
        configureOverlayXAxis(chart.getXAxis());
        configureOverlayYAxis(chart.getYAxis());
        Utils.getChartContent(chart).needsLayoutProperty().addListener(layoutRequestListener);
    }

    private static void applyOverlayStyle(XYChart<?, ?> chart) {
        chart.getStylesheets().add(OVERLAY_CHART_CSS);
        chart.setTitle(null);
    }

    /**
     * The overlay charts have background fill set to null (in CSS) and their background should be transparent to mouse
     * events so the charts below can react on events.
     */
    private static void makeTransparentToMouseEventsExceptPlotContent(XYChart<?, ?> chart) {
        makeTransparentToMouseEvents(chart, Utils.getPlotContent(chart));
    }

    private static boolean makeTransparentToMouseEvents(Node node, Node plotContent) {
        node.setPickOnBounds(false);
        if (node == plotContent) {
            return true;
        }

        boolean containsPlotContent = false;
        if (node instanceof Parent) {
            for (Node childNode : ((Parent) node).getChildrenUnmodifiable()) {
                if (makeTransparentToMouseEvents(childNode, plotContent)) {
                    containsPlotContent = true;
                }
            }
        }
        node.setMouseTransparent(!containsPlotContent);
        return containsPlotContent;
    }

    private void configureOverlayXAxis(Axis<?> xAxis) {
        xAxis.setOpacity(0);
        // Turn off the auto-ranging - the X range is dictated by the base chart
        xAxis.setAutoRanging(false);
        bindAxisBounds(xAxis, baseChart.getXAxis());
    }

    private void configureOverlayYAxis(Axis<Y> yAxis) {
        if (isCommonYAxis()) {
            yAxis.setOpacity(0);
            yAxis.setAutoRanging(false);
            yAxis.setSide(getEffectiveVerticalSide(baseChart.getYAxis()));
            bindAxisBounds(yAxis, baseChart.getYAxis());
        } else {
            yAxis.setOpacity(1);
            yAxis.prefWidthProperty().set(USE_COMPUTED_SIZE);
            unbindAxisBounds(yAxis);
        }
    }

    private void overlayChartRemoved(XYChart<?, ?> chart) {
        unbindAxisBounds(chart.getXAxis());
        unbindAxisBounds(chart.getYAxis());
        Utils.getChartContent(chart).needsLayoutProperty().removeListener(layoutRequestListener);
    }

    private static void bindAxisBounds(Axis<?> axis, Axis<?> baseAxis) {
        if (Axes.isValueAxis(axis) && Axes.isValueAxis(baseAxis)) {
            Axes.bindBounds(Axes.toValueAxis(axis), Axes.toValueAxis(baseAxis));
        }
    }

    private static void unbindAxisBounds(Axis<?> axis) {
        if (Axes.isValueAxis(axis)) {
            Axes.unbindBounds(Axes.toValueAxis(axis));
        }
    }

    private final ListChangeListener<XYChartPlugin<X, Y>> pluginsChanged = (change) -> {
        while (change.next()) {
            change.getRemoved().forEach(this::pluginRemoved);
            change.getAddedSubList().forEach(this::pluginAdded);
        }
        updatePluginsArea();
    };

    private void pluginRemoved(XYChartPlugin<X, Y> plugin) {
        plugin.setChartPane(null);
        Group group = pluginGroups.remove(plugin);
        Bindings.unbindContent(group, plugin.getChartChildren());
        group.getChildren().clear();
        pluginsArea.getChildren().remove(group);
    }

    private void pluginAdded(XYChartPlugin<X, Y> plugin) {
        plugin.setChartPane(this);
        Group group = createChildGroup();
        Bindings.bindContent(group.getChildren(), plugin.getChartChildren());
        pluginGroups.put(plugin, group);
    }

    private void updatePluginsArea() {
        pluginsArea.getChildren().setAll(plugins.stream().map(plugin -> pluginGroups.get(plugin)).collect(toList()));
        // GUI-62
        requestLayout();
    }

    private final StringProperty title = new SimpleStringProperty(XYChartPane.this, "title") {
        @Override
        protected void invalidated() {
            titleLabel.setText(get());
            requestLayout();
        }
    };

    /**
     * Title property. Should be used instead of {@link Chart#titleProperty() title} of individual charts.
     *
     * @return the title property
     */
    public final StringProperty titleProperty() {
        return title;
    }

    /**
     * Returns the value of the {@link #titleProperty()}.
     *
     * @return the title
     */
    public final String getTitle() {
        return title.get();
    }

    /**
     * Sets the value of the {@link #titleProperty()}.
     *
     * @param value the new title
     */
    public final void setTitle(String value) {
        title.set(value);
    }

    private final ObjectProperty<Side> titleSide = new StyleableObjectProperty<Side>(Side.TOP) {
        @Override
        protected void invalidated() {
            requestLayout();
        }

        @Override
        public CssMetaData<XYChartPane<?, ?>, Side> getCssMetaData() {
            return StyleableProperties.TITLE_SIDE;
        }

        @Override
        public Object getBean() {
            return XYChartPane.this;
        }

        @Override
        public String getName() {
            return "titleSide";
        }
    };

    /**
     * The side of the chart where the title is displayed.
     * <p>
     * <b>Default Value: Side.TOP</b>
     * </p>
     *
     * @return the titleSide property
     */
    public final ObjectProperty<Side> titleSideProperty() {
        return titleSide;
    }

    /**
     * Returns the value of the {@link #titleSideProperty()}.
     *
     * @return the title side
     */
    public final Side getTitleSide() {
        return titleSide.get();
    }

    /**
     * Sets the value of the {@link #titleSideProperty()}.
     *
     * @param value the title side
     */
    public final void setTitleSide(Side value) {
        titleSide.set(value);
    }

    private final BooleanProperty legendVisible = new StyleableBooleanProperty(true) {
        @Override
        protected void invalidated() {
            requestLayout();
        }

        @Override
        public CssMetaData<XYChartPane<?, ?>, Boolean> getCssMetaData() {
            return StyleableProperties.LEGEND_VISIBLE;
        }

        @Override
        public Object getBean() {
            return XYChartPane.this;
        }

        @Override
        public String getName() {
            return "legendVisible";
        }
    };

    /**
     * Indicates whether the chart legend should be visible.
     *
     * @return the legend's visibility flag
     */
    public final BooleanProperty legendVisibleProperty() {
        return legendVisible;
    }

    /**
     * Returns the value of the {@link #legendVisibleProperty()}.
     *
     * @return {@code true} if the legend is displayed, else {@code false}
     */
    public final boolean isLegendVisible() {
        return legendVisibleProperty().getValue();
    }

    /**
     * Sets the value of the {@link #legendVisibleProperty()}.
     *
     * @param value {@code true} to display the legend, {@code false} to hide it.
     */
    public final void setLegendVisible(boolean value) {
        legendVisibleProperty().setValue(value);
    }

    private final ObjectProperty<Side> legendSide = new StyleableObjectProperty<Side>(Side.BOTTOM) {
        @Override
        protected void invalidated() {
            legend.setOrientation(getLegendSide().isHorizontal() ? Orientation.HORIZONTAL : Orientation.VERTICAL);
            requestLayout();
        }

        @Override
        public CssMetaData<XYChartPane<?, ?>, Side> getCssMetaData() {
            return StyleableProperties.LEGEND_SIDE;
        }

        @Override
        public Object getBean() {
            return XYChartPane.this;
        }

        @Override
        public String getName() {
            return "legendSide";
        }
    };

    /**
     * Side of the chart pane where the legend should be displayed.
     * <p>
     * <b>Default Value: Side.BOTTOM</b>
     * </p>
     *
     * @return the legendSide property
     */
    public final ObjectProperty<Side> legendSideProperty() {
        return legendSide;
    }

    /**
     * Returns the value of the {@link #legendSideProperty()}.
     *
     * @return legend side
     */
    public final Side getLegendSide() {
        return legendSideProperty().get();
    }

    /**
     * Sets the value of the {@link #legendSideProperty()}.
     *
     * @param value the side on which legend should be displayed
     */
    public final void setLegendSide(Side value) {
        legendSideProperty().set(value);
    }

    private class ChartPaneLayoutManager {
        final Region baseChartContent;
        final Map<Axis<Y>, Double> yAxesPrefWidths;
        final double overlayYAxisTotalWidth;
        double baseChartContentXOffset;
        double top, left, bottom, right;
        final double chartPaneWidth = getWidth();
        final double chartPaneHeight = getHeight();

        ChartPaneLayoutManager() {
            top = baseChart.snappedTopInset();
            bottom = baseChart.snappedBottomInset();
            left = baseChart.snappedLeftInset();
            right = baseChart.snappedRightInset();

            baseChartContent = Utils.getChartContent(baseChart);

            if (overlayCharts.isEmpty() || isCommonYAxis()) {
                yAxesPrefWidths = Collections.emptyMap();
                overlayYAxisTotalWidth = 0;
                baseChartContentXOffset = 0;
            } else {
                yAxesPrefWidths = yAxesPrefWidths(getHeight());
                overlayYAxisTotalWidth = yAxisTotalWidth();
                baseChartContentXOffset = baseChartContentXOffset();
            }
        }

        private Map<Axis<Y>, Double> yAxesPrefWidths(double height) {
            Map<Axis<Y>, Double> yAxesWidths = new HashMap<>();
            for (XYChart<X, Y> chart : overlayCharts) {
                Axis<Y> yAxis = chart.getYAxis();
                updateYAxisRange(chart);
                yAxis.prefWidthProperty().set(USE_COMPUTED_SIZE);
                yAxesWidths.put(yAxis, Math.ceil(yAxis.prefWidth(height)));
            }
            return yAxesWidths;
        }

        /**
         * The range is updated during layout pass of the chart (later) but we need it before to properly calculate axis
         * width (that depends on mark units and format).
         */
        private void updateYAxisRange(XYChart<X, Y> chart) {
            if (!chart.getYAxis().isAutoRanging()) {
                return;
            }
            List<Y> yData = new ArrayList<>();
            for (Series<X, Y> series : chart.getData()) {
                for (Data<X, Y> data : series.getData()) {
                    yData.add(data.getYValue());
                }
            }
            if (!yData.isEmpty()) {
                // If data is empty - the dataMin/dataMax in the ValueAxis will be updated from
                // lower/upper bound. If the autoRangePadding is set to a non-zero value,
                // doing the layout will each time increase the upper bound
                chart.getYAxis().invalidateRange(yData);
            }
        }

        private double yAxisTotalWidth() {
            double axesWidth = yAxesPrefWidths.values().stream().mapToDouble(Double::doubleValue).sum();
            for (int i = 0; i < overlayCharts.size() - 1; i++) {
                XYChart<X, Y> chart = overlayCharts.get(i);
                Side effectiveSide = getEffectiveVerticalSide(chart.getYAxis());
                axesWidth += effectiveSide == LEFT ? chart.snappedLeftInset() : chart.snappedRightInset();
            }
            return axesWidth;
        }

        private double baseChartContentXOffset() {
            double offset = 0;
            for (XYChart<X, Y> chart : overlayCharts) {
                if (getEffectiveVerticalSide(chart.getYAxis()) == LEFT) {
                    offset += prefWidth(chart.getYAxis()) + chart.snappedLeftInset();
                }
            }
            return offset;
        }

        private double prefWidth(Axis<Y> yAxis) {
            return yAxesPrefWidths.get(yAxis).doubleValue();
        }

        /**
         * Does the layout of all components.
         */
        void layout() {
            layoutTitle();
            layoutLegend();
            layoutBaseChart();
            layoutOverlayCharts();
        }

        private void layoutTitle() {
            getAllCharts().forEach(chart -> chart.setTitle(null));

            if (getTitle() == null) {
                titleLabel.setVisible(false);
            } else {
                titleLabel.setVisible(true);
                if (getTitleSide().isHorizontal()) {
                    double titleHeight = snapSize(titleLabel.prefHeight(chartPaneWidth - left - right));

                    if (getTitleSide().equals(Side.TOP)) {
                        titleLabel.resizeRelocate(left, top, chartPaneWidth - left - right, titleHeight);
                        top += titleHeight;
                    } else {
                        titleLabel.resizeRelocate(left, chartPaneHeight - bottom - titleHeight,
                                chartPaneWidth - left - right, titleHeight);
                        bottom += titleHeight;
                    }
                } else {
                    double titleWidth = snapSize(titleLabel.prefWidth(chartPaneHeight - top - bottom));

                    if (getTitleSide().equals(Side.LEFT)) {
                        titleLabel.resizeRelocate(left, top, titleWidth, chartPaneHeight - top - bottom);
                        left += titleWidth;
                    } else {
                        titleLabel.resizeRelocate(chartPaneWidth - right - titleWidth, top, titleWidth,
                                chartPaneHeight - top - bottom);
                        right += titleWidth;
                    }
                }
            }
        }

        private List<XYChart<?, ?>> getAllCharts() {
            List<XYChart<?, ?>> allCharts = new LinkedList<>();
            allCharts.add(baseChart);
            allCharts.addAll(overlayCharts);
            return allCharts;
        }

        private void layoutLegend() {
            List<Pane> allLegends = getAllCharts().stream().map(Utils::getLegend).filter(Objects::nonNull)
                    .collect(toList());
            List<Label> legendItems = Utils.getChildLabels(allLegends);
            legend.update(legendItems);
            legendItems.forEach(label -> label.setVisible(false));
            allLegends.forEach(chartLegend -> chartLegend.setPrefSize(0, 0));

            if (!isLegendVisible()) {
                legend.resize(0, 0);
                return;
            }

            double legendY, legendX, legendWidth, legendHeight;

            if (getLegendSide().isHorizontal()) {
                legendHeight = legend.prefHeight(chartPaneWidth - left - right);
                legendWidth = legend.prefWidth(legendHeight);

                legendX = left + (chartPaneWidth - left - right - legendWidth) / 2;

                if (getLegendSide() == Side.TOP) {
                    legendY = top;
                    top += legendHeight;
                } else {
                    legendY = chartPaneHeight - bottom - legendHeight;
                    bottom += legendHeight;
                }
            } else {
                legendWidth = legend.prefWidth(chartPaneHeight - top - bottom);
                legendHeight = legend.prefHeight(legendWidth);

                if (getLegendSide() == Side.LEFT) {
                    legendX = left;
                    left += legendWidth;
                } else {
                    legendX = chartPaneWidth - right - legendWidth;
                    right += legendWidth;
                }
                legendY = (chartPaneHeight - top - bottom - legendHeight) / 2;
            }
            legend.resizeRelocate(legendX, legendY, legendWidth, legendHeight);
        }

        private void layoutBaseChart() {
            double baseChartWidth = chartPaneWidth - overlayYAxisTotalWidth - left - right;
            double baseChartHeight = chartPaneHeight - top - bottom;

            baseChart.resizeRelocate(0, 0, baseChartWidth, baseChartHeight);
            baseChart.requestLayout();
            baseChart.layout();

            baseChartContent.setTranslateX(baseChartContentXOffset + left);
            baseChartContent.setTranslateY(top);
        }

        private void layoutOverlayCharts() {
            if (overlayCharts.isEmpty()) {
                return;
            }

            XYChart<X, Y> prevChart = baseChart;
            Side effectiveSide = getEffectiveVerticalSide(baseChart.getYAxis());
            Axis<Y> prevLeftAxis = effectiveSide == LEFT ? baseChart.getYAxis() : null;
            Axis<Y> prevRightAxis = effectiveSide == RIGHT ? baseChart.getYAxis() : null;

            for (XYChart<X, Y> chart : overlayCharts) {
                Region chartContent = Utils.getChartContent(chart);
                chart.resizeRelocate(0, 0, chartWidth(chart, chartContent), chartHeight(chart, chartContent));

                Axis<X> xAxis = chart.getXAxis();
                Axis<Y> yAxis = chart.getYAxis();
                copyXAxisCategories(xAxis);
                adjustPrefSize(xAxis, yAxis);

                chart.requestLayout();
                chart.layout();

                chartContent.setTranslateX(contentLayoutX(chartContent, yAxis) - chartContent.getLayoutX());
                chartContent.setTranslateY(contentLayoutY(chartContent) - chartContent.getLayoutY());

                if (!isCommonYAxis()) {
                    if (getEffectiveVerticalSide(yAxis) == LEFT) {
                        yAxis.setTranslateX(
                                leftAxisLayoutX(prevChart, prevLeftAxis, xAxis, yAxis) - yAxis.getLayoutX());
                        prevLeftAxis = yAxis;
                    } else {
                        yAxis.setTranslateX(rightAxisLayoutX(prevChart, prevRightAxis, xAxis) - yAxis.getLayoutX());
                        prevRightAxis = yAxis;
                    }
                }
                prevChart = chart;
            }
        }

        private void copyXAxisCategories(Axis<X> xAxis) {
            if (Axes.isCategoryAxis(xAxis) && Axes.isCategoryAxis(baseChart.getXAxis())) {
                ((CategoryAxis) xAxis).setCategories(((CategoryAxis) baseChart.getXAxis()).getCategories());
            }
        }

        private double chartWidth(XYChart<X, Y> chart, Region chartContent) {
            double chartWidth = baseChart.getXAxis().getWidth();
            chartWidth += Utils.getHorizontalInsets(chart.getInsets());
            chartWidth += Utils.getHorizontalInsets(chartContent.getInsets());
            chartWidth += isCommonYAxis() ? baseChart.getYAxis().getWidth() : prefWidth(chart.getYAxis());
            return snapSize(chartWidth);
        }

        private double chartHeight(XYChart<X, Y> chart, Region chartContent) {
            double chartHeight = baseChart.getYAxis().getHeight();
            chartHeight += Utils.getVerticalInsets(chart.getInsets());
            chartHeight += Utils.getVerticalInsets(chartContent.getInsets());
            chartHeight += baseChart.getXAxis().getHeight();
            return snapSize(chartHeight);
        }

        private void adjustPrefSize(Axis<X> xAxis, Axis<Y> yAxis) {
            /*
             * For the overlay X and Y axis we must do a strange thing to make it working i.e. we have to run the
             * calculation of prefHeight (for X axis) and prefWidth (for Y axis) with correct with/height respectively.
             * The reason for this is an implementation of the following methods: - CategoryAxis.calculateNewSpacing(..)
             * - CategoryAxis.calculateNewFirstPos(..) - ValueAxis.calculateNewScale(..) These methods are used during
             * calculation of pref width/heigth but at the same time they preserve the calculated result based on the
             * initial width/height given. So basically if we ask these axes: what would be your preferred width for
             * this height - they give the answer but at the same time update their internal scale and offsets that are
             * used to calculate display positions. So here we run prefHeight and prefWidth with final width/height to
             * ensure that the internal state of both axes is correct.
             */
            xAxis.prefHeightProperty().set(USE_COMPUTED_SIZE);
            xAxis.prefHeight(baseChart.getXAxis().getWidth());
            xAxis.prefHeightProperty().set(baseChart.getXAxis().getHeight());
            xAxis.prefWidthProperty().set(baseChart.getXAxis().getWidth());

            yAxis.prefWidthProperty().set(USE_COMPUTED_SIZE);
            yAxis.prefWidth(baseChart.getYAxis().getHeight());
            yAxis.prefWidthProperty().set(isCommonYAxis() ? baseChart.getYAxis().getWidth() : prefWidth(yAxis));
            yAxis.prefHeightProperty().set(baseChart.getYAxis().getHeight());
        }

        private double contentLayoutX(Region chartContent, Axis<Y> yAxis) {
            double xOffset = getEffectiveVerticalSide(yAxis) == LEFT ? yAxis.getWidth() : 0;
            return Utils.getLocationX(baseChartContent) + baseChart.getXAxis().getLayoutX() - xOffset
                    - chartContent.snappedLeftInset();
        }

        private double contentLayoutY(Region chartContent) {
            return Utils.getLocationY(baseChartContent) + baseChart.getYAxis().getLayoutY()
                    - chartContent.snappedTopInset();
        }

        private double leftAxisLayoutX(XYChart<X, Y> prevChart, Axis<Y> prevLeftAxis, Axis<X> xAxis, Axis<Y> yAxis) {
            double layoutX = 0;
            if (prevLeftAxis == null) {
                layoutX = xAxis.getLayoutX();
            } else {
                layoutX = Utils.getLocationX(prevLeftAxis) - prevChart.snappedLeftInset();
            }
            layoutX -= yAxis.getWidth();
            return layoutX;
        }

        private double rightAxisLayoutX(XYChart<X, Y> prevChart, Axis<Y> prevRightAxis, Axis<X> xAxis) {
            if (prevRightAxis == null) {
                return xAxis.getLayoutX() + xAxis.getWidth();
            }
            return Utils.getLocationX(prevRightAxis) + prevRightAxis.getWidth() + prevChart.snappedRightInset();
        }
    }

    /**
     * If side is badly configured (top/bottom for vertical or left/right for horizontal) - we use a default side.
     */
    private static Side getEffectiveVerticalSide(Axis<?> axis) {
        return axis.getSide().isVertical() ? axis.getSide() : LEFT;
    }

    private static class Legend extends TilePane {
        private static final String CHART_LEGEND_CSS = "chart-legend";
        private static final int GAP = 5;

        Legend() {
            super(GAP, GAP);
            setTileAlignment(Pos.CENTER_LEFT);
            getStyleClass().setAll(CHART_LEGEND_CSS);
        }

        /**
         * If the legend items have changed (text, graphic, style) in source charts, the method re-creates legend items,
         * cloning all the relevant properties.
         */
        void update(List<Label> legendItems) {
            if (legendItemsChanged(legendItems)) {
                getChildren().clear();

                for (Label item : legendItems) {
                    getChildren().add(clone(item));
                }
            }
        }

        private boolean legendItemsChanged(List<Label> legendItems) {
            if (getChildren().size() != legendItems.size()) {
                return true;
            }
            for (int i = 0, size = getChildren().size(); i < size; i++) {
                Label oldItem = getChildLabel(i);
                Label newItem = legendItems.get(i);

                if (legendLabelsDiffer(oldItem, newItem)
                        || !oldItem.getStylesheets().equals(Utils.getChart(newItem).getStylesheets())) {
                    return true;
                }
            }
            return false;
        }

        private Label getChildLabel(int index) {
            return ((LegendLabelPane) getChildren().get(index)).getLabel();
        }

        private static boolean legendLabelsDiffer(Label label1, Label label2) {
            if (!Objects.equals(label1.getText(), label2.getText())) {
                return true;
            }
            if (label1.getGraphic() == null ^ label2.getGraphic() == null) {
                return true;
            }
            if (label1.getGraphic() != null) {
                Node graphic1 = label1.getGraphic();
                Node graphic2 = label2.getGraphic();

                if (!graphic1.getClass().equals(graphic2.getClass())
                        || !graphic1.getStyleClass().equals(graphic2.getStyleClass())) {
                    return true;
                }
            }
            return false;
        }

        private static Node clone(Label src) {
            Label label = new Label(src.getText());
            label.getStyleClass().setAll(src.getStyleClass());
            label.setAlignment(Pos.CENTER_LEFT);
            label.setContentDisplay(ContentDisplay.LEFT);
            if (src.getGraphic() != null && src.getGraphic().getClass().equals(Region.class)) {
                Region symbol = new Region();
                symbol.getStyleClass().setAll(src.getGraphic().getStyleClass());
                label.setGraphic(symbol);
            }
            // Copy styles from the source chart so that the legend symbol style corresponds to the plot style
            label.getStylesheets().setAll(Utils.getChart(src).getStylesheets());

            return new LegendLabelPane(label, Utils.getChart(src).getStyleClass());
        }

        /**
         * Workaround for problem with CSS selectors. CSS properties configured for chart-line-symbol or
         * chart-legend-item using chart selector e.g. .my-chart .chart-line-symbol { ... } would not work as the legend
         * item is not placed inside original chart but in the XYChartPane (since we clone the legend item). To
         * "emulate" that the legend label is placed in the original chart (and therefore for the CSS selectors to work)
         * - we put the label inside a pane that has style class of the source chart. Not very happy with this hack but
         * don't know how to solve it else.
         */
        private static class LegendLabelPane extends StylelessBorderPane {

            public LegendLabelPane(Label child, Collection<String> chartStyleClass) {
                // Emulates chart-legend
                StylelessBorderPane chartLegendPane = new StylelessBorderPane();
                chartLegendPane.setCenter(child);
                chartLegendPane.getStyleClass().setAll(CHART_LEGEND_CSS);
                setCenter(chartLegendPane);
                // This emulates the source chart
                getStyleClass().setAll(chartStyleClass);
            }

            Label getLabel() {
                return (Label) ((BorderPane) getCenter()).getCenter();
            }
        }

        private static class StylelessBorderPane extends BorderPane {
            @Override
            public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
                // We don't want styles configured for the chart-legend or chart to be applied on this pane
                return Collections.emptyList();
            }
        }
    }

    // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------

    private static class StyleableProperties {
        private static final CssMetaData<XYChartPane<?, ?>, Side> TITLE_SIDE = new CssMetaData<XYChartPane<?, ?>, Side>(
                "-fx-title-side", new EnumConverter<>(Side.class), Side.TOP) {

            @Override
            public boolean isSettable(XYChartPane<?, ?> chartPane) {
                return chartPane.titleSide == null || !chartPane.titleSide.isBound();
            }

            @Override
            public StyleableProperty<Side> getStyleableProperty(XYChartPane<?, ?> chartPane) {
                return (StyleableProperty<Side>) (WritableValue<Side>) chartPane.titleSideProperty();
            }
        };

        private static final CssMetaData<XYChartPane<?, ?>, Side> LEGEND_SIDE = new CssMetaData<XYChartPane<?, ?>, Side>(
                "-fx-legend-side", new EnumConverter<>(Side.class), Side.BOTTOM) {

            @Override
            public boolean isSettable(XYChartPane<?, ?> chartPane) {
                return chartPane.legendSide == null || !chartPane.legendSide.isBound();
            }

            @Override
            public StyleableProperty<Side> getStyleableProperty(XYChartPane<?, ?> chartPane) {
                return (StyleableProperty<Side>) (WritableValue<Side>) chartPane.legendSideProperty();
            }
        };

        private static final CssMetaData<XYChartPane<?, ?>, Boolean> LEGEND_VISIBLE = new CssMetaData<XYChartPane<?, ?>, Boolean>(
                "-fx-legend-visible", BooleanConverter.getInstance(), Boolean.TRUE) {

            @Override
            public boolean isSettable(XYChartPane<?, ?> chartPane) {
                return chartPane.legendVisible == null || !chartPane.legendVisible.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override
            public StyleableProperty<Boolean> getStyleableProperty(XYChartPane<?, ?> chartPane) {
                return (StyleableProperty<Boolean>) chartPane.legendVisibleProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Region.getClassCssMetaData());
            styleables.add(TITLE_SIDE);
            styleables.add(LEGEND_VISIBLE);
            styleables.add(LEGEND_SIDE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}
