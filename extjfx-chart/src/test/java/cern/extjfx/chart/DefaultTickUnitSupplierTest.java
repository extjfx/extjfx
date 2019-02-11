/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.TreeSet;

import org.junit.Test;

import cern.extjfx.chart.DefaultTickUnitSupplier;

public class DefaultTickUnitSupplierTest {
    private static final double[] TEST_ARGS = {0.0017, 0.023, 0.74, 1.0, 1.2, 3.4, 5, 6.6, 7, 42, 765};
    
    @Test(expected=NullPointerException.class)
    public void testNullMultipliers() {
        new DefaultTickUnitSupplier(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testEmptyMultipliers() {
        new DefaultTickUnitSupplier(new TreeSet<>());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOutOfRangeMultipliers() {
        new DefaultTickUnitSupplier(new TreeSet<>(asList(0.5)));
    }

    @Test
    public void test1Multipliers() {
        double[] expected = {0.01, 0.1, 1, 1, 10, 10, 10, 10, 10, 100, 1000};
        checkResults(new DefaultTickUnitSupplier(new TreeSet<>(asList(1))), expected);
    }
    
    @Test
    public void test47Multipliers() {
        double[] expected = {0.004, 0.04, 4, 4, 4, 4, 7, 7, 7, 70, 4000};
        checkResults(new DefaultTickUnitSupplier(new TreeSet<>(asList(4, 7))), expected);
    }
    
    @Test
    public void test125Multipliers() {
        double[] expected = {0.002, 0.05, 1, 1, 2, 5, 5, 10, 10, 50, 1000};
        checkResults(new DefaultTickUnitSupplier(new TreeSet<>(asList(1, 2, 5))), expected);
    }
    
    @Test
    public void test1255Multipliers() {
        double[] expected = {0.0025, 0.025, 1, 1, 2.5, 5, 5, 10, 10, 50, 1000};
        checkResults(new DefaultTickUnitSupplier(new TreeSet<>(asList(1d, 2.5, 5d))), expected);
    }
    
    private static void checkResults(DefaultTickUnitSupplier supplier, double[] expected) {
        for (int i = 0; i < TEST_ARGS.length; i++) {
            assertEquals(expected[i], supplier.computeTickUnit(TEST_ARGS[i]), 0.00000001);
        }
    }
}
