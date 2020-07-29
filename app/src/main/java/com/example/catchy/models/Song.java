package com.example.catchy.models;

import android.graphics.Bitmap;
import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@ParseClassName("Song")
public class Song extends ParseObject {
    public static final String KEY_TITLE = "title";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_IMAGE_URL = "imageUrl";
    public static final String KEY_URI = "uri";
    public static final String KEY_SEEN = "seen";
    public static final String KEY_DURATION = "duration";
    public Bitmap bitmap;

    public Song() {
        super();
    }

    public Song(JSONObject json) throws JSONException {
        setObjectId(json.getString("objectId"));
        put(KEY_URI, json.getString(KEY_URI));
        put(KEY_TITLE, json.getString(KEY_TITLE));
        put(KEY_ARTIST, json.getString(KEY_ARTIST));
        put(KEY_IMAGE_URL, json.getString(KEY_IMAGE_URL));
    }

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public String getArtist() {
        return getString(KEY_ARTIST);
    }

    public void setArtist(String artist) {
        put(KEY_ARTIST, artist);
    }

    public String getImageUrl() {
        return getString(KEY_IMAGE_URL);
    }

    public void setImageUrl(String imageUrl) {
        put(KEY_IMAGE_URL, imageUrl);
    }

    public String getURI() {
        return getString(KEY_URI);
    }

    public void setURI(String uri) {
        put(KEY_URI, uri);
    }

    public boolean getSeen() {
        return getBoolean(KEY_SEEN);
    }

    ;

    public void setSeen(boolean seen) {
        put(KEY_SEEN, seen);
    }

    public long getDuration() {
        return getNumber("duration").longValue();
    }

    public void setDuration(long duration) {
        put(KEY_DURATION, duration);
    }


}
