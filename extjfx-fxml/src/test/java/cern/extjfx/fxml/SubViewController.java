/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.extjfx.fxml;

import java.net.URL;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.fxml.Initializable;

public class SubViewController implements Initializable {

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

    ResourceBundle resourceBundle;
    
    @Override
    public void initialize(URL location, ResourceBundle bundle) {
        this.resourceBundle = bundle;
    }
}
