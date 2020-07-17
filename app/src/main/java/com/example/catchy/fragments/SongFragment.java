package com.example.catchy.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.catchy.R;
import com.example.catchy.SpotifyAppRemoteSingleton;
import com.example.catchy.models.Song;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.Serializable;


public class SongFragment extends Fragment {
    Song song;
    private TextView tvTitle;
    private TextView tvArtist;
    private ImageView ivAlbumImage;
    private FloatingActionButton btnLike;
    private SpotifyBroadcastReceiver spotifyBroadcastReceiver;

    public SongFragment() {
        // Required empty public constructor
    }


    public static SongFragment newInstance(Song song, SpotifyBroadcastReceiver receiver) {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putParcelable("song", song);
        args.putSerializable("receiver", (Serializable) receiver);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            song = getArguments().getParcelable("song");
            spotifyBroadcastReceiver = (SpotifyBroadcastReceiver) getArguments().getSerializable("receiver");
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


        // Double tap
        view.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override // Like
                public boolean onDoubleTap(MotionEvent e) {
                    like();
                    return super.onDoubleTap(e);
                }

                @Override // Pause / resume
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    spotifyBroadcastReceiver.enqueueService(getContext(), SpotifyBroadcastReceiver.ACTION_PLAY_PAUSE);
                    return super.onSingleTapConfirmed(e);
                }
            });


            @Override
            public boolean onTouch(View view, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                like();
            }
        });

        tvTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());
        Picasso.with(getContext()).load(song.getImageUrl()).into(ivAlbumImage);


        spotifyBroadcastReceiver.playNew(getContext(), song.getURI());
        // spotifyBroadcastReceiver.enqueueService(getContext(), SpotifyBroadcastReceiver.ACTION_GET_RECS);

        return view;
    }

    private void like() {
        if (!song.isLiked()) {
            btnLike.setImageResource(R.drawable.ic_likes_filled);
            btnLike.setColorFilter(getResources().getColor(R.color.medium_red));

            // Set like on Parse side
            song.setLike(true);
        }
        else {
            btnLike.setImageResource(R.drawable.ic_likes);
            btnLike.clearColorFilter();

            song.setLike(false);
        }
    }
}