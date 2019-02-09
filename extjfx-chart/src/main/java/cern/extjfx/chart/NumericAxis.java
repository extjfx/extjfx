/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sun.javafx.css.converters.SizeConverter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableDoubleProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;

/**
 * A axis class that plots a range of numbers with major tick marks every "tickUnit". You can use any Number type with
 * this axis, Long, Double, BigDecimal etc.
 * <p>
 * Compared to the {@link NumberAxis}, this one has a few additional features:
 * <ul>
 * <li>Re-calculates tick unit also when the {@link #autoRangingProperty() auto-ranging} is off</li>
 * <li>Supports configuration of {@link #autoRangePaddingProperty() auto-range padding}</li>
 * <li>Supports configuration of {@link #autoRangeRoundingProperty() auto-range rounding}</li>
 * <li>Supports custom {@link #tickUnitSupplierProperty() tick unit suppliers}</li>
 * </ul>
 */
public final class NumericAxis extends AbstractNumericAxis {

    private static final int TICK_MARK_GAP = 6;
    private static final double NEXT_TICK_UNIT_FACTOR = 1.01;
    private static final int MAX_TICK_COUNT = 20;
    private static final TickUnitSupplier DEFAULT_TICK_UNIT_SUPPLIER = new DefaultTickUnitSupplier();

    private static final int DEFAULT_RANGE_LENGTH = 2;
    private static final double DEFAULT_RANGE_PADDING = 0.1;

    /**
     * Creates an {@link #autoRangingProperty() auto-ranging} NumericAxis.
     */
    public NumericAxis() {
        //
    }

    /**
     * Creates a {@link #autoRangingProperty() non-auto-ranging} NumericAxis with the given upper bound, lower bound and
     * tick unit.
     *
     * @param lowerBound the {@link #lowerBoundProperty() lower bound} of the axis
     * @param upperBound the {@link #upperBoundProperty() upper bound} of the axis
     * @param tickUnit the tick unit, i.e. space between tick marks
     */
    public NumericAxis(double lowerBound, double upperBound, double tickUnit) {
        this(null, lowerBound, upperBound, tickUnit);
    }

    /**
     * Create a {@link #autoRangingProperty() non-auto-ranging} NumericAxis with the given upper bound, lower bound and
     * tick unit.
     *
     * @param axisLabel the axis {@link #labelProperty() label}
     * @param lowerBound the {@link #lowerBoundProperty() lower bound} of the axis
     * @param upperBound the {@link #upperBoundProperty() upper bound} of the axis
     * @param tickUnit the tick unit, i.e. space between tick marks
     */
    public NumericAxis(String axisLabel, double lowerBound, double upperBound, double tickUnit) {
        super(axisLabel, lowerBound, upperBound);
        setTickUnit(tickUnit);
    }

    private final BooleanProperty forceZeroInRange = new SimpleBooleanProperty(this, "forceZeroInRange", true) {
        @Override
        protected void invalidated() {
            if (isAutoRanging()) {
                requestAxisLayout();
                invalidateRange();
            }
        }
    };

    /**
     * When {@code true} zero is always included in the visible range. This only has effect if
     * {@link #autoRangingProperty() auto-ranging} is on.
     *
     * @return forceZeroInRange property
     */
    public BooleanProperty forceZeroInRangeProperty() {
        return forceZeroInRange;
    }

    /**
     * Returns the value of the {@link #forceZeroInRangeProperty()}.
     *
     * @return value of the forceZeroInRange property
     */
    public boolean isForceZeroInRange() {
        return forceZeroInRange.getValue();
    }

    /**
     * Sets the value of the {@link #forceZeroInRangeProperty()}.
     *
     * @param value if {@code true}, zero is always included in the visible range
     */
    public void setForceZeroInRange(boolean value) {
        forceZeroInRange.setValue(value);
    }

    private final DoubleProperty autoRangePadding = new SimpleDoubleProperty(0);

