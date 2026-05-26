package com.usc.rentbnb.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.islands.IslandDetailsActivity;
import com.usc.rentbnb.models.Island;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IslandCardAdapter extends RecyclerView.Adapter<IslandCardAdapter.IslandViewHolder> implements ListPreloader.PreloadModelProvider<Island> {

    private List<Island> islands = new ArrayList<>();
    private List<String> favoriteIds = new ArrayList<>();

    private int cardWidth  = 0;
    private int cardHeight = 0;

    public interface onFavoriteClickListener {
        void onHeartClicked(Island island, boolean isCurrentlyFavorite);
    }

    private final onFavoriteClickListener listener;

    public void setIslands(List<Island> newList) {
        if (newList == null) newList = new ArrayList<>();
        
        // Use DiffUtil for better list update performance
        final List<Island> oldList = this.islands;
        final List<Island> finalList = newList;
        
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return oldList.size(); }
            @Override
            public int getNewListSize() { return finalList.size(); }
            @Override
            public boolean areItemsTheSame(int oldItemPos, int newItemPos) {
                return oldList.get(oldItemPos).getId().equals(finalList.get(newItemPos).getId());
            }
            @Override
            public boolean areContentsTheSame(int oldItemPos, int newItemPos) {
                return oldList.get(oldItemPos).equals(finalList.get(newItemPos));
            }
        });
        
        this.islands = newList;
        result.dispatchUpdatesTo(this);
    }

    public void setFavoriteIds(List<String> favoriteIds) {
        this.favoriteIds = (favoriteIds != null) ? favoriteIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void attachPreloader(RecyclerView recyclerView, int widthPx, int heightPx) {
        this.cardWidth  = widthPx;
        this.cardHeight = heightPx;

        FixedPreloadSizeProvider<Island> sizeProvider =
                new FixedPreloadSizeProvider<>(widthPx, heightPx);
        RecyclerViewPreloader<Island> preloader =
                new RecyclerViewPreloader<>(
                        Glide.with(recyclerView.getContext()),
                        this,
                        sizeProvider,
                        2
                );
        recyclerView.addOnScrollListener(preloader);
    }

    @NonNull
    @Override
    public List<Island> getPreloadItems(int position) {
        if (position >= islands.size()) return Collections.emptyList();
        Island island = islands.get(position);
        if (island.getImageUrl() == null || island.getImageUrl().isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(island);
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull Island island) {
        return Glide.with(appContext)
                .load(buildCloudinaryUrl(island.getImageUrl(), cardWidth))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();
    }

    private Context appContext;

    public IslandCardAdapter(onFavoriteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public IslandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (appContext == null) appContext = parent.getContext().getApplicationContext();
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

        // Pass the dimensions to the static bind method
        holder.bind(island, cardWidth, cardHeight, width -> {
            if (cardWidth == 0) cardWidth = width;
        }, height -> {
            if (cardHeight == 0) cardHeight = height;
        });
    }

    @Override
    public int getItemCount() {
        return islands.size();
    }

    static String buildCloudinaryUrl(String rawUrl, int displayWidthPx) {
        if (rawUrl == null || rawUrl.isEmpty()) return rawUrl;
        if (!rawUrl.contains("res.cloudinary.com")) return rawUrl;

        // Target width: display width × 1.5 for high-DPI screens, capped at 1200px
        int targetWidth = Math.min((int) (displayWidthPx * 1.5f), 1200);
        if (targetWidth <= 0) targetWidth = 800;

        // Insert the transformation segment after /upload/
        String transform = "w_" + targetWidth + ",c_fill,q_auto,f_auto";
        return rawUrl.replace("/upload/", "/upload/" + transform + "/");
    }

    interface DimensionCallback {
        void onDimensionKnown(int dimension);
    }

    static class IslandViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, descriptionView;
        ImageView heartIcon, islandImageView;

        IslandViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.islandName);
            descriptionView = itemView.findViewById(R.id.islandDescription);
            heartIcon = itemView.findViewById(R.id.icHeart);
            islandImageView = itemView.findViewById(R.id.islandImage);
        }

        void bind(Island island, int currentWidth, int currentHeight, DimensionCallback widthCallback, DimensionCallback heightCallback) {
            if (nameView != null) nameView.setText(island.getIslandName());
            if (descriptionView != null) descriptionView.setText(island.getDescription());

            if (islandImageView != null) {
                if (currentWidth == 0) {
                    islandImageView.post(() -> {
                        widthCallback.onDimensionKnown(islandImageView.getWidth());
                        heightCallback.onDimensionKnown(islandImageView.getHeight());
                    });
                }

                String rawUrl = island.getImageUrl();
                int displayWidth = currentWidth > 0 ? currentWidth
                        : islandImageView.getResources().getDisplayMetrics().widthPixels;
                String optimisedUrl = buildCloudinaryUrl(rawUrl, displayWidth);

                if (optimisedUrl != null && !optimisedUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(optimisedUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .thumbnail(
                                    Glide.with(itemView.getContext())
                                            .load(buildCloudinaryUrl(rawUrl, 80))
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .centerCrop()
                            )
                            .transition(DrawableTransitionOptions.withCrossFade(200))
                            .placeholder(R.color.teal_dark)
                            .error(R.color.teal_dark)
                            .into(islandImageView);
                } else {
                    islandImageView.setImageResource(0);
                    islandImageView.setBackgroundColor(
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