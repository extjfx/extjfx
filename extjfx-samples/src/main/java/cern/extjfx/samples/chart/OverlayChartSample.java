/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.samples.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.XYChartPane;
import cern.extjfx.chart.plugins.DataPointTooltip;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;

public class OverlayChartSample extends AbstractSamplePane {
    private static final List<String> MONTHS = new ArrayList<>(Arrays.asList("January", "February", "March", "April",
            "May", "June", "July", "August", "September", "October", "November", "December"));

	@Override
	public String getName() {
		return "Overlay Chart";
	}

	@Override
	public String getDescription() {
		return "Overlaying different chart types on top of each other.";
	}

	@Override
	public Node createSamplePane() {
	    NumericAxis yRainfallAxis = createYAxis();
	    yRainfallAxis.setLowerBound(0);
	    yRainfallAxis.setUpperBound(100);
	    yRainfallAxis.setAutoRanging(false);
        BarChart<String, Number> rainfallChart = new BarChart<>(createXAxis(), yRainfallAxis);
        rainfallChart.setTitle("Rainfall (mm)");
        rainfallChart.setAnimated(false);
        rainfallChart.getYAxis().setLabel("RainFall (mm)");
        rainfallChart.getYAxis().setTickLabelFill(Color.BLUE);
        rainfallChart.getYAxis().lookup(".axis-label").setStyle("-fx-text-fill: blue;");
        rainfallChart.getYAxis().setSide(Side.LEFT);
        
        NumericAxis yTempAxis = createYAxis();
        yTempAxis.setLowerBound(0);
        yTempAxis.setUpperBound(40);
        yTempAxis.setAutoRanging(false);

        LineChart<String, Number> temperatureChart = new LineChart<>(createXAxis(), yTempAxis);
        temperatureChart.setTitle("Average Temperature (°C)");
        temperatureChart.setAnimated(false);
        temperatureChart.setCreateSymbols(true);
        temperatureChart.getYAxis().setTickLabelFill(Color.RED);
        temperatureChart.getYAxis().lookup(".axis-label").setStyle("-fx-text-fill: red;");
        temperatureChart.getYAxis().setLabel("Average Temperature (°C)");
        temperatureChart.getYAxis().setSide(Side.RIGHT);

        XYChartPane<String, Number> chartPane = new XYChartPane<>(rainfallChart);
        chartPane.setCommonYAxis(false);
        chartPane.getOverlayCharts().add(temperatureChart);
        chartPane.getPlugins().add(new DataPointTooltip<>());
        chartPane.getStylesheets().add(OverlayChartSample.class.getResource("overlayChart.css").toExternalForm());


        rainfallChart.getData().add(new Series<>("Rainfall (mm)", createRainfallData()));
        temperatureChart.getData().add(new Series<>("Temperature (°C)", createTemperatureData()));
        
		return chartPane;
	}

    private ObservableList<Data<String, Number>> createTemperatureData() {
        List<Data<String, Number>> data = new ArrayList<>();
        data.addAll(Arrays.asList(createData(0, 3.5), createData(1, 5.3),
                createData(2, 9.2), createData(3, 13.5),
                createData(4, 17.6), createData(5, 21.8),
                createData(6, 24.6), createData(7, 23.7),
                createData(8, 20.3), createData(9, 13.9),
                createData(10, 8.0), createData(11, 4.2)));

        return FXCollections.observableArrayList(data);
    }

    private ObservableList<Data<String, Number>> createRainfallData() {
        List<Data<String, Number>> data = new ArrayList<>();
        data.addAll(Arrays.asList(createData(0, 32), createData(1, 37),
                createData(2, 43), createData(3, 51), createData(4, 72),
                createData(5, 91), createData(6, 94), createData(7, 80),
                createData(8, 55), createData(9, 41), createData(10, 50),
                createData(11, 43)));

        return FXCollections.observableArrayList(data);
    }

    private Data<String, Number> createData(int monthIndex, Number value) {
        return new Data<>(MONTHS.get(monthIndex), value);
    }
    
    private NumericAxis createYAxis() {
        NumericAxis yAxis = new NumericAxis();
        yAxis.setAnimated(false);
        yAxis.setForceZeroInRange(false);
        yAxis.setAutoRangePadding(0.1);
        yAxis.setAutoRangeRounding(false);
        return yAxis;
    }

    private CategoryAxis createXAxis() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setAnimated(false);
        return xAxis;
    }
}
