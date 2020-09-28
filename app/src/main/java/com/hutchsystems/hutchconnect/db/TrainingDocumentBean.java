package com.hutchsystems.hutchconnect.db;

public class TrainingDocumentBean {
    private int _id,DocumentId,DocumentSize,StatusId;
        private String CreatedDate,DocumentContentType, DocumentName,DocumentPath,DocumentType,ImportedDate,ModifiedDate;

    public TrainingDocumentBean() {
        super();
    }

    public TrainingDocumentBean(String DocumentContentType, String DocumentType,String s) {
        this.DocumentContentType = DocumentContentType;
        this.DocumentType = DocumentType;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }


    public String getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        CreatedDate = createdDate;
    }

    public int getDocumentId() {
            return DocumentId;
        }

        public void setDocumentId(int documentId) {
            DocumentId = documentId;
        }

        public int getDocumentSize() {
            return DocumentSize;
        }

        public void setDocumentSize(int documentSize) {
            DocumentSize = documentSize;
        }

        public int getStatusId() {
            return StatusId;
        }

        public void setStatusId(int statusId) {
            StatusId = statusId;
        }

        public String getDocumentContentType() {
            return DocumentContentType;
        }

        public void setDocumentContentType(String documentContentType) {
            DocumentContentType = documentContentType;
        }

        public String getDocumentName() {
            return DocumentName;
        }

        public void setDocumentName(String documentName) {
            DocumentName = documentName;
        }

        public String getDocumentPath() {
            return DocumentPath;
        }

        public void setDocumentPath(String documentPath) {
            DocumentPath = documentPath;
        }

        public String getDocumentType() {
            return DocumentType;
        }

        public void setDocumentType(String documentType) {
            DocumentType = documentType;
        }

        public String getImportedDate() {
            return ImportedDate;
        }

        public void setImportedDate(String importedDate) {
            ImportedDate = importedDate;
        }

        public String getModifiedDate() {
            return ModifiedDate;
        }

        public void setModifiedDate(String modifiedDate) {
            ModifiedDate = modifiedDate;
        }
}
