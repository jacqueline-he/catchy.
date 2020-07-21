package com.example.catchy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.catchy.R;
import com.example.catchy.models.Song;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongDetailsActivity extends AppCompatActivity {
    private ImageView ivAlbumImage;
    private TextView tvTitle;
    private TextView tvArtist;
    private FloatingActionButton btnLike;
    private boolean liked;
    private Song song;
    SpotifyBroadcastReceiver receiver;
    boolean playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_details);
        receiver = new SpotifyBroadcastReceiver();
        ivAlbumImage = findViewById(R.id.ivAlbumImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        btnLike = findViewById(R.id.btnLike);

        Intent intent = getIntent();
        song = (Song) intent.getExtras().get("song");
        playing = (boolean) intent.getExtras().get("playing");
        if (!playing) {
            receiver.playNew(this, song.getURI());
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
        if (!playing) {
            receiver.enqueueService(this, SpotifyBroadcastReceiver.ACTION_PAUSE);
        }
    }
}