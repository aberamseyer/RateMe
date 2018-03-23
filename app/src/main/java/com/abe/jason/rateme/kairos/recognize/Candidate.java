
package com.abe.jason.rateme.kairos.recognize;

import com.squareup.moshi.Json;

public class Candidate {

    @Json(name = "subject_id")
    private String subjectId;
    @Json(name = "confidence")
    private Double confidence;
    @Json(name = "enrollment_timestamp")
    private String enrollmentTimestamp;

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getEnrollmentTimestamp() {
        return enrollmentTimestamp;
    }

    public void setEnrollmentTimestamp(String enrollmentTimestamp) {
        this.enrollmentTimestamp = enrollmentTimestamp;
    }

}
