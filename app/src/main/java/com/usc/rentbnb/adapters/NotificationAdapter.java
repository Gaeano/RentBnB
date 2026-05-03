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
        Notification notification = notifications.get(position);

        if ("rent".equals(notification.getType())) {
            holder.tvTitle.setText("Someone rented your product!");
            holder.tvSubtitle.setText(notification.getProductName());
            holder.tvExtra.setText(notification.getProductCategory());
            holder.tvExtra.setVisibility(View.VISIBLE);
            holder.tvDetailsLink.setVisibility(View.VISIBLE);

            holder.ivIcon.setImageResource(R.drawable.yamaha_nmax);
            holder.ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else if ("listing".equals(notification.getType())) {
            holder.tvTitle.setText("New item near you!");
            holder.tvSubtitle.setText(notification.getProductName());
            holder.tvExtra.setText(notification.getProductCategory());
            holder.tvExtra.setVisibility(View.VISIBLE);
            holder.tvDetailsLink.setVisibility(View.VISIBLE);

            holder.ivIcon.setImageResource(R.drawable.ic_discover);
            holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else if ("chat".equals(notification.getType())) {
            holder.tvTitle.setText("New message");
            holder.tvSubtitle.setText(notification.getProductName());
            holder.tvExtra.setText(notification.getMessage());
            holder.tvExtra.setVisibility(View.VISIBLE);
            holder.tvDetailsLink.setVisibility(View.GONE);

            if (notification.getProductImage() != null && !notification.getProductImage().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(notification.getProductImage())
                        .placeholder(R.drawable.ic_user_placeholder)
                        .circleCrop()
                        .into(holder.ivIcon);
            } else {
                holder.ivIcon.setImageResource(R.drawable.ic_user_placeholder);
            }
            holder.ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            holder.tvTitle.setText(notification.getMessage());
            holder.tvSubtitle.setText(notification.getTimestamp());
            holder.tvExtra.setVisibility(View.GONE);
            holder.tvDetailsLink.setVisibility(View.GONE);
            holder.ivIcon.setImageResource(R.drawable.ic_notifications);
        }

        holder.viewUnreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvExtra, tvDetailsLink;
        ImageView ivIcon;
        View viewUnreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvSubtitle = itemView.findViewById(R.id.tvNotificationSubtitle);
            tvExtra = itemView.findViewById(R.id.tvNotificationExtra);
            tvDetailsLink = itemView.findViewById(R.id.tvDetailsLink);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
        }
    }
}