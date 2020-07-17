package com.example.catchy.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.catchy.HomeFragmentAdapter;
import com.example.catchy.MainActivity;
import com.example.catchy.R;
import com.example.catchy.SpotifyAppRemoteSingleton;
import com.example.catchy.models.Song;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.example.catchy.service.SpotifyService;

import java.util.ArrayList;
import java.util.List;

import static com.parse.Parse.getApplicationContext;


public class HomeFragment extends Fragment {
    HomeFragmentAdapter homeFragmentAdapter;
    ViewPager2 mViewPager;
    List<Song> arr;
    private SpotifyBroadcastReceiver SpotifyBroadcastReceiver;
    Context context;


    // private String CLIENT_ID;
    // private static final String REDIRECT_URI = "http://com.example.catchy./callback";
    // SpotifyAppRemoteSingleton singleton;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arr = ((MainActivity)getActivity()).arr;
        homeFragmentAdapter = new HomeFragmentAdapter(this, arr);

        SpotifyBroadcastReceiver = new SpotifyBroadcastReceiver();
        context = getContext();


        // CLIENT_ID = getString(R.string.spotify_client_id);
        // singleton = SpotifyAppRemoteSingleton.getInstance();

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(homeFragmentAdapter);
        mViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        getActivity().startService(new Intent(getActivity(),SpotifyService.class));

        /*        // Set the connection parameters
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

                        }

                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("HomeFragment", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
        */

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }




    @Override
    public void onStop() {
        super.onStop();
        // SpotifyAppRemote.disconnect(singleton.getSpotifyAppRemote());
    }

    @Override
    public void onPause() {
        super.onPause();
        // singleton.getSpotifyAppRemote().getPlayerApi().pause();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(SpotifyBroadcastReceiver);
        Log.d("HomeFragment", "Paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(SpotifyService.ACTION);
        LocalBroadcastManager.getInstance(context).registerReceiver(SpotifyBroadcastReceiver, filter);
        // or `registerReceiver(testReceiver, filter)` for a normal broadcast
    }
}