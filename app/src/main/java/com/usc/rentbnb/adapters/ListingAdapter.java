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
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.ui.listing.ListingsDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {
    private List<Listing> listings = new ArrayList<>();
    private List<String> favoriteIds = new ArrayList<>();

    public interface onFavoriteClickListener{
        void onHeartClicked(Listing listing, boolean isCurrentlyFavorite);
    }
    private onFavoriteClickListener listener;

    public ListingAdapter(onFavoriteClickListener listener) {
        this.listener = listener;
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
                            listener.onHeartClicked(listing, isFavorite);
                        }

                    })
                    .start();

        });


        holder.bind(listing);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvPrice, tvReviewCount, tvCategory;
        ImageView ivListingImage, heartIcon;

        public ListingViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.item_title);
            tvPrice = itemView.findViewById(R.id.item_price);
            tvReviewCount = itemView.findViewById(R.id.item_rent_count);
            tvCategory = itemView.findViewById(R.id.item_category);
            ivListingImage = itemView.findViewById(R.id.item_image);
            heartIcon = itemView.findViewById(R.id.favorite_heart_icon);
        }

        public void bind(Listing listing) {
            /// TODO: Add to Favorites button functional
            ///  TODO: onClickListener on card and redirects to listing details
            tvProductName.setText(listing.getProductName());
            tvPrice.setText("₱" + listing.getPrice() + "/" + listing.getPriceUnit());
            tvReviewCount.setText(listing.getTotalReviews() + " rents");
            tvCategory.setText(listing.getCategory());

            if (listing.getImageUrls() != null && !listing.getImageUrls().isEmpty()) {
                String coverPhotoUrl = listing.getImageUrls().get(0);

                Glide.with(itemView.getContext())
                        .load(coverPhotoUrl)
                        .centerCrop()
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

                intent.putExtra("product_name", listing.getProductName());
                intent.putExtra("price", String.valueOf(listing.getPrice()));
                intent.putExtra("price_unit", listing.getPriceUnit());
                intent.putExtra("category", listing.getCategory());
                itemView.getContext().startActivity(intent);
            });
        }
    }

}
