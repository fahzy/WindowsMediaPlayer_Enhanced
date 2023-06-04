import com.fahzycoding.windowsmediaplayer_enhanced.ApiCommunicator;
import okhttp3.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApiCommunicatorTest {

    private static final String BASE_URL = "https://example.com/api";
    private static final String AUTH_TOKEN = "auth_token";
    private static final String USERNAME = "test_user";
    private static final String PASSWORD = "test_password";
    private static final String MEDIA_ID = "12345";

    private ApiCommunicator apiCommunicator;

    @Mock
    private OkHttpClient httpClient;
    @Mock
    private Call mockedCall;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        apiCommunicator = new ApiCommunicator(BASE_URL);
        apiCommunicator.setHttpClient(httpClient);
    }

    @Test
    public void testLoginUser() throws IOException {
        // Prepare
        String expectedEndpointUrl = BASE_URL + "/login";
        String expectedPayload = "username=" + USERNAME + "&password=" + PASSWORD;
        Response mockedResponse = mock(Response.class);
        when(httpClient.newCall(any())).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);
        when(mockedResponse.isSuccessful()).thenReturn(true);
        when(mockedResponse.body()).thenReturn(mock(ResponseBody.class));

        // Execute
        Response response = apiCommunicator.loginUser(USERNAME, PASSWORD);

        // Verify
        verify(httpClient).newCall(argThat(request -> {
            assertEquals(expectedEndpointUrl, request.url().toString());

//            assertEquals(expectedPayload, request.body().toString());
            return true;
        }));
        verify(mockedCall).execute();
        assertEquals(mockedResponse, response);
    }

    @Test
    public void testRegisterUser() throws IOException {
        // Prepare
        String expectedEndpointUrl = BASE_URL + "/register";
        String expectedPayload = "username=" + USERNAME + "&password=" + PASSWORD + "&role=wmpUsers";
        Response mockedResponse = mock(Response.class);
        when(httpClient.newCall(any())).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);
        when(mockedResponse.isSuccessful()).thenReturn(true);
        when(mockedResponse.body()).thenReturn(mock(ResponseBody.class));

        // Execute
        Response response = apiCommunicator.registerUser(USERNAME, PASSWORD);

        // Verify
        verify(httpClient).newCall(argThat(request -> {
            assertEquals(expectedEndpointUrl, request.url().toString());
//            assertEquals(expectedPayload, request.body().toString());
            return true;
        }));
        verify(mockedCall).execute();
        assertEquals(mockedResponse, response);
    }

    @Test
    public void testUploadMedia() throws IOException {
        // Prepare
        String expectedEndpointUrl = BASE_URL + "/upload";
        File file = new File("C:\\Users\\lenovo\\Documents\\University\\Honours\\COS730\\A3\\WindowsMediaPlayer_Enhanced\\desktopClient\\src\\main\\resources\\music\\Athon by Hilton Wright II - Unminus.mp3"); // Replace with the actual file path
        Response mockedResponse = mock(Response.class);
        when(httpClient.newCall(any())).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);
        when(mockedResponse.isSuccessful()).thenReturn(true);
        when(mockedResponse.body()).thenReturn(mock(ResponseBody.class));

        // Execute
        Response response = apiCommunicator.uploadMedia(AUTH_TOKEN, file);

        // Verify
        verify(httpClient).newCall(argThat(request -> {
            assertEquals(expectedEndpointUrl, request.url().toString());
            assertEquals(AUTH_TOKEN, request.header("Authorization"));
            return true;
        }));
        verify(mockedCall).execute();
        assertEquals(mockedResponse, response);
    }


    @Test
    public void testGetMedia() throws IOException {
        // Prepare
        String expectedEndpointUrl = BASE_URL + "/media/" + MEDIA_ID;
        Response mockedResponse = mock(Response.class);
        when(httpClient.newCall(any())).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);
        when(mockedResponse.isSuccessful()).thenReturn(true);
        when(mockedResponse.body()).thenReturn(mock(ResponseBody.class));

        // Execute
        Response response = apiCommunicator.getMedia(AUTH_TOKEN, MEDIA_ID);

        // Verify
        verify(httpClient).newCall(argThat(request -> {
            assertEquals(expectedEndpointUrl, request.url().toString());
            assertEquals(AUTH_TOKEN, request.header("Authorization"));
            return true;
        }));
        verify(mockedCall).execute();
        assertEquals(mockedResponse, response);
    }

    @Test
    public void testGetAllMedia() throws IOException {
        // Prepare
        String expectedEndpointUrl = BASE_URL + "/media";
        Response mockedResponse = mock(Response.class);
        when(httpClient.newCall(any())).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);
        when(mockedResponse.isSuccessful()).thenReturn(true);
        when(mockedResponse.body()).thenReturn(mock(ResponseBody.class));

        // Execute
        Response response = apiCommunicator.getAllMedia(AUTH_TOKEN);

        // Verify
        verify(httpClient).newCall(argThat(request -> {
            assertEquals(expectedEndpointUrl, request.url().toString());
            assertEquals(AUTH_TOKEN, request.header("Authorization"));
            return true;
        }));
        verify(mockedCall).execute();
        assertEquals(mockedResponse, response);
    }

    @Test
    public void testDeleteMedia() throws IOException {
        // Prepare
        String expectedEndpointUrl = BASE_URL + "/media/" + MEDIA_ID;
        Response mockedResponse = mock(Response.class);
        when(httpClient.newCall(any())).thenReturn(mockedCall);
        when(mockedCall.execute()).thenReturn(mockedResponse);
        when(mockedResponse.isSuccessful()).thenReturn(true);
        when(mockedResponse.body()).thenReturn(mock(ResponseBody.class));

        // Execute
        Response response = apiCommunicator.deleteMedia(AUTH_TOKEN, MEDIA_ID);

        // Verify
        verify(httpClient).newCall(argThat(request -> {
            assertEquals(expectedEndpointUrl, request.url().toString());
            assertEquals(AUTH_TOKEN, request.header("Authorization"));
            return true;
        }));
        verify(mockedCall).execute();
        assertEquals(mockedResponse, response);
    }

}
