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


public class HomeFragment extends Fragment{
    HomeFragmentAdapter homeFragmentAdapter;
    ViewPager2 mViewPager;
    List<Song> arr;
    private SpotifyBroadcastReceiver spotifyBroadcastReceiver;
    Context context;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arr = ((MainActivity)getActivity()).arr;
        homeFragmentAdapter = new HomeFragmentAdapter(this, arr);

        spotifyBroadcastReceiver = new SpotifyBroadcastReceiver();
        context = getContext();

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(homeFragmentAdapter);
        mViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        spotifyBroadcastReceiver.initService(getContext());

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
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(spotifyBroadcastReceiver);
        Log.d("HomeFragment", "Paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(SpotifyService.ACTION);
        LocalBroadcastManager.getInstance(context).registerReceiver(spotifyBroadcastReceiver, filter);
        // or `registerReceiver(testReceiver, filter)` for a normal broadcast
    }
}