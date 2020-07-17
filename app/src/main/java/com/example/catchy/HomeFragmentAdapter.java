package com.example.catchy;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.catchy.fragments.SongFragment;
import com.example.catchy.models.Song;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragmentAdapter extends FragmentStateAdapter {
    List<Song> list;
    SpotifyBroadcastReceiver receiver;
    Context context;

    public HomeFragmentAdapter(@NonNull Fragment fragment, List<Song> list, Context context, SpotifyBroadcastReceiver receiver) {
        super(fragment);
        this.list = list;
        this.context = context;
        this.receiver = receiver;

    }

    @NonNull
    @Override
    public SongFragment createFragment(int position) {
        if (position == list.size() - 3) {
            Log.d("HomeFragmentAdapter", "get more");
            receiver.enqueueService(context, SpotifyBroadcastReceiver.ACTION_GET_RECS);

        }

        if (position == list.size() - 1) {
            queueSongs(); // Retrieve 10 more songs
        }
        return SongFragment.newInstance(list.get(position), receiver);
    }

    private void queueSongs() {

        ParseQuery<Song> query = ParseQuery.getQuery(Song.class);

        if (list.size() > 0) {
            Date newest = list.get(0).getCreatedAt();
            query.whereGreaterThan("createdAt", newest);
            // Date oldest = list.get(list.size() - 1).getCreatedAt();
            Log.i("HomeFragmentAdapter", "Getting inf scroll posts");
            // query.whereLessThan("createdAt", oldest);
        }

        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> posts, ParseException e) {
                if (e != null) {
                    Log.e("HomeFragmentAdapter", "Issue with getting posts", e);
                    return;
                }
                list.addAll(posts);
                notifyDataSetChanged();
                Log.i("HomeFragmentAdapter", "Adapter changed");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
