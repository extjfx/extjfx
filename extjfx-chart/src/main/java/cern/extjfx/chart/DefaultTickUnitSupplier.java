/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import java.util.Arrays;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * TickUnitSupplier that computes tick units as multiplications of powers of 10.
 * <p>
 * The multipliers (factors) can be specified during the construction. They are expected to be numbers between 1 
 * (inclusive) and 10 (exclusive). See {@link #computeTickUnit(double)} for more details.
 */
public final class DefaultTickUnitSupplier implements TickUnitSupplier {
    private static final int BASE = 10;
    private static final SortedSet<Number> DEFAULT_MULTIPLIERS = new TreeSet<>(Arrays.asList(1d, 2.5, 5d));
    private final double[] multipliers;

    /**
     * Creates a new instance of {@code DefaultTickUnitSupplier} initialized with multipliers: [1, 2.5, 5].
     */
    public DefaultTickUnitSupplier() {
        this(DEFAULT_MULTIPLIERS);
    }

    /**
     * Creates a new instance of {@code DefaultTickUnitSupplier} with specified multipliers.
     * 
     * @param multipliers an array of ascending numbers, with at least one element, from 1 (inclusive) to 10
     *            (exclusive).
     */
    public DefaultTickUnitSupplier(SortedSet<? extends Number> multipliers) {
        Objects.requireNonNull(multipliers, "The multipliers must not be null");
        if (multipliers.isEmpty()) {
            throw new IllegalArgumentException("The set of multipliers must not be empty");
        }
        checkRange(multipliers);

        this.multipliers = multipliers.stream().mapToDouble(multiplier -> multiplier.doubleValue()).toArray();
    }

    private static void checkRange(SortedSet<? extends Number> multipliers) {
        for (Number mult : multipliers) {
            if (mult.doubleValue() < 1 || mult.doubleValue() >= BASE) {
                throw new IllegalArgumentException("The multiplier values must be in range [1, 10)");
            }
        }
    }

    /**
     * Computes tick unit using the following formula: tickUnit = M*10^E, where M is one of the multipliers specified in
     * the constructor and E is an exponent of 10. Both M and E are selected so that the calculated unit is the smallest
     * (closest to the zero) value that is grater than or equal to the reference tick unit.
     * <p>
     * For example with multipliers [1, 2, 5], the method will give the following results: 
     * <pre>
     * computeTickUnit(0.01) returns 0.01
     * computeTickUnit(0.42) returns 0.5
     * computeTickUnit(1.73) returns 2
     * computeTickUnit(5)    returns 5
     * computeTickUnit(27)   returns 50
     * </pre>
     * @param referenceTickUnit the reference tick unit, must be a positive number
     */
    @Override
    public double computeTickUnit(double referenceTickUnit) {
        if (referenceTickUnit <= 0) {
            throw new IllegalArgumentException("The reference tick unit must be a positive number");
        }

        int exp = (int) Math.floor(Math.log10(referenceTickUnit));
        double factor = referenceTickUnit / Math.pow(BASE, exp);
        double multiplier = 0;

        int lastIndex = multipliers.length - 1;
        if (factor > multipliers[lastIndex]) {
            exp++;
            multiplier = multipliers[0];
        } else {
            for (int i = lastIndex; i >= 0; i--) {
                if (factor <= multipliers[i]) {
                    multiplier = multipliers[i];
                } else {
                    break;
                }
            }
        }
        return multiplier * Math.pow(BASE, exp);
    }
}
