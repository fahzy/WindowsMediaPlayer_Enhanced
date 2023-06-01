import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.fahzycoding.windowsmediaplayer_enhanced.ApiCommunicator;
public class ApiCommunicatorTest {

    @Test
    public void testLoginUser() throws IOException {
        // Mock the OkHttpClient
        OkHttpClient httpClient = mock(OkHttpClient.class);
        when(httpClient.newCall(any())).thenReturn(Mockito.mock(Call.class));
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://example.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "success"))
                .build();
        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(httpClient.newCall(any())).thenReturn(mockCall);

        // Create the NodeJsApiCommunicator with the mocked OkHttpClient
        ApiCommunicator apiCommunicator = new ApiCommunicator("http://example.com");
        apiCommunicator.setHttpClient(httpClient);

        // Test the loginUser method
        Response response = apiCommunicator.loginUser("username", "password");

        // Verify the HTTP request and assert the response
        verify(httpClient, times(1)).newCall(any());
        assertEquals("success", response);
    }

    @Test
    public void testRegisterUser() throws IOException {
        // Mock the OkHttpClient
        OkHttpClient httpClient = mock(OkHttpClient.class);
        when(httpClient.newCall(any())).thenReturn(Mockito.mock(Call.class));
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://example.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "success"))
                .build();
        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(httpClient.newCall(any())).thenReturn(mockCall);

        // Create the ApiCommunicator with the mocked OkHttpClient
        ApiCommunicator apiCommunicator = new ApiCommunicator("http://example.com");
        apiCommunicator.setHttpClient(httpClient);

        // Test the registerUser method
        Response response = apiCommunicator.registerUser("username", "password");

        // Verify the HTTP request and assert the response
        verify(httpClient, times(1)).newCall(any());
        assertEquals("success", response);
    }

    @Test
    public void testUploadMedia() throws IOException {
        // Mock the OkHttpClient
        OkHttpClient httpClient = mock(OkHttpClient.class);
        when(httpClient.newCall(any())).thenReturn(Mockito.mock(Call.class));
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://example.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "success"))
                .build();
        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(httpClient.newCall(any())).thenReturn(mockCall);

        // Create the ApiCommunicator with the mocked OkHttpClient
        ApiCommunicator apiCommunicator = new ApiCommunicator("http://example.com");
        apiCommunicator.setHttpClient(httpClient);

        // Test the uploadMedia method
        String authToken = "token123";
        File file = new File("path/to/file");
        Response response = apiCommunicator.uploadMedia(authToken, file);

        // Verify the HTTP request and assert the response
        verify(httpClient, times(1)).newCall(any());
        assertEquals("success", response);
    }

    @Test
    public void testGetMedia() throws IOException {
        // Mock the OkHttpClient
        OkHttpClient httpClient = mock(OkHttpClient.class);
        when(httpClient.newCall(any())).thenReturn(Mockito.mock(Call.class));
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://example.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "success"))
                .build();
        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(httpClient.newCall(any())).thenReturn(mockCall);

        // Create the ApiCommunicator with the mocked OkHttpClient
        ApiCommunicator apiCommunicator = new ApiCommunicator("http://example.com");
        apiCommunicator.setHttpClient(httpClient);

        // Test the getMedia method
        String authToken = "token123";
        String mediaId = "123";
        Response response = apiCommunicator.getMedia(authToken, mediaId);

        // Verify the HTTP request and assert the response
        verify(httpClient, times(1)).newCall(any());
        assertEquals("success", response);
    }

    @Test
    public void testGetAllMedia() throws IOException {
        // Mock the OkHttpClient
        OkHttpClient httpClient = mock(OkHttpClient.class);
        when(httpClient.newCall(any())).thenReturn(Mockito.mock(Call.class));
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://example.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "success"))
                .build();
        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);
        // Create the ApiCommunicator with the mocked OkHttpClient
        ApiCommunicator apiCommunicator = new ApiCommunicator("http://example.com");
        apiCommunicator.setHttpClient(httpClient);

        // Test the getMedia method
        String authToken = "token123";
        String mediaId = "123";
        Response response = apiCommunicator.getMedia(authToken, mediaId);

        // Verify the HTTP request and assert the response
        verify(httpClient, times(1)).newCall(any());
        assertEquals("success", response);
    }
}
