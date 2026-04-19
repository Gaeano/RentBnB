package com.usc.rentbnb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Listing;

import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {
    private List<Listing> listings;

    public ListingAdapter(List<Listing> listings) {
        this.listings = listings;
    }

    @Override
    public ListingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rentable_item_card, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListingViewHolder holder, int position) {
        Listing listing = listings.get(position);
        holder.bind(listing);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvPrice, tvReviewCount, tvCategory;

        public ListingViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.item_title);
            tvPrice = itemView.findViewById(R.id.item_price);
            tvReviewCount = itemView.findViewById(R.id.item_rent_count);
            tvCategory = itemView.findViewById(R.id.item_category);
        }

        public void bind(Listing listing) {
            tvProductName.setText(listing.getProductName());
            tvPrice.setText("₱" + listing.getPrice() + " / " + listing.getPriceUnit());
            tvReviewCount.setText(listing.getTotalReviews() + " reviews");
            tvCategory.setText(listing.getCategory());
        }
    }

}
