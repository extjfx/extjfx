/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.samples.chart;

import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.XYChartPane;
import cern.extjfx.chart.data.DataReducingObservableList;
import cern.extjfx.chart.plugins.CrosshairIndicator;
import cern.extjfx.chart.plugins.DataPointTooltip;
import cern.extjfx.chart.plugins.Panner;
import cern.extjfx.chart.plugins.Zoomer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;

public class LargeDataSetsSample extends AbstractSamplePane {
	DataReducingObservableList<Number, Number> data;

	@Override
	public String getName() {
		return "Large Data Set";
	}

	@Override
	public String getDescription() {
		return "The DataReducingObservableList reduces the number of points to be visualized in the given X range. "
		        + "Zooming-in allows to see more details in the selected area. "
		        + "The main purpose of the class is a visualisation of large data sets. In this sample we display 100'000 points.";
	}

	@Override
	public Node createSamplePane() {
		NumericAxis xAxis = new NumericAxis();
		xAxis.setAnimated(false);

		NumericAxis yAxis = new NumericAxis();
		yAxis.setAnimated(false);
		yAxis.setAutoRangePadding(0.1);

		final int pointsCount = 100_000;
		LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
		lineChart.setTitle("Series with " + pointsCount + " points");
		lineChart.setAnimated(false);
		lineChart.setCreateSymbols(false);

		data = new DataReducingObservableList<>(xAxis, RandomDataGenerator.generateData(0, 1, pointsCount));
		lineChart.getData().add(new Series<>("Test data", data));

		XYChartPane<Number, Number> chartPane = new XYChartPane<>(lineChart);
		chartPane.getPlugins().addAll(new Zoomer(), new Panner(), new DataPointTooltip(), new CrosshairIndicator<>());

		return chartPane;
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
		
		grid.add(new Label("Points #: "), 0, 4);
		
		Spinner<Integer> pointsCountSpinner = new Spinner<>(10, 5000, data.getMaxPointsCount(), 10);
		pointsCountSpinner.setPrefWidth(100);
		data.maxPointsCountProperty().bind(pointsCountSpinner.valueProperty());
		grid.add(pointsCountSpinner, 1, 4);
		
		return grid;
	}
}
