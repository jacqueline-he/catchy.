package com.example.catchy.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.catchy.R;
import com.example.catchy.models.Song;
import com.parse.ParseException;
import com.parse.SaveCallback;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.ContentApi;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.ListItem;
import com.spotify.protocol.types.ListItems;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_DISCONNECT;
import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_GET_RECS;
import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_INIT;
import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_PLAY;
import static com.example.catchy.service.SpotifyBroadcastReceiver.ACTION_PLAY_PAUSE;
import static com.spotify.protocol.types.Repeat.ONE;

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
                            mSpotifyAppRemote = spotifyAppRemote;
                            mPlayerApi = mSpotifyAppRemote.getPlayerApi();
                            mIsSpotifyConnected = true;
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
        Log.d(TAG, "Handling work with intent: [" + intent + "]");
        if (intent.getAction() != null) {
            switch(intent.getAction()) {
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
                case ACTION_GET_RECS:
                    getRecommendations();
                    break;
                case ACTION_DISCONNECT:
                    disconnect();
                    break;
            }
        }
    }

    private void getRecommendations() {
        Intent in = new Intent(ACTION_GET_RECS);
        mSpotifyAppRemote.getContentApi().getRecommendedContentItems(ContentApi.ContentType.DEFAULT).setResultCallback(new CallResult.ResultCallback<ListItems>() {
           @Override
           public void onResult(ListItems recItems) {
               Log.d("SpotifyService", "getRecommendedContentItems");
               ListItem[] recItemsArr = recItems.items;
               int i = (int) (Math.random()*19);
               ListItem homeTrendingItem = recItemsArr[7];
               mSpotifyAppRemote.getContentApi().getChildrenOfItem(homeTrendingItem, 20, 0).setResultCallback(new CallResult.ResultCallback<ListItems>() {
                   @Override
                   public void onResult(ListItems listItems) {
                       Log.d("SpotifyService", "Inside children of item");
                       ListItem[] childrenItemsArr = listItems.items;
                       for (int i = 0; i < childrenItemsArr.length; i++) {
                           ListItem track = childrenItemsArr[i];
                           Song song = new Song();
                           song.setURI(track.uri);
                           song.setImageUrl(track.imageUri.raw);
                           song.setTitle(track.title);
                           song.setArtist(track.subtitle);
                           song.saveInBackground(new SaveCallback() {
                               @Override
                               public void done(ParseException e) {
                                   if (e != null) {
                                       Log.e(TAG, "Error while saving rec", e);
                                       e.printStackTrace();
                                   }
                                   Log.i(TAG, "Rec save was successful!");
                               }
                           });
                       }
                   }
               });


           }
       });
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
    }

    private void playPause() {
        Log.d(TAG,"playPause");
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
        Log.d(TAG,"playNewSong");
        Intent in = new Intent(ACTION_PLAY);
        if (mPlayerApi != null && mSpotifyAppRemote.isConnected()) {
            mPlayerApi.play(newSongId);
            mPlayerApi.setRepeat(ONE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(in);
        }
    }

    /**
     * Initialize the service and notify the receiver of the current connection and playback states
     */
    private void initialize() {
        Log.d(TAG,"initialize");
        Intent in = new Intent(ACTION_INIT);
        if (mIsSpotifyConnected) {
            in.putExtra("RESULT", "RESULT_CONNECTED");
        } else {
            in.putExtra("RESULT", "RESULT_DISCONNECTED");
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
    }

    private void disconnect() {
        Log.d(TAG,"disconnect");
        Intent in = new Intent(ACTION_DISCONNECT);
        if (mIsSpotifyConnected) {
            mPlayerApi.pause();
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
            mIsSpotifyConnected = false;
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
    }

}
