package com.thecoffeecoders.chatex.models;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String username;
    private String displayName;
    private String email;
    private String bio;
    private String address;
    private String phoneNumber;
    private Uri profilePicURI;
    private Uri coverPictureURI;

    public User() {
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

    public Uri getProfilePicURI() {
        return profilePicURI;
    }

    public void setProfilePicURI(Uri profilePicURI) {
        this.profilePicURI = profilePicURI;
    }

    public Uri getCoverPictureURI() {
        return coverPictureURI;
    }

    public void setCoverPictureURI(Uri coverPictureURI) {
        this.coverPictureURI = coverPictureURI;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();

        result.put("username", username);
        result.put("displayname", displayName);
        result.put("email", email);
        result.put("bio", bio);
        result.put("address", address);
        result.put("phone", phoneNumber);
        result.put("profilepic", profilePicURI);
        result.put("coverpic", coverPictureURI);

        return result;
    }
}
