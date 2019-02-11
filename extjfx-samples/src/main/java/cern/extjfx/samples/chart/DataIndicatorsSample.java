/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.samples.chart;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.XYChartPane;
import cern.extjfx.chart.plugins.ChartOverlay;
import cern.extjfx.chart.plugins.ChartOverlay.OverlayArea;
import cern.extjfx.chart.plugins.DataPointTooltip;
import cern.extjfx.chart.plugins.Panner;
import cern.extjfx.chart.plugins.XValueIndicator;
import cern.extjfx.chart.plugins.YRangeIndicator;
import cern.extjfx.chart.plugins.Zoomer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

class DataIndicatorsSample extends AbstractSamplePane {
	private XValueIndicator<Number> xValueIndicator;
	private YRangeIndicator<Number> yRangeIndicator;

    @Override
	public String getName() {
	    return "Data Indicators";
	}
	
	@Override
	public String getDescription() {
	    return "Horizontal and vertical indicators showing a single value or range of values, with a configurable label (text and position)";
	}
	
	@Override
	protected Node createSamplePane() {
		LineChart<Number, Number> chart1 = createChart();
		chart1.getStylesheets().addAll(styles("chart.css", "chart1.css"));
		chart1.getYAxis().setLabel("Data1");
		chart1.getData().add(new Series<>("Data1", generateData1()));

		XYChartPane<Number, Number> chartPane = new XYChartPane<>(chart1);

		LineChart<Number, Number> chart2 = createChart();
		chart2.getStylesheets().addAll(styles("chart.css", "chart2.css"));
		chart2.getYAxis().setSide(Side.RIGHT);
		chart2.getYAxis().setLabel("Data2");
		chart2.getData().add(new Series<>("Data2", generateData2()));
		chartPane.getOverlayCharts().add(chart2);

		yRangeIndicator = new YRangeIndicator<>(-1, 1, (ValueAxis<Number>) chart2.getYAxis());
		xValueIndicator = new XValueIndicator<>(50);
		yRangeIndicator.setLabelHorizontalPosition(0.2);

		Button updateChart1Button = new Button("Update Data1");
		updateChart1Button.setOnAction(e -> chart1.getData().get(0).setData(generateData1()));
		
		Button updateChart2Button = new Button("Update Data2");
		updateChart2Button.setOnAction(e -> chart2.getData().get(0).setData(generateData2()));
		
		VBox buttonBox = new VBox(2);
		buttonBox.getChildren().addAll(updateChart1Button, updateChart2Button);
		
		AnchorPane.setTopAnchor(buttonBox, 5.0);
		AnchorPane.setLeftAnchor(buttonBox, 5.0);

		AnchorPane anchorPane = new AnchorPane(buttonBox);
		chartPane.getPlugins().add(new ChartOverlay<>(OverlayArea.PLOT_AREA, anchorPane));
		chartPane.getPlugins().add(xValueIndicator);
		chartPane.getPlugins().add(yRangeIndicator);
		chartPane.getPlugins().add(new Zoomer());
        chartPane.getPlugins().add(new Panner());
		chartPane.getPlugins().add(new DataPointTooltip<>());

		return chartPane;
	}

    private static ObservableList<Data<Number, Number>> generateData1() {
    	return RandomDataGenerator.generateData(0, 5, 20);
    }

    private static ObservableList<Data<Number, Number>> generateData2() {
    	return RandomDataGenerator.generateData(0, 50, 20);
    }

	private static List<String> styles(String... cssNames) {
		return Arrays.stream(cssNames).map(css -> DataIndicatorsSample.class.getResource(css).toExternalForm())
				.collect(Collectors.toList());
	}

	private LineChart<Number, Number> createChart() {
		LineChart<Number, Number> chart = new LineChart<>(createXAxis(), createYAxis());
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		chart.setHorizontalZeroLineVisible(false);
		return chart;
	}

	private static NumericAxis createYAxis() {
		NumericAxis yAxis = createXAxis();
		yAxis.setAutoRangePadding(0.1);
		yAxis.setAutoRangeRounding(false);
		return yAxis;
	}

	private static NumericAxis createXAxis() {
		NumericAxis xAxis = new NumericAxis();
		xAxis.setAnimated(false);
		xAxis.setForceZeroInRange(false);
		return xAxis;
	}

