
package com.abe.jason.rateme.api.enroll;

import com.squareup.moshi.Json;

public class Image {

    @Json(name = "attributes")
    private Attributes attributes;
    @Json(name = "transaction")
    private Transaction transaction;

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

}
