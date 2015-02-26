package com.satelinx.satelinx.helpers;

import com.google.gson.Gson;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by jlh on 2/25/15.
 */
public class ApiManager {

    public static RestAdapter getRestAdapter() {
        Gson gson = Serialization.getGsonInstance();
        return new RestAdapter.Builder()
                .setConverter(new GsonConverter(gson))
                .setEndpoint(SatelinxSession.API_ENDPOINT)
                .build();
    }

    public static SatelinxSession getSession() {
        return getRestAdapter().create(SatelinxSession.class);
    }
}
