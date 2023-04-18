package com.example.chatapplication.models;

public class UserModel {

    private String userId,name,phoneNo,profileImage;

    boolean group;
    String groupId;

    public UserModel() {
    }

    public UserModel(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public UserModel(String userId, String name,boolean group) {
        this(userId,name);
        this.group = group;
    }

    public UserModel(String userId, String name,boolean group,String groupId) {
        this(userId,name);
        this.group = group;
        this.groupId=groupId;
    }

    public UserModel(String userId, String name, String phoneNo, String profileImage) {
        this(userId, name);
        this.phoneNo = phoneNo;
        this.profileImage = profileImage;
    }
    public UserModel(String userId, String name, String phoneNo, String profileImage,boolean group) {
        this(userId,name,phoneNo,profileImage);
        this.group = group;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
