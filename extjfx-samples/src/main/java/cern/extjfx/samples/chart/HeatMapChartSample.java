/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.samples.chart;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import cern.extjfx.chart.HeatMapChart;
import cern.extjfx.chart.HeatMapChart.ColorGradient;
import cern.extjfx.chart.HeatMapChart.Data;
import cern.extjfx.chart.HeatMapChart.DefaultData;
import cern.extjfx.chart.NumericAxis;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HeatMapChartSample extends AbstractSamplePane {
    HeatMapChart<Number, Number> chart;

    @Override
    public String getName() {
        return "HeatMapChart";
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    protected Node createSamplePane() {
        NumericAxis xAxis = new NumericAxis();
        xAxis.setAnimated(false);
        xAxis.setAutoRangeRounding(false);
        xAxis.setLabel("X Position");

        NumericAxis yAxis = new NumericAxis();
        yAxis.setAnimated(false);
        yAxis.setAutoRangeRounding(false);
        yAxis.setLabel("Y Position");

        chart = new HeatMapChart<>(xAxis, yAxis);
        chart.setTitle("Beam Image");
        chart.setAnimated(false);

        chart.setData(loadHeatMapData());
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.RIGHT);

        return chart;
    }

    private Data<Number, Number> loadHeatMapData() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(HeatMapChartSample.class.getResourceAsStream("heat-map.txt")));
            String[] x = reader.readLine().split(" ");
            String[] y = reader.readLine().split(" ");
            String[] z = reader.readLine().split(" ");
            reader.close();

            Number[] xValues = Arrays.stream(x).map(Double::parseDouble).toArray(Number[]::new);
            Number[] yValues = Arrays.stream(y).map(Double::parseDouble).toArray(Number[]::new);

            double[][] zValues = new double[x.length][y.length];
            int zIdx = 0;
            for (int yIdx = 0; yIdx < y.length; yIdx++) {
                for (int xIdx = 0; xIdx < x.length; xIdx++) {
                    zValues[xIdx][yIdx] = Double.parseDouble(z[zIdx++]);
                }
            }
            return new DefaultData<>(xValues, yValues, zValues);
        } catch (Exception e) {
        }
        return new DefaultData<>(new Number[0], new Number[0], new double[0][0]);
    }

    @Override
    public Node createControlPane() {
        ChoiceBox<Side> legendSide = new ChoiceBox<>(FXCollections.observableArrayList(Side.values()));
        legendSide.setValue(Side.RIGHT);
        chart.legendSideProperty().bind(legendSide.valueProperty());

        final Map<String, ColorGradient> gradients = new LinkedHashMap<>();
        gradients.put("RAINBOW", ColorGradient.RAINBOW);
        gradients.put("BLACK_WHITE", ColorGradient.BLACK_WHITE);
        gradients.put("WHITE_BLACK", ColorGradient.WHITE_BLACK);
        gradients.put("SUNRISE", ColorGradient.SUNRISE);

        ChoiceBox<String> gradient = new ChoiceBox<>(FXCollections.observableArrayList(gradients.keySet()));
        gradient.setValue("RAINBOW");
        chart.colorGradientProperty()
                .bind(Bindings.createObjectBinding(() -> gradients.get(gradient.getValue()), gradient.valueProperty()));

        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 10, 10, 10));
        box.getChildren().addAll(new Label("Legend Side:"), legendSide, new Label("Color Gradient:"), gradient);
        return box;
    }
}
