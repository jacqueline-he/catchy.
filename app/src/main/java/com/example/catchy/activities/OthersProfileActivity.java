package com.example.catchy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.catchy.R;
import com.example.catchy.adapters.UserAdapter;
import com.example.catchy.fragments.BioDialogFragment;
import com.example.catchy.misc.BitmapCache;
import com.example.catchy.misc.DetailTransition;
import com.example.catchy.misc.EndlessRecyclerViewScrollListener;
import com.example.catchy.models.Following;
import com.example.catchy.models.Like;
import com.example.catchy.models.User;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.revely.gradient.RevelyGradient;

public class OthersProfileActivity extends AppCompatActivity {
    public static final String TAG = "ProfileFragment";
    private RecyclerView rvLikes;
    protected UserAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private ParseUser currentUser;
    private TextView tvUsername;
    private TextView tvBio;
    private ImageView ivProfileImage;
    private TextView tvFullName;
    private TextView tvLikedSongs;

    private TextView tvLikesCount;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;

    private List<ParseUser> following;
    private List<ParseUser> followers;

    private RelativeLayout layout;
    String username;

    private EndlessRecyclerViewScrollListener scrollListener;
    protected List<Like> userLikes;
    boolean infScroll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

        BitmapCache.InitBitmapCache();
        BitmapCache.clear(); // make sure it's empty

        // GET USER
        Intent intent = getIntent();
        currentUser = (ParseUser) intent.getExtras().get("user");



        tvUsername = findViewById(R.id.tvUsername);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvBio = findViewById(R.id.tvBio);
        tvFullName = findViewById(R.id.tvFullName);
        tvLikedSongs = findViewById(R.id.tvLikedSongs);
        tvLikesCount = findViewById(R.id.tvLikesCount);
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        layout = findViewById(R.id.layout);

        username = currentUser.getUsername();
        tvUsername.setText("@" + username);
        String name = currentUser.getString("fullName");
        tvFullName.setText(name);
        String bio = currentUser.getString("bio");

        if (bio != null)
            tvBio.setText(bio);

        ParseFile profileImage = currentUser.getParseFile("profilePic");
        if (profileImage != null) {
            Glide.with(this).load(profileImage.getUrl()).transform(new CircleCrop()).into(ivProfileImage);
        }

        rvLikes = findViewById(R.id.rvLikes);
        userLikes = new ArrayList<>();
        queryUserLikes();
        adapter = new UserAdapter(this, userLikes);
        gridLayoutManager = new GridLayoutManager(this, 3);
        rvLikes.setAdapter(adapter);
        rvLikes.setLayoutManager(gridLayoutManager);

        queryUserFollowing();
        tvFollowingCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OthersProfileActivity.this, FollowingActivity.class);
                intent.putParcelableArrayListExtra("following", (ArrayList<? extends Parcelable>) following);
                intent.putExtra("currentUser", false);
                startActivity(intent);
            }
        });
        queryUserFollowers();
        tvFollowersCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OthersProfileActivity.this, FollowersActivity.class);
                intent.putParcelableArrayListExtra("followers", (ArrayList<? extends Parcelable>) followers);
                intent.putExtra("currentUser", false);
                startActivity(intent);
            }
        });


        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                infScroll = true;
                Log.i(TAG, "Asking for inf scroll posts");
                queryUserLikes();
            }
        };
        rvLikes.addOnScrollListener(scrollListener);

        setBackgroundColor();

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BioDialogFragment dialog = BioDialogFragment.newInstance(name, bio);
                dialog.show(getSupportFragmentManager(), "From OthersProfileActivity");
            }
        });
    }

    // TODO change to user's own
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
                // ((RelativeLayout) findViewById(R.id.layout)).setBackgroundColor(swatch.getRgb());

                int color = swatch.getRgb();
                RevelyGradient
                        .linear()
                        .colors(new int[]{Color.parseColor("#000000"), color}).angle(270f).alpha(0.86f)
                        .onBackgroundOf(layout);
            }


        }
    }

    private void queryUserFollowers() {
        followers = new ArrayList<>();
        ParseQuery<Following> query = ParseQuery.getQuery(Following.class);
        query.whereEqualTo("followedBy", ParseUser.getCurrentUser()); // everyone the user follows
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Following>() {
            @Override
            public void done(List<Following> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting following", e);
                    return;
                }
                Log.d(TAG, "Query following success!");
                for (Following followingItem : objects) {
                    following.add(followingItem.getFollowing());
                }
                tvFollowingCount.setText(objects.size() + " following");
            }
        });
    }

    private void queryUserFollowing() {
        following = new ArrayList<>();
        ParseQuery<Following> query = ParseQuery.getQuery(Following.class);
        query.whereEqualTo("following", ParseUser.getCurrentUser()); // everyone who follows user
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Following>() {
            @Override
            public void done(List<Following> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting following", e);
                    return;
                }
                Log.d(TAG, "Query following success!");
                for (Following followingItem : objects) {
                    followers.add(followingItem.getFollowing());
                }
                tvFollowersCount.setText(objects.size() + " followers");
            }
        });
    }

    private void queryUserLikes() {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.include(Like.KEY_LIKED_BY);

        if (infScroll && userLikes.size() > 0) {
            Date oldest = userLikes.get(userLikes.size() - 1).getCreatedAt();
            Log.i(TAG, "Getting inf scroll posts");
            query.whereLessThan("createdAt", oldest);
            infScroll = false;
        }

        query.setLimit(24);
        query.addDescendingOrder("createdAt");
        Log.e(TAG, "Id: " + currentUser.getObjectId());
        query.whereEqualTo("likedBy", currentUser);
        query.findInBackground(new FindCallback<Like>() {
            @Override
            public void done(List<Like> likes, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Like like : likes) {
                    Log.i(TAG, "Liked: " + like.getTitle());
                }
                userLikes.addAll(likes);
                if (userLikes.size() == 1) {
                    tvLikesCount.setText("1 like");
                }
                else {
                    tvLikesCount.setText(userLikes.size() + " likes");
                }

                adapter.notifyDataSetChanged();
            }
        });
    }
}