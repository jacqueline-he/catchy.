package com.example.catchy.models;

import android.graphics.Bitmap;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Like")
public class Like extends ParseObject {
    public static final String KEY_TITLE = "title";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_IMAGE_URL = "imageUrl";
    public static final String KEY_LIKED_BY = "likedBy";
    public static final String KEY_URI = "uri";
    public static final String KEY_DURATION = "duration";

    public Bitmap bitmap; // no need to pass on to Parse

    public Like() {super();}

    public String getTitle() {return getString(KEY_TITLE);}

    public void setTitle(String title) {put(KEY_TITLE, title);}

    public String getArtist() {return getString(KEY_ARTIST);}

    public void setArtist(String artist) {put(KEY_ARTIST, artist);}

    public String getImageUrl() {return getString(KEY_IMAGE_URL);}

    public void setImageUrl(String imageUrl) {put(KEY_IMAGE_URL, imageUrl);}

    public String getURI() {return getString(KEY_URI);}

    public void setURI(String uri) {put(KEY_URI, uri);}

    public ParseUser getLikedBy() {
        return getParseUser(KEY_LIKED_BY);
    }

    public void setLikedBy(ParseUser user) {
        put(KEY_LIKED_BY, user);
    }

    public long getDuration() {return getNumber("duration").longValue();}

    public void setDuration(long duration) {put(KEY_DURATION, duration);}

}
