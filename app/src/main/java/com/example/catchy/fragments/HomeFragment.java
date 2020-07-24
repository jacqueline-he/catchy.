package com.example.catchy.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.catchy.adapters.HomeFragmentAdapter;
import com.example.catchy.activities.MainActivity;
import com.example.catchy.R;
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
import java.util.Random;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.client.Response;


public class HomeFragment extends Fragment{
    public static final String TAG = "HomeFragment";
    HomeFragmentAdapter homeFragmentAdapter;
    ViewPager2 mViewPager;
    kaaes.spotify.webapi.android.SpotifyService spotify;
    List<Song> arr;
    private SpotifyBroadcastReceiver spotifyBroadcastReceiver;
    Context context;
    private List<String> tracks;
    private List<String> artists;
    private List<String> genres;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arr = new ArrayList<Song>();
        spotifyBroadcastReceiver = ((MainActivity)getContext()).getReceiver();
        homeFragmentAdapter = new HomeFragmentAdapter(this, arr, getContext(), spotifyBroadcastReceiver);
        context = getContext();


        artists = new ArrayList<>();
        tracks = new ArrayList<>();
        genres = new ArrayList<>();
        // starter seeds
        artists.add("6qqNVTkY8uBg9cP3Jd7DAH");
        tracks.add("2X2J0BhxaLTmnxO4pPUhSd");
        genres.add("pop");
        genres.add("kpop");

        queueSongs();
    }

    // HTTP Request for new songs, add to Parse database
    private void addRecommendedSongs() {
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(ParseUser.getCurrentUser().getString("token"));
        // Map<String, Object> options = songRecommendation.getOptions();

        Random rand = new Random();
        // starter
        String seedArtists = artists.get( rand.nextInt(artists.size()));
        String seedGenres = genres.get( rand.nextInt(genres.size()));;
        String seedTracks = tracks.get( rand.nextInt(tracks.size()));

        Map<String, Object> options = new HashMap<>();
        options.put("limit", 20);
        options.put("min_popularity", 50);
        options.put("seed_tracks", seedTracks);
        options.put("seed_artists", seedArtists);
        options.put("seed_genres", seedGenres);
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
                    song.setDuration(track.duration_ms);
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

        if (arr.size() > 0) { // get the oldest songs that are still newer than the newest song in the list
            Date newest = arr.get(arr.size() - 1).getCreatedAt();
            query.whereGreaterThan("createdAt", newest);
            query.whereEqualTo("seen", false);
            Log.i(TAG, "Getting inf scroll posts");
        }

        query.setLimit(20);
        query.addAscendingOrder("createdAt"); // next oldest songs
        query.whereEqualTo("seen", false);
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                if (posts.size() < 10) {
                    addRecommendedSongs();
                }
                else {
                    arr.addAll(posts);
                    homeFragmentAdapter.notifyDataSetChanged();
                    Log.i(TAG, "Adapter changed");
                }
            }
        });


    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(homeFragmentAdapter);
        mViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        homeFragmentAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    private void populatePlaylist() throws ParseException {
        ParseQuery<Song> query = new ParseQuery<>(Song.class);
        query.addAscendingOrder("createdAt"); // oldest songs first
        query.whereEqualTo("seen", false);
        query.setLimit(10);
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting songs", e);
                    return;
                }
                for (Song song : objects) {
                    Log.i(TAG, "Song: " + song.getTitle());
                    arr.add(song);
                }

                homeFragmentAdapter.notifyDataSetChanged();

            }
        });
    }


/*    @Override
    public void onPause() {
        super.onPause();
        spotifyBroadcastReceiver.enqueueService(getContext(), SpotifyBroadcastReceiver.ACTION_PLAY_PAUSE);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(spotifyBroadcastReceiver);
        Log.d("HomeFragment", "Paused");
    } */

/*
    @Override
    public void onResume() {
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(SpotifyIntentService.ACTION);
        spotifyBroadcastReceiver.enqueueService(getContext(), SpotifyBroadcastReceiver.ACTION_PLAY_PAUSE);
        LocalBroadcastManager.getInstance(context).registerReceiver(spotifyBroadcastReceiver, filter);
        // or `registerReceiver(testReceiver, filter)` for a normal broadcast
    }
    */
}