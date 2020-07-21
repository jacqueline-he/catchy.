package com.example.catchy.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

import static android.app.Activity.RESULT_CANCELED;

public class SpotifyBroadcastReceiver extends BroadcastReceiver implements Serializable {
    // Action keys
    public static final String ACTION_PLAY = "action.PLAY";
    public static final String ACTION_INIT = "action.INIT";
    public static final String ACTION_PLAY_PAUSE = "action.PLAY_PAUSE";
    public static final String ACTION_PAUSE = "action.PAUSE";
    public static final String ACTION_DISCONNECT = "action.DISCONNECT";

    // Result keys
    public static final int RESULT_CONNECTED = 999;
    public static final int RESULT_DISCONNECTED = 111;

    // Unique job ID for this service
    private static final int PLAYER_JOB_ID = 1000;

    private static boolean mIsSpotifyConnected;

    @Override
    public void onReceive(Context context, Intent intent) {
        int resultCode = intent.getIntExtra("RESULT", RESULT_CANCELED);
        if (resultCode == RESULT_CONNECTED) {
            mIsSpotifyConnected = true;
        } else if (resultCode == RESULT_DISCONNECTED) {
            mIsSpotifyConnected = false;
        }
    }

    public static boolean isSpotifyConnected() {
        return mIsSpotifyConnected;
    }

    /**
     * Method to check service Spotify remote connection.
     */
    public void initService(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_INIT);
        SpotifyIntentService.enqueueWork(context, SpotifyIntentService.class, PLAYER_JOB_ID, intent);
    }

    /**
     * Convenience method for enqueuing work into this service.
     * Actions: PLAY/PAUSE, PAUSE, DISCONNECT
     */
    public static void enqueueService(Context context, String ACTION) {
        Intent intent = new Intent(context, SpotifyIntentService.class);
        // Only enqueue the action in the service if the Spotify remote player is already connected
        intent.setAction(ACTION);
        SpotifyIntentService.enqueueWork(context, SpotifyIntentService.class, PLAYER_JOB_ID, intent);
    }

    /**
     * Method to enqueue a play action into this service
     * @param songId the Spotify ID of the new song
     */
    public static void playNew(Context context, String songId) {
        Intent intent = new Intent(context, SpotifyIntentService.class);
        intent.putExtra("songuri", songId);
        // TODO check if connected
        intent.setAction(ACTION_PLAY);

        SpotifyIntentService.enqueueWork(context, SpotifyIntentService.class, PLAYER_JOB_ID, intent);
    }

}
