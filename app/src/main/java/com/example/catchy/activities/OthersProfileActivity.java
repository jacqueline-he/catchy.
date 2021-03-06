package com.example.catchy.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.example.catchy.databinding.ActivityOthersProfileBinding;
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
    private UserAdapter adapter;
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

    private ImageView ivFollow;

    private List<ParseUser> following;
    private List<ParseUser> followers;

    private RelativeLayout layout;
    String username;

    private EndlessRecyclerViewScrollListener scrollListener;
    protected List<Like> userLikes;
    boolean infScroll = false;

    boolean followed = false; // followed by user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityOthersProfileBinding binding = ActivityOthersProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        BitmapCache.InitBitmapCache(true); // for other user's likes
        BitmapCache.clearSongCache(); // make sure it's empty

        // GET USER
        Intent intent = getIntent();
        currentUser = (ParseUser) intent.getExtras().get("user");
        for (ParseUser user : User.following) {
            try {
                if (user.fetchIfNeeded().getUsername().equals(currentUser.getUsername())) {
                    followed = true;
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        tvUsername = binding.tvUsername;
        ivProfileImage = binding.ivProfileImage;
        tvBio = binding.tvBio;
        tvFullName = binding.tvFullName;
        tvLikedSongs = binding.tvLikedSongs;
        tvLikesCount = binding.tvLikesCount;
        tvFollowersCount = binding.tvFollowersCount;
        tvFollowingCount = binding.tvFollowingCount;
        ivFollow = binding.ivFollow;
        layout = binding.layout;

        if (followed) { // user following
            ivFollow.setImageResource(R.drawable.ic_followed);
            ivFollow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.medium_green));
        }

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

        rvLikes = binding.rvLikes;
        userLikes = new ArrayList<>();
        queryUserLikes();
        adapter = new UserAdapter(this, userLikes, false);
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
                intent.putExtra("user", currentUser);
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
                intent.putExtra("user", currentUser);
                startActivity(intent);
            }
        });


        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                infScroll = true;
                Log.i(TAG, "Asking for inf scroll users");
                queryUserLikes();
            }
        };
        rvLikes.addOnScrollListener(scrollListener);

        setBackgroundColor();

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BioDialogFragment dialog = BioDialogFragment.newInstance(name, bio, profileImage.getUrl());
                dialog.show(getSupportFragmentManager(), "From OthersProfileActivity");
            }
        });

        ivFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                follow();
            }
        });
    }

    private void follow() {
        if (followed) { // unfollow
            ivFollow.setImageResource(R.drawable.ic_circle_follow);
            ivFollow.clearColorFilter();
            followed = false;

            for (int i = 0; i < followers.size(); i++) {
                ParseUser user = followers.get(i);
                try {
                    if (user.fetchIfNeeded().getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                        followers.remove(i); // on other user's side - remove me
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < User.following.size(); i++) {
                ParseUser thisUser = User.following.get(i);
                try {
                    if (thisUser.getUsername().equals(currentUser.fetchIfNeeded().getUsername())) {
                        User.following.remove(i); // on my side - remove this user
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        } else { // follow
            followed = true;
            ivFollow.setImageResource(R.drawable.ic_followed);
            ivFollow.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.medium_green));
            followers.add(ParseUser.getCurrentUser()); // on other user's side
            User.following.add(currentUser); // on current user's side
        }

        if (followers.size() == 1) {
            tvFollowersCount.setText("1 follower");
        } else
            tvFollowersCount.setText(followers.size() + " followers");
    }

    private void setBackgroundColor() {
        Bitmap bitmap = User.otherUserBitmaps.get(currentUser.getObjectId());
        if (bitmap != null && !bitmap.isRecycled()) {
            Palette palette = Palette.from(bitmap).generate();
            Palette.Swatch swatch = palette.getLightVibrantSwatch();
            if (swatch == null) {
                swatch = palette.getDominantSwatch();
            }

            if (swatch != null) {
                int color = swatch.getRgb();
                RevelyGradient
                        .linear()
                        .colors(new int[]{Color.parseColor("#000000"), color}).angle(270f).alpha(0.66f)
                        .onBackgroundOf(layout);
            }


        } else {
            RevelyGradient
                    .linear()
                    .colors(new int[]{Color.parseColor("#000000"), Color.parseColor("#00EDFF")}).angle(270f).alpha(0.66f)
                    .onBackgroundOf(layout);
        }
    }

    private void queryUserFollowers() {
        followers = new ArrayList<>();
        ParseQuery<Following> query = ParseQuery.getQuery(Following.class);
        query.whereEqualTo("following", currentUser); // everyone the user follows
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
                    followers.add(followingItem.getFollowedBy());
                }

                if (followed && !followers.contains(ParseUser.getCurrentUser())) {
                    boolean userPresent = false;
                    for (ParseUser user : followers) {
                        try {
                            if (user.fetchIfNeeded().getUsername().equals(ParseUser.getCurrentUser().fetchIfNeeded().getUsername())) {
                                userPresent = true;
                            }
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }

                    if (!userPresent)
                        followers.add(ParseUser.getCurrentUser()); // This is for new development that hasn't been saved to Parse
                }

                if (followers.size() == 1) {
                    tvFollowersCount.setText("1 follower");
                } else
                    tvFollowersCount.setText(followers.size() + " followers");
            }
        });
    }

    private void queryUserFollowing() {
        following = new ArrayList<>();
        ParseQuery<Following> query = ParseQuery.getQuery(Following.class);
        query.whereEqualTo("followedBy", currentUser); // everyone who follows user
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

    private void queryUserLikes() {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.include(Like.KEY_LIKED_BY);

        if (infScroll && userLikes.size() > 0) {
            Date oldest = userLikes.get(userLikes.size() - 1).getCreatedAt();
            Log.i(TAG, "Getting inf scroll likes");
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
                    Log.e(TAG, "Issue with getting likes", e);
                    return;
                }
                for (Like like : likes) {
                    Log.i(TAG, "Liked: " + like.getTitle());
                }
                userLikes.addAll(likes);
                if (userLikes.size() == 1) {
                    tvLikesCount.setText("1 like");
                } else {
                    tvLikesCount.setText(userLikes.size() + " likes");
                }

                adapter.notifyDataSetChanged();
            }
        });
    }
}