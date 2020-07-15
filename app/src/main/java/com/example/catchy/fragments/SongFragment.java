package com.example.catchy.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.catchy.R;
import com.example.catchy.SpotifyAppRemoteSingleton;
import com.example.catchy.models.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.Track;
import com.squareup.picasso.Picasso;


public class SongFragment extends Fragment {
    Song song;
    private TextView tvTitle;
    private TextView tvArtist;
    private ImageView ivAlbumImage;
    private FloatingActionButton btnLike;
    SpotifyAppRemoteSingleton singleton;

    public SongFragment() {
        // Required empty public constructor
    }


    public static SongFragment newInstance(Song song) {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putParcelable("song", song);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            song = getArguments().getParcelable("song");
            singleton = SpotifyAppRemoteSingleton.getInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_song, container, false);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvArtist = view.findViewById(R.id.tvArtist);
        ivAlbumImage = view.findViewById(R.id.ivAlbumImage);
        btnLike = view.findViewById(R.id.btnLike);

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLike.setImageResource(R.drawable.ic_likes_filled);
                btnLike.setColorFilter(getResources().getColor(R.color.medium_red));
            }
        });

        tvTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());
        Picasso.with(getContext()).load(song.getImageUrl()).into(ivAlbumImage);

//        singleton.getSpotifyAppRemote().getPlayerApi()
//                .subscribeToPlayerState()
//                .setEventCallback(playerState -> {
//                    final Track track = playerState.track;
//                    if (track != null) {
//                        Log.d("SongFragment", track.name + " by " + track.artist.name);
//                        tvTitle.setText(track.name);
//                        tvArtist.setText(track.artist.name);
//
//                        String img = track.imageUri.raw.substring(14);
//                        Log.d("SongFragment", "raw: " + img);
//                        Picasso.with(getContext()).load("https://i.scdn.co/image/"+ img).into(ivAlbumImage);
//                    }
//                });
        singleton.getSpotifyAppRemote().getPlayerApi().play(song.getURI());
        return view;
    }
}