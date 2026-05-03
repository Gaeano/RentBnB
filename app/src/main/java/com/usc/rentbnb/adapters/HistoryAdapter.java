package com.usc.rentbnb.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Booking> bookings = new ArrayList<>();

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        holder.title.setText(booking.getTitle());
        holder.category.setText(booking.getCategory());
        holder.ownerName.setText(booking.getOwnerName());
        holder.price.setText(booking.getPrice());
        holder.dates.setText(booking.getDateRange());
        holder.status.setText(booking.getStatus().toUpperCase());

        // Handle visual distinction between Upcoming and Past
        if (booking.getStatus().equalsIgnoreCase("ACTIVE")) {
            holder.statusCard.setCardBackgroundColor(Color.parseColor("#5BC8D3")); // Teal
            holder.status.setTextColor(Color.WHITE);
            holder.itemView.setAlpha(1.0f); // Full opacity
        } else {
            holder.statusCard.setCardBackgroundColor(Color.parseColor("#E0E0E0")); // Grey
            holder.status.setTextColor(Color.parseColor("#757575"));
            holder.itemView.setAlpha(0.7f); // Slightly dim past items
        }

        // TODO: Load image using Glide or Picasso here
        // Glide.with(holder.itemView.getContext()).load(booking.getImageUrl()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, category, ownerName, price, dates, status;
        CardView statusCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            title = itemView.findViewById(R.id.item_title);
            category = itemView.findViewById(R.id.item_category);
            ownerName = itemView.findViewById(R.id.item_owner_name);
            price = itemView.findViewById(R.id.item_price);
            dates = itemView.findViewById(R.id.item_dates);
            status = itemView.findViewById(R.id.item_status);
            statusCard = itemView.findViewById(R.id.status_card);
        }
    }
}