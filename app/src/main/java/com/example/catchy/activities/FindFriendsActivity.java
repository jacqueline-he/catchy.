package com.example.catchy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.catchy.R;
import com.example.catchy.adapters.FindFriendsAdapter;
import com.example.catchy.misc.BitmapCache;
import com.example.catchy.misc.EndlessRecyclerViewScrollListener;
import com.example.catchy.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindFriendsActivity extends AppCompatActivity {
    public static final String TAG = "FindFriendsActivity";
    FindFriendsAdapter adapter;
    RecyclerView rvResults;
    EditText etSearch;
    ImageButton ibSearch;
    List<ParseUser> results;
    String username;
    String ownName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        rvResults = findViewById(R.id.rvResults);
        etSearch = findViewById(R.id.etSearch);
        ibSearch = findViewById(R.id.ibSearch);

        results = new ArrayList<>();
        adapter = new FindFriendsAdapter(results, this);


        // TODO add endless recyclerview scroll

        ownName = ParseUser.getCurrentUser().getUsername();

        rvResults.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvResults.setLayoutManager(linearLayoutManager);

        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = etSearch.getText().toString();
                if (username.isEmpty()) {
                    Toast.makeText(FindFriendsActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // clear search list
                results.clear();
                fetchUsers(username);
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    username = etSearch.getText().toString();
                    if (username.isEmpty()) {
                        Toast.makeText(FindFriendsActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    }

                    // clear search list
                    results.clear();
                    fetchUsers(username);
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void fetchUsers(String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContains("username", username);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    // The query was successful.
                    for (ParseUser user : objects) {
                        if (!user.getUsername().equals(ownName)){
                            results.add(user);
                        }
                    }
                } else {
                    // Something went wrong.
                    Log.e(TAG, "Retrieving users failed!", e);
                }
                adapter.notifyDataSetChanged();
                rvResults.smoothScrollToPosition(0);
            }
        });
    }
}