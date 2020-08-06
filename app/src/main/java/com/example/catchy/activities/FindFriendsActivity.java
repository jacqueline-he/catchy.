package com.example.catchy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.catchy.R;
import com.example.catchy.adapters.FindFriendsAdapter;
import com.example.catchy.databinding.ActivityFindFriendsBinding;
import com.example.catchy.misc.BitmapCache;
import com.example.catchy.misc.EndlessRecyclerViewScrollListener;
import com.example.catchy.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import co.revely.gradient.RevelyGradient;

public class FindFriendsActivity extends AppCompatActivity {
    public static final String TAG = "FindFriendsActivity";
    FindFriendsAdapter adapter;
    RecyclerView rvResults;
    EditText etSearch;
    ImageView ivSearch;
    List<ParseUser> results;
    String username;
    String ownName;
    private RelativeLayout layout;
    private int page;

    private EndlessRecyclerViewScrollListener scrollListener;
    boolean infScroll = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BitmapCache.InitBitmapCache(false); // for adapter; for users
        BitmapCache.clearUserCache();

        ActivityFindFriendsBinding binding = ActivityFindFriendsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        rvResults = binding.rvResults;
        etSearch = binding.etSearch;
        ivSearch = binding.ivSearch;

        results = new ArrayList<>();
        adapter = new FindFriendsAdapter(results, this);

        ownName = ParseUser.getCurrentUser().getUsername();

        layout = binding.layout;

        setTheme(R.style.CustomDialogTheme);

        rvResults.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvResults.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (results.size() > (page * 24)) {
                    infScroll = true;
                    fetchUsers(username);
                }
            }
        };
        rvResults.addOnScrollListener(scrollListener);

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = etSearch.getText().toString();
                if (username.isEmpty()) {
                    Toast toast = Toast.makeText(FindFriendsActivity.this, "Search cannot be empty", Toast.LENGTH_SHORT);
                    View toastView = toast.getView();

                    TextView text = (TextView) toastView.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(R.color.white));
                    toast.show();
                    return;
                }

                // clear search list
                results.clear();
                fetchUsers(username);
                scrollListener.resetState();
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    username = etSearch.getText().toString();
                    if (username.isEmpty()) {
                        Toast toast = Toast.makeText(FindFriendsActivity.this, "Search cannot be empty", Toast.LENGTH_SHORT);
                        View toastView = toast.getView();
                        TextView text = (TextView) toastView.findViewById(android.R.id.message);
                        text.setTextColor(getResources().getColor(R.color.white));
                        toast.show();
                    }

                    // clear search list
                    results.clear();
                    fetchUsers(username);
                    scrollListener.resetState();
                    handled = true;
                }
                return handled;
            }
        });
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
                        .colors(new int[]{Color.parseColor("#000000"), color}).angle(270f).alpha(0.76f)
                        .onBackgroundOf(layout);
            }
        }
        else {
            RevelyGradient
                    .linear()
                    .colors(new int[]{Color.parseColor("#000000"), Color.parseColor("#00EDFF")}).angle(270f).alpha(0.76f)
                    .onBackgroundOf(layout);
        }
    }

    private void fetchUsers(String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContains("username", username);
        query.setLimit(24);

        if (infScroll && results.size() > (page * 24)) {
            query.setSkip(results.size());
            infScroll = false;
        }
        query.orderByAscending("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    // The query was successful.
                    for (ParseUser user : objects) {
                        if (!user.getUsername().equals(ownName)) {
                            results.add(user);
                        }
                    }
                } else {
                    // Something went wrong.
                    Log.e(TAG, "Retrieving users failed!", e);
                }
                adapter.notifyDataSetChanged();
                rvResults.smoothScrollToPosition(0);
                BitmapCache.clearUserCache(); // make sure it's empty
                page++;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Back here");
        if (User.otherUserPos != -1) {
            adapter.notifyItemChanged(User.otherUserPos);
            User.otherUserPos = -1;
        }
    }
}