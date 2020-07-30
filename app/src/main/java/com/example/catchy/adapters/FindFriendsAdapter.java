package com.example.catchy.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.catchy.R;
import com.example.catchy.activities.OthersProfileActivity;
import com.example.catchy.models.User;
import com.parse.ParseFile;

import java.util.List;

public class FindFriendsAdapter extends RecyclerView.Adapter<FindFriendsAdapter.ViewHolder> {
    private List<User> results;
    private Context context;

    public FindFriendsAdapter(List<User> results, Context context) {
        this.results = results;
        this.context = context;
    }

    @NonNull
    @Override
    public FindFriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_user_result, parent, false);
        return new FindFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = results.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfileImage;
        private TextView tvUsername;
        private TextView tvFullName;

        private View itemView;

        public ViewHolder(@NonNull View view) {
            super(view);
            ivProfileImage = view.findViewById(R.id.ivProfileImage);
            tvUsername = view.findViewById(R.id.tvUsername);
            tvFullName = view.findViewById(R.id.tvFullName);
            itemView = view;
        }

        public void bind(User user) {
            tvUsername.setText(user.getString("username"));
            tvFullName.setText(user.getString("fullName"));
            ParseFile profileImage = user.getParseFile("profilePic");
            if (profileImage != null) {
                Glide.with(context).load(profileImage.getUrl()).into(ivProfileImage);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, OthersProfileActivity.class);
                    context.startActivity(intent);
                }
            });

        }
    }
}
