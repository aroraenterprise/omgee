package com.sajarora.omgee.api;

import com.sajarora.omgee.Config;

import retrofit.RestAdapter;

/**
 * Created by sajarora on 10/24/15.
 */
public class OmgeeApi {

    private static OmgeeApi mInstance;

    private OmgeeApi(){
    }

    public static OmgeeApi getInstance(){
        if (mInstance == null)
            mInstance = new OmgeeApi();
        return mInstance;
    }

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(Config.ENDPOINT)
            .build();


    public OmgeeService service = restAdapter.create(OmgeeService.class);
}
