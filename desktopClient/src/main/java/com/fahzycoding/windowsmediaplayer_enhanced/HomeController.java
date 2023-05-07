package com.fahzycoding.windowsmediaplayer_enhanced;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {
    @FXML
    Label nameLabel;

    public void displayName(String username){
        nameLabel.setText("Hello: " +username);
    }

}