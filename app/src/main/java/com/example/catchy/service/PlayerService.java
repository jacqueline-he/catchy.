package com.example.catchy.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.example.catchy.R;
import com.parse.SaveCallback;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import static com.example.catchy.service.PlayerResultReceiver.ACTION_DISCONNECT;
import static com.example.catchy.service.PlayerResultReceiver.ACTION_INIT;
import static com.example.catchy.service.PlayerResultReceiver.ACTION_PLAY;
import static com.example.catchy.service.PlayerResultReceiver.ACTION_PLAY_PAUSE;
import static com.example.catchy.service.PlayerResultReceiver.ACTION_SKIP;
import static com.example.catchy.service.PlayerResultReceiver.ACTION_UPDATE;
import static com.example.catchy.service.PlayerResultReceiver.PAUSED_KEY;
import static com.example.catchy.service.PlayerResultReceiver.PLAYBACK_POS_KEY;
import static com.example.catchy.service.PlayerResultReceiver.RECEIVER_KEY;
import static com.example.catchy.service.PlayerResultReceiver.RESULT_ALBUM_ART;
import static com.example.catchy.service.PlayerResultReceiver.RESULT_CONNECTED;
import static com.example.catchy.service.PlayerResultReceiver.RESULT_DISCONNECTED;
import static com.example.catchy.service.PlayerResultReceiver.RESULT_INSTALL_SPOTIFY;
import static com.example.catchy.service.PlayerResultReceiver.RESULT_NEW_SONG;
import static com.example.catchy.service.PlayerResultReceiver.RESULT_OPEN_SPOTIFY;
import static com.example.catchy.service.PlayerResultReceiver.RESULT_PLAYBACK;
import static com.example.catchy.service.PlayerResultReceiver.RESULT_PLAY_PAUSE;
import static com.example.catchy.service.PlayerResultReceiver.SONG_ID_KEY;
import static com.example.catchy.service.PlayerResultReceiver.bundleBitmap;
import static com.example.catchy.service.PlayerResultReceiver.bundlePlayback;
import static com.example.catchy.service.PlayerResultReceiver.bundleTrack;

public class PlayerService extends JobIntentService {
    public static final String TAG = "PlayerString";
    private static final String REDIRECT_URI = "http://com.example.catchy./callback";
    private static final int NEXT_SONG_PADDING = 2000;


    private static SpotifyAppRemote mSpotifyAppRemote;
    private static PlayerApi mPlayerApi;
    private static Subscription<PlayerState> mPlayerStateSubscription;
    private static ResultReceiver mResultReceiver;
    private static boolean mIsSpotifyConnected;

    private long mTimeRemaining;
    private static String mCurrSongID;
    private static boolean isRepeating;

