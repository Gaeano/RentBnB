package com.usc.rentbnb.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private List<Island> islandList = new ArrayList<>();

    public void setIslands(List<Island> islands) {
        this.islandList = islands;
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

        public FavoriteIslandViewHolder(@NonNull View itemView) {
            super(itemView);
            islandName = itemView.findViewById(R.id.island_name);
            islandLocation = itemView.findViewById(R.id.island_location);
            islandRating = itemView.findViewById(R.id.island_rating);
            islandImage = itemView.findViewById(R.id.island_image);
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
