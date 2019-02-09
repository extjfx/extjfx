/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.fxml;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class TestController implements Initializable {

    @Inject
    TestModel model;
    
    @Inject
    TestService service;
    
    @Named("user.home")
    @Inject
    String userHomeDirectory;
    
    @Inject 
    String fieldInitializedFromSystemProperties;
    
    @Inject 
    @Named("myIntField")
    int intField;
    
    @FXML
    HeaderController headerController;

    @FXML
    SubViewController subView1Controller;
    
    @FXML
    SubViewController subView2Controller;

    @FXML
    Button cancelButton;

    ResourceBundle resourceBundle;
    
    @Override
    public void initialize(URL location, ResourceBundle bundle) {
        this.resourceBundle = bundle;
    }
    
    @FXML
    void cancel() {
        //
    }
}
