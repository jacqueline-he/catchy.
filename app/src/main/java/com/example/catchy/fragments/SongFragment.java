package com.example.catchy.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.catchy.misc.DetailTransition;
import com.example.catchy.R;
import com.example.catchy.activities.SongDetailsActivity;
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
    private static final int LOOP_DURATION = 500;
    public static final String TAG = "SongFragment";
    boolean liked = false;
    boolean enteringSongDetails = false;
    private Handler mHandler;
    private Runnable mSeekRunnable;
    long progress = 0;
    boolean paused = false;

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
        tvTitle.setSelected(true);
        tvArtist = view.findViewById(R.id.tvArtist);
        tvArtist.setSelected(true);
        ivAlbumImage = view.findViewById(R.id.ivAlbumImage);
        btnLike = view.findViewById(R.id.btnLike);
        DetailTransition.liked = false;

        new Thread(() -> {
            try {
                DetailTransition.bitmap = Picasso.get().load(song.getImageUrl()).get();
            } catch (Exception e) {
                Log.e("SongFragment", "couldn't get bitmap"+e);
            }
        }).start();



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
                    if (paused) { // unpause
                        spotifyBroadcastReceiver.enqueueService(getContext(), SpotifyBroadcastReceiver.ACTION_PLAY);
                    }
                    else { // pause
                        spotifyBroadcastReceiver.enqueueService(getContext(), SpotifyBroadcastReceiver.ACTION_PAUSE);
                    }
                    paused = !paused;
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
        Glide.with(this).load(song.getImageUrl()).into(ivAlbumImage);

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

        // TODO pause mHandler when leaving fragment; update when resumed; restart to 0 if exceeds 30 sec
        // TODO add toggle for play to completion or not
        mHandler = new Handler();
        mSeekRunnable = new Runnable() {
            @Override
            public void run() {
                progress += LOOP_DURATION;
                mHandler.postDelayed(this, LOOP_DURATION);
            }
        };
        mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);

        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteringSongDetails = true; // pause
                mHandler.removeCallbacks(mSeekRunnable);
                Intent intent = new Intent(getContext(), SongDetailsActivity.class);
                // pack something
                intent.putExtra("song", song);
                intent.putExtra("liked", liked);
                intent.putExtra("from", "home");
                intent.putExtra("paused", paused); // SongFragment only
                intent.putExtra("progress", progress); // SongFragment only
                startActivity(intent);
            }
        });

        spotifyBroadcastReceiver.playNew(getContext(), song.getURI());

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
    public void onResume() {
        super.onResume();
        // if (fromDetails) {
            enteringSongDetails = DetailTransition.enteringSongDetails; // should be false
            liked = DetailTransition.liked; // should be true
            if (liked) {
                btnLike.setImageResource(R.drawable.ic_likes_filled);
                btnLike.setColorFilter(getResources().getColor(R.color.medium_red));
            }
            progress = DetailTransition.progress; // update from SongDetailsActivity
            mHandler.removeCallbacks(mSeekRunnable);    // resume counting
            mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
        // }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("SongFragment", "stopped " + song.getTitle() + ", like is " + liked);
        // update like
        if (liked && ! enteringSongDetails) {
            Like like = new Like();
            like.setTitle(song.getTitle());
            like.setArtist(song.getArtist());
            like.setImageUrl(song.getImageUrl());
            like.setURI(song.getURI());
            like.setDuration(song.getDuration());
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