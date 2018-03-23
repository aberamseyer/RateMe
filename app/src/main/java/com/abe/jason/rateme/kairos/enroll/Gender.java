
package com.abe.jason.rateme.kairos.enroll;

import com.squareup.moshi.Json;

public class Gender {

    @Json(name = "type")
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
