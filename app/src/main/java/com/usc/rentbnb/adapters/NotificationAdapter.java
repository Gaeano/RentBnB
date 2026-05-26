package com.usc.rentbnb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification n = notifications.get(position);
        String type = n.getType() != null ? n.getType() : "";

        holder.tvExtra.setVisibility(View.GONE);
        holder.tvDetailsLink.setVisibility(View.GONE);
        holder.ivIcon.setImageTintList(null);
        holder.ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.itemView.setAlpha(n.isRead() ? 0.6f : 1.0f);
        holder.viewUnreadIndicator.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);

        switch (type) {

            // ---- Someone rented your listing (owner receives this) ----
            case "rent":
            case "NEW_BOOKING_REQUEST": {
                holder.tvTitle.setText("New booking request!");
                holder.tvSubtitle.setText(n.getProductName() != null ? n.getProductName() : "");
                if (n.getProductCategory() != null && !n.getProductCategory().isEmpty()) {
                    holder.tvExtra.setText(n.getProductCategory());
                    holder.tvExtra.setVisibility(View.VISIBLE);
                }
                holder.tvDetailsLink.setVisibility(View.VISIBLE);

                String img = n.getProductImage();
                if (img != null && !img.isEmpty()) {
                    Glide.with(holder.ivIcon.getContext())
                            .load(img)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade(200))
                            .placeholder(R.drawable.ic_no_image_placeholder)
                            .error(R.drawable.ic_no_image_placeholder)
                            .into(holder.ivIcon);
                } else {
                    holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    holder.ivIcon.setImageResource(R.drawable.ic_calendar);
                    holder.ivIcon.setImageTintList(
                            android.content.res.ColorStateList.valueOf(
                                    android.graphics.Color.parseColor("#3DCFCF")));
                }
                break;
            }

            // ---- Booking status events (renter receives these) ----
            case "BOOKING_ACTIVE": {
                holder.tvTitle.setText(n.getTitle() != null ? n.getTitle() : "Booking Accepted");
                holder.tvSubtitle.setText(n.getBody() != null ? n.getBody() : "");
                holder.tvDetailsLink.setVisibility(View.VISIBLE);
                holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.ivIcon.setImageResource(R.drawable.ic_check_circle);
                holder.ivIcon.setImageTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#3DCFCF")));
                break;
            }
            case "BOOKING_REJECTED": {
                holder.tvTitle.setText(n.getTitle() != null ? n.getTitle() : "Booking Declined");
                holder.tvSubtitle.setText(n.getBody() != null ? n.getBody() : "");
                holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.ivIcon.setImageResource(R.drawable.ic_close);
                holder.ivIcon.setImageTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#FF4B4B")));
                break;
            }
            case "BOOKING_COMPLETED": {
                holder.tvTitle.setText(n.getTitle() != null ? n.getTitle() : "Booking Completed");
                holder.tvSubtitle.setText(n.getBody() != null ? n.getBody() : "");
                holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.ivIcon.setImageResource(R.drawable.ic_check_circle);
                holder.ivIcon.setImageTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#4CAF50")));
                break;
            }
            case "PENALTY_CHARGED": {
                holder.tvTitle.setText(n.getTitle() != null ? n.getTitle() : "Penalty Applied");
                holder.tvSubtitle.setText(n.getBody() != null ? n.getBody() : "");
                holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.ivIcon.setImageResource(R.drawable.ic_wallet);
                holder.ivIcon.setImageTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#FF4B4B")));
                break;
            }
            case "OVERDUE": {
                holder.tvTitle.setText(n.getTitle() != null ? n.getTitle() : "Booking Overdue");
                holder.tvSubtitle.setText(n.getBody() != null ? n.getBody() : "");
                holder.tvDetailsLink.setVisibility(View.VISIBLE);
                holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.ivIcon.setImageResource(R.drawable.ic_calendar);
                holder.ivIcon.setImageTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#FF4B4B")));
                break;
            }
            case "OVERDUE_WARNING": {
                holder.tvTitle.setText(n.getTitle() != null ? n.getTitle() : "Return Due Soon");
                holder.tvSubtitle.setText(n.getBody() != null ? n.getBody() : "");
                holder.tvDetailsLink.setVisibility(View.VISIBLE);
                holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.ivIcon.setImageResource(R.drawable.ic_calendar);
                holder.ivIcon.setImageTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#FFA500")));
                break;
            }

            // ---- Chat ----
            case "chat": {
                // Title: "sender_name messaged you" or "Someone messaged you"
                String senderName = n.getUsername();
                holder.tvTitle.setText(
                        (senderName != null && !senderName.isEmpty())
                                ? senderName + " messaged you"
                                : "New message");
                holder.tvSubtitle.setText(
                        n.getMessage() != null ? n.getMessage() : "");

                // Avatar: use photo URL if present, else userprofile.png
                String photoUrl = n.getProductImage(); // reused for chat sender photo
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(holder.ivIcon.getContext())
                            .load(photoUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .circleCrop()
                            .transition(DrawableTransitionOptions.withCrossFade(200))
                            .placeholder(R.drawable.userprofile)
                            .error(R.drawable.userprofile)
                            .into(holder.ivIcon);
                } else {
                    holder.ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    holder.ivIcon.setImageResource(R.drawable.userprofile);
                }
                break;
            }

            // ---- New listing near user ----
            case "listing": {
                holder.tvTitle.setText("New item near you!");
                holder.tvSubtitle.setText(n.getProductName() != null ? n.getProductName() : "");
                if (n.getProductCategory() != null && !n.getProductCategory().isEmpty()) {
                    holder.tvExtra.setText(n.getProductCategory());
                    holder.tvExtra.setVisibility(View.VISIBLE);
                }
                holder.tvDetailsLink.setVisibility(View.VISIBLE);

                String listingImg = n.getProductImage();
                if (listingImg != null && !listingImg.isEmpty()) {
                    Glide.with(holder.ivIcon.getContext())
                            .load(listingImg)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .transition(DrawableTransitionOptions.withCrossFade(200))
                            .placeholder(R.drawable.ic_no_image_placeholder)
                            .error(R.drawable.ic_no_image_placeholder)
                            .into(holder.ivIcon);
                } else {
                    holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    holder.ivIcon.setImageResource(R.drawable.ic_discover);
                }
                break;
            }

            // ---- Fallback for any unknown type ----
            default: {
                String titleText = n.getTitle() != null ? n.getTitle()
                        : (n.getMessage() != null ? n.getMessage() : "Notification");
                holder.tvTitle.setText(titleText);
                holder.tvSubtitle.setText(
                        n.getBody() != null ? n.getBody()
                                : (n.getTimestamp() != null ? n.getTimestamp() : ""));
                holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.ivIcon.setImageResource(R.drawable.ic_notifications);
                holder.ivIcon.setImageTintList(
                        android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#757575")));
                break;
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(n);
        });
    }

    @Override
    public int getItemCount() { return notifications != null ? notifications.size() : 0; }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvExtra, tvDetailsLink;
        ImageView ivIcon;
        View viewUnreadIndicator;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle             = itemView.findViewById(R.id.tvNotificationTitle);
            tvSubtitle          = itemView.findViewById(R.id.tvNotificationSubtitle);
            tvExtra             = itemView.findViewById(R.id.tvNotificationExtra);
            tvDetailsLink       = itemView.findViewById(R.id.tvDetailsLink);
            ivIcon              = itemView.findViewById(R.id.ivNotificationIcon);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
        }
    }
}
