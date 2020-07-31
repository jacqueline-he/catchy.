package com.example.catchy.models;

import android.graphics.Bitmap;

import com.parse.ParseClassName;
import com.parse.ParseUser;

import java.util.HashMap;

@ParseClassName("User")
public class User extends ParseUser {
    public static Bitmap profileBitmap;
    // TODO grab current user's list of followers, following / clear upon logout

}
