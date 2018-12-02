package com.thecoffeecoders.chatex.models;

public class Chat {
    private boolean seen;
    private long seenTimestamp;
    private String lastMessage;

    public Chat() {
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getSeenTimestamp() {
        return seenTimestamp;
    }

    public void setSeenTimestamp(long seenTimestamp) {
        this.seenTimestamp = seenTimestamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
