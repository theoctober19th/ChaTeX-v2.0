package com.thecoffeecoders.chatex.models;

public class Group {
    private String id;
    private String name;
    private long created;
    private int memberCount;
    private String groupPicURI;

    public Group() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getGroupPicURI() {
        return groupPicURI;
    }

    public void setGroupPicURI(String groupPicURI) {
        this.groupPicURI = groupPicURI;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}
