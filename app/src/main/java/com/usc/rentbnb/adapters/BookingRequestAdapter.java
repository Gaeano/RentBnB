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
import com.usc.rentbnb.models.Booking;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingRequestAdapter extends RecyclerView.Adapter<BookingRequestAdapter.ViewHolder> {

    public interface ActionListener {
        void onAccept(Booking booking);
        void onDecline(Booking booking);
    }

    private final List<Booking> items = new ArrayList<>();
    private final ActionListener listener;

    public BookingRequestAdapter(ActionListener listener) {
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
                .inflate(R.layout.item_booking_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = items.get(position);

        // Renter name
        String renterName = booking.getRenterName();
        if (renterName == null || renterName.isEmpty()) {
            if (booking.getRenterDetails() != null && booking.getRenterDetails().getName() != null) {
                renterName = booking.getRenterDetails().getName();
            } else {
                renterName = "Renter";
            }
        }
        holder.tvRenterName.setText(renterName);

        // Listing title — with Firestore fallback for bookings created before
        // the new controller stored listingTitle on the document
        String title = booking.getListingTitle();
        if (title == null || title.isEmpty()) title = booking.getProductName();

        if (title != null && !title.isEmpty()) {
            holder.tvRentedItemName.setText(title);
        } else if (booking.getListingId() != null && !booking.getListingId().isEmpty()) {
            holder.tvRentedItemName.setText("Loading...");
            String listingId = booking.getListingId();
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("listings")
                    .document(listingId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc == null || !doc.exists()) return;
                        String fetched = doc.getString("productName");
                        if (fetched != null) booking.setCachedListingTitle(fetched);
                        if (holder.getAdapterPosition() != androidx.recyclerview.widget.RecyclerView.NO_ID) {
                            holder.tvRentedItemName.setText(fetched != null ? fetched : "Item");
                        }
                    })
                    .addOnFailureListener(e -> holder.tvRentedItemName.setText("Item"));
        } else {
            holder.tvRentedItemName.setText("Item");
        }

        // Dates
        String startDate = formatDate(booking.getStartDate());
        String endDate   = formatDate(booking.getEndDate());
        holder.tvBookingDates.setText(startDate + " – " + endDate);

        // Payout
        double payout = booking.getTotalPrice();
        holder.tvBookingPayout.setText(String.format(Locale.getDefault(), "₱%,.2f", payout));

        // Avatar
        String photoUrl = booking.getRenterPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(holder.ivRenterAvatar.getContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.userprofile)
                    .circleCrop()
                    .into(holder.ivRenterAvatar);
        } else {
            holder.ivRenterAvatar.setImageResource(R.drawable.userprofile);
        }

        // Buttons
        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(booking);
        });
        holder.btnDecline.setOnClickListener(v -> {
            if (listener != null) listener.onDecline(booking);
        });
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "—";
        try {
            SimpleDateFormat input  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("MMM d", Locale.getDefault());
            Date date = input.parse(isoDate.substring(0, 10));
            return date != null ? output.format(date) : isoDate;
        } catch (ParseException e) {
            return isoDate;
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivRenterAvatar;
        TextView tvRenterName, tvRentedItemName, tvBookingDates, tvBookingPayout;
        View btnAccept, btnDecline;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRenterAvatar   = itemView.findViewById(R.id.ivRenterAvatar);
            tvRenterName     = itemView.findViewById(R.id.tvRenterName);
            tvRentedItemName = itemView.findViewById(R.id.tvRentedItemName);
            tvBookingDates   = itemView.findViewById(R.id.tvBookingDates);
            tvBookingPayout  = itemView.findViewById(R.id.tvBookingPayout);
            btnAccept        = itemView.findViewById(R.id.btnAccept);
            btnDecline       = itemView.findViewById(R.id.btnDecline);
        }
    }
}