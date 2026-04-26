package com.usc.rentbnb.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    public void setIslands(List<Island> islands) {
        this.islands = islands;
        notifyDataSetChanged();
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
        holder.bind(islands.get(position));
    }

    @Override
    public int getItemCount() {
        return islands.size();
    }

    static class IslandViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, descriptionView, trendingChipView;

        IslandViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.islandName);
            descriptionView = itemView.findViewById(R.id.islandDescription);
            trendingChipView = itemView.findViewById(R.id.islandTrendingChip);
        }

        void bind(Island island) {
            if (nameView != null) nameView.setText(island.getIslandName());
            if (descriptionView != null) descriptionView.setText(island.getDescription());

            if (trendingChipView != null) {
                String category = "";

                if (island.getCategory() != null) {
                    category = island.getCategory().toString().toLowerCase();
                }

                if (category.contains("popular") || category.contains("trending")) {
                    trendingChipView.setVisibility(View.VISIBLE);
                } else {
                    trendingChipView.setVisibility(View.GONE);
                }
            }

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