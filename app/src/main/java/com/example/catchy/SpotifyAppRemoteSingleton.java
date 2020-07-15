package com.example.catchy;

import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SpotifyAppRemoteSingleton {
    private SpotifyAppRemote mSpotifyAppRemote;

    private static SpotifyAppRemoteSingleton mSpotifyAppRemoteSingleton = null;

    private SpotifyAppRemoteSingleton() {}

    public static SpotifyAppRemoteSingleton getInstance() {
        if (mSpotifyAppRemoteSingleton == null) {
            mSpotifyAppRemoteSingleton = new SpotifyAppRemoteSingleton();
        }

        return mSpotifyAppRemoteSingleton;

    }

    public SpotifyAppRemote getSpotifyAppRemote() {

        return mSpotifyAppRemote;
    }

    public void setSpotifyAppRemote(SpotifyAppRemote spotifyAppRemote) {
        mSpotifyAppRemote = spotifyAppRemote;

    }
}
