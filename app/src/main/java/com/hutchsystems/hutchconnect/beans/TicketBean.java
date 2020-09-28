package com.hutchsystems.hutchconnect.beans;

/**
 * Created by Deepak Sharma on 1/12/2018.
 */

public class TicketBean {
    private int ticketId, ticketStatus,Type,rating;
    private String ticketNo;
    private String UserFeedback,comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String title;


    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public int getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(int ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUserFeedback() {
        return UserFeedback;
    }

    public void setUserFeedback(String userFeedback) {
        this.UserFeedback = userFeedback;
    }
}
