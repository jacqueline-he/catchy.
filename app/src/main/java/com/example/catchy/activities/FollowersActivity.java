package com.example.catchy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.catchy.R;
import com.example.catchy.adapters.FindFriendsAdapter;
import com.example.catchy.misc.BitmapCache;
import com.example.catchy.models.User;
import com.parse.ParseUser;

import java.util.List;

import co.revely.gradient.RevelyGradient;

public class FollowersActivity extends AppCompatActivity {

    private List<ParseUser> followers;
    boolean currentUser;
    ParseUser user;
    private FindFriendsAdapter adapter;
    androidx.coordinatorlayout.widget.CoordinatorLayout layout;
    RecyclerView rvFollowers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BitmapCache.InitBitmapCache(); // for adapter
        BitmapCache.clear();

        Intent intent = getIntent();
        followers = intent.getExtras().getParcelableArrayList("followers");
        user = intent.getExtras().getParcelable("user");
        currentUser = intent.getBooleanExtra("currentUser", false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvFollowers = findViewById(R.id.rvFollowers);
        adapter = new FindFriendsAdapter(followers, FollowersActivity.this);

        layout = findViewById(R.id.layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFollowers.setLayoutManager(layoutManager);
        rvFollowers.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        setBackgroundColor();
    }

    private void setBackgroundColor() {
        Bitmap bitmap;
        if (currentUser) {
            bitmap = User.profileBitmap;
        } else
            bitmap = User.otherUserBitmaps.get(user.getObjectId());

        if (bitmap != null && !bitmap.isRecycled()) {
            Palette palette = Palette.from(bitmap).generate();
            Palette.Swatch swatch = palette.getDarkVibrantSwatch();
            // int color = palette.getDarkMutedColor(0);
            if (swatch == null) {
                swatch = palette.getDominantSwatch();
            }

            // swatch.getRgb()
            if (swatch != null) {
                // ((RelativeLayout) findViewById(R.id.layout)).setBackgroundColor(swatch.getRgb());

                int color = swatch.getRgb();
                RevelyGradient
                        .linear()
                        .colors(new int[]{Color.parseColor("#000000"), color}).angle(90f).alpha(0.46f)
                        .onBackgroundOf(layout);
            }


        }


    }
}