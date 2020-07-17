package com.example.catchy.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.catchy.R;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class SpotifyService extends JobIntentService {
    private static final String REDIRECT_URI = "http://com.example.catchy./callback";
    public static final String TAG = "SpotifyService";
    private static SpotifyAppRemote mSpotifyAppRemote;
    private static PlayerApi mPlayerApi;
    private static boolean mIsSpotifyConnected;
    public static final String ACTION = "com.example.catchy.service.SpotifyService";


    @Override
    public void onCreate() {
        super.onCreate();
        connectSpotifyRemote(getApplicationContext(), getString(R.string.spotify_client_id));
    }

    private void connectSpotifyRemote(Context context, String clientID) {
        if (!mIsSpotifyConnected) {
            ConnectionParams connectionParams =
                    new ConnectionParams.Builder(clientID)
                            .setRedirectUri(REDIRECT_URI)
                            .showAuthView(true)
                            .build();

            SpotifyAppRemote.connect(context, connectionParams,
                    new Connector.ConnectionListener() {

                        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                            mSpotifyAppRemote = spotifyAppRemote;
                            Log.d(TAG, "Connected! Yay!");

                            // Now you can start interacting with App Remote
                            mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

                        }

                        public void onFailure(Throwable throwable) {
                            Log.e(TAG, throwable.getMessage(), throwable);

                            // Something went wrong when attempting to connect! Handle errors here
                        }
                    });
        }
    }

    // play track

    // pause track






    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "Inside onHandleWork");
        String val = intent.getStringExtra("foo");
        Intent in = new Intent(ACTION);
        in.putExtra("resultCode", Activity.RESULT_OK);
        in.putExtra("resultValue", "My Result Value. Passed in: " + val);
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
