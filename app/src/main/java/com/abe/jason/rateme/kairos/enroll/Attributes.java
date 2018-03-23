
package com.abe.jason.rateme.kairos.enroll;

import com.squareup.moshi.Json;

public class Attributes {

    @Json(name = "lips")
    private String lips;
    @Json(name = "asian")
    private Double asian;
    @Json(name = "gender")
    private Gender gender;
    @Json(name = "age")
    private Integer age;
    @Json(name = "hispanic")
    private Double hispanic;
    @Json(name = "other")
    private Double other;
    @Json(name = "black")
    private Double black;
    @Json(name = "white")
    private Double white;
    @Json(name = "glasses")
    private String glasses;

    public String getLips() {
        return lips;
    }

    public void setLips(String lips) {
        this.lips = lips;
    }

    public Double getAsian() {
        return asian;
    }

    public void setAsian(Double asian) {
        this.asian = asian;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getHispanic() {
        return hispanic;
    }

    public void setHispanic(Double hispanic) {
        this.hispanic = hispanic;
    }

    public Double getOther() {
        return other;
    }

    public void setOther(Double other) {
        this.other = other;
    }

    public Double getBlack() {
        return black;
    }

    public void setBlack(Double black) {
        this.black = black;
    }

    public Double getWhite() {
        return white;
    }

    public void setWhite(Double white) {
        this.white = white;
    }

    public String getGlasses() {
        return glasses;
    }

    public void setGlasses(String glasses) {
        this.glasses = glasses;
    }

}
