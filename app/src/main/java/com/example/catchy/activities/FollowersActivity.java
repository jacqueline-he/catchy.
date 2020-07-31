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
import com.parse.ParseUser;

import java.util.List;

public class FollowersActivity extends AppCompatActivity {

    private List<ParseUser> followers;
    boolean currentUser;
    private FindFriendsAdapter adapter;
    RecyclerView rvFollowers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        followers = intent.getExtras().getParcelableArrayList("followers");
        currentUser = intent.getBooleanExtra("currentUser", false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvFollowers = findViewById(R.id.rvFollowers);
        adapter = new FindFriendsAdapter(followers,FollowersActivity.this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFollowers.setLayoutManager(layoutManager);
        rvFollowers.setAdapter(adapter);

        rvFollowers.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.notifyDataSetChanged();



    }
}