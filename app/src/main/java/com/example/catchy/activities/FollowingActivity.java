package com.example.catchy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.WindowManager;

import com.example.catchy.R;
import com.example.catchy.adapters.FindFriendsAdapter;
import com.example.catchy.misc.BitmapCache;
import com.example.catchy.models.Following;
import com.example.catchy.models.User;
import com.parse.ParseUser;

import java.util.List;

import co.revely.gradient.RevelyGradient;

public class FollowingActivity extends AppCompatActivity {
    private List<ParseUser> following;
    boolean currentUser;
    ParseUser user;
    private FindFriendsAdapter adapter;
    private Toolbar toolbar;
    androidx.coordinatorlayout.widget.CoordinatorLayout layout;
    RecyclerView rvFollowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BitmapCache.InitBitmapCache(false); // for adapter, for users
        BitmapCache.clearUserCache();

        Intent intent = getIntent();
        following = intent.getExtras().getParcelableArrayList("following");
        user = intent.getExtras().getParcelable("user");
        currentUser = intent.getBooleanExtra("currentUser", false);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvFollowing = findViewById(R.id.rvFollowing);
        adapter = new FindFriendsAdapter(following, FollowingActivity.this);

        layout = findViewById(R.id.layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFollowing.setLayoutManager(layoutManager);
        rvFollowing.setAdapter(adapter);

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

            if (swatch != null) {
                int color = swatch.getRgb();
                RevelyGradient
                        .linear()
                        .colors(new int[]{Color.parseColor("#000000"), color}).angle(90f).alpha(0.46f)
                        .onBackgroundOf(layout);


            }


        }


    }
}