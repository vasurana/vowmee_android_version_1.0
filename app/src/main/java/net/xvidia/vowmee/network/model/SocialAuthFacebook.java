package net.xvidia.vowmee.network.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Ravi_office on 23-Nov-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SocialAuthFacebook {
    private String accessToken;
    private String userId;
    private String expiryDate;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
