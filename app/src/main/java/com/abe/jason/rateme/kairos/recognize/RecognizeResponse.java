
package com.abe.jason.rateme.kairos.recognize;

import com.squareup.moshi.Json;

import java.util.List;

public class RecognizeResponse {

    @Json(name = "images")
    private List<Image> images = null;

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

}
