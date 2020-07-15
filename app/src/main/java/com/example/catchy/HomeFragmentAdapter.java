package com.example.catchy;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.catchy.fragments.SongFragment;
import com.example.catchy.models.Song;

import java.util.ArrayList;
import java.util.List;

public class HomeFragmentAdapter extends FragmentStateAdapter {
    List<Song> list;

    public HomeFragmentAdapter(@NonNull Fragment fragment, List<Song> list) {
        super(fragment);
        this.list = list;

    }

    @NonNull
    @Override
    public SongFragment createFragment(int position) {
        return SongFragment.newInstance(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
