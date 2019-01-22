package com.example.karot.mymovies;

public class User {

    public String about;
    public String nick;
    public String avatar_path;
    public String uid;
    public Boolean isUserFriend;

    public User(String nick, String about) {
        this.about = about;
        this.nick = nick;
        this.avatar_path = "";
        this.uid = "";
        isUserFriend = true;
    }

    public User(String nick, String about, String avatar_path, String uid, Boolean isUserFriend) {
        this.about = about;
        this.nick = nick;
        this.avatar_path = avatar_path;
        this.uid = uid;
        this.isUserFriend = isUserFriend;
    }

    public void setAvatar_path(String path) {
        this.avatar_path = path;
    }

    public String getNick() {
        return nick;
    }

    public String getAbout() {
        return about;
    }

    public String getAvatar_path() {
        return avatar_path;
    }

    public String getUid() {return uid;}

    public Boolean getUserFriend() {
        return isUserFriend;
    }
};
