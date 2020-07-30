package com.example.catchy.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
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

import java.util.ArrayList;
import java.util.List;

public class FindFriendsActivity extends AppCompatActivity {
    public static final String TAG = "FindFriendsActivity";
    FindFriendsAdapter adapter;
    RecyclerView rvResults;
    EditText etSearch;
    ImageButton ibSearch;
    List<User> results;
    String query;

    private EndlessRecyclerViewScrollListener scrollListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        rvResults = findViewById(R.id.rvResults);
        etSearch = findViewById(R.id.etSearch);
        ibSearch = findViewById(R.id.ibSearch);

        results = new ArrayList<>();
        adapter = new FindFriendsAdapter(results, this);

        rvResults.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvResults.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                fetchUsers(query, page);
            }
        };
        rvResults.addOnScrollListener(scrollListener);

        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query = etSearch.getText().toString();
                if (query.isEmpty()) {
                    Toast.makeText(FindFriendsActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // clear search list
                results.clear();
                fetchUsers(query, 0);
                scrollListener.resetState();
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    query = etSearch.getText().toString();
                    if (query.isEmpty()) {
                        Toast.makeText(FindFriendsActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    }

                    // clear search list
                    results.clear();
                    fetchUsers(query, 0);
                    scrollListener.resetState();
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void fetchUsers(String query, int page) {

    }
}