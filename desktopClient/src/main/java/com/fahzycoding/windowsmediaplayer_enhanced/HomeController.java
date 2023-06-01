package com.fahzycoding.windowsmediaplayer_enhanced;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static java.lang.Math.floor;
//import static jdk.internal.logger.DefaultLoggerFinder.SharedLoggers.system;

public class HomeController implements Initializable {
    @FXML
    Label songTitle, currentDuration, endDuration;
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
    @FXML
    private ImageView imageView;
    @FXML
    private MenuItem openFiles;
    @FXML
    private ListView<String> libraryList;
    private Media media;
    private MediaPlayer mediaPlayer;
    private File directory;
    private File[] files;
    private ArrayList<File> songs;
    private int songNumber = 0;
    private Timer timer;
    private TimerTask task;
    private boolean running = false;
    private ObservableList<String> mediaLibrary = FXCollections.observableArrayList();;
    Stage stage;
    private ApiCommunicator client;
    private Properties properties;

    private String authToken;

    public static MediaType getMediaType(String filePath) {
        File file = new File(filePath);
        String extension = getFileExtension(file);

        if (extension != null) {
            switch (extension.toLowerCase()) {
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                    return MediaType.IMAGE;
                case "mp4":
                case "mov":
                case "avi":
                case "mkv":
                    return MediaType.VIDEO;
                case "mp3":
                case "wav":
                case "ogg":
                case "flac":
                    return MediaType.AUDIO;
            }
        }

        try {
            Media media = new Media(file.toURI().toString());
            if (extension == null) {
                String mediaType = media.getMetadata().get("handler").toString();
                if (mediaType.contains("video")) {
                    return MediaType.VIDEO;
                } else if (mediaType.contains("audio")) {
                    return MediaType.AUDIO;
                }
            }
        } catch (MediaException e) {
            // Handle exception if the file is not a valid media file
        }

        return MediaType.UNKNOWN;
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < name.length() - 1) {
            return name.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
    }

    public enum MediaType {
        IMAGE,
        VIDEO,
        AUDIO,
        UNKNOWN
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        libraryList.setItems(mediaLibrary);
        libraryList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = libraryList.getSelectionModel().getSelectedItem();
                // Play the selected media (you can implement this logic)
                // For example, you can use JavaFX's MediaPlayer to play the media.
                mediaExecution(new Media(new File(selectedItem).toURI().toString()));
            }
        });

         songs = new ArrayList<File>();

        // Load first song
        songs.add(new File("C:\\Users\\lenovo\\Documents\\University\\Honours\\COS730\\A3\\WindowsMediaPlayer_Enhanced\\desktopClient\\src\\main\\resources\\music\\Athon by Hilton Wright II - Unminus.mp3"));
        media = new Media(songs.get(songNumber).toURI().toString());

        mediaExecution(media);
//        playpauseMedia();
        songTitle.setText(songs.get(songNumber).getName());
        songProgressBar.setStyle("-fx-accent: #cccccc");
        scenePane.setCenter(visualizer);
        scenePane.setCenter(imageView);


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

//        Stage primaryStagev1 = (Stage) scenePane.getScene().getWindow();
//        primaryStagev1.setX(0);
//        primaryStagev1.setY(0);
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
            this.authToken = "";
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
//                System.out.println(media.getMetadata().toString());
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
         media.getMetadata().addListener(new MapChangeListener<String, Object>() {
             @Override
             public void onChanged(Change<? extends String, ? extends Object> ch) {
                 if (ch.wasAdded()) {
                     handleMetadata(ch.getKey(), ch.getValueAdded());
                 }
             }

             private void handleMetadata(String key, Object value) {
                 System.out.println(key+" - "+ value);
                 if (key.equals("album")) {
                     System.out.println(value.toString());
                 } else if (key.equals("artist")) {
                     System.out.println(value.toString());
                 } if (key.equals("title")) {
                     System.out.println(value.toString());
                 } if (key.equals("year")) {
                     System.out.println(value.toString());
                 } if (key.equals("image")) {
//                     visualizer.setVisible(false);
                     imageView.setImage((Image)value);
                 }
             }
         });
         visualizer.setMediaPlayer((mediaPlayer));
         playpauseMedia();
     }

    private void showImage(Object ob) {
        imageView.setImage((Image)ob);
        imageView.setVisible(true);
    }

    private void hideImage() {
        imageView.setVisible(false);
    }

    private void showMedia(MediaPlayer mp){
        visualizer.setMediaPlayer(mp);
        visualizer.setVisible(true);
        scenePane.setCenter(visualizer);
    }

    private void hideMedia(MediaPlayer mp){
        visualizer.setVisible(false);
    }

    public void setClient(ApiCommunicator client){
        this.client = client;
    }
    public void setProperties(Properties properties){
        this.properties = properties;
    }
    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }

    public void handleLoadMedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Media Files", "*.mp3", "*.mp4", "*.jpg", "*.png"),
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3"),
                new FileChooser.ExtensionFilter("Video Files", "*.mp4"),
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png")
        );

        fileChooser.setTitle("Select Media File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedFile = fileChooser.showOpenDialog(libraryList.getScene().getWindow());
        if (selectedFile != null) {
            // Save the selected file to the library
            // You can implement your own logic here to handle saving the file
            // For example, you can copy the file to a specific directory
            // or store its path in a database.
//            selectedFile.getAbsolutePath();
            System.out.println(selectedFile.getAbsolutePath());
            mediaLibrary.add(selectedFile.getAbsolutePath());
        }
    }

}