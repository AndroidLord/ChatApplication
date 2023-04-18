package com.example.chatapplication.models;

public class MessageModel {

    String message,messageId,senderId,imageUrl;

    String senderName;

    long timeStamp;

    public MessageModel() {
    }

    public MessageModel(String message, String senderId, String senderName, long timeStamp) {
        this(message, senderId, timeStamp);
        this.senderName = senderName;
    }


    public MessageModel( String senderId,String senderName, long timeStamp) {
        this.senderName = senderName;
        this.senderId = senderId;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
