package com.example.catchy.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.catchy.MainActivity;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class SpotifyBroadcastReceiver extends BroadcastReceiver {
    // Action keys
    public static final String ACTION_PLAY = "action.PLAY";
    public static final String ACTION_INIT = "action.INIT";
    public static final String ACTION_PLAY_PAUSE = "action.PLAY_PAUSE";
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

    /**
     * Method to check service Spotify remote connection.
     */
    public void initService(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_INIT);
        SpotifyService.enqueueWork(context, SpotifyService.class, PLAYER_JOB_ID, intent);
    }

}
