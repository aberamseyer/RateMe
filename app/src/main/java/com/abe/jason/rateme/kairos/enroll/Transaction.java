
package com.abe.jason.rateme.kairos.enroll;

import com.squareup.moshi.Json;

public class Transaction {

    @Json(name = "status")
    private String status;
    @Json(name = "topLeftX")
    private Integer topLeftX;
    @Json(name = "topLeftY")
    private Integer topLeftY;
    @Json(name = "gallery_name")
    private String galleryName;
    @Json(name = "timestamp")
    private String timestamp;
    @Json(name = "height")
    private Integer height;
    @Json(name = "quality")
    private Double quality;
    @Json(name = "confidence")
    private Double confidence;
    @Json(name = "subject_id")
    private String subjectId;
    @Json(name = "width")
    private Integer width;
    @Json(name = "face_id")
    private String faceId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTopLeftX() {
        return topLeftX;
    }

    public void setTopLeftX(Integer topLeftX) {
        this.topLeftX = topLeftX;
    }

    public Integer getTopLeftY() {
        return topLeftY;
    }

    public void setTopLeftY(Integer topLeftY) {
        this.topLeftY = topLeftY;
    }

    public String getGalleryName() {
        return galleryName;
    }

    public void setGalleryName(String galleryName) {
        this.galleryName = galleryName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Double getQuality() {
        return quality;
    }

    public void setQuality(Double quality) {
        this.quality = quality;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

}
