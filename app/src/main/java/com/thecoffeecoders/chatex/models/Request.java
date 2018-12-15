package com.thecoffeecoders.chatex.models;

public class Request {
    private String type;
    private long timestamp;

    public Request() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
