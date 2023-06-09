package com.fahzycoding.windowsmediaplayer_enhanced;

import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ApiCommunicator {
    private final String baseUrl;

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    private OkHttpClient httpClient;

    public ApiCommunicator(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient();
    }

    public Response loginUser(String username, String password) throws IOException {
        String endpointUrl = baseUrl + "/login";
        String payload = "username=" + username + "&password=" + password;
        return sendHttpPostRequest(endpointUrl, payload);
    }

    public Response registerUser(String username, String password) throws IOException {
        String endpointUrl = baseUrl + "/register";
        String payload = "username=" + username + "&password=" + password +"&role=wmpUsers";
        return sendHttpPostRequest(endpointUrl, payload);
    }

    public Response uploadMedia(String authToken, File file) throws IOException {
        String endpointUrl = baseUrl + "/upload";
        return sendHttpPostRequestWithFile(endpointUrl, authToken, file);
    }

    public Response getMedia(String authToken, String mediaId) throws IOException {
        String endpointUrl = baseUrl + "/media/" + mediaId;
        return sendHttpGetRequest(endpointUrl, authToken);
    }

    public Response getAllMedia(String authToken) throws IOException {
        String endpointUrl = baseUrl + "/media";
        return sendHttpGetRequest(endpointUrl, authToken);
    }

    public Response deleteMedia(String authToken, String mediaId) throws IOException {
        String endpointUrl = baseUrl + "/media/" + mediaId;
        return sendHttpDeleteRequest(endpointUrl, authToken);
    }

    public Request streamMedia(String authToken, String mediaId) throws IOException {
        String endpointUrl = baseUrl + "/stream/" + mediaId;
        return sendHttpGetRequestStream(endpointUrl, authToken);
    }

    private Response sendHttpPostRequest(String endpointUrl, String payload) throws IOException {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), payload);
        Request request = new Request.Builder()
                .url(endpointUrl)
                .post(requestBody)
                .build();

        Response response = httpClient.newCall(request).execute();
        return response;
    }

    private Response sendHttpGetRequest(String endpointUrl, String authToken) throws IOException {
        Request request = new Request.Builder()
                .url(endpointUrl)
                .header("Authorization", authToken)
                .get()
                .build();

        Response response = httpClient.newCall(request).execute();
        return response;
    }private Request sendHttpGetRequestStream(String endpointUrl, String authToken) throws IOException {
        Request request = new Request.Builder()
                .url(endpointUrl)
                .header("Authorization", authToken)
                .get()
                .build();

//        Response response = httpClient.newCall(request).execute();
        return request;
    }

    private Response sendHttpDeleteRequest(String endpointUrl, String authToken) throws IOException {
        Request request = new Request.Builder()
                .url(endpointUrl)
                .header("Authorization", authToken)
                .delete()
                .build();

        Response response = httpClient.newCall(request).execute();
        return response;
    }

    private Response sendHttpPostRequestWithFile(String endpointUrl, String authToken, File file) throws IOException {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", URLDecoder.decode( file.getName(), StandardCharsets.UTF_8), RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .addFormDataPart("filename", URLDecoder.decode(file.getName(), StandardCharsets.UTF_8))
                .addFormDataPart("mimetype", Files.probeContentType(file.toPath()))
                .build();

        Request request = new Request.Builder()
                .url(endpointUrl)
                .header("Authorization",  authToken)
                .post(requestBody)
                .build();

        Response response = httpClient.newCall(request).execute();
        return response;
    }

    public void setHttpClient(OkHttpClient client){
        this.httpClient = client;
    }

}
