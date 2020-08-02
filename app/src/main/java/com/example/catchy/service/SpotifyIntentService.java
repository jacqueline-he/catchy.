package com.example.catchy.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.catchy.R;
import com.example.catchy.models.User;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_DISCONNECT;
import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_INIT;
import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_PAUSE;
import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_PLAY;
import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_PLAY_PAUSE;
import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_UPDATE;
import static com.example.catchy.service.SpotifyBroadcastReceiver.PLAYBACK_POS_KEY;
import static com.spotify.protocol.types.Repeat.ONE;

public class SpotifyIntentService extends JobIntentService {
    private static final String REDIRECT_URI = "http://com.example.catchy./callback";
    public static final String TAG = "SpotifyIntentService";
    private static SpotifyAppRemote mSpotifyAppRemote;
    private static PlayerApi mPlayerApi;
    private static boolean mIsSpotifyConnected;
    public static final String ACTION = "com.example.catchy.service.SpotifyIntentService";
    private static boolean initPlayingNew = true;
    private static int count = 0;


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
                            mSpotifyAppRemote = spotifyAppRemote;
                            mPlayerApi = mSpotifyAppRemote.getPlayerApi();
                            mIsSpotifyConnected = true;

                            if (User.firstSong != null) {
                                playNewSong(User.firstSong.getURI());
                                User.firstSong = null;
                            } else { // Parse not updating in time
                                initPlayingNew = false;
                            }
                        }

                        public void onFailure(Throwable error) {
                            Log.e(TAG, error.getMessage(), error);
                            mIsSpotifyConnected = false;
                        }
                    });
        }
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "Handling work with intent: [" + intent + "]");
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_INIT:
                    initialize();
                    break;
                case ACTION_PLAY:
                    String newSongId = intent.getStringExtra("songuri");
                    playNewSong(newSongId);
                    break;
                case ACTION_PLAY_PAUSE:
                    playPause();
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_UPDATE:
                    long seekPosition = intent.getLongExtra(PLAYBACK_POS_KEY, 5);
                    seekTo(seekPosition);
                    break;
                case ACTION_DISCONNECT:
                    disconnect();
                    break;
            }
        }
    }

    private void pause() {
        Log.d(TAG, "pause");
        Intent in = new Intent(ACTION_PAUSE);
        if (mPlayerApi != null && mSpotifyAppRemote.isConnected()) {
            mPlayerApi.getPlayerState().setResultCallback(playerState -> {
                mPlayerApi.pause();
                LocalBroadcastManager.getInstance(this).sendBroadcast(in);
            });
        }
    }

    private void playPause() {
        Log.d(TAG, "playPause");
        Intent in = new Intent(ACTION_PLAY_PAUSE);
        if (mPlayerApi != null && mSpotifyAppRemote.isConnected()) {
            mPlayerApi.getPlayerState().setResultCallback(playerState -> {
                if (playerState.isPaused) {
                    mPlayerApi.resume();
                } else {
                    mPlayerApi.pause();
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(in);
            });
        }
    }

    private void playNewSong(String newSongId) {
        Log.d(TAG, "playNewSong");
        Intent in = new Intent(ACTION_PLAY);
        if (mPlayerApi != null && mSpotifyAppRemote.isConnected()) {
            mPlayerApi.play(newSongId);
            mPlayerApi.setRepeat(ONE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(in);
            Log.d(TAG, "Playing new song");
        } else {
            Log.e(TAG, "Receiver init too slow");
            if (!initPlayingNew && count < 10) {
                count++;
                playNewSong(newSongId);
            } else {
                Log.e(TAG, "Parse / Receiver init too slow");
            }
        }
    }

    private void seekTo(long progress) {
        Log.d(TAG, "seekTo");
        Intent in = new Intent(ACTION_UPDATE);
        if (mPlayerApi != null && mSpotifyAppRemote.isConnected()) {
            mPlayerApi.seekToRelativePosition(progress)
                    .setErrorCallback(error -> Log.e(TAG, "Cannot seek unless you have premium!", error));
            LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        }
    }

    /**
     * Initialize the service and notify the receiver of the current connection and playback states
     */
    private void initialize() {
        Log.d(TAG, "initialize");
        Intent in = new Intent(ACTION_INIT);
        if (mIsSpotifyConnected) {
            in.putExtra("RESULT", "RESULT_CONNECTED");
        } else {
            in.putExtra("RESULT", "RESULT_DISCONNECTED");
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
    }

    private void disconnect() {
        Log.d(TAG, "disconnect");
        Intent in = new Intent(ACTION_DISCONNECT);
        if (mIsSpotifyConnected) {
            mPlayerApi.pause();
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            mIsSpotifyConnected = false;
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
    }

}
