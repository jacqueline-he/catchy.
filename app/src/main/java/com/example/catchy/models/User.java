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

}
