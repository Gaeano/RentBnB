package com.usc.rentbnb.adapters;

import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.islands.IslandDetailsActivity;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.ui.favorites.FavoritesIslandsFragment;

import java.util.ArrayList;
import java.util.List;

public class FavoriteIslandAdapter extends RecyclerView.Adapter<FavoriteIslandAdapter.FavoriteIslandViewHolder> {

    public interface onFavoriteClickListener{
        void onHeartClicked(Island island, boolean isCurrentlyFavorite);
    }
    private List<Island> islandList = new ArrayList<>();
    private List<String> favoriteIslandIds = new ArrayList<>();

    public void submitData(List<Island> islands, List<String> favoriteIds){
        this.islandList = (islands != null) ? islands : new ArrayList<>();
        this.favoriteIslandIds = (favoriteIds != null) ? favoriteIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    private onFavoriteClickListener listener;

    public FavoriteIslandAdapter(onFavoriteClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteIslandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_island_card_item, parent, false);
        return new FavoriteIslandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteIslandViewHolder holder, int position) {
        Island currentIsland = islandList.get(position);

        boolean isFavorite = favoriteIslandIds.contains(currentIsland.getId());

        if (isFavorite){
            holder.heartIcon.setImageResource(R.drawable.ic_favorites_filled);
        } else {
            holder.heartIcon.setImageResource(R.drawable.ic_favorites);
        }

        holder.heartIcon.setOnClickListener(v -> {
            holder.heartIcon.animate()
                    .scaleX(0.7f)
                    .scaleY(0.7f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        if (isFavorite){
                            holder.heartIcon.setImageResource(R.drawable.ic_favorites);
                        } else {
                            holder.heartIcon.setImageResource(R.drawable.ic_favorites_filled);
                        }

                        holder.heartIcon.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(200)
                                .setInterpolator(new OvershootInterpolator())
                                .start();
                        if (listener != null){
                            listener.onHeartClicked(currentIsland, isFavorite);
                        }

                    })
                    .start();

        });
        holder.bind(currentIsland);

    }

    @Override
    public int getItemCount() {
        return islandList != null ? islandList.size() : 0;
    }

    static class FavoriteIslandViewHolder extends RecyclerView.ViewHolder {
        private final TextView islandName;
        private final TextView islandLocation;
        private final TextView islandRating;
        private final ImageView islandImage;
        private final ImageView heartIcon;

        public FavoriteIslandViewHolder(@NonNull View itemView) {
            super(itemView);
            islandName = itemView.findViewById(R.id.island_name);
            islandLocation = itemView.findViewById(R.id.island_location);
            islandRating = itemView.findViewById(R.id.island_rating);
            islandImage = itemView.findViewById(R.id.island_image);
            heartIcon = itemView.findViewById(R.id.favorite_heart_icon);
        }
        public void bind(Island island) {
            if (islandName != null) islandName.setText(island.getIslandName());
            if (islandLocation != null) islandLocation.setText(island.getLocation());
            if (islandRating != null) islandRating.setText(String.valueOf(island.getRating()));

            // If you are using Glide or Picasso for images, it goes here:
            // Glide.with(itemView.getContext()).load(island.getImageUrl()).into(islandImage);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), IslandDetailsActivity.class);
                intent.putExtra("island_name", island.getIslandName());
                intent.putExtra("location", island.getLocation());
                intent.putExtra("rating", island.getRating());
                intent.putExtra("description", island.getDescription());
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
