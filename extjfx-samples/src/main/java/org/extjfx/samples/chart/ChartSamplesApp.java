/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.extjfx.samples.chart;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ChartSamplesApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private ListView<String> samplesSelectionList;
    private Map<String, Node> samplesMap = new HashMap<>();
    private BorderPane rootPane;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(createRootPane(), 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ExtJFX Chart Samples");
        primaryStage.show();
    }

    private Parent createRootPane() {
        rootPane = new BorderPane();
        rootPane.setPadding(new Insets(5));

        samplesSelectionList = new ListView<>();
        samplesSelectionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> showSample(newVal));
        samplesSelectionList.setPrefWidth(150);
        BorderPane.setMargin(samplesSelectionList, new Insets(5));
        
        rootPane.setLeft(samplesSelectionList);
        
        registerSample(new DataIndicatorsSample());
        registerSample(new HeatMapChartSample());
        registerSample(new OverlayChartSample());
        registerSample(new LargeDataSetsSample());
        registerSample(new LogarithmicAxisSample());
        
        return rootPane;
    }
    
    private void showSample(String sampleName) {
        if (sampleName != null) {
            rootPane.setCenter(samplesMap.get(sampleName));
        }
    }

    private void registerSample(AbstractSamplePane sample) {
        samplesMap.put(sample.getName(), sample);
        samplesSelectionList.getItems().add(sample.getName());
        BorderPane.setMargin(sample, new Insets(5));
    }

}
