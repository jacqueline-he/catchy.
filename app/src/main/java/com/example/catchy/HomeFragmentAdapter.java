package com.example.catchy;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.catchy.fragments.SongFragment;

import java.util.ArrayList;

public class HomeFragmentAdapter extends FragmentStateAdapter {
    ArrayList<String> list;

    public HomeFragmentAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public HomeFragmentAdapter(@NonNull Fragment fragment, ArrayList<String> list) {
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
