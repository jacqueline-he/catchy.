package com.example.catchy.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.catchy.DetailTransition;
import com.example.catchy.R;
import com.example.catchy.activities.SongDetailsActivity;
import com.example.catchy.models.Song;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{
    private List<Song> results;
    private Context context;


    public SearchAdapter(List<Song> results, Context context) {
        this.results = results;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        Song song = results.get(position);
        holder.bind(song);


    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        results.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Song> list) {
        results.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAlbumImage;
        private TextView tvTitle;
        private TextView tvArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAlbumImage = itemView.findViewById(R.id.ivAlbumImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);


        }

        public void bind(Song song) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            Glide.with(context).load(song.getImageUrl()).into(ivAlbumImage);

            // TODO find way of grabbing entire itemview
            tvTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(() -> {
                        try {
                            DetailTransition.bitmap = Picasso.get().load(song.getImageUrl()).get();
                        } catch (Exception e) {
                            Log.e("SongDetailsActivity", "couldn't get bitmap"+e);
                        }
                    }).start();

                    Intent intent = new Intent(context, SongDetailsActivity.class);
                    // pack something
                    intent.putExtra("song", song);
                    intent.putExtra("liked", false); // TODO fix
                    intent.putExtra("playing", false);
                    intent.putExtra("from", "search");
                    context.startActivity(intent);
                }
            });

            ivAlbumImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new Thread(() -> {
                        try {
                            DetailTransition.bitmap = Picasso.get().load(song.getImageUrl()).get();
                        } catch (Exception e) {
                            Log.e("SongDetailsActivity", "couldn't get bitmap"+e);
                        }
                    }).start();

                    Intent intent = new Intent(context, SongDetailsActivity.class);
                    // pack something
                    intent.putExtra("song", song);
                    intent.putExtra("liked", false); // TODO fix
                    intent.putExtra("playing", false);
                    intent.putExtra("from", "search");
                    context.startActivity(intent);
                }
            });

        }
    }
}
