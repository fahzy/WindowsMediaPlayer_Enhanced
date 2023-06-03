import com.fahzycoding.windowsmediaplayer_enhanced.DatabaseManager;
import com.fahzycoding.windowsmediaplayer_enhanced.MediaInfo;
import javafx.application.Platform;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    private static final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final PrintStream originalOutput = System.out;

    private Properties properties;
    private static DatabaseManager manager;

    @BeforeAll
    public static void setUpClass() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("desktopClient/src/configs.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return;
        }

        manager = new DatabaseManager(properties.getProperty("internal_db_test"));
        manager.setSERVER_URL( properties.getProperty("internal_db_test"));
        manager.login();
//        manager.makeConnection();
        System.setOut(new PrintStream(output));

        // Create a user for testing
//        manager.createUsersTable();
//        manager.createMusicLibraryTable();
//        manager.createUser("john", "password123");
    }

    @AfterAll
    public static void erasure(){
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("desktopClient/src/configs.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
            System.exit(1);
            return;
        }


        // Create a File object representing the database file
        File databaseFile = new File(properties.getProperty("db_test_location"));

        // Check if the file exists
        if (databaseFile.exists()) {
            // Delete the file
            boolean deleted = databaseFile.delete();

            if (deleted) {
                System.out.println("Database file deleted successfully.");
            } else {
                System.out.println("Failed to delete the database file.");
            }
        } else {
            System.out.println("The database file does not exist.");
        }
    }


    @AfterEach
    public void tearDown() {
        deleteTables();
        System.setOut(originalOutput);
    }

    @Test
    public void testCreateUsersTable() throws SQLException {
//        manager.login();
        manager.createUsersTable();

        // Assert that the table exists in the database
        assertTrue(tableExists("users"));
    }

    @Test
    public void testCreateUser() throws SQLException {
        manager.createUsersTable();
        manager.createUser("mary", "pass123");
        // Assert that the user exists in the database
        assertTrue(userExists("mary"));
    }

    @Test
    public void testCreateMusicLibraryTable() throws SQLException {
//        manager.login();
        manager.createMusicLibraryTable();
        // Assert that the table exists in the database
        assertTrue(tableExists("music_library"));
    }

    @Test
    public void testRemoveMediaFile() {
        // Insert a media file for testing
        manager.createMusicLibraryTable();
        manager.insertMediaFile("john", "Song Title", "/path/to/file", "password123");

        // Remove the media file
        assertTrue(manager.removeMediaFile("Song Title", "john", "password123"));
    }

    @Test
    public void testFetchMediaFileByTitle_ExistingTitle() {
        // Insert a media file for testing
//        manager.login();
        manager.createMusicLibraryTable();
        manager.insertMediaFile("john", "Song Title", "/path/to/file",  "password123");

        // Fetch the media file
        MediaInfo mediaInfo = manager.fetchMediaFileByTitle("Song Title", "john", "password123");
//        System.out.println(mediaInfo.getUserId());
        assertNotNull(mediaInfo);
        assertEquals("Song Title", mediaInfo.getFilename());
    }

    @Test
    public void testFetchMediaFileByTitle_NonExistingTitle() {
        // Fetch a non-existing media file
        manager.createMusicLibraryTable();
        MediaInfo mediaInfo = manager.fetchMediaFileByTitle("Non-existing Title", "john", "password123");
        assertNull(mediaInfo);
    }

    @Test
    public void testFetchAllMedia() {
        // Insert multiple media files for testing
        manager.createMusicLibraryTable();
        manager.insertMediaFile("john", "Song 1",  "/path/to/file1", "password123");
        manager.insertMediaFile("john", "Song 2",  "/path/to/file2", "password123");
        manager.insertMediaFile("john", "Song 3", "/path/to/file3", "password123");

        // Fetch all media files for the user
        List<MediaInfo> mediaFiles = manager.fetchAllMedia("john", "password123");
        assertNotNull(mediaFiles);
        assertEquals(3, mediaFiles.size());
    }

    private boolean tableExists(String tableName) throws SQLException {
//        manager.login();
        DatabaseMetaData metadata = manager.getConnection().getMetaData();
        ResultSet resultSet = metadata.getTables(null, null, tableName, null);
        return resultSet.next();
    }

    private boolean userExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement statement = manager.getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {

                return resultSet.getInt(1) == 1;
            }
        }
    }

    private void deleteTables() {
        try {
            Connection connection1 = manager.getConnection();
            Statement statement = connection1.createStatement();

            // Delete the entries from the tables
            String deleteEntriesQuery1 = "DELETE FROM users;";
            String deleteEntriesQuery2 = "DELETE FROM music_library;";

            statement.executeUpdate(deleteEntriesQuery1);
            statement.executeUpdate(deleteEntriesQuery2);
//            statement.close();
            Statement statement1 = connection1.createStatement();
            // Drop the tables
            String deleteTable1Query = "DROP TABLE IF EXISTS users;";
            String deleteTable2Query = "DROP TABLE IF EXISTS music_library;";

            statement1.executeUpdate(deleteTable1Query);
            statement1.executeUpdate(deleteTable2Query);

//            statement.close();
//            connection1.close();
            System.out.println("Tables deleted successfully.");
        } catch (SQLException e) {
            System.err.println("Error deleting tables: " + e.getMessage());
        }
    }

}
