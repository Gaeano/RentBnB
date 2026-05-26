package com.usc.rentbnb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Listing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwnerListingAdapter extends RecyclerView.Adapter<OwnerListingAdapter.ViewHolder> {

    public interface ListingActionListener {
        void onTogglePause(Listing listing);
    }

    private final List<Listing> items = new ArrayList<>();
    private ListingActionListener actionListener;

    public void setActionListener(ListingActionListener listener) {
        this.actionListener = listener;
    }

    public void setItems(List<Listing> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Listing listing = items.get(position);

        holder.tvTitle.setText(listing.getProductName() != null ? listing.getProductName() : "Item");
        holder.tvCategory.setText(listing.getCategory() != null ? listing.getCategory() : "");
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₱%,.0f / %s",
                listing.getPrice(),
                listing.getPriceUnit() != null ? listing.getPriceUnit() : "day"));
        holder.tvStats.setText(String.format(Locale.getDefault(),
                "%d bookings · ★ %.1f", listing.getTimesRented(), listing.getRating()));

        // Status badge — priority: paused > being booked > active
        if (listing.isPaused()) {
            holder.tvStatus.setText("Paused");
            holder.tvStatus.setTextColor(holder.tvStatus.getContext().getColor(android.R.color.white));
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#757575")));
        } else if (listing.isBeingBooked()) {
            holder.tvStatus.setText("Being booked");
            holder.tvStatus.setTextColor(holder.tvStatus.getContext().getColor(android.R.color.white));
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFA500")));
        } else {
            String rawStatus = listing.getStatus();
            String displayStatus = (rawStatus != null && !rawStatus.isEmpty())
                    ? rawStatus.substring(0, 1).toUpperCase() + rawStatus.substring(1).toLowerCase()
                    : "Active";
            holder.tvStatus.setText(displayStatus);
            holder.tvStatus.setTextColor(holder.tvStatus.getContext().getColor(android.R.color.white));
            holder.tvStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3DCFCF")));
        }

        // Thumbnail
        List<String> images = listing.getImageUrls();
        if (images != null && !images.isEmpty()) {
            Glide.with(holder.ivThumb.getContext())
                    .load(images.get(0))
                    .placeholder(R.drawable.ic_no_image_placeholder)
                    .centerCrop()
                    .into(holder.ivThumb);
        } else {
            holder.ivThumb.setImageResource(R.drawable.ic_no_image_placeholder);
        }

        // Three-dot options menu — pause / unpause
        holder.ivOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            if (listing.isPaused()) {
                popup.getMenu().add(0, 1, 0, "Reactivate listing");
            } else {
                popup.getMenu().add(0, 1, 0, "Pause listing");
            }
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1 && actionListener != null) {
                    actionListener.onTogglePause(listing);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivThumb;
        ImageView ivOptions;
        TextView tvTitle, tvCategory, tvPrice, tvStats, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb    = itemView.findViewById(R.id.ivItemThumb);
            ivOptions  = itemView.findViewById(R.id.ivOptions);
            tvTitle    = itemView.findViewById(R.id.tvItemTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice    = itemView.findViewById(R.id.tvPrice);
            tvStats    = itemView.findViewById(R.id.tvStats);
            tvStatus   = itemView.findViewById(R.id.tvStatus);
        }
    }
}