package com.hutchsystems.hutchconnect.beans;


public class Annotation {
   int AnnotationId,companyID;
   String annotationValue;
   int displayOrder;

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    String description;


    public int getAnnotationId() {
        return AnnotationId;
    }

    public void setAnnotationId(int annotationId) {
        AnnotationId = annotationId;
    }

    public String getAnnotationValue() {
        return annotationValue;
    }

    public void setAnnotationValue(String annotationValue) {
        this.annotationValue = annotationValue;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
