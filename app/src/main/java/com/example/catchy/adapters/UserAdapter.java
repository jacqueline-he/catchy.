package com.example.catchy.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.catchy.databinding.ItemLikedSongBinding;
import com.example.catchy.misc.BitmapCache;
import com.example.catchy.misc.DetailTransition;
import com.example.catchy.misc.ImageLoaderTask;
import com.example.catchy.R;
import com.example.catchy.activities.SongDetailsActivity;
import com.example.catchy.models.Like;
import com.example.catchy.models.Song;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<Like> likes;
    private boolean currentUser;
    private ItemLikedSongBinding binding;

    public UserAdapter(Context context, List<Like> likes, boolean currentUser) {
        this.context = context;
        this.likes = likes;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        binding = ItemLikedSongBinding.inflate(inflater, parent, false);
        View view = binding.getRoot();
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
            ivLikedImage = binding.ivLikedImage;
        }

        public void bind(Like like) {
            String imgUrl = like.getImageUrl();
            if (imgUrl != null)
                Glide.with(context).load(imgUrl).apply(new RequestOptions().override(100, 100)).into(ivLikedImage);
            Log.d("UserAdapter", "bound image");

            int position = getAdapterPosition();
            Bitmap bitmap = null;

            bitmap = BitmapCache.getBitmapFromMemCache(position, true);
            if (bitmap != null) {
                like.bitmap = bitmap;
            } else {
                new ImageLoaderTask(position, like.getImageUrl(), true).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR, (Integer[]) null);
            }

            Log.d("UserAdapter", "like title " + like.getTitle());


            ivLikedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, SongDetailsActivity.class);
                    Song song = new Song();
                    song.setTitle(like.getTitle());
                    song.setURI(like.getURI());
                    song.setArtist(like.getArtist());
                    song.setImageUrl(like.getImageUrl());
                    song.setDuration(like.getDuration());

                    DetailTransition.bitmap = BitmapCache.getBitmapFromMemCache(position, true);
                    // pack something
                    intent.putExtra("song", song);
                    if (currentUser) {
                        intent.putExtra("liked", true);
                        intent.putExtra("from", "user");
                    }
                    else {
                        intent.putExtra("liked", false);
                        intent.putExtra("from", "otheruser");
                    }


                    DetailTransition.pos = getAdapterPosition();
                    context.startActivity(intent);
                }
            });
        }
    }
}
