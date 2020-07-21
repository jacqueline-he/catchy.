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
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.client.Response;

public class SongRecommendation {
    SpotifyService spotify;
    public static final String TAG = "SongRecommendation";
    String seedArtists;
    String seedGenres;
    String seedTracks;
    Map<String, Object> options;

    // TODO more custom generation
    public Map<String, Object> getOptions() {
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(ParseUser.getCurrentUser().getString("token"));
        spotify = spotifyApi.getService();
        options = new HashMap<>();

        options.put("limit", 20);
        options.put("min_popularity", 50);
        return getTopPreferences();




    }

    private Map<String, Object> getTopPreferences() {
        spotify.getTopTracks(new SpotifyCallback<Pager<Track>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.e("SongRecommendation", "error getting preferences", spotifyError);
            }

            @Override
            public void success(Pager<Track> trackPager, Response response) {
                List<Track> list = trackPager.items;
                int i = (int) (Math.random() * list.size());
                int j = (int) (Math.random() * list.size());;
                while (i == j)  {
                    j = (int) (Math.random() * list.size());
                }
                seedTracks = list.get(i).id; // + "," + list.get(j).id;
                Log.d("SongRec", "tracks: " + seedTracks);
                options.put("seed_tracks", seedTracks); // "0c6xIDDpzE81m2q797ordA"

                spotify.getTopArtists(new SpotifyCallback<Pager<Artist>>() {
                    @Override
                    public void failure(SpotifyError spotifyError) {
                        Log.e("SongRecommendation", "error getting preferences", spotifyError);
                    }

                    @Override
                    public void success(Pager<Artist> artistPager, Response response) {
                        List<Artist> list = artistPager.items;
                        int i = (int) (Math.random() * list.size());
                        int j = (int) (Math.random() * list.size());;
                        while (i == j)  {
                            j = (int) (Math.random() * list.size());
                        }

                        seedArtists = list.get(i).id; // + "," + list.get(j).id;
                        Log.d("SongRec", "artists: " + seedArtists);
                        options.put("seed_artists", seedArtists); // "4NHQUGzhtTLFvgF5SZesLK"

                        seedGenres = list.get(i).genres.get(0) + "," + list.get(j).genres.get(0);

                        options.put("seed_genres", seedGenres); // "pop,k-pop"
                    }
                });

            }
        });

        return options;
    }
}
