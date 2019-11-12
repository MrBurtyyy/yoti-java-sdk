package com.yoti.api.examples.springboot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseClass {

    @JsonProperty("id")
    private String id;

    public ResponseClass() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
