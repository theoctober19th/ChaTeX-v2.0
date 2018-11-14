package com.thecoffeecoders.chatex.models;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String username;
    private String displayName;
    private String email;
    private String bio;
    private String address;
    private String phoneNumber;
    private String gender;
    private String profilePicURI;
    private String coverPictureURI;
    private long lastOnline;
    private boolean onlineStatus;

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public User() {
        id = "";
        username = "";
        displayName = "";
        email = "";
        bio = "";
        address = "";
        phoneNumber = "";
        gender = "";
        profilePicURI = "";
        coverPictureURI = "";
        lastOnline = 0;
        onlineStatus = false;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePicURI() {
        return profilePicURI;
    }

    public void setProfilePicURI(String profilePicURI) {
        this.profilePicURI = profilePicURI;
    }

    public String getCoverPictureURI() {
        return coverPictureURI;
    }

    public void setCoverPictureURI(String coverPictureURI) {
        this.coverPictureURI = coverPictureURI;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }



    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("username", username);
        result.put("displayname", displayName);
        result.put("email", email);
        result.put("bio", bio);
        result.put("address", address);
        result.put("phone", phoneNumber);
        result.put("profilepic", profilePicURI);
        result.put("coverpic", coverPictureURI);
        result.put("online", onlineStatus);
        result.put("gender", gender);
        result.put("lastonline", lastOnline);

        return result;
    }
}
