package com.example.catchy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.catchy.R;


public class BioDialogFragment extends DialogFragment {
    private TextView tvFullName;
    private TextView tvBio;

    private String fullName;
    private String bio;

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

        tvFullName.setText(fullName);
        tvBio.setText(bio);
        super.onViewCreated(view, savedInstanceState);
    }
}