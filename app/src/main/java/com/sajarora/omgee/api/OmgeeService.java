package com.sajarora.omgee.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by sajarora on 8/24/15.
 */
public interface OmgeeService {

    @POST("/users")
    void login(@Body OmgeeUser user, Callback<OmgeeUser> callback);
}
