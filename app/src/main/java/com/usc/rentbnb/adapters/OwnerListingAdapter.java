package com.usc.rentbnb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private final List<Listing> items = new ArrayList<>();

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

        // R.id.tvItemTitle — verified from item_inventory_listing.xml
        holder.tvTitle.setText(listing.getProductName() != null ? listing.getProductName() : "Item");

        // R.id.tvCategory — verified from item_inventory_listing.xml
        holder.tvCategory.setText(listing.getCategory() != null ? listing.getCategory() : "");

        // R.id.tvPrice — verified from item_inventory_listing.xml
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₱%,.0f / %s",
                listing.getPrice(),
                listing.getPriceUnit() != null ? listing.getPriceUnit() : "day"));

        // R.id.tvStats — bookings count and rating
        holder.tvStats.setText(String.format(Locale.getDefault(),
                "%d bookings · ★ %.1f", listing.getTimesRented(), listing.getRating()));

        // R.id.tvStatus — always "Active" since getAllListings only returns active
        holder.tvStatus.setText("Active");

        // R.id.ivItemThumb — verified from item_inventory_listing.xml
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
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivThumb;
        TextView tvTitle, tvCategory, tvPrice, tvStats, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb    = itemView.findViewById(R.id.ivItemThumb);
            tvTitle    = itemView.findViewById(R.id.tvItemTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice    = itemView.findViewById(R.id.tvPrice);
            tvStats    = itemView.findViewById(R.id.tvStats);
            tvStatus   = itemView.findViewById(R.id.tvStatus);
        }
    }
}