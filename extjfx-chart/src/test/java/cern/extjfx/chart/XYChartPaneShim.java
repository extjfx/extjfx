/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import cern.extjfx.chart.Utils;
import cern.extjfx.chart.XYChartPane;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.layout.Region;

/**
 * https://wiki.openjdk.java.net/display/OpenJFX/OpenJFX+unit+tests#OpenJFXunittests-TheShims
 */
public final class XYChartPaneShim {

    public static Point2D getLocationInChartPane(XYChartPane<?, ?> chartPane, Node node) {
        return chartPane.getLocationInChartPane(node);
    }

    public static Point2D localToChartPane(XYChartPane<?, ?> chartPane, Node node, Point2D pt) {
        return chartPane.localToChartPane(node, pt);
    }

    public static Region getChartContent(Chart chart) {
        return Utils.getChartContent(chart);
    }
}
