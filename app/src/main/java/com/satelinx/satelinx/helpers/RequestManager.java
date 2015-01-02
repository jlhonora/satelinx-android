package com.satelinx.satelinx.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by jlh on 1/2/15.
 */
public class RequestManager {

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public void makePostRequest(JsonObject body, String url, ResponseHandler handler) {
        RequestBody reqbody = RequestBody.create(JSON, body.toString());
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .url(url)
                .post(reqbody)
                .build();

        performRequest(request, handler);
    }

    public void makeGetRequest(JsonObject params, String url, ResponseHandler handler) {
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .url(url)
                .build();

        performRequest(request, handler);
    }

    public void performRequest(Request request, ResponseHandler handler) {
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            handler.onFailure(e);
        }

        if (response == null || !response.isSuccessful()) {
            if (handler != null) {
                handler.onFailure(new IOException("Error in request"));
            }
        } else {
            handler.onSuccess(gson.fromJson(response.body().charStream(), JsonObject.class));
        }

    }

    public static class ResponseHandler {

        public void onSuccess(JsonObject object) {

        }

        public void onFailure(Exception e) {

        }

    }

}
