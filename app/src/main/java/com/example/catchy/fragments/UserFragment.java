package com.example.catchy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.catchy.EndlessRecyclerViewScrollListener;
import com.example.catchy.R;
import com.example.catchy.UserAdapter;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class UserFragment extends Fragment {

    public static final String TAG="ProfileFragment";
    private ImageView ivMore;
    private RecyclerView rvLikes;
    protected UserAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private ParseUser currentUser;
    private TextView tvUsername;
    private TextView tvBio;
    private EditText etBio;
    private Button btnUpdate;
    private ImageView ivProfileImage;
    private EndlessRecyclerViewScrollListener scrollListener;
    boolean infScroll = false;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        // Inflate the layout for this fragment
        ivMore = view.findViewById(R.id.ivMore);
        currentUser = ParseUser.getCurrentUser();
        tvUsername = view.findViewById(R.id.tvUsername);
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvBio = view.findViewById(R.id.tvBio);
        etBio = view.findViewById(R.id.etBio);
        btnUpdate = view.findViewById(R.id.btnUpdate);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUsername.setText(currentUser.getUsername());
        String bio = currentUser.getString("bio");

        if (bio != null)
            tvBio.setText(bio);
        else
            tvBio.setText("[update bio here]");

        tvBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvBio.setVisibility(View.GONE);
                etBio.setVisibility(View.VISIBLE);
                btnUpdate.setVisibility(View.VISIBLE);
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newBio = etBio.getText().toString();

                ParseUser currentUser = ParseUser.getCurrentUser();
                currentUser.put("bio", newBio);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d(TAG, "Updated bio successfully");
                        }
                    }
                });
                tvBio.setText(newBio);
                tvBio.setVisibility(View.VISIBLE);
                etBio.setVisibility(View.GONE);
                btnUpdate.setVisibility(View.GONE);

            }
        });

        ParseFile profileImage = currentUser.getParseFile("profilePic");
        if (profileImage != null) {
            Glide.with(this).load(profileImage.getUrl()).transform(new CircleCrop()).into(ivProfileImage);
        }

        /*rvPosts = view.findViewById(R.id.rvPosts);
        userPosts = new ArrayList<>();
        adapter = new UserAdapter(getContext(), userPosts);

        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(gridLayoutManager);
        queryUserPosts();

        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                infScroll = true;
                Log.i(TAG, "Asking for inf scroll posts");
                queryUserPosts();
            }
        };
        rvPosts.addOnScrollListener(scrollListener);*/
    }
}