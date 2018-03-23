
package com.abe.jason.rateme.kairos.enroll;

import com.squareup.moshi.Json;

import java.util.List;

public class EnrollResponse {

    @Json(name = "face_id")
    private String faceId;
    @Json(name = "images")
    private List<Image> images = null;

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

}
