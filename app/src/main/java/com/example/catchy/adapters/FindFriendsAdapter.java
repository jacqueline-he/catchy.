package com.example.catchy.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.catchy.R;
import com.example.catchy.activities.OthersProfileActivity;
import com.example.catchy.databinding.ItemUserResultBinding;
import com.example.catchy.misc.BitmapCache;
import com.example.catchy.misc.ImageLoaderTask;
import com.example.catchy.models.User;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.ViewHolder> {
    private List<ParseUser> results;
    private Context context;
    private ItemUserResultBinding binding;


    public FindFriendsAdapter(List<ParseUser> results, Context context) {
        this.results = results;
        this.context = context;
    }

    @NonNull
    @Override
    public FindFriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ItemUserResultBinding.inflate(inflater, parent, false);
        View view = binding.getRoot();
        return new FindFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser user = results.get(position);
        try {
            holder.bind(user);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfileImage;
        private TextView tvUsername;
        private TextView tvFullName;
        private ImageView ivFollow;
        private ParseUser user;
        private View itemView;
        boolean followed = false;

        public ViewHolder(@NonNull View view) {
            super(view);
            ivProfileImage = binding.ivProfileImage;
            tvUsername = binding.tvUsername;
            tvFullName = binding.tvFullName;
            ivFollow = binding.ivFollow;
            itemView = view;
        }

        public void bind(ParseUser user) throws ParseException {
            this.user = user;
            String username = user.fetchIfNeeded().getUsername();
            tvUsername.setText("@" + username);
            tvFullName.setText(user.getString("fullName"));
            ParseFile profileImage = user.getParseFile("profilePic");
            if (profileImage != null) {
                Glide.with(context).load(profileImage.getUrl()).transform(new CircleCrop()).into(ivProfileImage);
            }
            else {
                Glide.with(context).load(context.getResources().getIdentifier("ppic.jpg", "drawable", context.getPackageName())).transform(new CircleCrop()).into(ivProfileImage);
            }

            int position = getAdapterPosition();
            Bitmap bitmap = null;
            bitmap = BitmapCache.getBitmapFromMemCache(position, false);
            if (bitmap != null && !User.otherUserBitmaps.containsKey(user.getObjectId())) {
                User.otherUserBitmaps.put(user.getObjectId(), bitmap);
            } else {
                new ImageLoaderTask(position, profileImage.getUrl(), false).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, (Integer[]) null);
            }


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!User.otherUserBitmaps.containsKey(user.getObjectId())) {
                        Bitmap createdBitmap = BitmapCache.getBitmapFromMemCache(position, false);
                        if (createdBitmap != null)
                            User.otherUserBitmaps.put(user.getObjectId(), createdBitmap);
                    }

                    Intent intent = new Intent(context, OthersProfileActivity.class);
                    intent.putExtra("user", user);
                    User.otherUserPos = getAdapterPosition();
                    context.startActivity(intent);
                }
            });

            if (user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                ivFollow.setVisibility(View.GONE);
            } else {

                ivFollow.setImageResource(R.drawable.ic_follow);
                ivFollow.clearColorFilter();
                followed = false;

                if (User.following != null) {
                    for (int i = 0; i < User.following.size(); i++) {
                        ParseUser followingUser = User.following.get(i);
                        if (followingUser.fetchIfNeeded().getUsername().equals(user.getUsername())) {
                            followed = true;
                            ivFollow.setImageResource(R.drawable.ic_followed);
                            ivFollow.setColorFilter(ContextCompat.getColor(context, R.color.medium_green));
                        }
                    }
                }

                ivFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        follow();
                    }
                });
            }


        }

        private void follow() {
            if (!followed) {
                ivFollow.setImageResource(R.drawable.ic_followed);
                ivFollow.setColorFilter(ContextCompat.getColor(context, R.color.medium_green));
                User.following.add(user);
                followed = true;
            } else {
                ivFollow.setImageResource(R.drawable.ic_follow);
                ivFollow.clearColorFilter();
                for (int i = 0; i < User.following.size(); i++) {
                    if (User.following.get(i).getUsername().equals(user.getUsername())) {
                        User.following.remove(i);
                    }
                }
                followed = false;
            }
        }
    }


}
