package com.example.catchy.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.catchy.R;
import com.example.catchy.SpotifyAppRemoteSingleton;


public class SongFragment extends Fragment {
    String song;
    private TextView tvTitle;
    SpotifyAppRemoteSingleton singleton;

    public SongFragment() {
        // Required empty public constructor
    }


    public static SongFragment newInstance(String str) {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putString("song", str);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            song = getArguments().getString("song");
            singleton = SpotifyAppRemoteSingleton.getInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_song, container, false);
        tvTitle = view.findViewById(R.id.songTitle);
        tvTitle.setText(song);
        singleton.getSpotifyAppRemote().getPlayerApi().play(song);
        return view;
    }
}