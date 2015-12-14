package net.xvidia.vowmee.network.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Ravi_office on 23-Nov-15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Register {
    private String username;
    private String password;
    private String faceBookId;
    private long twitterId;
    private String name;
    private String emailId;
    private String contactNo;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFaceBookId() {
        return faceBookId;
    }

    public void setFaceBookId(String faceBookId) {
        this.faceBookId = faceBookId;
    }

    public long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(long twitterId) {
        this.twitterId = twitterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }
}
