package com.example.hospitalfinder;

public class RegisterResponse {
    private boolean success; // Matches "success" in the server's JSON
    private String message;  // Matches "message" in the server's JSON

    // Getter for success
    public boolean isSuccess() {
        return success;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }
}
