package com.example.hospitalfinder;

import com.google.gson.annotations.SerializedName;

public class ServerResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
