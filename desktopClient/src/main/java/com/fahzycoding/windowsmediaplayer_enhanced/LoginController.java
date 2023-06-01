package com.fahzycoding.windowsmediaplayer_enhanced;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import com.fahzycoding.windowsmediaplayer_enhanced.ApiCommunicator;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginController implements Initializable {
    @FXML
    TextField usernameField;
    @FXML
    TextField passwordField;
    @FXML
    TextField registerUser;
    @FXML
    PasswordField registerPw;
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Notifications notifications;
    private ApiCommunicator client;
    private Properties properties;
    private String authToken;
    private Stage currentStage;

    public void setCurrentStage(Stage stage){
        this.currentStage = stage;
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("desktopClient/src/configs.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return;
        }


        String SERVER_URL = properties.getProperty("server_url");
        client = new ApiCommunicator(SERVER_URL);

    }

    public void login(ActionEvent event) throws IOException {

        notifications = new Notifications((Stage) usernameField.getScene().getWindow());

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            notifications.showNotification("Please enter a username!");
        }else {

            Response res = client.loginUser(username, password);
//            ResponseBody resBody = res.body();
            System.out.println("body" + res.isSuccessful());
            if (res.isSuccessful()) {
                // Successful login

                // Clear the fields for the next login attempt
                usernameField.clear();
                passwordField.clear();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home.fxml"));
                root = loader.load();

                HomeController home_page = loader.getController();
                home_page.setAuthToken(authToken);
                home_page.setClient(client);
                home_page.setProperties(properties);

                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {
                System.out.println("Stats: "+res.isSuccessful());
//                ResponseBody responseBody = res.body();
//                String resString = resBody.string();

                System.out.println("Signup failed with status code: " + res.code());
                System.out.println("Error message: " + res.message());
                notifications.showNotification(res.message());
                res.body().close();
            }
        }

    }

    public void register(ActionEvent event) throws IOException {

        String username = registerUser.getText();
        String password = registerPw.getText();

//        authToken = client.registerUser(username, password);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home.fxml"));
        root = loader.load();

        HomeController home_page = loader.getController();
        home_page.setAuthToken(authToken);
        home_page.setClient(client);
        home_page.setProperties(properties);

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    public void setClient(ApiCommunicator client){
        this.client = client;
    }

    public void setProperties(Properties properties){
        this.properties = properties;
    }

    public void setNotifications(Notifications notifications){
        this.notifications = notifications;
    }
}
