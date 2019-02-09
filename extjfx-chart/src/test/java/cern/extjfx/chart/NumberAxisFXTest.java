/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import org.junit.Test;
import org.junit.runner.RunWith;

import cern.extjfx.chart.NumericAxis;
import cern.extjfx.test.FxJUnit4Runner;
import javafx.geometry.Side;

@RunWith(FxJUnit4Runner.class)
public class NumberAxisFXTest {

    @Test
    public void testLowerUpperBoundEqual() {
        NumericAxis axis = new NumericAxis();
        axis.setSide(Side.BOTTOM);
        axis.setAutoRanging(false);
        axis.setLowerBound(0);
        axis.setUpperBound(0);
    }
}
