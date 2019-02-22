/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;

/**
 * An implementation of an {@link ObservableList} that contains a reduced number of points with respect to the wrapped
 * source list of data. The data reduction is performed by the {@link #dataReducerProperty() data reducer strategy} on
 * data that is within the bounds of associated X axis. By default {@link DefaultDataReducer} is used, but you can
 * provide other implementation of {@link DataReducer} to this property to use it instead.
 * <p>
 * The primary usage of this collection is an efficient display of series containing a large number of data points,
 * allowing to see more "details" when the visible data range is narrowed e.g. by performing a zoom-in.
 * <p>
 * This collection is unmodifiable - all changes to the data should be performed on the wrapped source data.
 *
 * @param <X> X coordinate type
 * @param <Y> Y coordinate type
 * @author Andreas Schaller
 * @author Grzegorz Kruk
 */
public class DataReducingObservableList<X extends Number, Y extends Number>
        extends ModifiableObservableListBase<Data<X, Y>> {

    /**
     * The default value of {@link #maxPointsCountProperty()}.
     */
    public static final int DEFAULT_MAX_POINTS_COUNT = 200;
    private static final int MIN_POINTS_COUNT = 2;

    private RangeCalculator<X, Y> rangeCalculator;
    private final ValueAxis<X> xAxis;
    private List<Data<X, Y>> reducedData = Collections.emptyList();
    private final AxisRangeChangeListener axisRangeChangeListener = new AxisRangeChangeListener();

    // used for testing
    boolean immediateReduce = false;

    private InvalidationListener chartDataListener = obs -> {
        if (immediateReduce) {
            reduce();
        } else {
            Platform.runLater(this::reduce);
        }
    };

    /**
     * Creates a new instance of DataReducingObservableList without any data. The data should be set using
     * {@link #setData(ChartData)}.
     *
     * @param xAxis X coordinates axis of the chart displaying the data
     */
    public DataReducingObservableList(ValueAxis<X> xAxis) {
        this(xAxis, (ChartData<X, Y>) null);
    }

    public DataReducingObservableList(ValueAxis<X> xAxis, ObservableList<Data<X, Y>> sourceData) {
        this(xAxis, new ListData<>(sourceData));
    }

    /**
     * Creates a new instance of DataReducingObservableList.
     *
     * @param xAxis X coordinates axis of the chart displaying the data
     * @param chartData data to be reduced
     */
    public DataReducingObservableList(ValueAxis<X> xAxis, ChartData<X, Y> chartData) {
        this.xAxis = requireNonNull(xAxis, "X ValueAxis must be specified");

        xAxis.lowerBoundProperty().addListener(axisRangeChangeListener);
        xAxis.upperBoundProperty().addListener(axisRangeChangeListener);
        xAxis.autoRangingProperty().addListener(new AutoRangingChangeListener());

        data.addListener((obs, oldData, newData) -> {
            if (oldData != null) {
                oldData.removeListener(chartDataListener);
            }
            if (newData != null) {
                newData.addListener(chartDataListener);
                rangeCalculator.updateRange(newData);
                reduce();
            }
        });

        determineRangeCalculator(xAxis.isAutoRanging());
        setData(chartData);
    }

    private void determineRangeCalculator(Boolean autoRangeActive) {
        if (autoRangeActive) {
            rangeCalculator = new AutoRangingCalculator<X, Y>();
        } else {
            rangeCalculator = new AxisBoundRangeCalculator<X, Y>(this.xAxis);
        }
    }

    private final ObjectProperty<ChartData<X, Y>> data = new SimpleObjectProperty<>(this, "data");

    /**
     * Array data to be reduced.
     * 
     * @return data
     */
    public final ObjectProperty<ChartData<X, Y>> dataProperty() {
        return data;
    }

    /**
     * Sets the value of the {@link #dataProperty()}.
     *
     * @param value data to be set.
     */
    public final void setData(ChartData<X, Y> value) {
        dataProperty().set(value);
    }

    /**
     * Returns the value of the {@link #dataProperty()}.
     *
     * @return the data
     */
    public final ChartData<X, Y> getData() {
        return dataProperty().get();
    }

    private final ObjectProperty<DataReducer<X, Y>> dataReducer = new SimpleObjectProperty<DataReducer<X, Y>>(this,
            "dataReducer", new DefaultDataReducer<>()) {
        @Override
        protected void invalidated() {
            reduce();
        }
    };

    /**
     * Property holding the data reduction strategy. By default initialized to {@link DataReducer}.
     *
     * @return data reducer property
     */
    public final ObjectProperty<DataReducer<X, Y>> dataReducerProperty() {
        return dataReducer;
    }

    /**
     * Returns the data reduction strategy used by this list.
     *
     * @return the data reduction strategy
     */
    public final DataReducer<X, Y> getDataReducer() {
        return dataReducerProperty().get();
    }

    /**
     * Sets the data reduction strategy to be used.
     *
     * @param reducer data reduction strategy. If {@code null}, no reduction will be applied on the source data.
     *            {@link #maxPointsCountProperty()} property is ignored in such case.
     */
    public final void setDataReducer(DataReducer<X, Y> reducer) {
        dataReducerProperty().set(reducer);
    }

    private final IntegerProperty maxPointsCount = new SimpleIntegerProperty(this, "maxPointsCount",
            DEFAULT_MAX_POINTS_COUNT) {
        @Override
        protected void invalidated() {
            if (get() < MIN_POINTS_COUNT) {
                throw new IllegalArgumentException("pointsCount must be grater than 1");
            }
            reduce();
        }
    };

    /**
     * The maximum number of points that the list should contain after the reduction. By default initialized to
     * {@value #DEFAULT_MAX_POINTS_COUNT}.
     *
     * @return property holding the maximum number of points
     */
    public final IntegerProperty maxPointsCountProperty() {
        return maxPointsCount;
    }

    /**
     * Returns value of the {@link #maxPointsCountProperty()}.
     *
     * @return the max number of points after data reduction
     */
    public final int getMaxPointsCount() {
        return maxPointsCountProperty().get();
    }

    /**
     * Sets the value of {@link #maxPointsCountProperty()}.
     *
     * @param value the maximum number of points (grater than 1) that the collection can contain.
     */
    public final void setMaxPointsCount(int value) {
        maxPointsCountProperty().set(value);
    }

    @Override
    protected void doAdd(int index, Data<X, Y> element) {
        throw new UnsupportedOperationException("Add operation is not allowed");
    }

    @Override
    protected Data<X, Y> doSet(int index, Data<X, Y> element) {
        throw new UnsupportedOperationException("Set operation is not allowed");
    }

    @Override
    protected Data<X, Y> doRemove(int index) {
        throw new UnsupportedOperationException("Remove operation is not allowed");
    }

    @Override
    public Data<X, Y> get(int index) {
        return reducedData.get(index);
    }

    @Override
    public int size() {
        return reducedData.size();
    }

    private void reduce() {
        beginChange();
        nextRemove(0, reducedData);
        reducedData = recreateData(reduceData());
        nextAdd(0, reducedData.size());
        endChange();
    }

    private List<Data<X, Y>> reduceData() {
        if (getDataSize() == 0) {
            return Collections.emptyList();
        }
        Range<Double> dataRange = rangeCalculator.getRange();
        return getDataReducer().reduce(getData(), dataRange, getMaxPointsCount());
    }

    private int getDataSize() {
        return getData() == null ? 0 : getData().size();
    }

    /**
     * Re-create data objects due to bug a in JavaFX charts. If not done, the points are not properly rendered. To be
     * investigated further.
     */
    private static <X, Y> List<Data<X, Y>> recreateData(List<Data<X, Y>> dataList) {
        List<Data<X, Y>> result = new ArrayList<>(dataList.size());
        for (Data<X, Y> d : dataList) {
            Data<X, Y> newData = new Data<>(d.getXValue(), d.getYValue(), d.getExtraValue());
            newData.setNode(d.getNode());
            result.add(newData);
        }
        return result;
    }

    /**
     * Listens to changes in lower and upper bound of the X axis and runs the reduce() method after EVENT_DELAY_MILLIS
     * to schedule only one reduction (instead of two) in case both lower and upper bound are changed (and therefore two
     * events are fired).
     */
    private class AxisRangeChangeListener implements ChangeListener<Number> {
        private final Timer timer = new Timer(true);
        private TimerTask task = null;
        private static final int EVENT_DELAY_MILLIS = 50;

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (immediateReduce) {
                reduce();
                return;
            }
            if (task != null) {
                task.cancel();
            }
            task = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(DataReducingObservableList.this::reduce);
                }
            };
            timer.schedule(task, EVENT_DELAY_MILLIS);
        }
    }

    /**
     * Listens to {@link ValueAxis#autoRangingProperty()} and decides whether it should use
     * {@link AutoRangingCalculator} if auto-range is active or {@link AxisBoundRangeCalculator} if it´s not.
     */
    final class AutoRangingChangeListener implements ChangeListener<Boolean> {
        @Override
        public void changed(ObservableValue<? extends Boolean> a, Boolean b, Boolean autoRangeActive) {
            determineRangeCalculator(autoRangeActive);
        }
    }
}