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

import com.example.catchy.HomeAdapter;
import com.example.catchy.R;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    HomeAdapter homeAdapter;
    ViewPager2 mViewPager;
    ArrayList<String> arr;


    private String CLIENT_ID;
    private static final String REDIRECT_URI = "http://com.example.catchy./callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CLIENT_ID = getString(R.string.spotify_client_id);

        arr = new ArrayList<String>();

        arr.add("spotify:track:7AEAGTc8cReDqcbPoY9gwo"); // we are never ever
        arr.add("spotify:track:2X2J0BhxaLTmnxO4pPUhSd"); // the lucky ones
        arr.add("spotify:track:786NsUYn4GGUf8AOt0SQhP"); // state of grace
        arr.add("spotify:track:7AEAGTc8cReDqcbPoY9gwo"); // we are never ever - t. swift
        arr.add("spotify:track:2X2J0BhxaLTmnxO4pPUhSd"); // the lucky ones - t. swift
        arr.add("spotify:track:786NsUYn4GGUf8AOt0SQhP"); // state of grace - t. swift


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
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("HomeFragment", "Connected! Yay!");

                        if (mSpotifyAppRemote != null) {
                            homeAdapter = new HomeAdapter(getContext(), mSpotifyAppRemote);
                            homeAdapter.addAll(arr);
                            mViewPager = view.findViewById(R.id.viewpager);
                            mViewPager.setAdapter(homeAdapter);
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
        mSpotifyAppRemote.getPlayerApi().pause();
        Log.d("HomeFragment", "switched");
    }

    @Override
    public void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}