package com.usc.rentbnb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Listing;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwnerListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_PROFILE = 0;
    private static final int VIEW_TYPE_DASHBOARD = 1;

    private List<Listing> listings = new ArrayList<>();
    private final boolean isProfileMode;
    private ActionListener actionListener;

    public interface ActionListener {
        void onActionClicked(Listing listing);
    }

    public OwnerListingAdapter() {
        this.isProfileMode = false;
    }

    public OwnerListingAdapter(boolean isProfileMode) {
        this.isProfileMode = isProfileMode;
    }

    public void submitData(List<Listing> newListings) {
        this.listings = newListings;
        notifyDataSetChanged();
    }

    public void setItems(List<Listing> items) {
        this.listings = items;
        notifyDataSetChanged();
    }

    public void setActionListener(ActionListener listener) {
        this.actionListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return isProfileMode ? VIEW_TYPE_PROFILE : VIEW_TYPE_DASHBOARD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PROFILE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.owner_item_card_profile, parent, false);
            return new ProfileViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_inventory_listing, parent, false);
            return new DashboardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Listing listing = listings.get(position);
        if (holder instanceof ProfileViewHolder) {
            ((ProfileViewHolder) holder).bind(listing);
        } else if (holder instanceof DashboardViewHolder) {
            ((DashboardViewHolder) holder).bind(listing);
        }
    }

    @Override
    public int getItemCount() {
        if (isProfileMode){
            return Math.min(listings.size(), 5);
        } else {
            return listings.size();
        }
    }

    // --- VIEW HOLDERS ---

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivHeart;
        TextView tvName, tvPrice;
        TextView chipNew, chipTrending, chipRating, chipIsland;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.item_image);
            ivHeart = itemView.findViewById(R.id.favorite_heart_icon);
            tvName = itemView.findViewById(R.id.item_title);
            tvPrice = itemView.findViewById(R.id.item_price);

            chipNew = itemView.findViewById(R.id.chip_new);
            chipTrending = itemView.findViewById(R.id.chip_trending);
            chipRating = itemView.findViewById(R.id.chip_rating);
            chipIsland = itemView.findViewById(R.id.chip_island);
        }

        public void bind(Listing listing) {
            tvName.setText(listing.getProductName());
            tvPrice.setText(String.format(Locale.getDefault(), "₱%,.0f/%s", listing.getPrice(), listing.getPriceUnit()));

            if (listing.getImageUrls() != null && !listing.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(listing.getImageUrls().get(0))
                        .placeholder(R.drawable.ic_no_image_placeholder)
                        .centerCrop()
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.ic_no_image_placeholder);
            }

            // Island chip
            if (listing.getIsland() != null && !listing.getIsland().isEmpty()) {
                chipIsland.setText(listing.getIsland());
                chipIsland.setVisibility(View.VISIBLE);
            } else {
                chipIsland.setVisibility(View.GONE);
            }

            // Rating chip
            if (listing.getRating() == 0){
                chipRating.setVisibility(View.GONE);
            } else{
                chipRating.setText(String.format(Locale.getDefault(), "%.1f", listing.getRating()));
            }

            // New tag
            chipNew.setVisibility(listing.isNew() ? View.VISIBLE : View.GONE);

            // Trending tag (Threshold: 20 rentals)
            boolean isTrending = listing.getTimesRented() >= 20;
            chipTrending.setVisibility(isTrending ? View.VISIBLE : View.GONE);
        }
    }

    class DashboardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb, ivOptions;
        TextView tvTitle, tvCategory, tvPrice, tvStats, tvStatus;

        public DashboardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivItemThumb);
            ivOptions = itemView.findViewById(R.id.ivOptions);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStats = itemView.findViewById(R.id.tvStats);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Listing listing) {
            tvTitle.setText(listing.getProductName());
            tvCategory.setText(listing.getCategory());
            tvPrice.setText(String.format(Locale.getDefault(), "₱%,.0f/%s", listing.getPrice(), listing.getPriceUnit()));
            
            int bookings = 0; // Backend should provide this
            tvStats.setText(String.format(Locale.getDefault(), "%d bookings • ★ %.1f", bookings, listing.getRating()));

            String status = listing.getStatus();
            boolean isPaused = "paused".equalsIgnoreCase(status);
            tvStatus.setText(isPaused ? "👁 Paused" : "👁 Active");
            tvStatus.setTextColor(isPaused ? 0xFF757575 : 0xFF008080);

            if (listing.getImageUrls() != null && !listing.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(listing.getImageUrls().get(0))
                        .placeholder(R.drawable.ic_no_image_placeholder)
                        .centerCrop()
                        .into(ivThumb);
            }

            ivOptions.setOnClickListener(v -> {
                if (actionListener != null) actionListener.onActionClicked(listing);
            });
        }
    }
}