    // Default constructor
    public PlayerService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connectSpotifyRemote(getApplicationContext(), getString(R.string.spotify_client_id));

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "Handling work with intent: [" + intent + "]");
        if (intent.getAction() != null) {
            mResultReceiver = intent.getParcelableExtra(RECEIVER_KEY);
            switch (intent.getAction()) {
                case ACTION_INIT:
                    initialize();
                    break;
                case ACTION_PLAY:
                    String newSongId = intent.getStringExtra(SONG_ID_KEY);
                    playNewSong(newSongId);
                    break;
                case ACTION_SKIP:
                    playNext();
                    break;
                case ACTION_PLAY_PAUSE:
                    playPause();
                    break;
                case ACTION_UPDATE:
                    long seekPosition = intent.getLongExtra(PLAYBACK_POS_KEY, 0);
                    seekTo(seekPosition);
                    break;
                case ACTION_DISCONNECT:
                    disconnect();
                    break;
            }
        }
    }

    private void seekTo(long progress) {
        if (mPlayerApi != null && mSpotifyAppRemote.isConnected()) {
            mPlayerApi.seekTo(progress)
                    .setErrorCallback(error -> Log.e(TAG, "Cannot seek unless you have premium!", error));
        }
    }

    @SuppressLint("RestrictedApi")
    private void initialize() {
        if (mIsSpotifyConnected) {
            mResultReceiver.send(RESULT_CONNECTED, null);
            getCurrentTrack();
        } else {
            mResultReceiver.send(RESULT_DISCONNECTED, null);
        }
    }

    private void disconnect() {
        pauseRunnable();
        if (mIsSpotifyConnected) {
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            mIsSpotifyConnected = false;
        }
    }

    final Handler runnableHandler = new Handler();
    final Runnable autoplayRunnable = this::playNext;

    private void pauseRunnable() {
        runnableHandler.removeCallbacks(autoplayRunnable);
    }

    private void playRunnable() {
        runnableHandler.removeCallbacks(autoplayRunnable);
        if (mTimeRemaining > NEXT_SONG_PADDING) {
            runnableHandler.postDelayed(autoplayRunnable, mTimeRemaining - NEXT_SONG_PADDING);
        } else {
            Log.e(TAG, "Play runnable called with not enough time remaining"); // This line should never be reached
        }
    }

    /**
     * Connection listener for the Spotify Remote Player connection.
     * On result, notifies the service's result receiver if the remote player is connected.
     * If connected, subscribes to remote player state.
     */
    Connector.ConnectionListener mConnectionListener = new Connector.ConnectionListener() {
        // Called when connection to the Spotify app has been established
        @SuppressLint("RestrictedApi")
        @Override
        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
            mIsSpotifyConnected = true;
            mSpotifyAppRemote = spotifyAppRemote;
            mPlayerApi = mSpotifyAppRemote.getPlayerApi();

            // Notify the receiver that the Spotify remote player is connected
            mResultReceiver.send(RESULT_CONNECTED, null);
            subscribeToPlayerState();
        }

        // Called when connection to the Spotify app fails or is lost
        @SuppressLint("RestrictedApi")
        @Override
        public void onFailure(Throwable error) {
            mIsSpotifyConnected = false;
            disconnect();
            // Notify the receiver that the Spotify remote player is disconnected
            if (mResultReceiver != null) {
                mResultReceiver.send(RESULT_DISCONNECTED, null);
                if (error instanceof NotLoggedInException) {
                    Log.e(TAG, "User is not logged in to Spotify.");
                    mResultReceiver.send(RESULT_OPEN_SPOTIFY, null);
                } else if (error instanceof UserNotAuthorizedException) {
                    Log.e(TAG, "User is not authorized.", error);
                } else if (error instanceof CouldNotFindSpotifyApp) {
                    Log.e(TAG, "User does not have Spotify app installed on device.");
                    mResultReceiver.send(RESULT_INSTALL_SPOTIFY, null);
                }
            }
        }
    };

    private void subscribeToPlayerState() {
        checkExistingSubscription();
        mPlayerStateSubscription = (Subscription<PlayerState>) mPlayerApi.subscribeToPlayerState()
                .setEventCallback(mPlayerStateEventCallback)
                .setLifecycleCallback(new Subscription.LifecycleCallback() {
                    @Override
                    public void onStart() {
                        Log.d(TAG, "PlayerState subscription start");
                    }

                    @Override
                    public void onStop() {
                        Log.d(TAG, "PlayerState subscription end");
                    }
                })
                .setErrorCallback(throwable -> Log.e(TAG, throwable + "Subscribe to PlayerState failed!"));

    }

    private void checkExistingSubscription() {
        if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
            mPlayerStateSubscription = null;
        }
    }

    /**
     * Callback for Spotify remote app player state subscription. onEvent is triggered any time
     * the remote app starts a new track, pauses, plays, or seeks to a new playback position.
     * If a new song begins playing, passes the information to the service receiver.
     */
    @SuppressLint("RestrictedApi")
    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback = playerState -> {
        if (playerState.track != null) {
            if (isNewSong(playerState.track)) {
                Log.d(TAG, "Event with new song " + playerState.track.name);
                updateCurrentSong(playerState);

            } else {
                // Update the receiver with the current playback position
                mResultReceiver.send(RESULT_PLAYBACK, bundlePlayback(playerState));
            }
            // Update the runnable
            mTimeRemaining = playerState.track.duration - playerState.playbackPosition;
            if (playerState.isPaused) {
                pauseRunnable();
            } else {
                playRunnable();
            }
        }
    };

    @SuppressLint("RestrictedApi")
    private void updateCurrentSong(PlayerState playerState) {
        // Delete the "spotify:track:" prefix from the uri
        mCurrSongID = playerState.track.uri.substring(14);
        // Update the receiver
        mResultReceiver.send(RESULT_NEW_SONG, bundleTrack(playerState));
        loadAlbumArt();
    }

    @SuppressLint("RestrictedApi")
    private void loadAlbumArt() {
        mPlayerApi.getPlayerState().setResultCallback(playerState -> {
            mSpotifyAppRemote.getImagesApi()
                    .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                    .setResultCallback(bitmap -> {
                        mResultReceiver.send(RESULT_ALBUM_ART, bundleBitmap(bitmap));
                    });
        });
    }


    private boolean isNewSong(Track track) {
        if (isRepeating) {
            isRepeating = false;
            return true;
        }
        return !track.uri.equals("spotify:track:" + mCurrSongID) && track.name != null;
    }


    private void playPause() {
        if (mPlayerApi != null && mSpotifyAppRemote.isConnected()) {
            mPlayerApi.getPlayerState().setResultCallback(playerState -> {
                if (playerState.isPaused) {
                    resumePlayer();
                } else {
                    pausePlayer();
                }
            });
        }
    }

    @SuppressLint("RestrictedApi")
    private void pausePlayer() {
        mPlayerApi.pause()
                .setResultCallback(empty -> {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(PAUSED_KEY, true);
                    mResultReceiver.send(RESULT_PLAY_PAUSE, bundle);
                })
                .setErrorCallback(throwable -> Log.e(TAG, "Error pausing play!", throwable));
    }

    @SuppressLint("RestrictedApi")
    private void resumePlayer() {
        mPlayerApi.resume()
                .setResultCallback(empty -> {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(PAUSED_KEY, false);
                    mResultReceiver.send(RESULT_PLAY_PAUSE, bundle);
                })
                .setErrorCallback(throwable -> Log.e(TAG, "Error resuming play!", throwable));
    }

    private void playNext() {
    }

    private void playNewSong(String spotifyId) {
        if (mPlayerApi != null && mSpotifyAppRemote.isConnected()) {
            mPlayerApi.play("spotify:track:" + spotifyId)
                    .setResultCallback(empty -> {
                        if (spotifyId.equals(mCurrSongID)) {
                            isRepeating = true;
                        }
                    })
                    .setErrorCallback(throwable -> Log.e(TAG, "Error playing new song " + spotifyId, throwable));
        }
    }



    @SuppressLint("RestrictedApi")
    private void getCurrentTrack() {
        mPlayerApi.getPlayerState().setResultCallback(playerState -> {
            mResultReceiver.send(RESULT_NEW_SONG, bundleTrack(playerState));
        });
        loadAlbumArt();
    }

    private void connectSpotifyRemote(Context context, String clientID) {
        if (!mIsSpotifyConnected) {
            SpotifyAppRemote.connect(context, new ConnectionParams.Builder(clientID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(true)
                    .build(), mConnectionListener);
        }
    }


}
