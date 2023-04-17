package com.example.chatapplication.models;

public class UserModel {

    String userId,name,phoneNo,profileImage;

    public UserModel() {
    }

    public UserModel(String userId, String name, String phoneNo, String profileImage) {
        this.userId = userId;
        this.name = name;
        this.phoneNo = phoneNo;
        this.profileImage = profileImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
