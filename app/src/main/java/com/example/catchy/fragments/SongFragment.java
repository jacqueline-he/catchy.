package com.example.catchy.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityOptionsCompat;
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
import com.example.catchy.activities.MainActivity;
import com.example.catchy.misc.DetailTransition;
import com.example.catchy.R;
import com.example.catchy.activities.SongDetailsActivity;
import com.example.catchy.models.Like;
import com.example.catchy.models.Song;
import com.example.catchy.models.User;
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
    private Runnable mRunnable;
    long progress = 0;
    boolean paused = false;

    boolean durationPref;

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
            DetailTransition.progress = 0;

            if (!User.passedFirstSong) {
                User.firstSong = song;
                User.passedFirstSong = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song, container, false);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setSelected(true);
        tvArtist = view.findViewById(R.id.tvArtist);
        tvArtist.setSelected(true);
        ivAlbumImage = view.findViewById(R.id.ivAlbumImage);
        btnLike = view.findViewById(R.id.btnLike);
        DetailTransition.liked = false;

        durationPref = ParseUser.getCurrentUser().getBoolean("durationPref"); // true  -> play full-length, false -> play 30-sec. snippet


        new Thread(() -> {
            try {
                DetailTransition.bitmap = Picasso.get().load(song.getImageUrl()).get();
            } catch (Exception e) {
                Log.e("SongFragment", "couldn't get bitmap" + e);
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
                    } else { // pause
                        spotifyBroadcastReceiver.enqueueService(getContext(), SpotifyBroadcastReceiver.ACTION_PAUSE);
                    }
                    paused = !paused;
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
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
        Context context = getContext();
        spotifyBroadcastReceiver.playNew(context, song.getURI());


        mHandler = new Handler();
        if (!durationPref) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    progress += LOOP_DURATION;
                    mHandler.postDelayed(this, LOOP_DURATION);
                }
            };
        } else { // 30-sec. snippet
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    if (progress == 30000) {
                        progress = 0;
                        spotifyBroadcastReceiver.updatePlayer(context, -30000);
                        mHandler.postDelayed(this, LOOP_DURATION);
                    } else {
                        progress += LOOP_DURATION;
                        mHandler.postDelayed(this, LOOP_DURATION);
                    }

                }
            };
        }

        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteringSongDetails = true; // pause
                mHandler.removeCallbacks(mRunnable);
                Intent intent = new Intent(getContext(), SongDetailsActivity.class);
                // pack something
                intent.putExtra("song", song);
                intent.putExtra("liked", liked);
                intent.putExtra("from", "home");
                intent.putExtra("paused", paused); // SongFragment only
                intent.putExtra("progress", progress); // SongFragment only
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation((Activity) context, (View)ivAlbumImage, "albumImg");
                startActivity(intent, options.toBundle());
            }
        });

        return view;
    }

    private void like() {
        if (!liked) {
            btnLike.setImageResource(R.drawable.ic_likes_filled);
            btnLike.setColorFilter(getResources().getColor(R.color.medium_red));
            liked = true;
        } else {
            btnLike.setImageResource(R.drawable.ic_likes);
            btnLike.clearColorFilter();
            liked = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        enteringSongDetails = DetailTransition.enteringSongDetails; // should be false
        liked = DetailTransition.liked; // should be true
        if (liked) {
            btnLike.setImageResource(R.drawable.ic_likes_filled);
            btnLike.setColorFilter(getResources().getColor(R.color.medium_red));
        }
        progress = DetailTransition.progress; // update from SongDetailsActivity
        DetailTransition.progress = 0; // reset
        mHandler.removeCallbacks(mRunnable);    // resume counting
        mHandler.postDelayed(mRunnable, LOOP_DURATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("SongFragment", "stopped " + song.getTitle() + ", like is " + liked);
        // update like
        if (liked && !enteringSongDetails) {
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
        mHandler.removeCallbacks(mRunnable);
    }
}