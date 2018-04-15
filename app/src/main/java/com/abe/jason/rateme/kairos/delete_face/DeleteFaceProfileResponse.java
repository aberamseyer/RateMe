
package com.abe.jason.rateme.kairos.delete_face;

import com.squareup.moshi.Json;

public class DeleteFaceProfileResponse {

    @Json(name = "status")
    private String status;
    @Json(name = "message")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
