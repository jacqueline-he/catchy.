package com.example.catchy;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.example.catchy.activities.SongDetailsActivity;
import com.example.catchy.service.SpotifyBroadcastReceiver;

public class SongProgressBar {
    private static final int LOOP_DURATION = 500;
    private SeekBar mSeekBar;
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

    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            long progress = seekBar.getProgress();
            // receiver.updatePlayer(context, progress); // TODO new method
            update(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // disable
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            long progress = seekBar.getProgress();


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
        mSeekBar.setProgress((int) progress);
        mHandler.removeCallbacks(mSeekRunnable);
        mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
    }

    public void setMax(long duration) {
        mSeekBar.setMax((int) duration);
    }

    public void pause() {
        mHandler.removeCallbacks(mSeekRunnable);
    }

    public void unpause() {
        mHandler.removeCallbacks(mSeekRunnable);
        mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
    }

}
