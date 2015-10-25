package com.sajarora.omgee.api;

/**
 * Created by sajarora on 10/24/15.
 */
public class OmgeeUser {
    public String id;
    public String username;
    public Long createdAt;

    public OmgeeUser(String username) {
        this.username = username;
    }
}
