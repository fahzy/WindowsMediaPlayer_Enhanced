package com.fahzycoding.windowsmediaplayer_enhanced;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class HomeController implements Initializable {
    @FXML
    Label songTitle;
    @FXML
    private AnchorPane scenePane;
    @FXML
    private Button playBtn, nextBtn, previousBtn;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgress;
    private Media media;
    private MediaPlayer mediaPlayer;
    private File directory;
    private File[] files;
    private ArrayList<File> songs;
    private int songNumber = 0;
    private Timer timer;
    private TimerTask task;
    private boolean running = false;
    Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        songs = new ArrayList<File>();
        directory = new File("./desktopClient/src/main/resources/music");
        files = directory.listFiles();

        if (files != null){
            for(File file: files){
                songs.add(file);
//                System.out.println(file);
            }
        }
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        songTitle.setText(songs.get(songNumber).getName());
    }

    public void logout(ActionEvent event){

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(("You are about to logout!"));
        alert.setContentText("Do you want to save before exiting?: ");

        if(alert.showAndWait().get() == ButtonType.OK) {

            stage = (Stage) scenePane.getScene().getWindow();
            System.out.println("You successfully logged out");
            stage.close();
        }
    }
     public void playpauseMedia(){
        if(!running) {
            mediaPlayer.play();
            running = true;
        }
        else {
            mediaPlayer.pause();
            running = false;
        }
     }

     public void prevMedia(){
        if(mediaPlayer.getCurrentTime().lessThan(Duration.seconds(1))){
            if (songNumber > 0) {songNumber--;}
            else{songNumber = songs.size() - 1; }

                mediaPlayer.stop();
                running = false;
                media = new Media(songs.get(songNumber).toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                songTitle.setText(songs.get(songNumber).getName());
                mediaPlayer.play();
                running = true;
        }else mediaPlayer.seek(Duration.seconds(0) );
     }

     public void nextMedia(){
        if (songNumber < songs.size() - 1) {songNumber++;}
        else{songNumber = 0; }

         mediaPlayer.stop();
         running = false;
         media = new Media(songs.get(songNumber).toURI().toString());
         mediaPlayer = new MediaPlayer(media);
         songTitle.setText(songs.get(songNumber).getName());
         mediaPlayer.play();
         running = true;
     }

     public void beginTimer(){

     }

     public void cancelTimer(){

     }
}