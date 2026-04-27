package com.usc.rentbnb.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.ui.listing.ListingsDetailsActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {
    private static final int TRENDING_THRESHOLD = 20;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    private List<Listing> listings = new ArrayList<>();
    private List<String> favoriteIds = new ArrayList<>();
    private double userLat = 10.3157;
    private double userLon = 123.8854;

    public interface onFavoriteClickListener{
        void onHeartClicked(Listing listing, boolean isCurrentlyFavorite);
    }
    private onFavoriteClickListener listener;

    public ListingAdapter(onFavoriteClickListener listener) {
        this.listener = listener;
    }

    public void setUserLocation(double lat, double lon) {
        this.userLat = lat;
        this.userLon = lon;
        notifyDataSetChanged();
    }

    public void submitData(List<Listing> listings, List<String> favoriteIds) {
        this.listings = (listings != null) ? listings : new ArrayList<>();
        this.favoriteIds = (favoriteIds != null) ? favoriteIds : new ArrayList<>();
        notifyDataSetChanged();
    }


    @Override
    public ListingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rentable_item_card, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListingViewHolder holder, int position) {
        Listing listing = listings.get(position);
        boolean isFavorite = favoriteIds.contains(listing.getId());

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
                            listener.onHeartClicked(listing, isFavorite);
                        }
                    })
                    .start();
        });
        holder.bind(listing, userLat, userLon);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvPrice, tvRentCount, tvCategory;
        ImageView ivListingImage, heartIcon;

        TextView chipNew, chipTrending, chipRating, chipIsland;

        public ListingViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.item_title);
            tvPrice = itemView.findViewById(R.id.item_price);
            tvRentCount = itemView.findViewById(R.id.item_rent_count);
            tvCategory = itemView.findViewById(R.id.item_category);
            ivListingImage = itemView.findViewById(R.id.item_image);
            heartIcon = itemView.findViewById(R.id.favorite_heart_icon);

            chipNew = itemView.findViewById(R.id.chip_new);
            chipTrending = itemView.findViewById(R.id.chip_trending);
            chipRating = itemView.findViewById(R.id.chip_rating);
            chipIsland = itemView.findViewById(R.id.chip_island);
        }

        public void bind(Listing listing, double userLat, double userLon) {
            ///  TODO: onClickListener on card and redirects to listing details
            tvProductName.setText(listing.getProductName());
            tvPrice.setText("₱" + listing.getPrice() + "/" + listing.getPriceUnit());
            tvRentCount.setText(listing.getTimesRented() + " rents");
            tvCategory.setText(listing.getCategory());

            // rating chip
            if (listing.getRating() > 0) {
                chipRating.setVisibility(View.VISIBLE);
                chipRating.setText(String.format(Locale.getDefault(), "%.1f", listing.getRating() ));
            } else {
                chipRating.setVisibility(View.GONE);
            }

            // island chip
            if (listing.getIsland() != null && !listing.getIsland().isEmpty()) {
                chipIsland.setVisibility(View.VISIBLE);
                chipIsland.setText(listing.getIsland());
            } else {
                chipIsland.setVisibility(View.GONE);
            }

            // new chip
            boolean showNewChip = false;
            String createdAt = listing.getCreatedAt();
            if (createdAt != null && !createdAt.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date dateCreated = sdf.parse(createdAt);

                    if (dateCreated != null) {
                        long diffInMillis = System.currentTimeMillis() - dateCreated.getTime();
                        long hoursDiff = diffInMillis / (1000 * 60 * 60);
                        showNewChip = hoursDiff <= 48;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            chipNew.setVisibility(showNewChip ? View.VISIBLE : View.GONE);

            boolean showTrendingChip = listing.getTimesRented() > TRENDING_THRESHOLD;
            chipTrending.setVisibility(showTrendingChip ? View.VISIBLE : View.GONE);

            if (listing.getImageUrls() != null && !listing.getImageUrls().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(listing.getImageUrls().get(0))
                        .centerCrop()
                        .placeholder(R.drawable.ic_no_image_placeholder)
                        .into(ivListingImage);
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.ic_no_image_placeholder)
                        .centerCrop()
                        .into(ivListingImage);
            }

            //TODO: instead of intents, fetch the entire data from db and pass it onto the next activity
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), ListingsDetailsActivity.class);
                // Pass the entire object in one move
                intent.putExtra("listing_object", listing);
                itemView.getContext().startActivity(intent);
            });
        }
    }

}
