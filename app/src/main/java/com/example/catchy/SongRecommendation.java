package com.example.catchy;

import android.util.Log;

import com.example.catchy.models.Song;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.client.Response;

public class SongRecommendation {
    SpotifyService spotify;
    public static final String TAG = "SongRecommendation";

    // TODO more custom generation
    public Map<String, Object> getOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("limit", 20);
        options.put("min_popularity", 50);
        options.put("seed_artists", "4NHQUGzhtTLFvgF5SZesLK");
        options.put("seed_genres", "pop,k-pop");
        options.put("seed_tracks", "0c6xIDDpzE81m2q797ordA");
        return options;
    }
}
