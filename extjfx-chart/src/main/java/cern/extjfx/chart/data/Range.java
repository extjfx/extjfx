/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart.data;

/**
 * Closed range of comparable values.
 * 
 * @param <C> the range type
 */
public final class Range<C extends Comparable<C>> {

    private final C lowerBound;
    private final C upperBound;

    public Range(C lowerBound, C upperBound) {
        if (lowerBound != null && upperBound != null && lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException(
                    "The lowerBound [" + lowerBound + "] can't be bigger than upperBound [" + upperBound + "]");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Returns the lower bound of the range
     * 
     * @return lower bound
     */
    public C getLowerBound() {
        return lowerBound;
    }

    /**
     * Returns the upper bound of the range
     * 
     * @return upper bound
     */
    public C getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return "[" + lowerBound + "," + upperBound + "]";
    }
}
