package com.example.catchy.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.catchy.activities.FindFriendsActivity;
import com.example.catchy.activities.FollowersActivity;
import com.example.catchy.activities.FollowingActivity;
import com.example.catchy.databinding.FragmentSearchBinding;
import com.example.catchy.databinding.FragmentUserBinding;
import com.example.catchy.misc.BitmapCache;
import com.example.catchy.misc.DetailTransition;
import com.example.catchy.misc.EndlessRecyclerViewScrollListener;
import com.example.catchy.activities.SettingsPrefActivity;
import com.example.catchy.R;
import com.example.catchy.adapters.UserAdapter;
import com.example.catchy.models.Following;
import com.example.catchy.models.Like;
import com.example.catchy.models.User;
import com.example.catchy.service.SpotifyBroadcastReceiver;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.revely.gradient.RevelyGradient;


public class UserFragment extends Fragment {

    public static final String TAG = "UserFragment";
    private RecyclerView rvLikes;
    protected UserAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private ParseUser currentUser;
    private TextView tvUsername;
    private TextView tvBio;
    private ImageView ivProfileImage;
    private ImageView ivMore;
    private ImageView ivFindFriends;
    private TextView tvFullName;
    private TextView tvLikedSongs;

    private TextView tvLikesCount;
    private TextView tvFollowersCount;
    private TextView tvFollowingCount;

    private String imgUrl;

    private RelativeLayout layout;

    private FragmentUserBinding binding;

    String name;
    String bio;

    private EndlessRecyclerViewScrollListener scrollListener;
    protected List<Like> userLikes;
    boolean infScroll = false;
    SpotifyBroadcastReceiver spotifyBroadcastReceiver;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotifyBroadcastReceiver = new SpotifyBroadcastReceiver();
        spotifyBroadcastReceiver.enqueueService(getContext(), SpotifyBroadcastReceiver.ACTION_PAUSE);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(spotifyBroadcastReceiver);
        DetailTransition.liked = true;
        BitmapCache.InitBitmapCache(true); // new cache for likes
        BitmapCache.clearSongCache(); // make sure it's empty
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUser = ParseUser.getCurrentUser();

        ivMore = binding.ivMore;
        tvUsername = binding.tvUsername;
        ivProfileImage = binding.ivProfileImage;
        tvBio = binding.tvBio;

        ivFindFriends = binding.ivFindFriends;

        tvFullName = binding.tvFullName;
        tvLikedSongs = binding.tvLikedSongs;

        tvLikesCount = binding.tvLikesCount;
        tvFollowersCount = binding.tvFollowersCount;
        tvFollowingCount = binding.tvFollowingCount;

        layout = binding.layout;

        tvUsername.setText("@" + currentUser.getUsername());
        name = currentUser.getString("fullName");
        tvFullName.setText(name);

        Log.d("UserFragment", "name: " + name);
        bio = currentUser.getString("bio");

        if (bio != null)
            tvBio.setText(bio);

        ivFindFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FindFriendsActivity.class);
                startActivity(intent);
            }
        });


        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SettingsPrefActivity.class);
                startActivity(intent);
            }
        });


        ParseFile profileImage = currentUser.getParseFile("profilePic");
        if (profileImage != null) {
            Glide.with(this).load(profileImage.getUrl()).transform(new CircleCrop()).into(ivProfileImage);
        }

        rvLikes = binding.rvLikes;
        userLikes = new ArrayList<>();
        queryUserLikes();
        adapter = new UserAdapter(getContext(), userLikes, true);
        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rvLikes.setAdapter(adapter);
        rvLikes.setLayoutManager(gridLayoutManager);

        queryUserFollowing();
        tvFollowingCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FollowingActivity.class);
                intent.putParcelableArrayListExtra("following", (ArrayList<? extends Parcelable>) User.following);
                intent.putExtra("currentUser", true);
                startActivity(intent);
            }
        });
        queryUserFollowers();
        tvFollowersCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), FollowersActivity.class);
                intent.putParcelableArrayListExtra("followers", (ArrayList<? extends Parcelable>) User.followers);
                intent.putExtra("currentUser", true);
                startActivity(intent);
            }
        });


        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                infScroll = true;
                Log.i(TAG, "Asking for inf scroll likes");
                queryUserLikes();
            }
        };
        rvLikes.addOnScrollListener(scrollListener);

        setBackgroundColor();

        imgUrl = profileImage.getUrl();
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BioDialogFragment dialog = BioDialogFragment.newInstance(name, bio, imgUrl);
                dialog.show(getFragmentManager(), "From UserFragment");
            }
        });
    }

    private void setBackgroundColor() {
        if (User.profileBitmap != null && !User.profileBitmap.isRecycled()) {
            Palette palette = Palette.from(User.profileBitmap).generate();
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
        }
        else { // try again
            new Thread(() -> {
                try {
                    User.profileBitmap = Picasso.get().load(ParseUser.getCurrentUser().getParseFile("profilePic").getUrl()).get();
                } catch (Exception e) {
                    Log.e(TAG, "couldn't get bitmap"+e);
                }
            }).start();
            RevelyGradient
                    .linear()
                    .colors(new int[]{Color.parseColor("#000000"), Color.parseColor("#00EDFF")}).angle(270f).alpha(0.66f)
                    .onBackgroundOf(layout);
        }
    }

    // Following IS changed
    private void queryUserFollowing() {
        if (User.following != null) {
            tvFollowingCount.setText(User.following.size() + " following");
        }
        else {
            User.following = new ArrayList<>();
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
                    User.followingItems = objects;
                    for (Following followingItem : objects) {
                        User.following.add(followingItem.getFollowing());
                    }
                    tvFollowingCount.setText(User.following.size() + " following");
                }
            });
        }


    }

    // Followers are NOT changed
    private void queryUserFollowers() {
        if (User.followers != null) {
            if (User.followers.size() == 1)
                tvFollowersCount.setText("1 follower");
            else
                tvFollowersCount.setText(User.followers.size() + " followers");
        }
        else {
            User.followers = new ArrayList<>();
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
                        User.followers.add(followingItem.getFollowedBy());
                    }
                    if (objects.size() == 1) {
                        tvFollowersCount.setText("1 follower");
                    }
                    else
                        tvFollowersCount.setText(objects.size() + " followers");
                }
            });
        }

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
                }
                else {
                    tvLikesCount.setText(userLikes.size() + " likes");
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // reset profile pic
        ParseFile profileImage = currentUser.getParseFile("profilePic");
        if (profileImage != null) {
            Glide.with(this).load(profileImage.getUrl()).transform(new CircleCrop()).into(ivProfileImage);
            imgUrl = profileImage.getUrl(); // updated url for bio fragment
        }

        if (!DetailTransition.liked && DetailTransition.leavingSongDetails) {
            userLikes.remove(DetailTransition.pos);
            adapter.notifyItemRemoved(DetailTransition.pos);
            DetailTransition.leavingSongDetails = false;

            if (userLikes.size() == 1) {
                tvLikesCount.setText("1 like");
            } else {
                tvLikesCount.setText(userLikes.size() + " likes");
            }
        }

        if (User.profPicChanged) {
            setBackgroundColor();
            User.profPicChanged = false;
        }

        if (User.bioChanged) {
            bio = User.changedBio;
            User.bioChanged = false;
        }

        if (User.nameChanged) {
            name = User.changedName;
            User.nameChanged = false;
        }

        queryUserFollowers();
        queryUserFollowing();

        BitmapCache.clearSongCache(); // make sure it's empty
        adapter.notifyDataSetChanged();

        if (User.itemsAdded > 0) {
            for (Like like : User.otherLikes) {
                userLikes.add(0, like);
            }

            User.otherLikes.clear(); // reset

            adapter.notifyItemRangeInserted(0, User.itemsAdded);
            User.itemsAdded = 0; // reset
        }

    }
}