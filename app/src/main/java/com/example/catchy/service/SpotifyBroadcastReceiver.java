package com.example.catchy.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.catchy.MainActivity;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class SpotifyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
        if (resultCode == RESULT_OK) {
            String resultValue = intent.getStringExtra("resultValue");
        }
    }
}
