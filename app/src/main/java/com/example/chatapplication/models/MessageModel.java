package com.example.chatapplication.models;

public class MessageModel {

    String message,messageId,senderId,imageUrl;

    String senderName;
    String pdfUrl,pdfName;

    long timeStamp;

    public MessageModel() {
    }

    public MessageModel(String message, String senderId, String senderName, long timeStamp) {
        this.message = message;
        this.senderId = senderId;
        this.senderName = senderName;
        this.timeStamp = timeStamp;
    }

    public MessageModel(String message, String senderId, String senderName, long timeStamp, String pdfUrl,String pdfName) {
        this.message = message;
        this.senderId = senderId;
        this.senderName = senderName;
        this.timeStamp = timeStamp;
        this.pdfUrl = pdfUrl;
        this.pdfName=pdfName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public String getPdfName() {
        return pdfName;
    }

    public void setPdfName(String pdfName) {
        this.pdfName = pdfName;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
