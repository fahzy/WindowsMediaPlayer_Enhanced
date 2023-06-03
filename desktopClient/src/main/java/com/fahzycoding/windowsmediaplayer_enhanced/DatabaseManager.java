package com.fahzycoding.windowsmediaplayer_enhanced;
import javafx.application.Platform;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseManager {

    private String SERVER_URL;

    private Connection connection;

    public DatabaseManager(String url) {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("desktopClient/src/configs.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return;
        }


        setSERVER_URL(url);
    }

    public void createUsersTable() {

        try {
//            connection = DriverManager.getConnection(SERVER_URL);
            Statement statement = connection.createStatement();

            String query = "CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, " +
                    "password TEXT NOT NULL)";
            statement.executeUpdate(query);

//            statement.close();
//            connection.close();
//            System.out.println("Created table");
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        }
    }

    public void createUser(String username, String password) {
        try {
//            connection = DriverManager.getConnection(SERVER_URL);

            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();

//            statement.close();
//            connection.close();
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
    }

    public void createMusicLibraryTable() {
        try {
//            login();
            Statement statement = connection.createStatement();

            String query = "CREATE TABLE IF NOT EXISTS music_library (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "added_by TEXT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "directory TEXT NOT NULL)";
            statement.executeUpdate(query);

//            statement.close();
        } catch (SQLException e) {
            System.err.println("Error creating music library table: " + e.getMessage());
        }
    }

    public boolean removeMediaFile(String title, String username, String password) {
        try {
            authenticateUser(username, password);
            String query = "DELETE FROM music_library WHERE title = ? AND added_by = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, title);
            statement.setString(2, username);
            statement.executeUpdate();

            System.out.println("Media file removed: " + title);

//            statement.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Error removing media file: " + e.getMessage());
            return false;
        }
    }

    public boolean insertMediaFile(String addedBy, String title,String fileDirectory, String password) {
        try {
            authenticateUser(addedBy, password);
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO music_library (added_by, title, directory) " +
                            "VALUES (?, ?, ?)");

            statement.setString(1, addedBy);
            statement.setString(2, title);
            statement.setString(3, fileDirectory);

            statement.executeUpdate();
//            statement.close();
            System.out.println("Media file inserted successfully");
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting media file: " + e.getMessage());
            return false;
        }
    }

    public MediaInfo fetchMediaFileByTitle(String title, String username, String password) {
        try {
            authenticateUser(username, password);
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM music_library WHERE title = ? AND added_by = ?");

            statement.setString(1, title);
            statement.setString(2, username);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String addedBy = resultSet.getString("added_by");
                String fileDirectory = resultSet.getString("directory");

                MediaInfo item = new MediaInfo();
                item.setDirectory(fileDirectory);
                item.setOnDevice(true);
                item.setFilename(title);
                item.setUserId(username);
                item.setBackedUp(false);

                System.out.println("Media file found:");
                System.out.println("ID: " + id);
                System.out.println("Added By: " + addedBy);
                System.out.println("Title: " + title);
                System.out.println("File Directory: " + fileDirectory);
//                statement.close();
                return item;

            } else {
                System.out.println("Media file not found in the database");
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching media file: " + e.getMessage());
            return null;
        }
    }

    public List<MediaInfo> fetchAllMedia(String  username, String password){
        try {
            authenticateUser(username, password);
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM music_library WHERE added_by = ?");

            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            List<MediaInfo> mediaFiles = new ArrayList<MediaInfo>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String addedBy = resultSet.getString("added_by");
                String fileDirectory = resultSet.getString("directory");
                String title = resultSet.getString("title");

                MediaInfo item = new MediaInfo();
                item.setDirectory(fileDirectory);
                item.setOnDevice(true);
                item.setFilename(title);
                item.setUserId(username);
                item.setBackedUp(false);

                mediaFiles.add(item);
            }
//            statement.close();
            return mediaFiles;

        } catch (SQLException e) {
            System.err.println("Error fetching media file: " + e.getMessage());
            return null;
        }
    }

    public void login(){
        try{
            connection = DriverManager.getConnection(SERVER_URL);
            System.out.println("Logged in");
        } catch (SQLException e) {
            System.err.println("Error Logging in" + e.getMessage());
        }
    }
    public void logout() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Logged out");
            }
        } catch (SQLException e) {
            System.err.println("Error logging out: " + e.getMessage());
        }
    }

    private boolean authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {

                if(resultSet.getInt(1) == 1){
//                    statement.close();
                    return true;
                }
                return false;
            }
        }
    }
    public Connection getConnection() {
        return connection;
    }
    public void setSERVER_URL(String SERVER_URL) {
        this.SERVER_URL = SERVER_URL;
    }

//    public static void main(String[] args) {
//        DatabaseManager manager = new DatabaseManager();
//        manager.createUsersTable();
//        manager.createMusicLibraryTable();
//        manager.logout();
//    }
}

