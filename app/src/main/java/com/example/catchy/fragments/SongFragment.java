package com.example.catchy.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.catchy.R;
import com.example.catchy.SpotifyAppRemoteSingleton;
import com.example.catchy.models.Like;
import com.example.catchy.models.Song;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.Serializable;


public class SongFragment extends Fragment {
    Song song;
    private TextView tvTitle;
    private TextView tvArtist;
    private ImageView ivAlbumImage;
    private FloatingActionButton btnLike;
    private SpotifyBroadcastReceiver spotifyBroadcastReceiver;
    public static final String TAG = "SongFragment";
    boolean liked = false;

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

        song.setSeen(true);
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


        spotifyBroadcastReceiver.playNew(getContext(), song.getURI());
        // spotifyBroadcastReceiver.enqueueService(getContext(), SpotifyBroadcastReceiver.ACTION_GET_RECS);

        return view;
    }

    private void like() {
        if (!liked) {
            btnLike.setImageResource(R.drawable.ic_likes_filled);
            btnLike.setColorFilter(getResources().getColor(R.color.medium_red));
            liked = true;
        }
        else {
            btnLike.setImageResource(R.drawable.ic_likes);
            btnLike.clearColorFilter();
            liked = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("SongFragment", "paused " + song.getTitle() + ", like is " + liked);
        // update like
        if (liked) {
            Like like = new Like();
            like.setTitle(song.getTitle());
            like.setArtist(song.getArtist());
            like.setImageUrl(song.getImageUrl());
            like.setURI(song.getURI());
            like.setLikedBy(ParseUser.getCurrentUser());

            like.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e("SongFragment", "Error while saving", e);
                        e.printStackTrace();
                    }
                    Log.i("SongFragment", "Post save was successful!");
                }
            });
        }
    }
}