package com.example.karot.mymovies;

public class Movie {

    // Store the id of the  movie poster
    private String posterURL;
    // Store the name of the movie
    private String mName;
    // Store the release date of the movie
    private String mRelease;

    private String rating;
    private String genre;
    private String duration;
    private String description;
    private String id;

    // Constructor that is used to create an instance of the Movie object
    public Movie(String posterURL, String mName, String mRelease, String rating, String genre, String duration, String description, String id) {
        this.posterURL = posterURL;
        this.mName = mName;
        this.mRelease = mRelease;
        this.rating = rating;
        this.genre = genre;
        this.duration = duration;
        this.description = description;
        this.id = id;
    }

    public String getmImageDrawable() {
        return posterURL;
    }

    public void setmImageDrawable(String posterURL) {
        this.posterURL = posterURL;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmRelease() {
        return mRelease;
    }

    public void setmRelease(String mRelease) {
        this.mRelease = mRelease;
    }

    public String getRating() {
        return rating;
    }

    public String getGenre() {
        return genre;
    }

    public String getDuration() {
        return duration;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

}