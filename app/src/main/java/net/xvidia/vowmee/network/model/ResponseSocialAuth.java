package net.xvidia.vowmee.network.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Ravi_office on 23-Nov-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseSocialAuth {
    private String username;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
