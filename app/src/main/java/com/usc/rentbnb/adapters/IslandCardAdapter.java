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

import com.usc.rentbnb.R;
import com.usc.rentbnb.islands.IslandDetailsActivity;
import com.usc.rentbnb.models.Island;

import java.util.ArrayList;
import java.util.List;

public class IslandCardAdapter extends RecyclerView.Adapter<IslandCardAdapter.IslandViewHolder> {

    private List<Island> islands = new ArrayList<>();
    private List<String> favoriteIds = new ArrayList<>();

    public interface onFavoriteClickListener{
        void onHeartClicked(Island island, boolean isCurrentlyFavorite);
    }

    private onFavoriteClickListener listener;

    public void setIslands(List<Island> islands) {
        this.islands = (islands != null) ? islands : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setFavoriteIds(List<String> favoriteIds) {
        this.favoriteIds = (favoriteIds != null) ? favoriteIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    public IslandCardAdapter(onFavoriteClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public IslandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_island, parent, false);
        return new IslandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IslandViewHolder holder, int position) {
        Island island = islands.get(position);
        boolean isFavorite = favoriteIds.contains(island.getId());
        if (isFavorite) {
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
                        if (isFavorite) {
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

                        if (listener != null) {
                            listener.onHeartClicked(island, isFavorite);
                        }
                    })
                    .start();
        });

        holder.bind(island);
    }

    @Override
    public int getItemCount() {
        return islands.size();
    }

    static class IslandViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, descriptionView;
        ImageView heartIcon;

        IslandViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.islandName);
            descriptionView = itemView.findViewById(R.id.islandDescription);
            heartIcon = itemView.findViewById(R.id.icHeart);
        }

        void bind(Island island) {
            if (nameView != null) nameView.setText(island.getIslandName());
            if (descriptionView != null) descriptionView.setText(island.getDescription());

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