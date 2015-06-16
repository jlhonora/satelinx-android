package com.satelinx.satelinx.helpers;

import com.satelinx.satelinx.models.Account;
import com.satelinx.satelinx.models.Coordinate;
import com.satelinx.satelinx.models.User;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by jlh on 1/2/15.
 */
public interface SatelinxSession {

    public static final String API_ENDPOINT = EnvironmentManager.getIp();
    public static final String AUTH_HEADER = "X-UNIQUE-HASH";

    @GET("/sessions/{username}/salt")
    User getSalt(@Path("username") String username);

    @POST("/sessions/{username}/authenticate")
    User authenticate(@Path("username") String username, @Header(AUTH_HEADER) String authHeader, @Body String body);

    @GET("/accounts/{account_id}")
    Account populate(@Path("account_id") long accountId, @Header(AUTH_HEADER) String authHeader);

    @GET("/trackables/{trackable_id}/show_date/{date}")
    List<Coordinate> showDate(@Path("trackable_id") long trackableId, @Path("date") String date, @Header(AUTH_HEADER) String authHeader);

}
