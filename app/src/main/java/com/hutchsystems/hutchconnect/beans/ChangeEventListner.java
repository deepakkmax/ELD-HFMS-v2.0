package com.hutchsystems.hutchconnect.beans;

public interface ChangeEventListner {

    // Change event listner to check the event is applicable for split sleeep
    void changeEventListner(String eventDateTime, Boolean isEdited, int driverId, int currentEventType, int currentEventCode);

    void onSpecialStatusChange(int specialStatusFg);
}
