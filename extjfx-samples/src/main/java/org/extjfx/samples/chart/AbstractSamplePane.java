/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.extjfx.samples.chart;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

abstract class AbstractSamplePane extends BorderPane {

    AbstractSamplePane() {
        init();
    }

    private void init() {
        Label titleLabel = new Label(getDescription());
        titleLabel.setStyle("-fx-border-color: grey");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        setTop(titleLabel);

        SplitPane centerPane = new SplitPane(createSamplePane());
        Node controlPane = createControlPane();
        if (controlPane != null) {
            centerPane.getItems().add(controlPane);
            centerPane.setDividerPositions(0.65);
        }
        setCenter(centerPane);
    }

    public abstract String getName();

    public abstract String getDescription();

    protected abstract Node createSamplePane();

    protected Node createControlPane() {
        return null;
    }
}
