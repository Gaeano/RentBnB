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

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Booking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Booking> bookings = new ArrayList<>();


    // --- Review click listener interface ---
    public interface OnReviewClickListener {
        void onReviewClick(Booking booking);
    }

    private OnReviewClickListener reviewClickListener;

    public void setOnReviewClickListener(OnReviewClickListener listener) {
        this.reviewClickListener = listener;
    }

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

        // Title
        holder.title.setText(
                booking.getListingTitle() != null
                        ? booking.getListingTitle()
                        : "Booking #" + booking.getId().substring(0, 8)
        );

        // Image
        if (booking.getListingImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(booking.getListingImageUrl())
                    .placeholder(R.drawable.ic_no_image_placeholder)
                    .centerCrop()
                    .into(holder.image);
        }

        // Owner name
        holder.ownerName.setText(
                booking.getOwnerName() != null
                        ? "Owner: " + booking.getOwnerName()
                        : "Owner ID: " + booking.getOwnerId()
        );

        // Status
        String status = booking.getStatus() != null ? booking.getStatus() : "unknown";
        holder.status.setText(status);

        // Dates
        if (booking.getSchedule() != null) {
            String start = formatDate(booking.getSchedule().getStartDate());
            String end = formatDate(booking.getSchedule().getEndDate());
            holder.dates.setText(start + " → " + end);
        } else {
            holder.dates.setText("No dates available");
        }

        // Price
        if (booking.getFinancialSummary() != null) {
            holder.price.setText(String.format("₱%.2f", booking.getFinancialSummary().getTotalCharged()));
        } else {
            holder.price.setText("₱0.00");
        }

        // Status card color
        if ("ACTIVE".equalsIgnoreCase(status) || "PENDING_OWNER_APPROVAL".equalsIgnoreCase(status)) {
            holder.statusCard.setCardBackgroundColor(Color.parseColor("#5BC8D3"));
            holder.status.setTextColor(Color.WHITE);
            holder.itemView.setAlpha(1.0f);
        } else if ("PENDING".equalsIgnoreCase(status)){
            holder.statusCard.setCardBackgroundColor(Color.parseColor("#FEFFE7"));
            holder.status.setTextColor(Color.parseColor("#D1A500"));
            holder.itemView.setAlpha(1.0f);
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            holder.statusCard.setCardBackgroundColor(Color.parseColor("#C8E6C9")); // soft green
            holder.status.setTextColor(Color.parseColor("#2E7D32"));
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.statusCard.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
            holder.status.setTextColor(Color.parseColor("#757575"));
            holder.itemView.setAlpha(0.7f);
        }

        // Leave Review button — only show for COMPLETED bookings
        if ("COMPLETED".equalsIgnoreCase(status)) {
            holder.btnLeaveReview.setVisibility(View.VISIBLE);
            holder.btnLeaveReview.setOnClickListener(v -> {
                if (reviewClickListener != null) {
                    reviewClickListener.onReviewClick(booking);
                }
            });
        } else {
            holder.btnLeaveReview.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, ownerName, price, dates, status;
        CardView statusCard;
        MaterialButton btnLeaveReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            title = itemView.findViewById(R.id.item_title);
            ownerName = itemView.findViewById(R.id.item_owner_name);
            price = itemView.findViewById(R.id.item_price);
            dates = itemView.findViewById(R.id.item_dates);
            status = itemView.findViewById(R.id.item_status);
            statusCard = itemView.findViewById(R.id.status_card);
            btnLeaveReview = itemView.findViewById(R.id.btn_leave_review);
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null) return "N/A";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat output = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return output.format(input.parse(isoDate));
        } catch (Exception e) {
            return isoDate;
        }
    }
}