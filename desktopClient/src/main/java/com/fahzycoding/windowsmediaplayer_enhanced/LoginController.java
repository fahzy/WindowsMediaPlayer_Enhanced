package com.fahzycoding.windowsmediaplayer_enhanced;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
//import com.fasterxml.jackson.annotation.JsonValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


import com.fahzycoding.windowsmediaplayer_enhanced.ApiCommunicator;
import com.fahzycoding.windowsmediaplayer_enhanced.DatabaseManager;
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
    private DatabaseManager db_manager;
    private Properties properties;
    private String authToken;
    private Stage currentStage;
    private String SECRET_KEY;
    private String SERVER_URL;
    private String DB_LOCATION;

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


        SERVER_URL = properties.getProperty("server_url");
        SECRET_KEY = properties.getProperty("secret_key");
        DB_LOCATION = properties.getProperty("internal_db");
//        System.out.println(DB_LOCATION);
        client = new ApiCommunicator(SERVER_URL);
        db_manager = new DatabaseManager(DB_LOCATION);
    }

    public void login(ActionEvent event) throws IOException {

        notifications = new Notifications((Stage) usernameField.getScene().getWindow());

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            notifications.showNotification("Please enter a username!");
        }else {

            Response res = client.loginUser(username, password);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(res.body().charStream(), JsonObject.class);
            if (res.isSuccessful()) {
                // Successful login

                // Clear the fields for the next login attempt
                usernameField.clear();
                passwordField.clear();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home.fxml"));
                root = loader.load();


                HomeController home_page = loader.getController();

                db_manager.login();
                authToken = jsonObject.get("token").getAsString();

                // Decode the JWT
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                        .build()
                        .verify(authToken);

                Object userObject = jwt.getClaim("user").as(Object.class);
                String jsonString = userObject.toString();

                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(jsonString);
                JsonObject jsonObject2 = jsonElement.getAsJsonObject();

                home_page.setUsername(jsonObject2.get("username").getAsString());
                home_page.setPassword(jsonObject2.get("id").getAsString());
                home_page.setAuthToken(authToken);
                home_page.setClient(client);
                home_page.setProperties(properties);
                home_page.setDB_manager(db_manager);
                home_page.setNotifications(notifications);

                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {

                usernameField.clear();
                passwordField.clear();

                String errorMess = jsonObject.get("message").getAsString();
                System.out.println("Signup failed with status code: " + res.code());
                System.out.println("Error message: " + errorMess);
                notifications.showNotification(errorMess);
            }
        }

    }

    public void register(ActionEvent event) throws IOException {

        notifications = new Notifications((Stage) usernameField.getScene().getWindow());

        String username = registerUser.getText();
        String password = registerPw.getText();

        if (username.isEmpty()) {
            notifications.showNotification("Please enter a username!");
        }else if(password.isEmpty()){
                notifications.showNotification("Please enter a password!");
        }else {

            Response res = client.registerUser(username, password);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(res.body().charStream(), JsonObject.class);
            if (res.isSuccessful()) {
                usernameField.clear();
                passwordField.clear();

//        authToken = client.registerUser(username, password);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/home.fxml"));
                root = loader.load();

                HomeController home_page = loader.getController();

                authToken = jsonObject.get("token").getAsString();
                System.out.println(authToken);

                // Decode the JWT
                DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                        .build()
                        .verify(authToken);

                Object userObject = jwt.getClaim("user").as(Object.class);
                String jsonString = userObject.toString();

                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement = jsonParser.parse(jsonString);
                JsonObject jsonObject2 = jsonElement.getAsJsonObject();

                String un = jsonObject2.get("username").getAsString();
                String pw = jsonObject2.get("id").getAsString();

                home_page.setUsername(un);
                home_page.setPassword(pw);

                home_page.setAuthToken(authToken);
                home_page.setClient(client);
                home_page.setProperties(properties);
                home_page.setDB_manager(db_manager);
                home_page.setNotifications(notifications);

                db_manager.login();
                db_manager.createUser(un, pw);

                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }else{
                usernameField.clear();
                passwordField.clear();

                String errorMess = jsonObject.get("message").getAsString();
                System.out.println("Signup failed with status code: " + res.code());
                System.out.println("Error message: " + errorMess);
                notifications.showNotification(errorMess);
            }
            res.close();
        }
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
