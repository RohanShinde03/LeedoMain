package com.tribeappsoft.leedo.models;

import java.io.Serializable;

public class SocialUserModel implements Serializable
{

    private String firstName;
    private String lastName;
    private String fullName;
    private String position;
    private String photoPath;
    private String password;
    private String fcmToken;


    private  String facebookID;
    private  String gmailID;
    private  int  socialType;
    private boolean isFBLogin;
    private  boolean isGmailLogin;

    private String email;
    private String mobile;


    public SocialUserModel()
    {

    }

    public SocialUserModel(String fullName, String position, String photoPath, String email, String mobile) {
        this.fullName = fullName;
        this.position = position;
        this.photoPath = photoPath;
        this.email = email;
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }



    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFacebookID() {
        return facebookID;
    }

    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
    }

    public String getGmailID() {
        return gmailID;
    }

    public void setGmailID(String gmailID) {
        this.gmailID = gmailID;
    }

    public int getSocialType() {
        return socialType;
    }

    public void setSocialType(int socialType) {
        this.socialType = socialType;
    }

    public boolean isFBLogin() {
        return isFBLogin;
    }

    public void setFBLogin(boolean FBLogin) {
        isFBLogin = FBLogin;
    }

    public boolean isGmailLogin() {
        return isGmailLogin;
    }

    public void setGmailLogin(boolean gmailLogin) {
        isGmailLogin = gmailLogin;
    }


    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
