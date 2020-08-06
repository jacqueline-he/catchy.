package com.example.catchy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.catchy.databinding.ActivitySettingsPrefBinding;
import com.example.catchy.databinding.ActivitySongDetailsBinding;
import com.example.catchy.misc.DetailTransition;
import com.example.catchy.misc.MarqueeTextView;
import com.example.catchy.R;
import com.example.catchy.misc.SongProgressBar;
import com.example.catchy.models.Like;
import com.example.catchy.models.Song;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import co.revely.gradient.RevelyGradient;

public class SongDetailsActivity extends AppCompatActivity {
    private ImageView ivAlbumImage;
    private MarqueeTextView tvTitle;
    private MarqueeTextView tvArtist;
    private FloatingActionButton btnLike;
    private Song song;
    SpotifyBroadcastReceiver receiver;
    boolean liked;
    FloatingActionButton btnPlayPause;
    FloatingActionButton btnRewind; // -5 seconds
    FloatingActionButton btnForward; // +5 seconds
    String from;
    boolean paused = false;
    SongProgressBar songProgressBar;
    long progress = 0;
    SeekBar seekbar;
    TextView tvCurrPos;
    TextView tvFullPos;
    RelativeLayout layout;
    ActivitySongDetailsBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new SpotifyBroadcastReceiver();


        binding = ActivitySongDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tvTitle = binding.tvTitle;
        tvTitle.setSelected(true);
        tvTitle.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // do nothing
            }
        });

        tvArtist = binding.tvArtist;
        tvArtist.setSelected(true);
        tvArtist.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // do nothing
            }
        });

        ivAlbumImage = binding.ivAlbumImage;

        btnLike = binding.btnLike;
        btnPlayPause = binding.btnPlayPause;

        btnRewind = binding.btnRewind;
        btnForward = binding.btnForward;

        seekbar = binding.seekbar;
        tvCurrPos = binding.tvCurrPos;
        tvFullPos = binding.tvFullPos;

        layout = binding.layout;

        Intent intent = getIntent();
        song = (Song) intent.getExtras().get("song");
        from = intent.getStringExtra("from");

        if (!from.equals("home")) {
            receiver.playNew(this, song.getURI());
        } else {
            paused = intent.getBooleanExtra("paused", false);
            if (paused) { // set button icon to pause
                btnPlayPause.setImageResource(R.drawable.ic_play128128);
            }
            progress = intent.getLongExtra("progress", 0);
            progress %= (song.getDuration());
            Log.d("SongDetailsActivity", "progress: " + progress);

        }

        setSeekbar();

        liked = (boolean) intent.getExtras().get("liked");
        if (liked) {
            btnLike.setImageResource(R.drawable.ic_likes_filled);
            btnLike.setColorFilter(getResources().getColor(R.color.medium_red));
        } else {
            btnLike.setImageResource(R.drawable.ic_likes);
            btnLike.clearColorFilter();
        }

        tvTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());

        Glide.with(this).load(song.getImageUrl()).into(ivAlbumImage);

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                like();
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receiver.enqueueService(SongDetailsActivity.this, SpotifyBroadcastReceiver.ACTION_PLAY_PAUSE);
                if (paused) { // not playing
                    btnPlayPause.setImageResource(R.drawable.ic_pause128128);
                    songProgressBar.unpause();
                } else {
                    btnPlayPause.setImageResource(R.drawable.ic_play128128);

                    songProgressBar.pause();
                }
                paused = !paused;
            }
        });


        btnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // rewind 5 seconds
                receiver.updatePlayer(SongDetailsActivity.this, -5000);
                songProgressBar.skip(-5000);
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // fast-forward 5 seconds
                receiver.updatePlayer(SongDetailsActivity.this, 5000);
                songProgressBar.skip(5000);
            }
        });

        View view = binding.layout;

        view.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(SongDetailsActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override // Like
                public boolean onDoubleTap(MotionEvent e) {
                    like();
                    return super.onDoubleTap(e);
                }
            });


            @Override
            public boolean onTouch(View view, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        setBackgroundColor();

    }

    public ActivitySongDetailsBinding getBinding() {
        return binding;
    }

    private void setSeekbar() {
        seekbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        songProgressBar = new SongProgressBar(seekbar, this);
        songProgressBar.setMax(song.getDuration());
        songProgressBar.update(progress);
        int minutes = (int) ((song.getDuration() / 1000) / 60);
        int seconds = (int) ((song.getDuration() / 1000) % 60);
        String time;
        if (seconds < 10) {
            time = minutes + ":0" + seconds;
        } else {
            time = minutes + ":" + seconds;
        }
        tvFullPos.setText(time);
        if (paused) {
            songProgressBar.pause();
        }

    }

    private void setBackgroundColor() {
        if (DetailTransition.bitmap != null && !DetailTransition.bitmap.isRecycled()) {
            Palette palette = Palette.from(DetailTransition.bitmap).generate();
            Palette.Swatch swatch = palette.getDarkVibrantSwatch();

            if (swatch == null) {
                swatch = palette.getDominantSwatch();
            }

            if (swatch != null) {
                int color = swatch.getRgb();
                RevelyGradient
                        .linear()
                        .colors(new int[]{Color.parseColor("#212121"), color}).angle(90f).alpha(0.76f)
                        .onBackgroundOf(layout);

                btnPlayPause.setColorFilter(swatch.getRgb());
            }
        } else {
            RevelyGradient
                    .linear()
                    .colors(new int[]{Color.parseColor("#000000"), Color.parseColor("#00EDFF")}).angle(90f).alpha(0.76f)
                    .onBackgroundOf(layout);
        }

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
    public void onBackPressed() {
        super.onBackPressed();
        DetailTransition.liked = liked;
        DetailTransition.enteringSongDetails = false;
        DetailTransition.song = song;
        DetailTransition.progress = songProgressBar.getSeekBar().getProgress();
        if (!from.equals("home")) {
            receiver.enqueueService(this, SpotifyBroadcastReceiver.ACTION_PAUSE);
        }

        if (from.equals("search") && liked) {
            // Add song to likes
            Like like = new Like();
            like.setTitle(song.getTitle());
            like.setArtist(song.getArtist());
            like.setImageUrl(song.getImageUrl());
            like.setURI(song.getURI());
            like.setLikedBy(ParseUser.getCurrentUser());
            like.setDuration(song.getDuration());

            like.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e("SongDetailsActivity", "Error while saving", e);
                        e.printStackTrace();
                    }
                    Log.i("SongDetailsActivity", "Post save was successful!");
                }
            });
        } else if (from.equals("user") && !liked) {
            // Remove this song from likes
            ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
            query.include(Like.KEY_LIKED_BY);
            query.whereEqualTo("title", song.getTitle());
            query.findInBackground(new FindCallback<Like>() {
                @Override
                public void done(List<Like> objects, ParseException e) {
                    if (e != null) {
                        Log.e("SongDetailsActivity", "Issue with getting post to remove", e);
                        return;
                    }
                    objects.get(0).deleteInBackground();
                }
            });
            supportFinishAfterTransition();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        songProgressBar.pause();
    }


}