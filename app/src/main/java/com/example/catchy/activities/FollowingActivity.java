package com.example.catchy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.catchy.R;
import com.example.catchy.adapters.FindFriendsAdapter;
import com.example.catchy.models.Following;
import com.parse.ParseUser;

import java.util.List;

public class FollowingActivity extends AppCompatActivity {
    private List<ParseUser> following;
    boolean currentUser;
    private FindFriendsAdapter adapter;
    RecyclerView rvFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        following = intent.getExtras().getParcelableArrayList("following");
        currentUser = intent.getBooleanExtra("currentUser", false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvFollowing = findViewById(R.id.rvFollowing);
        adapter = new FindFriendsAdapter(following,FollowingActivity.this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFollowing.setLayoutManager(layoutManager);
        rvFollowing.setAdapter(adapter);

        rvFollowing.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.notifyDataSetChanged();



    }
}