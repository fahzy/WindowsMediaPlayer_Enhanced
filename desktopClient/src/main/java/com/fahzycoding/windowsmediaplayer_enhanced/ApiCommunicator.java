package com.fahzycoding.windowsmediaplayer_enhanced;

import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class ApiCommunicator {
    private final String baseUrl;
    private OkHttpClient httpClient;

    public ApiCommunicator(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient();
    }

    public String loginUser(String username, String password) throws IOException {
        String endpointUrl = baseUrl + "/login";
        String payload = "username=" + username + "&password=" + password;
        return sendHttpPostRequest(endpointUrl, payload);
    }

    public String registerUser(String username, String password) throws IOException {
        String endpointUrl = baseUrl + "/register";
        String payload = "username=" + username + "&password=" + password +"&role=wmpUsers";
        return sendHttpPostRequest(endpointUrl, payload);
    }

    public String uploadMedia(String authToken, File file) throws IOException {
        String endpointUrl = baseUrl + "/upload";
        return sendHttpPostRequestWithFile(endpointUrl, authToken, file);
    }

    public String getMedia(String authToken, String mediaId) throws IOException {
        String endpointUrl = baseUrl + "/media/" + mediaId;
        return sendHttpGetRequest(endpointUrl, authToken);
    }

    public String getAllMedia(String authToken) throws IOException {
        String endpointUrl = baseUrl + "/media";
        return sendHttpGetRequest(endpointUrl, authToken);
    }

    public String deleteMedia(String authToken, String mediaId) throws IOException {
        String endpointUrl = baseUrl + "/media/" + mediaId;
        return sendHttpDeleteRequest(endpointUrl, authToken);
    }

    public String streamMedia(String authToken, String mediaId) throws IOException {
        String endpointUrl = baseUrl + "/media/stream/" + mediaId;
        return sendHttpGetRequest(endpointUrl, authToken);
    }

    private String sendHttpPostRequest(String endpointUrl, String payload) throws IOException {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), payload);
        Request request = new Request.Builder()
                .url(endpointUrl)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private String sendHttpGetRequest(String endpointUrl, String authToken) throws IOException {
        Request request = new Request.Builder()
                .url(endpointUrl)
                .header("Authorization", authToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private String sendHttpDeleteRequest(String endpointUrl, String authToken) throws IOException {
        Request request = new Request.Builder()
                .url(endpointUrl)
                .header("Authorization", authToken)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private String sendHttpPostRequestWithFile(String endpointUrl, String authToken, File file) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        Request request = new Request.Builder()
                .url(endpointUrl)
                .header("Authorization",  authToken)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public void setHttpClient(OkHttpClient client){
        this.httpClient = client;
    }

}
