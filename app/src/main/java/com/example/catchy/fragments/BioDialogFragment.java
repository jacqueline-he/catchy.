package com.example.catchy.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.catchy.R;
import com.example.catchy.models.User;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import co.revely.gradient.RevelyGradient;


public class BioDialogFragment extends DialogFragment {
    private TextView tvFullName;
    private TextView tvBio;

    private String fullName;
    private String bio;

    private RelativeLayout layout;

    public BioDialogFragment() {
        // Required empty public constructor
    }

    public static BioDialogFragment newInstance(String fullName, String bio) {
        BioDialogFragment fragment = new BioDialogFragment();
        Bundle args = new Bundle();
        args.putString("fullName", fullName);
        args.putString("bio", bio);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.fullName = getArguments().getString("fullName");
            this.bio = getArguments().getString("bio");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bio_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvFullName = view.findViewById(R.id.tvFullName);
        tvBio = view.findViewById(R.id.tvBio);
        layout = view.findViewById(R.id.biodialog);

        tvFullName.setText(fullName);
        tvBio.setText(bio);
        super.onViewCreated(view, savedInstanceState);

        setBackgroundColor();
    }

    private void setBackgroundColor() {
        if (User.profileBitmap != null && !User.profileBitmap.isRecycled()) {
            Palette palette = Palette.from(User.profileBitmap).generate();
            Palette.Swatch swatch = palette.getDarkVibrantSwatch();
            // int color = palette.getDarkMutedColor(0);
            if (swatch == null) {
                swatch = palette.getDominantSwatch();
            }

            // swatch.getRgb()
            if (swatch != null) {

                int color = swatch.getRgb();
                RevelyGradient
                        .linear()
                        .colors(new int[]{Color.parseColor("#000000"), color}).angle(270f).alpha(0.66f)
                        .onBackgroundOf(layout);
            }
        }
        else { // try again
            new Thread(() -> {
                try {
                    User.profileBitmap = Picasso.get().load(ParseUser.getCurrentUser().getParseFile("profilePic").getUrl()).get();
                } catch (Exception e) {
                    Log.e("BioDialogFragment", "couldn't get bitmap"+e);
                }
            }).start();
            RevelyGradient
                    .linear()
                    .colors(new int[]{Color.parseColor("#000000"), Color.parseColor("#00EDFF")}).angle(270f).alpha(0.66f)
                    .onBackgroundOf(layout);
        }
    }
}