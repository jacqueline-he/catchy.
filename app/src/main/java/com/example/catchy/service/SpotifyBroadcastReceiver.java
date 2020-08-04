package com.example.catchy.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.Serializable;

import static android.app.Activity.RESULT_CANCELED;

public class SpotifyBroadcastReceiver extends BroadcastReceiver implements Serializable {
    // Data keys
    public static final String PLAYBACK_POS_KEY = "playbackPosition";

    // Action keys
    public static final String ACTION_PLAY = "action.PLAY";
    public static final String ACTION_INIT = "action.INIT";
    public static final String ACTION_PLAY_PAUSE = "action.PLAY_PAUSE";
    public static final String ACTION_PAUSE = "action.PAUSE";
    public static final String ACTION_UPDATE = "action.UPDATE";
    public static final String ACTION_DISCONNECT = "action.DISCONNECT";

    // Result keys
    public static final int RESULT_CONNECTED = 999;
    public static final int RESULT_DISCONNECTED = 111;
    public static final int RESULT_OPEN_SPOTIFY = 2000;
    public static final int RESULT_INSTALL_SPOTIFY = 3000;

    // Unique job ID for this service
    private static final int PLAYER_JOB_ID = 1000;

    private static boolean mIsSpotifyConnected;

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        int resultCode = intent.getIntExtra("RESULT", RESULT_CANCELED);
        this.context = context;
        if (resultCode == RESULT_CONNECTED) {
            mIsSpotifyConnected = true;
        } else if (resultCode == RESULT_DISCONNECTED) {
            mIsSpotifyConnected = false;
        }
        else if (resultCode == RESULT_OPEN_SPOTIFY) {
            openSpotify();
        }
        else if (resultCode == RESULT_INSTALL_SPOTIFY) {
            installSpotify();
        }
    }

    private void openSpotify() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra(Intent.EXTRA_REFERRER,
                Uri.parse("android-app://" + context.getPackageName()));
        context.startActivity(intent);
    }

    private void installSpotify() {
        final String appPackageName = "com.spotify.music";
        final String referrer = "adjust_campaign=PACKAGE_NAME&adjust_tracker=ndjczk&utm_source=adjust_preinstall";
        try {
            Uri uri = Uri.parse("market://details")
                    .buildUpon()
                    .appendQueryParameter("id", appPackageName)
                    .appendQueryParameter("referrer", referrer)
                    .build();
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (android.content.ActivityNotFoundException ignored) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details")
                    .buildUpon()
                    .appendQueryParameter("id", appPackageName)
                    .appendQueryParameter("referrer", referrer)
                    .build();
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
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
     * Method to enqueue an update action into this service
     *
     * @param playbackPos the new playback position in the current song
     */
    public static void updatePlayer(Context context, long playbackPos) {
        Intent intent = new Intent(context, SpotifyIntentService.class);
        intent.putExtra(PLAYBACK_POS_KEY, playbackPos);
        // Only enqueue the action in the service if the Spotify remote player is already connected
        intent.setAction(ACTION_UPDATE);
        SpotifyIntentService.enqueueWork(context, SpotifyIntentService.class, PLAYER_JOB_ID, intent);
    }


    /**
     * Method to enqueue a play action into this service
     *
     * @param songId the Spotify ID of the new song
     */
    public static void playNew(Context context, String songId) {
        Intent intent = new Intent(context, SpotifyIntentService.class);
        intent.putExtra("songuri", songId);
        intent.setAction(ACTION_PLAY);

        SpotifyIntentService.enqueueWork(context, SpotifyIntentService.class, PLAYER_JOB_ID, intent);
    }

}
