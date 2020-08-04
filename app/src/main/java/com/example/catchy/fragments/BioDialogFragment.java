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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.catchy.R;
import com.example.catchy.models.User;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import co.revely.gradient.RevelyGradient;


public class BioDialogFragment extends DialogFragment {
    private TextView tvFullName;
    private TextView tvBio;
    private ImageView ivProfileImage;

    private String fullName;
    private String bio;
    private String imgUrl;

    private RelativeLayout layout;

    public BioDialogFragment() {
        // Required empty public constructor
    }

    public static BioDialogFragment newInstance(String fullName, String bio, String imgUrl) {
        BioDialogFragment fragment = new BioDialogFragment();
        Bundle args = new Bundle();
        args.putString("fullName", fullName);
        args.putString("bio", bio);
        args.putString("imgUrl", imgUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.fullName = getArguments().getString("fullName");
            this.bio = getArguments().getString("bio");
            this.imgUrl = getArguments().getString("imgUrl");
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
        ivProfileImage = view.findViewById(R.id.ivProfileImage);

        tvFullName.setText(fullName);
        tvBio.setText(bio);
        Glide.with(this).load(imgUrl).into(ivProfileImage);
        super.onViewCreated(view, savedInstanceState);
    }

}