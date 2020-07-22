package com.example.catchy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.catchy.DetailTransition;
import com.example.catchy.R;
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

public class SongDetailsActivity extends AppCompatActivity {
    private ImageView ivAlbumImage;
    private TextView tvTitle;
    private TextView tvArtist;
    private FloatingActionButton btnLike;
    private Song song;
    SpotifyBroadcastReceiver receiver;
    boolean playing;
    boolean liked;
    FloatingActionButton btnPlayPause;
    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_details);
        receiver = new SpotifyBroadcastReceiver();
        ivAlbumImage = findViewById(R.id.ivAlbumImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        btnLike = findViewById(R.id.btnLike);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPlayPause.setTag(R.drawable.ic_pause128128);

        Intent intent = getIntent();
        song = (Song) intent.getExtras().get("song");
        playing = (boolean) intent.getExtras().get("playing");
        from = intent.getStringExtra("from");

        if (!playing) {
            receiver.playNew(this, song.getURI());
        }

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
                if ((Integer)btnPlayPause.getTag() == R.drawable.ic_play128128) {
                    btnPlayPause.setImageResource(R.drawable.ic_pause128128);
                    btnPlayPause.setTag(R.drawable.ic_pause128128);
                }
                else {
                    btnPlayPause.setImageResource(R.drawable.ic_play128128);
                    btnPlayPause.setTag(R.drawable.ic_play128128);
                }
            }
        });

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
        if (!playing) {
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
}