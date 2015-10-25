package com.sajarora.omgee.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by sajarora on 8/24/15.
 */
public interface OmgeeService {

    @POST("/users")
    void login(@Body OmgeeUser user, Callback<OmgeeUser> callback);

    @POST("/users/{username}/checkin")
    void postCheckin(@Path("username") String username,
                     @Body OmgeeCheckin checkin, Callback<OmgeeCheckin> callback);

    @GET("/users/{username}/checkin")
    void getCheckin(@Path("username") String username,
                    Callback<OmgeeCheckin> callback);
}
