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
import com.usc.rentbnb.models.ChatRoom;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder> {

    private final List<ChatRoom> chatRooms;
    private final List<ChatRoom> allChatRooms;
    private final String currentUserId;
    private OnChatRoomClickListener clickListener;

    private final java.util.Map<String, String> userNames;

    private final java.util.Map<String, String> userPhotos;



    public interface OnChatRoomClickListener {
        void onChatRoomClicked(@NonNull ChatRoom chatRoom);
    }


    public ChatListAdapter(@NonNull String currentUserId) {
        this.currentUserId = currentUserId;
        this.chatRooms = new ArrayList<>();
        this.allChatRooms = new ArrayList<>();
        this.userNames = new java.util.HashMap<>();
        this.userPhotos = new java.util.HashMap<>();
    }


    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom room = chatRooms.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() { return chatRooms.size(); }

    public void setChatRooms(@NonNull List<ChatRoom> rooms) {
        allChatRooms.clear();
        allChatRooms.addAll(rooms);
        chatRooms.clear();
        chatRooms.addAll(rooms);
        notifyDataSetChanged();
    }

    public void putUserName(@NonNull String userId, @NonNull String name) {
        userNames.put(userId, name);
        notifyDataSetChanged();
    }

    public void putUserPhoto(@NonNull String userId, @NonNull String photoUrl) {
        userPhotos.put(userId, photoUrl);
        notifyDataSetChanged();
    }

    public void setOnChatRoomClickListener(OnChatRoomClickListener listener) {
        this.clickListener = listener;
    }

    public void filter(String text) {
        chatRooms.clear();
        if (text.isEmpty()) {
            // If search is empty, show everything
            chatRooms.addAll(allChatRooms);
        } else {
            String query = text.toLowerCase();
            for (ChatRoom room : allChatRooms) {
                String otherUserId = currentUserId.equals(room.getRenterId()) ? room.getOwnerId() : room.getRenterId();
                String name = userNames.getOrDefault(otherUserId, "").toLowerCase();
                String title = room.getListingTitle() != null ? room.getListingTitle().toLowerCase() : "";
                String lastMsg = room.getLastMessage() != null ? room.getLastMessage().toLowerCase() : "";

                // If the search matches the name, the listing, or the last message, keep it!
                if (name.contains(query) || title.contains(query) || lastMsg.contains(query)) {
                    chatRooms.add(room);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivProfilePic;
        private final TextView tvParticipantName;
        private final TextView tvListingTitle;
        private final TextView tvLastMessage;
        private final TextView tvTimestamp; // <-- NEW
        private final View vUnreadDot;

        ChatRoomViewHolder(@NonNull View v) {
            super(v);
            ivProfilePic = v.findViewById(R.id.ivProfilePic);
            tvParticipantName = v.findViewById(R.id.tvParticipantName);
            tvListingTitle = v.findViewById(R.id.tvListingTitle);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            tvTimestamp = v.findViewById(R.id.tvTimestamp); // <-- NEW
            vUnreadDot = v.findViewById(R.id.vUnreadDot);
        }

        void bind(@NonNull ChatRoom room) {
            String otherUserId = currentUserId.equals(room.getRenterId())
                    ? room.getOwnerId()
                    : room.getRenterId();

            String name = userNames.getOrDefault(otherUserId, "Loading...");
            tvParticipantName.setText(name);

            tvListingTitle.setText(room.getListingTitle());

            String lastMsg = room.getLastMessage();
            tvLastMessage.setText(lastMsg != null ? lastMsg : "");

            // --- TIMESTAMP LOGIC ---
            String timeText = room.getFormattedTime(); // Pull from the ChatRoom model!
            tvTimestamp.setText(timeText);
            tvTimestamp.setVisibility(timeText.isEmpty() ? View.GONE : View.VISIBLE);

            int unread = room.getUnreadCountForUser(currentUserId);
            boolean hasUnread = unread > 0;
            vUnreadDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);

            itemView.setAlpha(hasUnread ? 1.0f : 0.85f);

            String photoUrl = userPhotos.get(otherUserId);
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(ivProfilePic);
            } else {
                ivProfilePic.setImageResource(R.drawable.ic_profile);
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onChatRoomClicked(room);
            });
        }
    }
}