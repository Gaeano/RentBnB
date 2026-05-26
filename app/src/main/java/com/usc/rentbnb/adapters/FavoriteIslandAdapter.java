package com.usc.rentbnb.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.islands.IslandDetailsActivity;
import com.usc.rentbnb.models.Island;

import java.util.ArrayList;
import java.util.List;

public class FavoriteIslandAdapter extends RecyclerView.Adapter<FavoriteIslandAdapter.FavoriteIslandViewHolder> {

    public interface onFavoriteClickListener {
        void onHeartClicked(Island island, boolean isCurrentlyFavorite);
    }

    private List<Island> islandList = new ArrayList<>();
    private List<String> favoriteIslandIds = new ArrayList<>();
    private final onFavoriteClickListener listener;

    public FavoriteIslandAdapter(onFavoriteClickListener listener) {
        this.listener = listener;
    }

    public void submitData(List<Island> islands, List<String> favoriteIds) {
        this.islandList = (islands != null) ? islands : new ArrayList<>();
        this.favoriteIslandIds = (favoriteIds != null) ? favoriteIds : new ArrayList<>();
        notifyDataSetChanged();
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

        holder.heartIcon.setImageResource(
                isFavorite ? R.drawable.ic_favorites_filled : R.drawable.ic_favorites);

        holder.heartIcon.setOnClickListener(v -> {
            holder.heartIcon.animate()
                    .scaleX(0.7f).scaleY(0.7f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        holder.heartIcon.setImageResource(
                                isFavorite ? R.drawable.ic_favorites : R.drawable.ic_favorites_filled);
                        holder.heartIcon.animate()
                                .scaleX(1.0f).scaleY(1.0f)
                                .setDuration(200)
                                .setInterpolator(new OvershootInterpolator())
                                .start();
                        if (listener != null) listener.onHeartClicked(currentIsland, isFavorite);
                    })
                    .start();
        });

        holder.bind(currentIsland);
    }

    @Override
    public int getItemCount() { return islandList != null ? islandList.size() : 0; }

    static class FavoriteIslandViewHolder extends RecyclerView.ViewHolder {
        private final TextView islandName;
        private final ImageView islandImage;
        private final ImageView heartIcon;

        public FavoriteIslandViewHolder(@NonNull View itemView) {
            super(itemView);
            islandName     = itemView.findViewById(R.id.island_name);
            islandImage    = itemView.findViewById(R.id.island_image);
            heartIcon      = itemView.findViewById(R.id.favorite_heart_icon);
        }

        public void bind(Island island) {
            if (islandName != null) islandName.setText(island.getIslandName());

            if (islandImage != null) {
                String imageUrl = island.getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade(300))
                            .placeholder(R.color.teal_dark)
                            .error(R.color.teal_dark)
                            .into(islandImage);
                } else {
                    islandImage.setImageResource(0);
                    islandImage.setBackgroundColor(
                            itemView.getContext().getColor(R.color.teal_dark));
                }
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), IslandDetailsActivity.class);
                intent.putExtra("island_name", island.getIslandName());
                intent.putExtra("description", island.getDescription());
                intent.putExtra("imageUrl", island.getImageUrl());
                itemView.getContext().startActivity(intent);
            });
        }
    }
}