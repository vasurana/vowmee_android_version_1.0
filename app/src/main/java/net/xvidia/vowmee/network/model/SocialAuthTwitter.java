package net.xvidia.vowmee.network.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Ravi_office on 23-Nov-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SocialAuthTwitter {
    private String accessToken;
    private String accessSecret;
    private long userId;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

}