    /**
     * Fraction of the range to be applied as padding on both sides of the axis range. E.g. if set to 0.1 (10%) on axis
     * with data range [10, 20], the new automatically calculated range will be [9, 21].
     *
     * @return autoRangePadding property
     */
    public DoubleProperty autoRangePaddingProperty() {
        return autoRangePadding;
    }

    /**
     * Returns the value of the {@link #autoRangePaddingProperty()}.
     *
     * @return the auto range padding
     */
    public double getAutoRangePadding() {
        return autoRangePaddingProperty().get();
    }

    /**
     * Sets the value of the {@link #autoRangePaddingProperty()}
     *
     * @param padding padding factor
     */
    public void setAutoRangePadding(double padding) {
        autoRangePaddingProperty().set(padding);
    }

    private final SimpleStyleableDoubleProperty tickUnit = new SimpleStyleableDoubleProperty(
            StyleableProperties.TICK_UNIT, this, "tickUnit", 5d) {
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }
    };

    /**
     * The value between each major tick mark in data units. This is automatically set if we are auto-ranging.
     *
     * @return tickUnit property
     */
    public DoubleProperty tickUnitProperty() {
        return tickUnit;
    }

    /**
     * Returns tick unit value expressed in data units.
     *
     * @return major tick unit value
     */
    public double getTickUnit() {
        return tickUnitProperty().get();
    }

    /**
     * Sets the value of the {@link #tickUnitProperty()}.
     *
     * @param unit major tick unit
     */
    public void setTickUnit(double unit) {
        tickUnitProperty().set(unit);
    }

    private final ObjectProperty<TickUnitSupplier> tickUnitSupplier = new SimpleObjectProperty<>(this,
            "tickUnitSupplier", DEFAULT_TICK_UNIT_SUPPLIER);

    /**
     * Strategy to compute major tick unit when auto-range is on or when axis bounds change. By default initialized to
     * {@link DefaultTickUnitSupplier}.
     * <p>
     * See {@link TickUnitSupplier} for more information about the expected behavior of the strategy.
     * </p>
     *
     * @return tickUnitSupplier property
     */
    public ObjectProperty<TickUnitSupplier> tickUnitSupplierProperty() {
        return tickUnitSupplier;
    }

    /**
     * Returns the value of the {@link #tickUnitSupplierProperty()}.
     *
     * @return the TickUnitSupplier
     */
    public TickUnitSupplier getTickUnitSupplier() {
        return tickUnitSupplierProperty().get();
    }

    /**
     * Sets the value of the {@link #tickUnitSupplierProperty()}.
     *
     * @param supplier the tick unit supplier. If {@code null}, the default one will be used
     */
    public void setTickUnitSupplier(TickUnitSupplier supplier) {
        tickUnitSupplierProperty().set(supplier);
    }

    @Override
    protected void setRange(Object range, boolean animate) {
        super.setRange(range, animate);
        setTickUnit(((NumericAxisRange) range).tickUnit);
    }

    @Override
    protected List<Number> calculateTickValues(double axisLength, Object range) {
        NumericAxisRange rangeImpl = (NumericAxisRange) range;
        if (rangeImpl.lowerBound == rangeImpl.upperBound || rangeImpl.tickUnit <= 0) {
            return Arrays.asList(rangeImpl.lowerBound);
        }
        List<Number> tickValues = new ArrayList<>();
        double firstTick = computeFistMajorTick(rangeImpl.lowerBound, rangeImpl.tickUnit);
        for (double major = firstTick; major <= rangeImpl.upperBound; major += rangeImpl.tickUnit) {
            tickValues.add(major);
        }
        return tickValues;
    }

    private static double computeFistMajorTick(double lowerBound, double tickUnit) {
        return Math.ceil(lowerBound / tickUnit) * tickUnit;
    }

    @Override
    protected List<Number> calculateMinorTickMarks() {
        if (getMinorTickCount() == 0 || getTickUnit() == 0) {
            return Collections.emptyList();
        }

        List<Number> minorTickMarks = new ArrayList<>();
        final double lowerBound = getLowerBound();
        final double upperBound = getUpperBound();
        final double majorUnit = getTickUnit();

        final double firstMajorTick = computeFistMajorTick(lowerBound, majorUnit);
        final double minorUnit = majorUnit / getMinorTickCount();

        for (double majorTick = firstMajorTick - majorUnit; majorTick < upperBound; majorTick += majorUnit) {
            double nextMajorTick = majorTick + majorUnit;
            for (double minorTick = majorTick + minorUnit; minorTick < nextMajorTick; minorTick += minorUnit) {
                if (minorTick >= lowerBound && minorTick <= upperBound) {
                    minorTickMarks.add(minorTick);
                }
            }
        }
        return minorTickMarks;
    }

    @Override
    protected Range getRange() {
        return new NumericAxisRange(super.getRange(), getTickUnit());
    }

    @Override
    protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {
        double min = minValue > 0 && isForceZeroInRange() ? 0 : minValue;
        double max = maxValue < 0 && isForceZeroInRange() ? 0 : maxValue;
        double padding = getEffectiveRange(min, max) * getAutoRangePadding();
        double paddedMin = clampBoundToZero(min - padding, min);
        double paddedMax = clampBoundToZero(max + padding, max);

        return computeRange(paddedMin, paddedMax, length, labelSize);
    }

    private static double getEffectiveRange(double min, double max) {
        double effectiveRange = max - min;
        if (effectiveRange == 0) {
            effectiveRange = (min == 0) ? DEFAULT_RANGE_LENGTH : Math.abs(min);
        }
        return effectiveRange;
    }

    /**
     * If padding pushed the bound above or below zero - stick it to zero.
     */
    private static double clampBoundToZero(double paddedBound, double bound) {
        if ((paddedBound < 0 && bound >= 0) || (paddedBound > 0 && bound <= 0)) {
            return 0;
        }
        return paddedBound;
    }

    @Override
    protected Range computeRange(double min, double max, double axisLength, double labelSize) {
        double minValue = min;
        double maxValue = max;
        if (max - min == 0) {
            double padding = getAutoRangePadding() == 0 ? DEFAULT_RANGE_PADDING : getAutoRangePadding();
            double paddedRange = getEffectiveRange(min, max) * padding;
            minValue = min - paddedRange / 2;
            maxValue = max + paddedRange / 2;
        }
        return computeRangeImpl(minValue, maxValue, axisLength, labelSize);
    }

    private NumericAxisRange computeRangeImpl(double min, double max, double axisLength, double labelSize) {
        final int numOfFittingLabels = (int) Math.floor(axisLength / labelSize);
        final int numOfTickMarks = Math.max(Math.min(numOfFittingLabels, MAX_TICK_COUNT), 2);

        double rawTickUnit = (max - min) / numOfTickMarks;
        double prevTickUnitRounded;
        double tickUnitRounded = Double.MIN_VALUE;
        double minRounded = min;
        double maxRounded = max;
        int ticksCount;
        double reqLength;
        String tickNumFormat = "#.0#";

        do {
            if (Double.isNaN(rawTickUnit)) {
                throw new IllegalArgumentException( "Can't calculate axis range: data contains NaN value");
            }
            // Here we ignore the tickUnit property, so even if the tick unit was specified and the auto-range is off
            // we don't use it. When narrowing the range (e.g. zoom-in) - this is usually ok, but if one wants
            // explicitly change bounds while preserving the specified tickUnit, this won't work. Perhaps the usage of
            // tickUnit should be independent of the auto-range so we should introduce autoTickUnit. The other option is
            // to provide custom TickUnitSupplier that always returns the same tick unit.
            prevTickUnitRounded = tickUnitRounded;
            tickUnitRounded = computeTickUnit(rawTickUnit);
            if (tickUnitRounded <= prevTickUnitRounded) {
                break;
            }
            tickNumFormat = computeTickNumFormat(tickUnitRounded);

            double firstMajorTick;
            if (isAutoRanging() && isAutoRangeRounding()) {
                minRounded = Math.floor(min / tickUnitRounded) * tickUnitRounded;
                maxRounded = Math.ceil(max / tickUnitRounded) * tickUnitRounded;
                firstMajorTick = minRounded;
            } else {
                firstMajorTick = Math.ceil(min / tickUnitRounded) * tickUnitRounded;
            }

            ticksCount = 0;
            double maxReqTickGap = 0;
            double halfOfLastTickSize = 0;
            for (double major = firstMajorTick; major <= maxRounded; major += tickUnitRounded, ticksCount++) {
                double tickMarkSize = measureTickMarkLength(major, tickNumFormat);
                if (major == firstMajorTick) {
                    halfOfLastTickSize = tickMarkSize / 2;
                } else {
                    maxReqTickGap = Math.max(maxReqTickGap, halfOfLastTickSize + TICK_MARK_GAP + (tickMarkSize / 2));
                }
            }
            reqLength = (ticksCount - 1) * maxReqTickGap;
            rawTickUnit = tickUnitRounded * NEXT_TICK_UNIT_FACTOR;
        } while (numOfTickMarks > 2 && (reqLength > axisLength || ticksCount > MAX_TICK_COUNT));

        double newScale = calculateNewScale(axisLength, minRounded, maxRounded);
        return new NumericAxisRange(minRounded, maxRounded, newScale, tickNumFormat, tickUnitRounded);
    }

    private double measureTickMarkLength(double major, String formatter) {
        Dimension2D size = super.measureTickMarkSizeWithFormat(major, formatter);
        return getSide().isVertical() ? size.getHeight() : size.getWidth();
    }

    private double computeTickUnit(double rawTickUnit) {
        TickUnitSupplier unitSupplier = getTickUnitSupplier();
        if (unitSupplier == null) {
            unitSupplier = DEFAULT_TICK_UNIT_SUPPLIER;
        }
        double majorUnit = unitSupplier.computeTickUnit(rawTickUnit);
        if (majorUnit <= 0) {
            throw new IllegalArgumentException("The " + unitSupplier.getClass().getName()
                    + " computed illegal unit value [" + majorUnit + "] for argument " + rawTickUnit);
        }
        return majorUnit;
    }

    private static String computeTickNumFormat(double tickUnit) {
        int log10 = (int) Math.floor(Math.log10(tickUnit));
        boolean unitHasFraction = Math.rint(tickUnit) != tickUnit;
        if (log10 >= 1 && !unitHasFraction) {
            return "#,##0";
        }
        int fractDigitsCount = unitHasFraction ? Math.abs(log10) + 1 : Math.abs(log10);
        StringBuilder format = new StringBuilder("0");
        if (fractDigitsCount > 0) {
            format.append('.');
        }
        for (int i = 0; i < fractDigitsCount; i++) {
            format.append('0');
        }
        return format.toString();
    }
    // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------

    private static class StyleableProperties {
        private static final CssMetaData<NumericAxis, Number> TICK_UNIT = new CssMetaData<NumericAxis, Number>(
                "-fx-tick-unit", SizeConverter.getInstance(), 5.0) {

            @Override
            public boolean isSettable(NumericAxis axis) {
                return axis.tickUnit == null || !axis.tickUnit.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override
            public StyleableProperty<Number> getStyleableProperty(NumericAxis axis) {
                return (StyleableProperty<Number>) axis.tickUnitProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ValueAxis.getClassCssMetaData());
            styleables.add(TICK_UNIT);
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

    private static class NumericAxisRange extends Range {
        final double tickUnit;

        NumericAxisRange(Range range, double tickUnit) {
            this(range.lowerBound, range.upperBound, range.scale, range.tickFormat, tickUnit);
        }

        NumericAxisRange(double lowerBound, double upperBound, double scale, String tickFormat, double tickUnit) {
            super(lowerBound, upperBound, scale, tickFormat);
            this.tickUnit = tickUnit;
        }
    }
}