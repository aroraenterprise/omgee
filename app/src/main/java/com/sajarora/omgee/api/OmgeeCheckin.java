package com.sajarora.omgee.api;

/**
 * Created by sajarora on 10/25/15.
 */
public class OmgeeCheckin {
    public String value;
    public Long timestamp;

    public static final String CHECK_IN = "postCheckin";
    public static final String CHECK_OUT = "checkout";


    public OmgeeCheckin(String value){
        this.value = value;
    }
}
