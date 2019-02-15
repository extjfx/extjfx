/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.scene.chart.ValueAxis;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * An abstract numeric axis that supports re-calculation of tick units even with {@link #autoRangingProperty()
 * auto-ranging} off.
 */
public abstract class AbstractNumericAxis extends ValueAxis<Number> {

    private static final int RANGE_ANIMATION_DURATION_MS = 700;

    private final Timeline animator = new Timeline();
    private final StringProperty currentTickFormat = new SimpleStringProperty(this, "currentFormatter", "0.######");
    private final DefaultFormatter defaultFormatter = new DefaultFormatter(this);

    /** Used to update scale property in ValueAxis (that is read-only) */
    private final DoubleProperty scaleBinding = new SimpleDoubleProperty(this, "scaleBinding", getScale()) {
        @Override
        protected void invalidated() {
            setScale(get());
        }
    };

    /**
     * Creates an {@link #autoRangingProperty() auto-ranging} AbstractNumericAxis.
     */
    protected AbstractNumericAxis() {
        bindToBounds();
    }

    /**
     * Creates a {@link #autoRangingProperty() non-auto-ranging} AbstractNumericAxis with the given upper bound, lower
     * bound and tick unit.
     *
     * @param lowerBound the {@link #lowerBoundProperty() lower bound} of the axis
     * @param upperBound the {@link #upperBoundProperty() upper bound} of the axis
     */
    protected AbstractNumericAxis(double lowerBound, double upperBound) {
        this(null, lowerBound, upperBound);
    }

    /**
     * Create a {@link #autoRangingProperty() non-auto-ranging} AbstractNumericAxis with the given upper bound and lower
     * bound.
     *
     * @param axisLabel the axis {@link #labelProperty() label}
     * @param lowerBound the {@link #lowerBoundProperty() lower bound} of the axis
     * @param upperBound the {@link #upperBoundProperty() upper bound} of the axis
     */
    protected AbstractNumericAxis(String axisLabel, double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
        setLabel(axisLabel);
        bindToBounds();
    }

    private void bindToBounds() {
        ChangeListener<Number> rangeUpdater = (obj, oldValue, newValue) -> {
            if (!isAutoRanging()) {
                if (getLowerBound() <= getUpperBound()) {
                    // If auto-range off - update ticks on bounds changes
                    setRange(computeRange(), false);
                } else {
                    throw new IllegalArgumentException("lowerBound [" + getLowerBound()
                            + "] must not be grater than upperBound [" + getUpperBound() + "]");
                }
            }
        };

        lowerBoundProperty().addListener(rangeUpdater);
        upperBoundProperty().addListener(rangeUpdater);
    }

    private final BooleanProperty autoRangeRounding = new SimpleBooleanProperty(true);

    /**
     * With {@link #autoRangingProperty()} on, defines if the range should be extended to the major tick unit value. For
     * example with range [3, 74] and major tick unit [5], the range will be extended to [0, 75].
     * <p>
     * <b>Default value: {@code true}</b>
     * </p>
     *
     * @return autoRangeRounding property
     */
    public BooleanProperty autoRangeRoundingProperty() {
        return autoRangeRounding;
    }

    /**
     * Returns the value of the {@link #autoRangeRoundingProperty()}.
     *
     * @return the auto range rounding flag
     */
    public boolean isAutoRangeRounding() {
        return autoRangeRoundingProperty().get();
    }

    /**
     * Sets the value of the {@link #autoRangeRoundingProperty()}
     *
     * @param round if {@code true}, lower and upper bound will be adjusted to the tick unit value
     */
    public void setAutoRangeRounding(boolean round) {
        autoRangeRoundingProperty().set(round);
    }

    /**
     * Get the string label name for a tick mark with the given value
     *
     * @param value The value to format into a tick label string
     * @return A formatted string for the given value
     */
    @Override
    protected String getTickMarkLabel(Number value) {
        StringConverter<Number> formatter = getTickLabelFormatter();
        if (formatter == null) {
            formatter = defaultFormatter;
        }
        return formatter.toString(value);
    }

    @Override
    protected Range getRange() {
        return new Range(getLowerBound(), getUpperBound(), getScale(), currentTickFormat.get());
    }

    @Override
    protected void setRange(Object rangeObj, boolean animate) {
        Range range = (Range) rangeObj;
        currentTickFormat.set(range.tickFormat);
        double oldLowerBound = getLowerBound();
        if (getLowerBound() != range.lowerBound) {
            setLowerBound(range.lowerBound);
        }
        if (getUpperBound() != range.upperBound) {
            setUpperBound(range.upperBound);
        }

        if (animate) {
            animator.stop();
            animator.getKeyFrames()
                    .setAll(new KeyFrame(Duration.ZERO, new KeyValue(currentLowerBound, oldLowerBound),
                            new KeyValue(scaleBinding, getScale())),
                            new KeyFrame(Duration.millis(RANGE_ANIMATION_DURATION_MS),
                                    new KeyValue(currentLowerBound, range.lowerBound),
                                    new KeyValue(scaleBinding, range.scale)));
            animator.play();
        } else {
            currentLowerBound.set(range.lowerBound);
            setScale(range.scale);
        }
    }

