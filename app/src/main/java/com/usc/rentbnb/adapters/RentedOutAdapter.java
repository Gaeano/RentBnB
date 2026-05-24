package com.usc.rentbnb.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Booking;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RentedOutAdapter extends RecyclerView.Adapter<RentedOutAdapter.ViewHolder> {

    public interface CompleteListener {
        void onMarkComplete(Booking booking);
    }

    private final List<Booking> items = new ArrayList<>();
    private final CompleteListener listener;

    public RentedOutAdapter(CompleteListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Booking> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rented_out, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = items.get(position);

        // Title
        String title = booking.getListingTitle();
        if (title == null || title.isEmpty()) title = booking.getProductName();
        if (title == null) title = "Item";
        holder.tvItemName.setText(title);

        // Rented by
        String renterName = booking.getRenterName();
        if (renterName == null || renterName.isEmpty()) {
            if (booking.getRenterDetails() != null) renterName = booking.getRenterDetails().getName();
        }
        holder.tvRentedBy.setText("Rented by " + (renterName != null ? renterName : "Renter"));

        // Status pill
        applyStatusPill(holder.tvStatusPill, booking);

        // Thumbnail
        String imageUrl = booking.getListingImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) imageUrl = booking.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.ivItemThumbnail.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_no_image_placeholder)
                    .centerCrop()
                    .into(holder.ivItemThumbnail);
        } else {
            holder.ivItemThumbnail.setImageResource(R.drawable.ic_no_image_placeholder);
        }

        // Long-press to mark complete
        holder.itemView.setOnLongClickListener(v -> {
            if ("ACTIVE".equals(booking.getStatus()) || "OVERDUE".equals(booking.getStatus())) {
                if (listener != null) listener.onMarkComplete(booking);
            }
            return true;
        });
    }

    private void applyStatusPill(TextView pill, Booking booking) {
        String status  = booking.getStatus();
        String endDate = booking.getEndDate();
        long daysLeft  = getDaysUntil(endDate);

        if ("OVERDUE".equals(status)) {
            pill.setText("Overdue");
            pill.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
            pill.setTextColor(Color.parseColor("#FF4B4B"));
        } else if (daysLeft == 0) {
            pill.setText("Returns today");
            pill.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
            pill.setTextColor(Color.parseColor("#FFA500"));
        } else if (daysLeft <= 2) {
            pill.setText("Returns in " + daysLeft + " day" + (daysLeft == 1 ? "" : "s"));
            pill.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
            pill.setTextColor(Color.parseColor("#FFA500"));
        } else {
            pill.setText("Returns in " + daysLeft + " days");
            pill.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFEFEF")));
            pill.setTextColor(Color.parseColor("#616161"));
        }
    }

    private long getDaysUntil(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date end  = sdf.parse(isoDate.substring(0, 10));
            Date now  = new Date();
            if (end == null) return -1;
            long diff = end.getTime() - now.getTime();
            return TimeUnit.MILLISECONDS.toDays(diff);
        } catch (ParseException e) {
            return -1;
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivItemThumbnail;
        TextView tvItemName, tvRentedBy, tvStatusPill;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemThumbnail = itemView.findViewById(R.id.ivItemThumbnail);
            tvItemName      = itemView.findViewById(R.id.tvItemName);
            tvRentedBy      = itemView.findViewById(R.id.tvRentedBy);
            tvStatusPill    = itemView.findViewById(R.id.tvStatusPill);
        }
    }
}