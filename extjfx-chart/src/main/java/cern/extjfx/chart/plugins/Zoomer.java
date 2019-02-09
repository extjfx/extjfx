/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.plugins;

import static cern.extjfx.chart.AxisMode.XY;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import cern.extjfx.chart.Axes;
import cern.extjfx.chart.AxisMode;
import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.XYChartPlugin;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Zoom capabilities along X, Y or both axis. For every zoom-in operation the current X and Y range is remembered and
 * restored upon following zoom-out operation.
 * <ul>
 * <li>zoom-in - triggered on {@link MouseEvent#MOUSE_PRESSED MOUSE_PRESSED} event that is accepted by
 * {@link #getZoomInMouseFilter() zoom-in filter}. It shows a zooming rectangle determining the zoom window once mouse
 * button is released.</li>
 * <li>zoom-out - triggered on {@link MouseEvent#MOUSE_CLICKED MOUSE_CLICKED} event that is accepted by
 * {@link #getZoomOutMouseFilter() zoom-out filter}. It restores the previous ranges on both axis.</li>
 * <li>zoom-origin - triggered on {@link MouseEvent#MOUSE_CLICKED MOUSE_CLICKED} event that is accepted by
 * {@link #getZoomOriginMouseFilter() zoom-origin filter}. It restores the initial ranges on both axis as it was at the
 * moment of the first zoom-in operation.</li>
 * </ul>
 * {@code Zoomer} works properly only if both X and Y axis are instances of {@link NumericAxis}.
 * <p>
 * CSS class name of the zoom rectangle: {@value #STYLE_CLASS_ZOOM_RECT}.
 * </p>
 */
public class Zoomer extends XYChartPlugin<Number, Number> {

    /**
     * Name of the CCS class of the zoom rectangle.
     */
    public static final String STYLE_CLASS_ZOOM_RECT = "chart-zoom-rect";
    private static final int ZOOM_RECT_MIN_SIZE = 5;
    private static final Duration DEFAULT_ZOOM_DURATION = Duration.millis(500);

    /**
     * Default zoom-in mouse filter passing on left mouse button (only).
     */
    public static final Predicate<MouseEvent> DEFAULT_ZOOM_IN_MOUSE_FILTER = event -> MouseEvents
            .isOnlyPrimaryButtonDown(event) && MouseEvents.modifierKeysUp(event);

    /**
     * Default zoom-out mouse filter passing on right mouse button (only).
     */
    public static final Predicate<MouseEvent> DEFAULT_ZOOM_OUT_MOUSE_FILTER = event -> MouseEvents
            .isOnlySecondaryButtonDown(event) && MouseEvents.modifierKeysUp(event);

    /**
     * Default zoom-origin mouse filter passing on right mouse button with {@link MouseEvent#isControlDown() control key
     * down}.
     */
    public static final Predicate<MouseEvent> DEFAULT_ZOOM_ORIGIN_MOUSE_FILTER = event -> MouseEvents
            .isOnlySecondaryButtonDown(event) && MouseEvents.isOnlyCtrlModifierDown(event);

    private Predicate<MouseEvent> zoomInMouseFilter = DEFAULT_ZOOM_IN_MOUSE_FILTER;
    private Predicate<MouseEvent> zoomOutMouseFilter = DEFAULT_ZOOM_OUT_MOUSE_FILTER;
    private Predicate<MouseEvent> zoomOriginMouseFilter = DEFAULT_ZOOM_ORIGIN_MOUSE_FILTER;

    private final Rectangle zoomRectangle = new Rectangle();
    private Point2D zoomStartPoint = null;
    private Point2D zoomEndPoint = null;
    private final Map<XYChart<Number, Number>, Deque<Rectangle2D>> zoomStacks = new HashMap<>();

    /**
     * Creates a new instance of Zoomer with animation disabled and with {@link #axisModeProperty() zoomMode}
     * initialized to {@link AxisMode#XY}.
     */
    public Zoomer() {
        this(AxisMode.XY);
    }

    /**
     * Creates a new instance of Zoomer with animation disabled.
     *
     * @param zoomMode initial value of {@link #axisModeProperty() zoomMode} property
     */
    public Zoomer(AxisMode zoomMode) {
        this(zoomMode, false);
    }

    /**
     * Creates a new instance of Zoomer with {@link #axisModeProperty() zoomMode} initialized to {@link AxisMode#XY}.
     *
     * @param animated initial value of {@link #animatedProperty() animated} property
     */
    public Zoomer(boolean animated) {
        this(AxisMode.XY, animated);
    }

    /**
     * Creates a new instance of Zoomer.
     *
     * @param zoomMode initial value of {@link #axisModeProperty() axisMode} property
     * @param animated initial value of {@link #animatedProperty() animated} property
     */
    public Zoomer(AxisMode zoomMode, boolean animated) {
        setAxisMode(zoomMode);
        setAnimated(animated);
        setDragCursor(Cursor.CROSSHAIR);

        zoomRectangle.setManaged(false);
        zoomRectangle.getStyleClass().add(STYLE_CLASS_ZOOM_RECT);
        getChartChildren().add(zoomRectangle);
        registerMouseHandlers();
    }

    private void registerMouseHandlers() {
        registerMouseEventHandler(MouseEvent.MOUSE_PRESSED, zoomInStartHandler);
        registerMouseEventHandler(MouseEvent.MOUSE_DRAGGED, zoomInDragHandler);
        registerMouseEventHandler(MouseEvent.MOUSE_RELEASED, zoomInEndHandler);
        registerMouseEventHandler(MouseEvent.MOUSE_CLICKED, zoomOutHandler);
        registerMouseEventHandler(MouseEvent.MOUSE_CLICKED, zoomOriginHandler);
    }

    /**
     * Returns zoom-in mouse event filter.
     *
     * @return zoom-in mouse event filter
     * @see #setZoomInMouseFilter(Predicate)
     */
    public Predicate<MouseEvent> getZoomInMouseFilter() {
        return zoomInMouseFilter;
    }

    /**
     * Sets filter on {@link MouseEvent#DRAG_DETECTED DRAG_DETECTED} events that should start zoom-in operation.
     *
     * @param zoomInMouseFilter the filter to accept zoom-in mouse event. If {@code null} then any DRAG_DETECTED event
     *            will start zoom-in operation. By default it's set to {@link #DEFAULT_ZOOM_IN_MOUSE_FILTER}.
     * @see #getZoomInMouseFilter()
     */
    public void setZoomInMouseFilter(Predicate<MouseEvent> zoomInMouseFilter) {
        this.zoomInMouseFilter = zoomInMouseFilter;
    }

    /**
     * Returns zoom-out mouse filter.
     *
     * @return zoom-out mouse filter
     * @see #setZoomOutMouseFilter(Predicate)
     */
    public Predicate<MouseEvent> getZoomOutMouseFilter() {
        return zoomOutMouseFilter;
    }

    /**
     * Sets filter on {@link MouseEvent#MOUSE_CLICKED MOUSE_CLICKED} events that should trigger zoom-out operation.
     *
     * @param zoomOutMouseFilter the filter to accept zoom-out mouse event. If {@code null} then any MOUSE_CLICKED event
     *            will start zoom-out operation. By default it's set to {@link #DEFAULT_ZOOM_OUT_MOUSE_FILTER}.
     * @see #getZoomOutMouseFilter()
     */
    public void setZoomOutMouseFilter(Predicate<MouseEvent> zoomOutMouseFilter) {
        this.zoomOutMouseFilter = zoomOutMouseFilter;
    }

    /**
     * Returns zoom-origin mouse filter.
     *
     * @return zoom-origin mouse filter
     * @see #setZoomOriginMouseFilter(Predicate)
     */
    public Predicate<MouseEvent> getZoomOriginMouseFilter() {
        return zoomOriginMouseFilter;
    }

    /**
     * Sets filter on {@link MouseEvent#MOUSE_CLICKED MOUSE_CLICKED} events that should trigger zoom-origin operation.
     *
     * @param zoomOriginMouseFilter the filter to accept zoom-origin mouse event. If {@code null} then any MOUSE_CLICKED
     *            event will start zoom-origin operation. By default it's set to
     *            {@link #DEFAULT_ZOOM_ORIGIN_MOUSE_FILTER}.
     * @see #getZoomOriginMouseFilter()
     */
    public void setZoomOriginMouseFilter(Predicate<MouseEvent> zoomOriginMouseFilter) {
        this.zoomOriginMouseFilter = zoomOriginMouseFilter;
    }

    private final ObjectProperty<AxisMode> axisMode = new SimpleObjectProperty<AxisMode>(this, "axisMode", XY) {
        @Override
        protected void invalidated() {
            Objects.requireNonNull(get(), "The " + getName() + " must not be null");
        }
    };

    /**
     * The mode defining axis along which the zoom can be performed. By default initialized to {@link AxisMode#XY}.
     *
     * @return the axis mode property
     */
    public final ObjectProperty<AxisMode> axisModeProperty() {
        return axisMode;
    }

    /**
     * Sets the value of the {@link #axisModeProperty()}.
     *
     * @param mode the mode to be used
     */
    public final void setAxisMode(AxisMode mode) {
        axisModeProperty().set(mode);
    }

    /**
     * Returns the value of the {@link #axisModeProperty()}.
     *
     * @return current mode
     */
    public final AxisMode getAxisMode() {
        return axisModeProperty().get();
    }

    private Cursor originalCursor;
    private final ObjectProperty<Cursor> dragCursor = new SimpleObjectProperty<>(this, "dragCursor");

    /**
     * Mouse cursor to be used during drag operation.
     *
     * @return the mouse cursor property
     */
    public final ObjectProperty<Cursor> dragCursorProperty() {
        return dragCursor;
    }

    /**
     * Sets value of the {@link #dragCursorProperty()}.
     *
     * @param cursor the cursor to be used by the plugin
     */
    public final void setDragCursor(Cursor cursor) {
        dragCursorProperty().set(cursor);
    }

    /**
     * Returns the value of the {@link #dragCursorProperty()}
     *
     * @return the current cursor
     */
    public final Cursor getDragCursor() {
        return dragCursorProperty().get();
    }

    private void installCursor() {
        originalCursor = getChartPane().getCursor();
        if (getDragCursor() != null) {
            getChartPane().setCursor(getDragCursor());
        }
    }

    private void uninstallCursor() {
        getChartPane().setCursor(originalCursor);
    }

    private final BooleanProperty animated = new SimpleBooleanProperty(this, "animated", false);

    /**
     * When {@code true} zooming will be animated. By default it's {@code false}.
     *
     * @return the animated property
     * @see #zoomDurationProperty()
     */
    public final BooleanProperty animatedProperty() {
        return animated;
    }

    /**
     * Sets the value of the {@link #animatedProperty()}.
     *
     * @param value if {@code true} zoom will be animated
     * @see #setZoomDuration(Duration)
     */
    public final void setAnimated(boolean value) {
        animatedProperty().set(value);
    }

    /**
     * Returns the value of the {@link #animatedProperty()}.
     *
     * @return {@code true} if zoom is animated, {@code false} otherwise
     * @see #getZoomDuration()
     */
    public final boolean isAnimated() {
        return animatedProperty().get();
    }

    private final ObjectProperty<Duration> zoomDuration = new SimpleObjectProperty<Duration>(this, "zoomDuration",
            DEFAULT_ZOOM_DURATION) {
        @Override
        protected void invalidated() {
            Objects.requireNonNull(get(), "The " + getName() + " must not be null");
        }
    };

    /**
     * Duration of the animated zoom (in and out). Used only when {@link #animatedProperty()} is set to {@code true}. By
     * default initialized to 500ms.
     *
     * @return the zoom duration property
     */
    public final ObjectProperty<Duration> zoomDurationProperty() {
        return zoomDuration;
    }

    /**
     * Sets the value of the {@link #zoomDurationProperty()}.
     *
     * @param duration duration of the zoom
     */
    public final void setZoomDuration(Duration duration) {
        zoomDurationProperty().set(duration);
    }

    /**
     * Returns the value of the {@link #zoomDurationProperty()}.
     *
     * @return the current zoom duration
     */
    public final Duration getZoomDuration() {
        return zoomDurationProperty().get();
    }

    private final EventHandler<MouseEvent> zoomInStartHandler = event -> {
        if (getZoomInMouseFilter() == null || getZoomInMouseFilter().test(event)) {
            zoomInStarted(event);
            event.consume();
        }
    };

    private final EventHandler<MouseEvent> zoomInDragHandler = event -> {
        if (zoomOngoing()) {
            zoomInDragged(event);
            event.consume();
        }
    };

    private final EventHandler<MouseEvent> zoomInEndHandler = event -> {
        if (zoomOngoing()) {
            zoomInEnded();
            event.consume();
        }
    };

    private boolean zoomOngoing() {
        return zoomStartPoint != null;
    }

    private final EventHandler<MouseEvent> zoomOutHandler = event -> {
        if (getZoomOutMouseFilter() == null || getZoomOutMouseFilter().test(event)) {
            boolean zoomOutPerformed = zoomOut();
            if (zoomOutPerformed) {
                event.consume();
            }
        }
    };

    private final EventHandler<MouseEvent> zoomOriginHandler = event -> {
        if (getZoomOriginMouseFilter() == null || getZoomOriginMouseFilter().test(event)) {
            boolean zoomOutPerformed = zoomOrigin();
            if (zoomOutPerformed) {
                event.consume();
            }
        }
    };

    private void zoomInStarted(MouseEvent event) {
        zoomStartPoint = new Point2D(event.getX(), event.getY());
        zoomRectangle.setX(zoomStartPoint.getX());
        zoomRectangle.setY(zoomStartPoint.getY());
        zoomRectangle.setWidth(0);
        zoomRectangle.setHeight(0);
        zoomRectangle.setVisible(true);
        installCursor();
    }

    private void zoomInDragged(MouseEvent event) {
        Bounds plotAreaBounds = getChartPane().getPlotAreaBounds();
        zoomEndPoint = limitToPlotArea(event, plotAreaBounds);

        double zoomRectX = plotAreaBounds.getMinX();
        double zoomRectY = plotAreaBounds.getMinY();
        double zoomRectWidth = plotAreaBounds.getWidth();
        double zoomRectHeight = plotAreaBounds.getHeight();

        if (getAxisMode().allowsX()) {
            zoomRectX = Math.min(zoomStartPoint.getX(), zoomEndPoint.getX());
            zoomRectWidth = Math.abs(zoomEndPoint.getX() - zoomStartPoint.getX());
        }
        if (getAxisMode().allowsY()) {
            zoomRectY = Math.min(zoomStartPoint.getY(), zoomEndPoint.getY());
            zoomRectHeight = Math.abs(zoomEndPoint.getY() - zoomStartPoint.getY());
        }
        zoomRectangle.setX(zoomRectX);
        zoomRectangle.setY(zoomRectY);
        zoomRectangle.setWidth(zoomRectWidth);
        zoomRectangle.setHeight(zoomRectHeight);
    }

    private Point2D limitToPlotArea(MouseEvent event, Bounds plotBounds) {
        double limitedX = Math.max(Math.min(event.getX(), plotBounds.getMaxX()), plotBounds.getMinX());
        double limitedY = Math.max(Math.min(event.getY(), plotBounds.getMaxY()), plotBounds.getMinY());
        return new Point2D(limitedX, limitedY);
    }

    private void zoomInEnded() {
        zoomRectangle.setVisible(false);
        if (zoomRectangle.getWidth() > ZOOM_RECT_MIN_SIZE && zoomRectangle.getHeight() > ZOOM_RECT_MIN_SIZE) {
            performZoomIn();
        }
        zoomStartPoint = zoomEndPoint = null;
        uninstallCursor();
    }

    private void performZoomIn() {
        clearZoomStackIfAxisAutoRangingIsEnabled();
        pushCurrentZoomWindows();
        performZoom(getZoomDataWindows());
    }

    private void pushCurrentZoomWindows() {
        for (XYChart<Number, Number> chart : getCharts()) {
            pushCurrentZoomWindow(chart);
        }
    }

    private void pushCurrentZoomWindow(XYChart<Number, Number> chart) {
        ValueAxis<Number> xAxis = Axes.toValueAxis(chart.getXAxis());
        ValueAxis<Number> yAxis = Axes.toValueAxis(chart.getYAxis());

        Deque<Rectangle2D> zoomStack = zoomStacks.get(chart);
        if (zoomStack == null) {
            zoomStack = new ArrayDeque<>();
            zoomStacks.put(chart, zoomStack);
        }

        zoomStack.addFirst(new Rectangle2D(xAxis.getLowerBound(), yAxis.getLowerBound(),
                xAxis.getUpperBound() - xAxis.getLowerBound(), yAxis.getUpperBound() - yAxis.getLowerBound()));
    }

    private Map<XYChart<Number, Number>, Rectangle2D> getZoomDataWindows() {
        Map<XYChart<Number, Number>, Rectangle2D> zoomWindows = new HashMap<>();
        for (XYChart<Number, Number> chart : getCharts()) {
            zoomWindows.put(chart, getZoomDataWindow(chart));
        }
        return zoomWindows;
    }

    private Rectangle2D getZoomDataWindow(XYChart<Number, Number> chart) {
        double minX = zoomRectangle.getX();
        double minY = zoomRectangle.getY() + zoomRectangle.getHeight();
        double maxX = zoomRectangle.getX() + zoomRectangle.getWidth();
        double maxY = zoomRectangle.getY();

        Data<Number, Number> dataMin = toDataPoint(chart.getYAxis(), getChartPane().toPlotArea(minX, minY));
        Data<Number, Number> dataMax = toDataPoint(chart.getYAxis(), getChartPane().toPlotArea(maxX, maxY));

        double dataMinX = dataMin.getXValue().doubleValue();
        double dataMinY = dataMin.getYValue().doubleValue();
        double dataMaxX = dataMax.getXValue().doubleValue();
        double dataMaxY = dataMax.getYValue().doubleValue();

        double dataRectWidth = dataMaxX - dataMinX;
        double dataRectHeight = dataMaxY - dataMinY;

        return new Rectangle2D(dataMinX, dataMinY, dataRectWidth, dataRectHeight);
    }

    private void performZoom(Map<XYChart<Number, Number>, Rectangle2D> zoomWindows) {
        for (Entry<XYChart<Number, Number>, Rectangle2D> entry : zoomWindows.entrySet()) {
            performZoom(entry.getKey(), entry.getValue());
        }
        // Change of ranges may change format of tick values and therefore Y axis width so needs to re-layout
        // to take it into account for overlay charts
        getChartPane().requestLayout();
    }

    private void performZoom(XYChart<Number, Number> chart, Rectangle2D zoomWindow) {
        ValueAxis<Number> xAxis = Axes.toValueAxis(chart.getXAxis());
        ValueAxis<Number> yAxis = Axes.toValueAxis(chart.getYAxis());

        if (getAxisMode().allowsX()) {
            xAxis.setAutoRanging(false);
        }
        if (getAxisMode().allowsY()) {
            yAxis.setAutoRanging(false);
        }

        if (isAnimated()) {
            if (!Axes.hasBoundedRange(xAxis)) {
                Timeline xZoomAnimation = new Timeline();
                xZoomAnimation.getKeyFrames().setAll(
                        new KeyFrame(Duration.ZERO, new KeyValue(xAxis.lowerBoundProperty(), xAxis.getLowerBound()),
                                new KeyValue(xAxis.upperBoundProperty(), xAxis.getUpperBound())),
                        new KeyFrame(getZoomDuration(), new KeyValue(xAxis.lowerBoundProperty(), zoomWindow.getMinX()),
                                new KeyValue(xAxis.upperBoundProperty(), zoomWindow.getMaxX())));
                xZoomAnimation.play();
            }
            if (!Axes.hasBoundedRange(yAxis)) {
                Timeline yZoomAnimation = new Timeline();
                yZoomAnimation.getKeyFrames().setAll(
                        new KeyFrame(Duration.ZERO, new KeyValue(yAxis.lowerBoundProperty(), yAxis.getLowerBound()),
                                new KeyValue(yAxis.upperBoundProperty(), yAxis.getUpperBound())),
                        new KeyFrame(getZoomDuration(), new KeyValue(yAxis.lowerBoundProperty(), zoomWindow.getMinY()),
                                new KeyValue(yAxis.upperBoundProperty(), zoomWindow.getMaxY())));
                yZoomAnimation.play();
            }
        } else {
            if (!Axes.hasBoundedRange(xAxis)) {
                xAxis.setLowerBound(zoomWindow.getMinX());
                xAxis.setUpperBound(zoomWindow.getMaxX());
            }
            if (!Axes.hasBoundedRange(yAxis)) {
                yAxis.setLowerBound(zoomWindow.getMinY());
                yAxis.setUpperBound(zoomWindow.getMaxY());
            }
        }
    }

    /**
     * Invokes zoom out action on chart if there was a zoom active.
     * 
     * @return true if there was a zoom active and we could zoom out, false otherwise.
     */
    public boolean zoomOut() {
        clearZoomStackIfAxisAutoRangingIsEnabled();
        Map<XYChart<Number, Number>, Rectangle2D> zoomWindows = getZoomWindows(Deque::pollFirst);

        if (zoomWindows.isEmpty()) {
            return false;
        }
        performZoom(zoomWindows);
        return true;
    }

    private boolean zoomOrigin() {
        clearZoomStackIfAxisAutoRangingIsEnabled();
        Map<XYChart<Number, Number>, Rectangle2D> zoomWindows = getZoomWindows(Deque::peekLast);
        if (zoomWindows.isEmpty()) {
            return false;
        }
        clear();
        performZoom(zoomWindows);
        return true;
    }

    /**
     * While performing zoom-in on all charts we disable auto-ranging on axes (depending on the axisMode) so if user has
     * enabled back the auto-ranging - he wants the chart to adapt to the data. Therefore keeping the zoom stack doesn't
     * make sense - performing zoom-out would again disable auto-ranging and put back ranges saved during the previous
     * zoom-in operation. Also if user enables auto-ranging between two zoom-in operations, the saved zoom stack becomes
     * irrelevant.
     */
    private void clearZoomStackIfAxisAutoRangingIsEnabled() {
        for (XYChart<Number, Number> chart : getCharts()) {
            if ((getAxisMode().allowsX() && chart.getXAxis().isAutoRanging())
                    || (getAxisMode().allowsY() && chart.getYAxis().isAutoRanging())) {
                clear();
                return;
            }
        }
    }

    private Map<XYChart<Number, Number>, Rectangle2D> getZoomWindows(
            Function<Deque<Rectangle2D>, Rectangle2D> extractor) {
        Map<XYChart<Number, Number>, Rectangle2D> zoomWindows = new HashMap<>();
        for (XYChart<Number, Number> chart : getCharts()) {
            Deque<Rectangle2D> deque = zoomStacks.get(chart);
            if (deque == null || deque.isEmpty()) {
                return Collections.emptyMap();
            }
            zoomWindows.put(chart, extractor.apply(deque));
        }
        return zoomWindows;
    }

    /**
     * Clears the stack of zoom windows saved during zoom-in operations.
     */
    public void clear() {
        zoomStacks.clear();
    }
}
