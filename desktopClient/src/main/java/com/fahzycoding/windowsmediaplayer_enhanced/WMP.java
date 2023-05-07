package com.fahzycoding.windowsmediaplayer_enhanced;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class WMP extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("login_page.fxml"));
        Scene scene = new Scene(root);

        Image icon = new Image("C:\\Users\\lenovo\\Documents\\University\\Honours\\COS730\\A3\\WindowsMediaPlayer_Enhanced\\desktopClient\\src\\assets\\images\\WMP_logo.png");
        stage.getIcons().add(icon);
        stage.setTitle("Windows Media Player (Enhanced)");
//        stage.setFullScreen(true);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}