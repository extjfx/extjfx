/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.chart;

import java.util.LinkedList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

class Utils {

    private Utils() {
        //
    }

    static double getLocationX(Node node) {
        return node.getLayoutX() + node.getTranslateX();
    }

    static double getLocationY(Node node) {
        return node.getLayoutY() + node.getTranslateY();
    }

    static Region getChartContent(Chart chart) {
        return (Region) chart.lookup(".chart-content");
    }

    static Node getPlotContent(XYChart<?, ?> chart) {
        return chart.lookup(".plot-content");
    }

    static Pane getLegend(XYChart<?, ?> chart) {
        return (Pane) chart.lookup(".chart-legend");
    }

    static double getHorizontalInsets(Insets insets) {
        return insets.getLeft() + insets.getRight();
    }

    static double getVerticalInsets(Insets insets) {
        return insets.getTop() + insets.getBottom();
    }

    /**
     * Returns Chart instance containing given child node.
     * 
     * @param chartChildNode the node contained within the chart
     * @return chart or {@code null} if the node does not belong to chart
     */
    static Chart getChart(final Node chartChildNode) {
        Node node = chartChildNode;
        while (node != null && !(node instanceof Chart)) {
            node = node.getParent();
        }
        return (Chart) node;
    }

    static List<Label> getChildLabels(List<? extends Parent> parents) {
        List<Label> labels = new LinkedList<>();
        for (Parent parent : parents) {
            for (Node node : parent.getChildrenUnmodifiable()) {
                if (node instanceof Label) {
                    labels.add((Label) node);
                }
            }
        }
        return labels;
    }
}
