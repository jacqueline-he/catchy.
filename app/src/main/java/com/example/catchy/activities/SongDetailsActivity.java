package com.example.catchy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_details);
        receiver = new SpotifyBroadcastReceiver();
        ivAlbumImage = findViewById(R.id.ivAlbumImage);

        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setSelected(true);
        tvTitle.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // do nothing
            }
        });

        tvArtist = findViewById(R.id.tvArtist);
        tvArtist.setSelected(true);
        tvArtist.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // do nothing
            }
        });

        btnLike = findViewById(R.id.btnLike);
        btnPlayPause = findViewById(R.id.btnPlayPause);

        btnRewind = findViewById(R.id.btnRewind);
        btnForward = findViewById(R.id.btnForward);

        seekbar = findViewById(R.id.seekbar);
        tvCurrPos = findViewById(R.id.tvCurrPos);
        tvFullPos = findViewById(R.id.tvFullPos);

        Intent intent = getIntent();
        song = (Song) intent.getExtras().get("song");
        from = intent.getStringExtra("from");

        if (!from.equals("home")) {
            receiver.playNew(this, song.getURI());
        }
        else {
            paused = intent.getBooleanExtra("paused", false);
            if (paused) { // set button icon to pause
                btnPlayPause.setImageResource(R.drawable.ic_play128128);
            }
            progress = intent.getLongExtra("progress", 0);
            Log.d("SongDetailsActivity", "progress: " + progress);

        }

        setSeekbar();

        liked = (boolean) intent.getExtras().get("liked");
        if (liked) {
            btnLike.setImageResource(R.drawable.ic_likes_filled);
            btnLike.setColorFilter(getResources().getColor(R.color.medium_red));
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
                }
                else {
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

        View view = findViewById(R.id.layout);
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
        int minutes = (int) ((song.getDuration() / 1000)  / 60);
        int seconds = (int)((song.getDuration() / 1000) % 60);
        String time;
        if (seconds < 10) {
            time = minutes + ":0" + seconds;
        }
        else {
            time = minutes + ":" + seconds;
        }
        tvFullPos.setText(time);
        if (paused) {
            songProgressBar.pause();
        }

    }

    private void setBackgroundColor() {
/*        try {
            URL url = new URL(song.getImageUrl());
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch(IOException e) {
            System.out.println(e);
        }*/

        if (DetailTransition.bitmap != null && !DetailTransition.bitmap.isRecycled()) {
            Palette palette = Palette.from(DetailTransition.bitmap).generate();
            Palette.Swatch swatch = palette.getDarkVibrantSwatch();
            // int color = palette.getDarkMutedColor(0);
            if (swatch == null) {
                swatch = palette.getDominantSwatch();
            }

            // swatch.getRgb()
            if (swatch != null) {
                // ((RelativeLayout) findViewById(R.id.layout)).setBackgroundColor(swatch.getRgb());

                int color = swatch.getRgb();
                RevelyGradient
                        .linear()
                        .colors(new int[] {Color.parseColor("#212121"), color}).angle(90f).alpha(0.76f)
                        .onBackgroundOf(findViewById(R.id.layout));

                btnPlayPause.setColorFilter(swatch.getRgb());
            }


        }

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
    public void onBackPressed() {
        super.onBackPressed();
        DetailTransition.liked = liked;
        DetailTransition.enteringSongDetails = false;
        DetailTransition.song = song;
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
        }
        else if (from.equals("user") && ! liked) {
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

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}