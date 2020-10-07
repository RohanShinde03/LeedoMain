package com.tribeappsoft.leedo.admin.models;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 21/8/19.
 */
public class EventProjectDocsModel implements Serializable
{

    private int docId;
    private int doc_type_id;
    private String docName;
    private int media_type_id;
    private String brochure_description;
    private int project_id;
    private String projectName;

    public int getMedia_type_id() {
        return media_type_id;
    }

    public void setMedia_type_id(int media_type_id) {
        this.media_type_id = media_type_id;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private String docType;
    private String docPath;
    private String docThumbnail;
    private String docText;
    private String date;
    private int isRequired;  //TODO 1 -> Required, 0-> not required
    private int isUploaded;     // TODO 1 -> uploaded , 0 -> not uploaded


    public EventProjectDocsModel() {
    }

    public EventProjectDocsModel(int docId, String docName, String docType, String docPath, String docThumbnail, String docText, String date) {
        this.docId = docId;
        this.docName = docName;
        this.docType = docType;
        this.docPath = docPath;
        this.docThumbnail = docThumbnail;
        this.docText = docText;
        this.date = date;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocPath() {
        return docPath;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    public String getDocThumbnail() {
        return docThumbnail;
    }

    public void setDocThumbnail(String docThumbnail) {
        this.docThumbnail = docThumbnail;
    }

    public String getDocText() {
        return docText;
    }

    public void setDocText(String docText) {
        this.docText = docText;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(int isRequired) {
        this.isRequired = isRequired;
    }

    public int getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(int isUploaded) {
        this.isUploaded = isUploaded;
    }

    public int getDoc_type_id() {
        return doc_type_id;
    }

    public void setDoc_type_id(int doc_type_id) {
        this.doc_type_id = doc_type_id;
    }

    public String getBrochure_description() {
        return brochure_description;
    }

    public void setBrochure_description(String brochure_description) {
        this.brochure_description = brochure_description;
    }


}