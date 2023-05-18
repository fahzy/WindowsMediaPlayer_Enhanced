package com.fahzycoding.windowsmediaplayer_enhanced;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.floor;
//import static jdk.internal.logger.DefaultLoggerFinder.SharedLoggers.system;

public class HomeController implements Initializable {
    @FXML
    Label songTitle, currentDuration, endDuration;
    @FXML
    private BorderPane views6;
    @FXML
    private BorderPane scenePane;
    @FXML
    private Button playBtn, nextBtn, previousBtn;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;
    @FXML
    private MediaView visualizer;
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

        // Load first song
        mediaExecution(media);
        songTitle.setText(songs.get(songNumber).getName());
        songProgressBar.setStyle("-fx-accent: #cccccc");
        scenePane.setCenter(visualizer);


        // Set the volume property
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            }
        });

//      Media Seeking functionality of the media player
        songProgressBar.setOnMousePressed(event -> {
            double position = event.getX() / songProgressBar.getWidth();
            mediaPlayer.seek(mediaPlayer.getMedia().getDuration().multiply(position));
        });

        // Duration appearing in media player
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                if (!songProgressBar.isPressed()) {
                    songProgressBar.setProgress(newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
                }
            }
        });
    }

    public void logout(ActionEvent event) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText(("You are about to logout!"));
        alert.setContentText("Do you want to save before exiting?: ");


        if(alert.showAndWait().get() == ButtonType.OK) {
            // Logging out of the main application
            stage = (Stage) scenePane.getScene().getWindow();
            System.out.println("You successfully logged out");
            // TODO: Remove credentials from the application
            // Closes the main application
            stage.close();

            // Going back to the login/sign up page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login_page.fxml"));
            Parent root = loader.load();
            // Creating a new stage for the login page
            Stage newStage = new Stage();
            Scene scene = new Scene(root);
            // Creating an icon for the window (appears in the top left and the icon in the task bar
            Image icon = new Image(getClass().getResource("/images/WMP_logo.png").openStream());
            stage.getIcons().add(icon);
            stage.setTitle("Windows Media Player (Enhanced)");

            newStage.setScene(scene);
            newStage.show();
        }
    }
     public void playpauseMedia(){
        mediaPlayer.setVolume(volumeSlider.getValue());
        if(!running) {
            mediaPlayer.play();
            beginTimer();
            running = true;
        }
        else {
            mediaPlayer.pause();
            cancelTimer();
//            running = false;
        }
     }

     public void prevMedia(){
        if(mediaPlayer.getCurrentTime().lessThan(Duration.seconds(1))){
            if (songNumber > 0) {songNumber--;}
            else{songNumber = songs.size() - 1; }

                mediaPlayer.stop();
                if (running){ timer.cancel();}
                media = new Media(songs.get(songNumber).toURI().toString());
                mediaExecution(media);
                songTitle.setText(songs.get(songNumber).getName());
                mediaPlayer.play();
                System.out.println(media.getMetadata().toString());
                beginTimer();
        }else {
            songProgressBar.setProgress(0);
            mediaPlayer.seek(Duration.seconds(0) );
        }
     }

     public void nextMedia(){
        if (songNumber < songs.size() - 1) {songNumber++;}
        else{songNumber = 0; }

         mediaPlayer.stop();
         if (running){ timer.cancel();}
         running = false;
         media = new Media(songs.get(songNumber).toURI().toString());
         mediaExecution(media);
         songTitle.setText(songs.get(songNumber).getName());
         mediaPlayer.play();
         System.out.println(media.getMetadata().toString());
         beginTimer();
     }

     public void beginTimer(){
        timer = new Timer();
        task = new TimerTask(){
            public void run(){
                Platform.runLater(()->{
                    running = true;
                    double current = mediaPlayer.getCurrentTime().toSeconds();
                    double end = media.getDuration().toSeconds();
                    songProgressBar.setProgress(current/end);
                    double secondsLeft = current % 60 ;
                    String currDur = Integer.toString((int)mediaPlayer.getCurrentTime().toMinutes())+ ":" + Integer.toString((int)(secondsLeft));
                    String endDur = Integer.toString((int)media.getDuration().toMinutes())+ ":" + Integer.toString((int)(media.getDuration().toSeconds()%60));
//                    System.out.println((int)(media.getDuration().toSeconds()));
                    currentDuration.setText(currDur);
                    endDuration.setText(endDur);
                    if(current/end == 1){
                        cancelTimer();
                    }
                });

            }
        };

        timer.scheduleAtFixedRate(task, 1000, 1000);
     }

     public void cancelTimer(){
        running = false;
        timer.cancel();
     }

     public void muteMedia(){
        if(mediaPlayer.isMute()) mediaPlayer.setMute(false);
        else mediaPlayer.setMute(true);
//         mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
     }

     private void mediaExecution(Media media){
         mediaPlayer = new MediaPlayer(media);
         visualizer.setMediaPlayer((mediaPlayer));
     }

}