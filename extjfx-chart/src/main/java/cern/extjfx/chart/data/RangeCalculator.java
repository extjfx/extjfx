/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

import javafx.scene.chart.ValueAxis;

/**
 * Responsible for calculating a proper range when reducing data in {@link DataReducingObservableList}. To avoid
 * unnecessary calculations, the range is supposed to be calculated in {@link #updateRange(ChartData)} method, and later
 * retrieved by {@link #getRange()}.
 * 
 * @author mhrabia
 * @param <X> - X coordinate type
 * @param <Y> - Y coordinate type
 */
interface RangeCalculator<X extends Number, Y extends Number> {

    /**
     * Gets currently valid {@link Range}
     * 
     * @return currently valid {@link Range}
     */
    Range<Double> getRange();

    /**
     * Updates the internal {@link Range} based on provided {@link ChartData}. This method should be called only when
     * the data has changed, so the range will be calculated only once, avoiding unnecessary recalculations.
     * 
     * @param newData - new data to calculate {@link Range} from
     */
    void updateRange(ChartData<X, Y> newData);
}

/**
 * Used for calculating {@link Range} when auto-ranging is active. Takes the first and the last X values of provided
 * data {@link ChartData} and treats them as the {@link Range#getLowerBound() and Range#getUpperBound() respectively.
 * 
 * @author mhrabia
 * @param <X> - X coordinate type
 * @param <Y> - Y coordinate type
 */
class AutoRangingCalculator<X extends Number, Y extends Number> implements RangeCalculator<X, Y> {
    private double xStart;
    private double xEnd;

    @Override
    public Range<Double> getRange() {
        return new Range<>(xStart, xEnd);
    }

    @Override
    public void updateRange(ChartData<X, Y> data) {
        if (data != null && data.size() > 0) {
            xStart = getFirstXValue(data);
            xEnd = getLastYValue(data);
        } else {
            xStart = 0;
            xEnd = 0;
        }
    }

    private double getFirstXValue(ChartData<X, Y> data) {
        return data.get(0).getXValue().doubleValue();
    }

    private double getLastYValue(ChartData<X, Y> data) {
        return data.get(data.size() - 1).getXValue().doubleValue();
    }
}

/**
 * Used for calculating {@link Range} when auto-ranging is not active. It treats {@link ValueAxis#getLowerBound()} and
 * {@link ValueAxis#getUpperBound()} as the range, so the calculated {@link Range} will always be independent of
 * {@link ChartData}.
 * 
 * @author mhrabia
 * @param <X> - X coordinate type
 * @param <Y> - Y coordinate type
 */
class AxisBoundRangeCalculator<X extends Number, Y extends Number> implements RangeCalculator<X, Y> {
    private final ValueAxis<X> xAxis;

    public AxisBoundRangeCalculator(ValueAxis<X> xAxis) {
        this.xAxis = xAxis;
    }

    @Override
    public Range<Double> getRange() {
        return new Range<>(xAxis.getLowerBound(), xAxis.getUpperBound());
    }

    @Override
    public void updateRange(ChartData<X, Y> newData) {
        // nothing to do since we only care about xAxis bounds
    }
}
