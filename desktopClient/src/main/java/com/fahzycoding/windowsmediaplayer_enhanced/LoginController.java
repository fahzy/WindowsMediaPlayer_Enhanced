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

    private ApiCommunicator client;
    private Properties properties;
    private String authToken;
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

        String username = usernameField.getText();
        String password = passwordField.getText();

        authToken = client.loginUser(username, password);

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

    public void register(ActionEvent event) throws IOException {

        String username = registerUser.getText();
        String password = registerPw.getText();

        authToken = client.registerUser(username, password);

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
}
