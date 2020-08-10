package com.example.catchy.adapters;

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

import java.util.Date;
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

public class HomeFragmentAdapter extends FragmentStateAdapter {
    public static final String TAG = "HomeFragmentAdapter";
    List<Song> list;
    SpotifyService spotify;
    SpotifyBroadcastReceiver receiver;
    Context context;

    // default
    String seedArtists = "4NHQUGzhtTLFvgF5SZesLK";
    String seedGenres = "pop,k-pop";
    String seedTracks = "0c6xIDDpzE81m2q797ordA";
    boolean adding = false;

    public HomeFragmentAdapter(@NonNull Fragment fragment, List<Song> list, Context context, SpotifyBroadcastReceiver receiver) {
        super(fragment);
        this.list = list;
        this.context = context;
        this.receiver = receiver;
    }

    @NonNull
    @Override
    public SongFragment createFragment(int position) {
        if (position > list.size() - 3 && !adding) {
            Log.d("HomeFragmentAdapter", "get more");
            adding = true;
            addRecommendedSongs();
        }

        if (position == list.size() - 4) {
            queueSongs();
        }
        return SongFragment.newInstance(list.get(position), receiver);
    }

    // HTTP Request for new songs, add to Parse database
    private void addRecommendedSongs() {
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(ParseUser.getCurrentUser().getString("token"));
        spotify = spotifyApi.getService();
        // Map<String, Object> options = songRecommendation.getOptions();
        Map<String, Object> options = new HashMap<>();
        options.put("limit", 20);
        options.put("min_popularity", 50);

        if (seedArtists == null)
            seedArtists = "4NHQUGzhtTLFvgF5SZesLK";

        if (seedGenres == null)
            seedGenres = "pop,k-pop";

        if (seedTracks == null)
            seedTracks = "0c6xIDDpzE81m2q797ordA";

        options.put("seed_artists", seedArtists);
        options.put("seed_tracks", seedTracks);
        options.put("seed_genres", seedGenres);


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
            }
        });

        getSeedTracks();
        getSeedArtists(); // artists and genres
        adding = false;
    }

    public void getSeedTracks() {
        spotify.getTopTracks(new SpotifyCallback<Pager<Track>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.e("HomeFragmentAdapter", "error getting preferences", spotifyError);
            }

            @Override
            public void success(Pager<Track> trackPager, Response response) {
                List<Track> list = trackPager.items;
                int i = (int) (Math.random() * list.size());
                int j = (int) (Math.random() * list.size());
                ;
                while (i == j) {
                    j = (int) (Math.random() * list.size());
                }
                seedTracks = list.get(i).id + "," + list.get(j).id;
                Log.d("HomeFragmentAdapter", "Loaded: tracks = " + seedTracks);

            }
        });
    }

    public void getSeedArtists() {
        spotify.getTopArtists(new SpotifyCallback<Pager<Artist>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.e("HomeFragmentAdapter", "error getting preferences", spotifyError);
            }

            @Override
            public void success(Pager<Artist> artistPager, Response response) {
                List<Artist> list = artistPager.items;
                int i = (int) (Math.random() * list.size());
                int j = (int) (Math.random() * list.size());
                ;
                while (i == j) {
                    j = (int) (Math.random() * list.size());
                }

                if (list.size() > 0) {      // avoids index out of bounds exception
                    seedArtists = list.get(i).id + "," + list.get(j).id;
                    Log.d("HomeFragmentAdapter", "Loaded: artists = " + seedArtists);
                    if (list.get(i).genres.size() > 0) {
                        seedGenres = list.get(i).genres.get(0) + "," + list.get(j).genres.get(0);
                        Log.d("HomeFragmentAdapter", "Loaded: genres = " + seedGenres);
                    }
                    else {
                        seedGenres = "pop,k-pop"; // default
                    }
                }

            }
        });
    }


    private void queueSongs() {

        ParseQuery<Song> query = ParseQuery.getQuery(Song.class);

        if (list.size() > 0) { // get the oldest songs that are still newer than the newest song in the list
            Date newest = list.get(list.size() - 1).getCreatedAt();
            query.whereGreaterThan("createdAt", newest);
            query.whereEqualTo("seen", false);
            // Date oldest = list.get(list.size() - 1).getCreatedAt();
            Log.i("HomeFragmentAdapter", "Getting inf scroll songs");
        }

        query.setLimit(20);
        query.addAscendingOrder("createdAt"); // next oldest songs
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> songs, ParseException e) {
                if (e != null) {
                    Log.e("HomeFragmentAdapter", "Issue with getting songs", e);
                    return;
                }
                list.addAll(songs);
                notifyDataSetChanged();
                Log.i("HomeFragmentAdapter", "Adapter changed");
            }
        });

        deleteSongs();


    }

    private void deleteSongs() {
        ParseQuery<Song> query = ParseQuery.getQuery(Song.class);
        query.setLimit(20);
        query.whereEqualTo("seen", true);
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> songs, ParseException e) {
                if (e != null) {
                    Log.e("HomeFragmentAdapter", "Issue with getting songs", e);
                    return;
                }
                for (int i = 0; i < songs.size(); i++) {
                    songs.get(i).deleteInBackground();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
