package com.example.catchy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.catchy.HomeFragmentAdapter;
import com.example.catchy.R;
import com.example.catchy.SpotifyAppRemoteSingleton;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;


public class HomeFragment extends Fragment {
    HomeFragmentAdapter homeFragmentAdapter;
    ViewPager2 mViewPager;
    ArrayList<String> arr;


    private String CLIENT_ID;
    private static final String REDIRECT_URI = "http://com.example.catchy./callback";
    SpotifyAppRemoteSingleton singleton;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CLIENT_ID = getString(R.string.spotify_client_id);
        singleton = SpotifyAppRemoteSingleton.getInstance();

        arr = new ArrayList<String>();

        arr.add("spotify:track:7AEAGTc8cReDqcbPoY9gwo"); // we are never ever
        arr.add("spotify:track:2X2J0BhxaLTmnxO4pPUhSd"); // the lucky ones
        arr.add("spotify:track:786NsUYn4GGUf8AOt0SQhP"); // state of grace
        arr.add("spotify:track:12M5uqx0ZuwkpLp5rJim1a"); // cornelia street
        arr.add("spotify:track:1fzAuUVbzlhZ1lJAx9PtY6"); // daylight
        homeFragmentAdapter = new HomeFragmentAdapter(this, arr);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.connect(getContext(), connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        singleton.setSpotifyAppRemote(spotifyAppRemote);
                        Log.d("HomeFragment", "Connected! Yay!");

                        if (singleton.getSpotifyAppRemote() != null) {
                            mViewPager = view.findViewById(R.id.viewpager);
                            mViewPager.setAdapter(homeFragmentAdapter);
                            mViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
                        }

                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("HomeFragment", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }



    @Override
    public void onPause() {
        super.onPause();
        singleton.getSpotifyAppRemote().getPlayerApi().pause();
        Log.d("HomeFragment", "switched");
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(singleton.getSpotifyAppRemote());
    }
}