package com.hutchsystems.hutchconnect.beans;

public class NotificationBean {
    String Title;
    String Comment;
    int status;
    int NotificationID;
    String notiFicationDate;

    public String getNotiFicationDate() {
        return notiFicationDate;
    }

    public void setNotiFicationDate(String notiFicationDate) {
        this.notiFicationDate = notiFicationDate;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNotificationID() {
        return NotificationID;
    }

    public void setNotificationID(int notificationID) {
        NotificationID = notificationID;
    }
}
