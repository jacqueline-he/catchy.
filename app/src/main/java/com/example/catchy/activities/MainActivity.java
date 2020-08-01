package com.example.catchy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.catchy.R;
import com.example.catchy.fragments.HomeFragment;
import com.example.catchy.fragments.SearchFragment;
import com.example.catchy.fragments.UserFragment;
import com.example.catchy.models.Following;
import com.example.catchy.models.Song;
import com.example.catchy.models.User;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;
    SpotifyBroadcastReceiver spotifyBroadcastReceiver;
    Fragment fragment;

    // API stuff
    private static final int REQUEST_CODE = 1337;
    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private,user-top-read";


    public SpotifyBroadcastReceiver getReceiver() {
        return spotifyBroadcastReceiver;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boolean returning = getIntent().getBooleanExtra("returning", false);

        // If the user is a returning user, we don't need to show the dialog option again. Otherwise, we can show it
        authenticateSpotify(!returning);


        spotifyBroadcastReceiver = new SpotifyBroadcastReceiver();
        spotifyBroadcastReceiver.initService(this);


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

        new Thread(() -> {
                        try {
                            User.profileBitmap = Picasso.get().load(ParseUser.getCurrentUser().getParseFile("profilePic").getUrl()).get();
                        } catch (Exception e) {
                            Log.e(TAG, "couldn't get bitmap"+e);
                        }
                    }).start();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SpotifyBroadcastReceiver.enqueueService(this, SpotifyBroadcastReceiver.ACTION_DISCONNECT);
        deleteSongs();


        for (Following item : User.followingItems) {
            item.deleteInBackground();
        }

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (User.following != null) {       // This is if UserFragment is never reached
            for (ParseUser user : User.following) {
                Following following = new Following();
                following.setFollowedBy(currentUser);
                following.setFollowing(user);
                following.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error while saving relationship", e);
                            e.printStackTrace();
                        }
                        Log.i(TAG, "Relationship save was successful!");
                    }
                });
            }
            User.followers = null;
            User.following = null;
        }

    }

    private void deleteSongs() {
        ParseQuery<Song> query = ParseQuery.getQuery(Song.class);
        query.whereEqualTo("seen", true);
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> posts, ParseException e) {
                if (e != null) {
                    Log.e("MainActivity", "Issue with getting posts", e);
                    return;
                }
                for (int i = 0; i < posts.size(); i++) {
                    posts.get(i).deleteInBackground();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    ParseUser.getCurrentUser().put("token", response.getAccessToken());
                    ParseUser.getCurrentUser().saveInBackground();
                    Log.d(TAG, "Successfully got auth token");
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.d(TAG, "Error authenticating!");
                    retryLogin();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    Log.d(TAG, "Authentication flow cancelled");
                    retryLogin();
            }
        }
    }

    private void retryLogin() {
        ParseUser.logOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }


    private void authenticateSpotify(boolean dialog) {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(getString(R.string.spotify_client_id), AuthenticationResponse.Type.TOKEN, "http://com.example.catchy./callback");
        builder.setScopes(new String[]{SCOPES});
        builder.setShowDialog(dialog);
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }
}