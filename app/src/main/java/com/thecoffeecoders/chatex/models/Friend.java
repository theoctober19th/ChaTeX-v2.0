package com.thecoffeecoders.chatex.models;

public class Friend {
    private String since;

    public Friend() {
    }

    public Friend(String since) {
        this.since = since;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }
}