	@Override
	protected Node createControlPane() {
		TextField xValueField = new TextField("15");
		TextField yRangeLowerBoundtField = new TextField("-10");
		TextField yRangeUpperBoundField = new TextField("10");
		TextField xValueLabelField = new TextField("X Value");
		TextField yRangeLabelField = new TextField("Y Range");

		Spinner<Double> xValueLabelVPosSpinner = new Spinner<>(0, 1, 0.7, 0.1);
		Spinner<Double> yRangeLabelHPosSpinner = new Spinner<>(0, 1, 0.2, 0.1);
		Spinner<Double> yRangeLabelVPosSpinner = new Spinner<>(0, 1, 0.5, 0.1);

		ChoiceBox<HPos> xValueHAnchor = choiceBox(HPos.values(), HPos.CENTER);
		ChoiceBox<VPos> xValueVAnchor = choiceBox(VPos.values(), VPos.CENTER);
		ChoiceBox<HPos> yRangeHAnchor = choiceBox(HPos.values(), HPos.CENTER);
		ChoiceBox<VPos> yRangeVAnchor = choiceBox(VPos.values(), VPos.CENTER);

		// Bindings
		bind(xValueIndicator.valueProperty(), xValueField.textProperty());
		bind(yRangeIndicator.lowerBoundProperty(), yRangeLowerBoundtField.textProperty());
		bind(yRangeIndicator.upperBoundProperty(), yRangeUpperBoundField.textProperty());

		xValueIndicator.textProperty().bind(xValueLabelField.textProperty());
		yRangeIndicator.textProperty().bind(yRangeLabelField.textProperty());

		xValueIndicator.labelPositionProperty().bind(xValueLabelVPosSpinner.valueProperty());
		yRangeIndicator.labelHorizontalPositionProperty().bind(yRangeLabelHPosSpinner.valueProperty());
		yRangeIndicator.labelVerticalPositionProperty().bind(yRangeLabelVPosSpinner.valueProperty());

		xValueIndicator.labelHorizontalAnchorProperty().bind(xValueHAnchor.valueProperty());
		xValueIndicator.labelVerticalAnchorProperty().bind(xValueVAnchor.valueProperty());
		yRangeIndicator.labelHorizontalAnchorProperty().bind(yRangeHAnchor.valueProperty());
		yRangeIndicator.labelVerticalAnchorProperty().bind(yRangeVAnchor.valueProperty());

		// Layout
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(5, 5, 5, 5));
		grid.setHgap(5);
		grid.setVgap(5);
		grid.getColumnConstraints().add(new ColumnConstraints(90));
		grid.getColumnConstraints().add(new ColumnConstraints(90));
		grid.getColumnConstraints().add(new ColumnConstraints(90));

		grid.add(new Label("Tooltip:"), 0, 0);
		grid.add(new Label("Move mouse over point"), 1, 0, 2, 1);
		grid.add(new Label("Zoom-in:"), 0, 1);
		grid.add(new Label("drag left-mouse"), 1, 1, 2, 1);
		grid.add(new Label("Zoom-out:"), 0, 2);
		grid.add(new Label("right-click"), 1, 2, 2, 1);
		grid.add(new Label("Zoom-origin:"), 0, 3);
		grid.add(new Label("CTRL + rigth-click"), 1, 3, 2, 1);
		grid.add(new Label("Pan:"), 0, 4);
		grid.add(new Label("CTRL + drag left-mouse"), 1, 4, 2, 1);
		grid.add(new Separator(), 0, 5, 3, 1);

		
		grid.add(new Label("X Value"), 1, 6);
		grid.add(new Label("Y Range"), 2, 6);

		grid.add(new Label("Lo. Bound:"), 0, 7);
		grid.add(xValueField, 1, 7);
		grid.add(yRangeLowerBoundtField, 2, 7);

		grid.add(new Label("Up. Bound:"), 0, 8);
		grid.add(yRangeUpperBoundField, 2, 8);

		grid.add(new Label("Label:"), 0, 9);
		grid.add(xValueLabelField, 1, 9);
		grid.add(yRangeLabelField, 2, 9);

		grid.add(new Label("Label HPos:"), 0, 10);
		grid.add(yRangeLabelHPosSpinner, 2, 10);

		grid.add(new Label("Label VPos:"), 0, 11);
		grid.add(xValueLabelVPosSpinner, 1, 11);
		grid.add(yRangeLabelVPosSpinner, 2, 11);

		grid.add(new Label("HAnchor:"), 0, 12);
		xValueHAnchor.setMaxWidth(Double.MAX_VALUE);
		grid.add(xValueHAnchor, 1, 12);
		yRangeHAnchor.setMaxWidth(Double.MAX_VALUE);
		grid.add(yRangeHAnchor, 2, 12);

		grid.add(new Label("VAnchor:"), 0, 13);
		GridPane.setFillWidth(xValueVAnchor, true);
		grid.add(xValueVAnchor, 1, 13);
		GridPane.setFillWidth(yRangeVAnchor, true);
		grid.add(yRangeVAnchor, 2, 13);

		return grid;
	}

	private static <T> ChoiceBox<T> choiceBox(T[] values, T defaultValue) {
		ChoiceBox<T> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(values));
		choiceBox.setValue(defaultValue);
		choiceBox.setMaxWidth(Double.MAX_VALUE);
		return choiceBox;
	}

	private void bind(DoubleProperty doubleProperty, StringProperty stringProperty) {
		doubleProperty.bind(Bindings.createDoubleBinding(() -> {
			try {
				return Double.parseDouble(stringProperty.get());
			} catch (NumberFormatException nfe) {
				return 0.0;
			}
		}, stringProperty));
	}
}
