package com.app.filmtracker.vo;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;

public class Friend {
    private String email;
    private String fullName;
    private String username;
    private Boolean has_image;

    @Expose(serialize = false, deserialize = false)
    private Bitmap profileImage;



    public Boolean getHas_image() {
        return has_image;
    }

    public void setHas_image(Boolean has_image) {
        this.has_image = has_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }
}
