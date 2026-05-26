package com.usc.rentbnb.ui.booking;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Booking;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private List<Booking> bookingList;

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Title
        holder.tvTitle.setText(
                booking.getListingTitle() != null ? booking.getListingTitle() : "Booking #" + booking.getId().substring(0, 8)
        );

        // Image
        if (booking.getListingImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(booking.getListingImageUrl())
                    .placeholder(R.drawable.ic_no_image_placeholder)
                    .into(holder.ivImage);
        }

        // Dates
        if (booking.getSchedule() != null) {
            holder.tvDates.setText(booking.getSchedule().getStartDate() + " - " + booking.getSchedule().getEndDate());
        } else {
            holder.tvDates.setText("No dates available");
        }

        // Price
        if (booking.getFinancialSummary() != null) {
            holder.tvPrice.setText("₱" + booking.getFinancialSummary().getTotalCharged());
        } else {
            holder.tvPrice.setText("₱0.00");
        }

        // Status
        String status = booking.getStatus() != null ? booking.getStatus() : "unknown";
        holder.tvStatus.setText(status);

        switch (status.toLowerCase()) {
            case "pending":
            case "pending_owner_approval":
                holder.tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
                break;
            case "approved":
            case "accepted":
            case "active":
                holder.tvStatus.setBackgroundResource(R.drawable.status_accepted_bg);
                break;
            case "rejected":
            case "cancelled":
                holder.tvStatus.setBackgroundResource(R.drawable.status_rejected_bg);
                break;
            default:
                holder.tvStatus.setBackgroundColor(Color.GRAY);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDates, tvPrice, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivBookingImage);
            tvTitle = itemView.findViewById(R.id.tvBookingTitle);
            tvDates = itemView.findViewById(R.id.tvBookingDates);
            tvPrice = itemView.findViewById(R.id.tvBookingPrice);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
        }
    }
}