
package com.abe.jason.rateme.api.recognize;

import com.squareup.moshi.Json;

import java.util.List;

public class Image {

    @Json(name = "transaction")
    private Transaction transaction;
    @Json(name = "candidates")
    private List<Candidate> candidates = null;

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

}
