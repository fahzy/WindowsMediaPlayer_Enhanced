package com.fahzycoding.windowsmediaplayer_enhanced;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.parser.Parser;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.xml.sax.SAXException;

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
    private DatabaseManager db_manager;

    public Notifications getNotifications() {
        return notifications;
    }

    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    private Notifications notifications;
    private Properties properties;

    private String authToken;
    private String username;
    private String password;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println(authToken);
        libraryList.setItems(mediaLibrary);
        libraryList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = libraryList.getSelectionModel().getSelectedItem();
                String fileLocation = db_manager.fetchMediaFileByTitle(selectedItem, username,password).getDirectory();
                try {
                    mediaExecution(new Media(new File(fileLocation).toURI().toString()));
                } catch (TikaException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                }
            }
        });

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

        Platform.runLater(() -> {

            List<MediaInfo> users_lib = getDB_manager().fetchAllMedia(username, password);

            users_lib.forEach(item -> {
                addToLibrary(new File(item.getDirectory()));
            });

            if (mediaLibrary.size() == 0) {
                media = new Media(new File("C:\\Users\\lenovo\\Documents\\University\\Honours\\COS730\\A3\\WindowsMediaPlayer_Enhanced\\desktopClient\\src\\main\\resources\\music\\Athon by Hilton Wright II - Unminus.mp3").toURI().toString());
                addToLibrary(new File("C:\\Users\\lenovo\\Documents\\University\\Honours\\COS730\\A3\\WindowsMediaPlayer_Enhanced\\desktopClient\\src\\main\\resources\\music\\Athon by Hilton Wright II - Unminus.mp3"));
            }
            else {
//                media = new Media(new File(db_manager.fetchMediaFileByTitle(mediaLibrary.get(0).toString(),username, password).getDirectory()).toURI().toString());
//                addToLibrary(new File(db_manager.fetchMediaFileByTitle(mediaLibrary.get(0).toString(),username, password).getDirectory()));
            }
            try {
                mediaExecution(media);
            } catch (TikaException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
//            songTitle.setText(mediaLibrary.get(0));
            songProgressBar.setStyle("-fx-accent: #cccccc");
            scenePane.setCenter(visualizer);
            scenePane.setCenter(imageView);
            // Duration appearing in media player
            mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
                @Override
                public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                    if (!songProgressBar.isPressed()) {
                        songProgressBar.setProgress(newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
                    }
                }
            });

            ContextMenu contextMenu = new ContextMenu();

            MenuItem menuItem1 = new MenuItem("Upload");
            MenuItem menuItem2 = new MenuItem("Stream");
            MenuItem menuItem3 = new MenuItem("Download");

            // Assign event handlers to menu items
            menuItem1.setOnAction(e -> {
                // Perform action for Option 1
                MediaInfo item = db_manager.fetchMediaFileByTitle(libraryList.getSelectionModel().getSelectedItem(), username, password);
//                if (item == null){
//                    System.out.println("haai");
//                }
                try {
                    Response res = client.uploadMedia(authToken, new File(item.getDirectory()));
                    if(res.isSuccessful()){
                        String responseBody = res.body().string();

                        // Parse the response body string into a JSON object
                        Gson gson = new Gson();
                        JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        notifications.showNotification(jsonObject.get("message").getAsString());
                    }
                    else{
                        String responseBody = res.body().string();
                        System.out.println(responseBody);

                        // Parse the response body string into a JSON object
                        Gson gson = new Gson();
                        JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        notifications.showNotification(jsonObject.get("message").getAsString());
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

            menuItem2.setOnAction(e -> {
                // Perform action for Option 2

//                try {
//                    Response res = client.getAllMedia(authToken);
//                    Gson gson;
//                    if (res.isSuccessful()) {
//                        String responseBody = res.body().string();
//
//                        // Parse the response body string into a JSON object
//                        gson = new Gson();
//                        MediaClass[] mediaArray = gson.fromJson(responseBody, MediaClass[].class);
//
//                        // Process the media objects
//                        for (MediaClass media : mediaArray) {
//                            // Access the media properties
//                            String _id = media.get_id();
//                            String userId = media.getUserId();
//                            String filename = media.getFilename();
//                            String url2 = media.getUrl();
//                            if (filename == libraryList.getSelectionModel().getSelectedItem()) {
//                                Request req = client.streamMedia(authToken, _id);
//                                client.getHttpClient().newCall(req).enqueue(new Callback() {
//                                    @Override
//                                    public void onFailure(Call call, IOException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                    @Override
//                                    public void onResponse(Call call, Response response) throws IOException {
//                                        if (response.isSuccessful()) {
//                                            // Get the media content as a byte array
//                                            InputStream inputStream = response.body().byteStream();
//                                            FileOutputStream outputStream = new FileOutputStream("temp.mp4");
//                                            BufferedSink sink = Okio.buffer(Okio.sink(outputStream));
//                                            sink.writeAll(Okio.source(inputStream));
//                                            sink.close();
//
//                                            // Create a Media object from the byte array
//                                            Media media2 = new Media(new File("temp.mp4").toURI().toString());
//
//                                            // Create a MediaPlayer
//                                            try {
//                                                mediaExecution(media2);
//                                            } catch (TikaException ex) {
//                                                throw new RuntimeException(ex);
//                                            } catch (SAXException ex) {
//                                                throw new RuntimeException(ex);
//                                            }
//
//                                            // Create a MediaView to display the media
//                                            showMedia(mediaPlayer);
//
//                                            // Create a Scene with the MediaView
//
//
//                                            // Start playing the media
//                                            mediaPlayer.play();
//                                        } else {
//                                            System.out.println("Failed to retrieve media: " + response.code());
//                                        }
//                                    }
//                                });
//                            } else {
//                                notifications.showNotification("File not Found");
//                            }
//                        }
//                    }
//                    else{
//                        String responseBody = res.body().string();
//                        System.out.println(responseBody);
//
//                        // Parse the response body string into a JSON object
//                        gson = new Gson();
//                        JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);
//                        JsonObject jsonObject = jsonElement.getAsJsonObject();
//                        notifications.showNotification(jsonObject.get("message").getAsString());
//                    }
//                }catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
                notifications.showNotification("Feature is not available yet.");
                });

            menuItem3.setOnAction(e -> {
                // Perform action for Option 3
                Response res = null;
                try {
                    res = client.getAllMedia(authToken);

                Gson gson;
                    if (res.isSuccessful()) {
                        String responseBody = res.body().string();

                        // Parse the response body string into a JSON object
                        gson = new Gson();
                        MediaClass[] mediaArray = gson.fromJson(responseBody, MediaClass[].class);

                        // Process the media objects
                        for (MediaClass media : mediaArray) {
                            // Access the media properties
                            String _id = media.get_id();
                            String userId = media.getUserId();
                            String filename = media.getFilename();
                            String url2 = media.getUrl();
//                            System.out.println(_id);
//                            System.out.println(filename);
//                            System.out.println(libraryList.getSelectionModel().getSelectedItem());
                            if (filename.equals(libraryList.getSelectionModel().getSelectedItem())) {
//                                    System.out.println("ggg");
                                    res = client.getMedia(authToken, _id);
                                    if(res.isSuccessful()){
//                                        System.out.println("We major");
                                        String loc = chooseDownloadDirectory();
                                        ResponseBody resBody = res.body();

                                        InputStream inputStream = resBody.byteStream();
                                        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                                        FileOutputStream fileOutputStream = new FileOutputStream(loc+"/"+filename+".mp4");

                                        byte[] buffer = new byte[1024];
                                        int bytesRead;
                                        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                            fileOutputStream.write(buffer, 0, bytesRead);
                                        }

                                        fileOutputStream.flush();
                                        fileOutputStream.close();
                                        notifications.showNotification("File downloaded successfully.");
                                    }
                                    else{
                                        responseBody = res.body().string();
                                        System.out.println(responseBody);

                                        // Parse the response body string into a JSON object
                                        gson = new Gson();
                                        JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);
                                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                                        notifications.showNotification(jsonObject.get("message").getAsString());
                                    }
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
//                if (item == null){
//                    System.out.println("haai");
//                }
            });

            // Add menu items to the context menu
            contextMenu.getItems().addAll(menuItem1, menuItem2, menuItem3);

            // Set the context menu to the list items
            libraryList.setCellFactory(param -> {
                ListCell<String> cell = new TextFieldListCell<>();
                cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                    if (isEmpty) {
                        cell.setContextMenu(null);
                    } else {
                        cell.setContextMenu(contextMenu);
                    }
                });
                return cell;
            });
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
            getDB_manager().logout();
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

     public void prevMedia() throws TikaException, IOException, SAXException {
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

     public void nextMedia() throws TikaException, IOException, SAXException {
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

     private void mediaExecution(Media media) throws TikaException, IOException, SAXException {
        if(mediaPlayer != null) {
            if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                mediaPlayer.stop();
                hideImage();
//             mediaPlayer.
            }
        }
         mediaPlayer = new MediaPlayer(media);
         File file = new File(URLDecoder.decode( media.getSource().replace("file:/",""), StandardCharsets.UTF_8));
        if (media.getMetadata().get("title") == null){
            songTitle.setText(URLDecoder.decode( file.getName(), StandardCharsets.UTF_8));
        }
        else {
            songTitle.setText(URLDecoder.decode(media.getMetadata().get("title").toString(), StandardCharsets.UTF_8));
        }
         final Image[] albumArt = new Image[1];
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
                     albumArt[0] = (Image)value;
                     showImage(albumArt[0]);
                 }
             }
         });
         InputStream inputStream = new FileInputStream(file);
         Parser parser = new AutoDetectParser();
         Metadata metadata = new Metadata();
         ParseContext parseContext = new ParseContext();
         BodyContentHandler handler = new BodyContentHandler();

         parser.parse(inputStream, handler, metadata, parseContext);

         // Get all the metadata values
         String[] metadataNames = metadata.names();
         String type = metadata.get("Content-Type").toString();
//             System.out.println("MetaData");
//             for (String name : metadataNames) {
//                 System.out.println(name + ":-: " + metadata.get(name));
//             }
         if(type.contains("audio")){
             showImage(albumArt[0]);
//             hideMedia();
         }
         else if(type.contains("video")){
             showMedia(mediaPlayer);
             hideImage();
//         playpauseMedia();
         }
         else{
             showMedia(mediaPlayer);
             hideImage();
         }
     }

    private void showImage(Image ob) {
        imageView.setImage(ob);
        imageView.setVisible(true);
        scenePane.setCenter(imageView);
    }

    private void hideImage() {
        imageView.setVisible(false);
    }

    private void showMedia(MediaPlayer mp){
        visualizer.setMediaPlayer(mp);
        visualizer.setVisible(true);
        scenePane.setCenter(visualizer);
    }

    private void hideMedia(){
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

    public void handleLoadMedia() throws IOException, TikaException, SAXException {
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

//        System.out.println("Name: "+metadata.get);

        if (selectedFile != null) {
            // Save the selected file to the library
            // You can implement your own logic here to handle saving the file
            // For example, you can copy the file to a specific directory
            // or store its path in a database.
//            selectedFile.getAbsolutePath();
            System.out.println(selectedFile.getAbsolutePath());
            addToLibrary(selectedFile);
//            mediaLibrary.add(selectedFile.getAbsolutePath());
        }
    }

    public void addToLibrary(File file){
        media = new Media(file.toURI().toString());
        String title;
        if (media.getMetadata().get("title") == null){
            title = URLDecoder.decode( file.getName(), StandardCharsets.UTF_8);
        }
        else {
            title = media.getMetadata().get("title").toString();
        }
        if (db_manager.insertMediaFile(username, title, file.getAbsolutePath(), password)) {
            mediaLibrary.add(title);
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DatabaseManager getDB_manager() {
        return db_manager;
    }

    public void setDB_manager(DatabaseManager DB_manager) {
        this.db_manager = DB_manager;
    }

    private String chooseDownloadDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose download directory");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory != null) {
            // Perform the download and save the file in the selected directory
            System.out.println("Downloading to directory: " + selectedDirectory.getAbsolutePath());
            return selectedDirectory.getAbsolutePath();
        }
        return null;
    }
}