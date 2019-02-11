/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.samples.chart;

import java.util.ArrayList;
import java.util.List;

import cern.extjfx.chart.LogarithmicAxis;
import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.XYChartPane;
import cern.extjfx.chart.plugins.CrosshairIndicator;
import cern.extjfx.chart.plugins.DataPointTooltip;
import cern.extjfx.chart.plugins.Panner;
import cern.extjfx.chart.plugins.Zoomer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;

public class LogarithmicAxisSample extends AbstractSamplePane {
    LogarithmicAxis yAxis;
    
	@Override
	public String getName() {
		return "Logarithmic Axis";
	}

	@Override
	public String getDescription() {
		return "LogarithmicAxis with configurable logarithm base.";
	}

	@Override
	public Node createSamplePane() {
		NumericAxis xAxis = new NumericAxis();
		xAxis.setAnimated(false);
		xAxis.setLabel("x");

		yAxis = new LogarithmicAxis();
		yAxis.setAnimated(false);
		yAxis.setLabel("f(x)");

		LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
		lineChart.setTitle("Logarithmic Axis Example");
		lineChart.setAnimated(false);

		lineChart.getData().add(new Series<>("f(x) = (x/4)^3", createData()));

		XYChartPane<Number, Number> chartPane = new XYChartPane<>(lineChart);
		chartPane.getPlugins().addAll(new Zoomer(), new Panner(), new DataPointTooltip(), new CrosshairIndicator<>());

		return chartPane;
	}

	private static ObservableList<Data<Number, Number>> createData() {
        List<Data<Number, Number>> data = new ArrayList<>();
        for (int i = 1; i < 30; i++) {
            data.add(new Data<>(i, Math.pow(i/4.0, 3)));
        }
        return FXCollections.observableArrayList(data);

	}
	
	@Override
	public Node createControlPane() {
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.setHgap(10);
		grid.setVgap(10);
		
		grid.add(new Label("Zoom-in:"), 0, 0);
		grid.add(new Label("drag left-mouse"), 1, 0);
		grid.add(new Label("Zoom-out:"), 0, 1);
		grid.add(new Label("right-click"), 1, 1);
		grid.add(new Label("Zoom-origin:"), 0, 2);
		grid.add(new Label("CTRL + right-click"), 1, 2);
		grid.add(new Label("Pan:"), 0, 3);
		grid.add(new Label("CTRL + drag left-mouse"), 1, 3);
		
		grid.add(new Label("Log base: "), 0, 4);
		
		Spinner<Integer> logBaseSpinner = new Spinner<>(2, 20, 10, 1);
		logBaseSpinner.setPrefWidth(100);
		yAxis.logarithmBaseProperty().bind(logBaseSpinner.valueProperty());
		grid.add(logBaseSpinner, 1, 4);
		
		return grid;
	}
}
