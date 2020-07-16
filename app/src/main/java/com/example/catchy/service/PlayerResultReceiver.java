package com.example.catchy.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.spotify.protocol.types.PlayerState;

import java.io.ByteArrayOutputStream;

public class PlayerResultReceiver extends ResultReceiver {
    private static final String TAG = "PlayerResultReceiver";
    // Data keys
    public static final String RECEIVER_KEY = "receiver";
    public static final String SONG_ID_KEY = "spotifyId";
    public static final String PLAYBACK_POS_KEY = "playbackPosition";
    public static final String DURATION_KEY = "duration";
    public static final String PAUSED_KEY = "isPaused";
    public static final String TITLE_KEY = "title";
    public static final String ARTIST_KEY = "artist";
    public static final String IMAGE_KEY = "image";

    // Action keys
    public static final String ACTION_PLAY = "action.PLAY";
    public static final String ACTION_UPDATE = "action.UPDATE";
    public static final String ACTION_INIT = "action.INIT";
    public static final String ACTION_SKIP = "action.SKIP";
    public static final String ACTION_PLAY_PAUSE = "action.PLAY_PAUSE";
    public static final String ACTION_DISCONNECT = "action.DISCONNECT";

    // Result keys
    public static final int RESULT_CONNECTED = 999;
    public static final int RESULT_DISCONNECTED = 111;
    public static final int RESULT_NEW_SONG = 123;
    public static final int RESULT_PLAY_PAUSE = 456;
    public static final int RESULT_ALBUM_ART = 789;
    public static final int RESULT_PLAYBACK = 1000;
    public static final int RESULT_OPEN_SPOTIFY = 2000;
    public static final int RESULT_INSTALL_SPOTIFY = 3000;


    private Receiver mReceiver;

    // Unique job ID for this service
    private static final int PLAYER_JOB_ID = 1000;

    private static boolean mIsSpotifyConnected;
    private static PlayerResultReceiver mPlayerResultReceiver;

    public PlayerResultReceiver(Handler handler) {
        super(handler);
        mIsSpotifyConnected = false;
        mPlayerResultReceiver = this;
    }

    // Setter assigns the receiver
    public void setReceiver(Receiver receiver) {
        this.mReceiver = receiver;
    }

    // Interface for communication between activity and service
    public interface Receiver {
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    /**
     * This method is called on result from the service.
     * If the Spotify remote player is connected or disconnected, it sets mIsSpotifyConnected.
     * Otherwise, it passes the result to the receiver if the receiver has been assigned.
     *
     * @param resultCode the integer code for the result from the service
     * @param resultData any data from the service
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == RESULT_CONNECTED) {
            mIsSpotifyConnected = true;
        } else if (resultCode == RESULT_DISCONNECTED) {
            mIsSpotifyConnected = false;
            Bundle bundle = new Bundle();
            bundle.putBoolean(PAUSED_KEY, true);
            mReceiver.onReceiveResult(RESULT_PLAY_PAUSE, bundle);
        } else if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

    public static boolean isSpotifyConnected() {
        return mIsSpotifyConnected;
    }

    /**
     * Convenience method for enqueuing work into this service.
     */
    public static void enqueueService(Context context, String ACTION) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.putExtra(RECEIVER_KEY, mPlayerResultReceiver);
        // Only enqueue the action in the service if the Spotify remote player is already connected
        if (PlayerResultReceiver.isSpotifyConnected()) {
            intent.setAction(ACTION);
        } else {
            Log.e(TAG, "Spotify is not connected");
            intent.setAction(ACTION_DISCONNECT);
        }
        PlayerService.enqueueWork(context, PlayerService.class, PLAYER_JOB_ID, intent);
    }

    /**
     * Method to check service Spotify remote connection.
     */
    public static void initService(Context context) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.putExtra(RECEIVER_KEY, mPlayerResultReceiver);
        intent.setAction(ACTION_INIT);
        PlayerService.enqueueWork(context, PlayerService.class, PLAYER_JOB_ID, intent);
    }

    /**
     * Method to enqueue an update action into this service
     * @param playbackPos the new playback position in the current song
     */
    public static void updatePlayer(Context context, long playbackPos) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.putExtra(RECEIVER_KEY, mPlayerResultReceiver);
        intent.putExtra(PLAYBACK_POS_KEY, playbackPos);
        // Only enqueue the action in the service if the Spotify remote player is already connected
        if (PlayerResultReceiver.isSpotifyConnected()) {
            intent.setAction(ACTION_UPDATE);
        } else {
            Log.e(TAG, "Spotify is not connected");
            intent.setAction(ACTION_DISCONNECT);
        }
        PlayerService.enqueueWork(context, PlayerService.class, PLAYER_JOB_ID, intent);
    }

    /**
     * Method to enqueue a play action into this service
     * @param songId the Spotify ID of the new song
     */
    public static void playNew(Context context, String songId) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.putExtra(RECEIVER_KEY, mPlayerResultReceiver);
        intent.putExtra(SONG_ID_KEY, songId);
        // Only enqueue the action in the service if the Spotify remote player is already connected
        if (PlayerResultReceiver.isSpotifyConnected()) {
            intent.setAction(ACTION_PLAY);
        } else {
            Log.e(TAG, "Spotify is not connected");
            intent.setAction(ACTION_DISCONNECT);
        }
        PlayerService.enqueueWork(context, PlayerService.class, PLAYER_JOB_ID, intent);
    }

    public static Bundle bundleTrack(PlayerState playerState) {
        Bundle bundle = new Bundle();
        bundle.putString(SONG_ID_KEY, playerState.track.uri);
        bundle.putString(TITLE_KEY, playerState.track.name);
        bundle.putString(ARTIST_KEY, playerState.track.artist.name);
        bundle.putLong(DURATION_KEY, playerState.track.duration);
        bundle.putBoolean(PAUSED_KEY, playerState.isPaused);
        bundle.putLong(PLAYBACK_POS_KEY, playerState.playbackPosition);
        return bundle;
    }

    public static Bundle bundlePlayback(PlayerState playerState) {
        Bundle bundle = new Bundle();
        bundle.putLong(PLAYBACK_POS_KEY, playerState.playbackPosition);
        bundle.putBoolean(PAUSED_KEY, playerState.isPaused);
        return bundle;
    }

    public static Bundle bundleBitmap(Bitmap bitmap) {
        Bundle bundle = new Bundle();
        // convert bitmap to Byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bundle.putByteArray(IMAGE_KEY,byteArray);
        return bundle;
    }
}
