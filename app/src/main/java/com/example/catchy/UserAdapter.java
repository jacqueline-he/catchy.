package com.example.catchy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.catchy.models.Like;
import com.example.catchy.models.Song;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<Like> likes;

    public UserAdapter(Context context, List<Like> likes) {
        this.context = context;
        this.likes = likes;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_liked_song, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        Like like = likes.get(position);
        holder.bind(like);
    }

    @Override
    public int getItemCount() {
        return likes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivLikedImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLikedImage = itemView.findViewById(R.id.ivLikedImage);
        }

        public void bind(Like like) {
            String imgUrl = like.getImageUrl();
            if (imgUrl != null)
                Glide.with(context).load(imgUrl).apply(new RequestOptions().override(100, 100)).into(ivLikedImage);
            Log.d("UserAdapter", "bound image");

            ivLikedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, SongDetailsActivity.class);
                    Song song = new Song();
                    song.setTitle(like.getTitle());
                    song.setURI(like.getURI());
                    song.setArtist(like.getArtist());
                    song.setImageUrl(like.getImageUrl());
                    // pack something
                    intent.putExtra("song", song);
                    context.startActivity(intent);
                }
            });
        }
    }
}
