package com.example.catchy.models;

import android.graphics.Bitmap;

import com.parse.ParseClassName;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;

@ParseClassName("User")
public class User extends ParseUser {
    public static Bitmap profileBitmap;
    public static List<ParseUser> followers;
    public static List<ParseUser> following;
    public static List<Following> followingItems; // from Parse
    public static Song firstSong;
    public static boolean passedFirstSong = false;
    public static HashMap<String, Bitmap> otherUserBitmaps = new HashMap<>();
    public static boolean profPicChanged;
    public static int otherUserPos = -1;
    public static boolean nameChanged = false;
    public static String changedName;
    public static boolean bioChanged = false;
    public static String changedBio;
}
