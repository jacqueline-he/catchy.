package com.example.catchy.misc;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.catchy.R;
import com.example.catchy.activities.SongDetailsActivity;
import com.example.catchy.service.SpotifyBroadcastReceiver;

public class SongProgressBar {
    private static final int LOOP_DURATION = 500;
    SeekBar mSeekBar;
    private Handler mHandler;
    SpotifyBroadcastReceiver receiver;
    Context context;

    public SongProgressBar(SeekBar seekbar, Context context) {
        mSeekBar = seekbar;
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mHandler = new Handler();
        receiver = new SpotifyBroadcastReceiver();
        this.context = context;
    }

    public SeekBar getSeekBar() {
        return mSeekBar;
    }

    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            long progress = seekBar.getProgress();
            update(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // disable
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private final Runnable mSeekRunnable = new Runnable() {

        @Override
        public void run() {
            int progress = mSeekBar.getProgress();
            mSeekBar.setProgress(progress + LOOP_DURATION);
            mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
        }
    };

    public void update(long progress) {
        if (mSeekBar.getProgress() == mSeekBar.getMax()) {
            mSeekBar.setProgress(0);
        }
        else {
            mSeekBar.setProgress((int) progress);
        }

        mHandler.removeCallbacks(mSeekRunnable);
        mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);

        int minutes = (int) ((progress / 1000)  / 60);
        int seconds = (int)((progress / 1000) % 60);
        String time;
        if (minutes < 0 || seconds < 0) {
            time = "0:00";
        }
        else if (seconds < 10) {
            time = minutes + ":0" + seconds;
        }
        else {
            time = minutes + ":" + seconds;
        }
        TextView tvCurrPos = ((SongDetailsActivity)context).findViewById(R.id.tvCurrPos);
        tvCurrPos.setText(time);
        Log.d("SongProgressBar", "progress: " + time);
    }

    public void setMax(long duration) {
        mSeekBar.setMax((int) duration);
    }

    public void unpause() {
        mHandler.removeCallbacks(mSeekRunnable);
        mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
    }

    public void pause() {
        mHandler.removeCallbacks(mSeekRunnable);
    }

    public void skip(int duration) {
        int newProgress = mSeekBar.getProgress() + duration;
        update(newProgress);

    }

}
