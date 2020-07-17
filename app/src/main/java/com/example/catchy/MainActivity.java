package com.example.catchy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.JobIntentService;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.catchy.fragments.HomeFragment;
import com.example.catchy.fragments.SearchFragment;
import com.example.catchy.fragments.UserFragment;
import com.example.catchy.models.Song;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.example.catchy.service.SpotifyService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;
    SpotifyBroadcastReceiver spotifyBroadcastReceiver;
    Fragment fragment;
    public List<Song> arr;

    public SpotifyBroadcastReceiver getReceiver() {
        return spotifyBroadcastReceiver;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arr = new ArrayList<Song>();
        spotifyBroadcastReceiver = new SpotifyBroadcastReceiver();
        spotifyBroadcastReceiver.initService(this);

        try {
            populatePlaylist();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        fragment = new HomeFragment();
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        break;
                    case R.id.action_search:
                        fragment = new SearchFragment();
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        break;
                    case R.id.action_user:
                    default:
                        fragment = new UserFragment();
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        break;
                }

                return true;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    private void populatePlaylist() throws ParseException {
        ParseQuery<Song> query = new ParseQuery<>(Song.class);
        query.addDescendingOrder("createdAt");
        query.setLimit(20);
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting songs", e);
                    return;
                }
                for (Song song : objects) {
                    Log.i(TAG, "Song: " + song.getTitle());
                    arr.add(song);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SpotifyBroadcastReceiver.enqueueService(this, SpotifyBroadcastReceiver.ACTION_DISCONNECT);
    }
}