    @Override
    protected Dimension2D measureTickMarkSize(Number value, Object range) {
        return measureTickMarkSizeWithFormat(value, ((Range) range).tickFormat);
    }

    protected final Dimension2D measureTickMarkSizeWithFormat(Number value, String tickNumFormat) {
        StringConverter<Number> formatter = getTickLabelFormatter();
        if (formatter == null) {
            formatter = defaultFormatter;
        }
        String labelText;
        if (formatter instanceof DefaultFormatter) {
            labelText = ((DefaultFormatter) formatter).toString(value, tickNumFormat);
        } else {
            labelText = formatter.toString(value);
        }
        return measureTickMarkLabelSize(labelText, getTickLabelRotation());
    }

    private Range computeRange() {
        if (getSide() == null) {
            return getRange();
        }
        double length = getSide().isVertical() ? getHeight() : getWidth();
        double labelSize = getTickLabelFont().getSize() * 2;
        return computeRange(getLowerBound(), getUpperBound(), length, labelSize);
    }

    /**
     * Computes range of this axis, similarly to {@link #autoRange(double, double, double, double)}. The major
     * difference is that this method is called when {@link #autoRangingProperty() auto-range} is off.
     *
     * @param minValue The min data value that needs to be plotted on this axis
     * @param maxValue The max data value that needs to be plotted on this axis
     * @param axisLength The length of the axis in display coordinates
     * @param labelSize The approximate average size a label takes along the axis
     * @return The calculated range
     * @see #autoRange(double, double, double, double)
     */
    protected abstract Range computeRange(double minValue, double maxValue, double axisLength, double labelSize);

    /**
     * Default number formatter.
     */
    public static class DefaultFormatter extends StringConverter<Number> {
        private DecimalFormat decimalFormat;
        private String prefix = null;
        private String suffix = null;

        /**
         * @param axis axis to format ticks for
         */
        public DefaultFormatter(AbstractNumericAxis axis) {
            decimalFormat = new DecimalFormat(axis.currentTickFormat.get());
            axis.currentTickFormat.addListener((observable, oldValue, newValue) -> {
                decimalFormat = new DecimalFormat(axis.currentTickFormat.get());
            });
        }

        /**
         * @param axis axis to format ticks for
         * @param prefix prefix of the formatter number or {@code null}
         * @param suffix suffix of the formatter number or {@code null}
         */
        public DefaultFormatter(AbstractNumericAxis axis, String prefix, String suffix) {
            this(axis);
            this.prefix = prefix;
            this.suffix = suffix;
        }

        /**
         * Formats given number. 
         */
        @Override
        public String toString(Number number) {
            return toString(number, decimalFormat);
        }

        private String toString(Number object, String format) {
            if (format == null || format.isEmpty()) {
                return toString(object, decimalFormat);
            }
            return toString(object, new DecimalFormat(format));
        }

        private String toString(Number object, DecimalFormat formatter) {
            String pref = prefix == null ? "" : prefix;
            String suff = suffix == null ? "" : suffix;
            return pref + formatter.format(object) + suff;
        }

        /**
         * Converts given string to number, taking into account prefix and suffix. 
         */
        @Override
        public Number fromString(String string) {
            try {
                int prefixLength = (prefix == null) ? 0 : prefix.length();
                int suffixLength = (suffix == null) ? 0 : suffix.length();
                return decimalFormat.parse(string.substring(prefixLength, string.length() - suffixLength));
            } catch (ParseException exc) {
                throw new IllegalArgumentException(exc);
            }
        }
    }

    /**
     * Holds the range of the axis along with {@link ValueAxis#getScale() scale} and tick numbers format to be used.
     */
    public static class Range {
        final double lowerBound;
        final double upperBound;
        final double scale;
        final String tickFormat;

        /**
         * Creates new Range.
         *
         * @param lowerBound the lower bound of the axis
         * @param upperBound the upper bound of the axis
         * @param scale the calculated {@link ValueAxis#getScale() scale}
         * @param tickFormat decimal format pattern to be applied for tick numbers
         */
        public Range(double lowerBound, double upperBound, double scale, String tickFormat) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.scale = scale;
            this.tickFormat = tickFormat;
        }

        /**
         * @return the lower bound of the axis
         */
        public double getLowerBound() {
            return lowerBound;
        }

        /**
         * @return the upper bound of the axis
         */
        public double getUpperBound() {
            return upperBound;
        }

        /**
         * @return the calculated {@link ValueAxis#getScale() scale}
         */
        public double getScale() {
            return scale;
        }

        /**
         * @return decimal format pattern to be applied for tick numbers
         */
        public String getTickFormat() {
            return tickFormat;
        }
    }
}