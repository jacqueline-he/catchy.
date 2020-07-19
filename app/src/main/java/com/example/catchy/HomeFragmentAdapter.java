package com.example.catchy;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.catchy.fragments.SongFragment;
import com.example.catchy.models.Song;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
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

public class HomeFragmentAdapter extends FragmentStateAdapter {
    public static final String TAG = "HomeFragmentAdapter";
    List<Song> list;
    SpotifyService spotify;
    SpotifyBroadcastReceiver receiver;
    Context context;

    public HomeFragmentAdapter(@NonNull Fragment fragment, List<Song> list, Context context, SpotifyBroadcastReceiver receiver) {
        super(fragment);
        this.list = list;
        this.context = context;
        this.receiver = receiver;

    }

    @NonNull
    @Override
    public SongFragment createFragment(int position) {
        if (position == list.size() - 2) {
            Log.d("HomeFragmentAdapter", "get more");
            // receiver.enqueueService(context, SpotifyBroadcastReceiver.ACTION_GET_RECS);
            addRecommendedSongs();

        }

        if (position > list.size() - 1) {
            // queueSongs(); // Retrieve 10 more songs
        }
        return SongFragment.newInstance(list.get(position), receiver);
    }

    // HTTP Request for new songs, add to Parse database
    private void addRecommendedSongs() {
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(ParseUser.getCurrentUser().getString("token"));
        Map<String, Object> options = new HashMap<>();
        options.put("limit", 20);
        // options.put("min_popularity", 50);
        options.put("seed_artists", "4NHQUGzhtTLFvgF5SZesLK");
        options.put("seed_genres", "pop,k-pop");
        options.put("seed_tracks", "0c6xIDDpzE81m2q797ordA");

        spotify = spotifyApi.getService();
        spotify.getRecommendations(options, new SpotifyCallback<Recommendations>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.e(TAG, "getting recommended tracks failed!", spotifyError);
            }

            @Override
            public void success(Recommendations recommendations, Response response) {
                List<Track> tracks = recommendations.tracks;
                for (int i = 0; i < tracks.size(); i++) {
                    Track track = tracks.get(i);
                    Song song = new Song();
                    song.setURI(track.uri);
                    song.setImageUrl(track.album.images.get(0).url);
                    song.setTitle(track.name);
                    String artists = "";
                    List<ArtistSimple> artistList = track.artists;
                    for (int j = 0; j < artistList.size(); j++) {
                        artists += artistList.get(j).name + ", ";
                    }
                    song.setArtist(artists.substring(0, artists.length() - 2));
                    song.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error while saving rec", e);
                                e.printStackTrace();
                            }
                            Log.i(TAG, "Rec save was successful!");
                        }
                    });
                }
                queueSongs();
            }
        });
    }

    private void queueSongs() {

        ParseQuery<Song> query = ParseQuery.getQuery(Song.class);

        if (list.size() > 0) { // get the oldest songs that are still newer than the newest song in the list
            Date newest = list.get(list.size() - 1).getCreatedAt();
            query.whereGreaterThan("createdAt", newest);
            // query.whereEqualTo("seen", false);
            // Date oldest = list.get(list.size() - 1).getCreatedAt();
            Log.i("HomeFragmentAdapter", "Getting inf scroll posts");
        }

        query.setLimit(20);
        query.addAscendingOrder("createdAt"); // next oldest songs
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> posts, ParseException e) {
                if (e != null) {
                    Log.e("HomeFragmentAdapter", "Issue with getting posts", e);
                    return;
                }
                list.addAll(posts);
                notifyDataSetChanged();
                Log.i("HomeFragmentAdapter", "Adapter changed");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
