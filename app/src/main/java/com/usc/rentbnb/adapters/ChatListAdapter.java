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

/**
 * RecyclerView Adapter for the Chat List screen (ChatFragment tabs).
 *
 * Each row shows:
 *   - Other participant's profile icon (loaded via Glide)
 *   - Other participant's name
 *   - Listing title being discussed
 *   - Last message snippet
 *   - Red dot indicator if unread count > 0
 *
 * Read rooms are shown at lower alpha (0.5f); unread at full alpha (1.0f).
 *
 * Layout file: item_chat_room.xml
 *
 * Dependencies needed in build.gradle:
 *   implementation 'com.github.bumptech.glide:glide:4.16.0'
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder> {

    // ─── State ────────────────────────────────────────────────────────────────
    private final List<ChatRoom> chatRooms;
    private final String currentUserId;
    private OnChatRoomClickListener clickListener;

    /**
     * Map of userId → display name, used to show the other participant's name.
     * Populate this from Firestore user lookups in the Fragment.
     */
    private final java.util.Map<String, String> userNames;

    /**
     * Map of userId → photoUrl, used to load profile pictures.
     */
    private final java.util.Map<String, String> userPhotos;

    // ─── Click Interface ──────────────────────────────────────────────────────

    public interface OnChatRoomClickListener {
        void onChatRoomClicked(@NonNull ChatRoom chatRoom);
    }

    // ─── Constructor ──────────────────────────────────────────────────────────

    public ChatListAdapter(@NonNull String currentUserId) {
        this.currentUserId = currentUserId;
        this.chatRooms     = new ArrayList<>();
        this.userNames     = new java.util.HashMap<>();
        this.userPhotos    = new java.util.HashMap<>();
    }

    // ─── Adapter Overrides ────────────────────────────────────────────────────

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

    // ─── Public Data Methods ──────────────────────────────────────────────────

    /** Replaces the full list. Called when Firestore snapshot fires. */
    public void setChatRooms(@NonNull List<ChatRoom> rooms) {
        chatRooms.clear();
        chatRooms.addAll(rooms);
        notifyDataSetChanged();
    }

    /** Updates display name cache for a user. Triggers refresh. */
    public void putUserName(@NonNull String userId, @NonNull String name) {
        userNames.put(userId, name);
        notifyDataSetChanged();
    }

    /** Updates photo URL cache for a user. Triggers refresh. */
    public void putUserPhoto(@NonNull String userId, @NonNull String photoUrl) {
        userPhotos.put(userId, photoUrl);
        notifyDataSetChanged();
    }

    public void setOnChatRoomClickListener(OnChatRoomClickListener listener) {
        this.clickListener = listener;
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    class ChatRoomViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivProfilePic;
        private final TextView tvParticipantName;
        private final TextView tvListingTitle;
        private final TextView tvLastMessage;
        private final View     vUnreadDot;

        ChatRoomViewHolder(@NonNull View v) {
            super(v);
            ivProfilePic      = v.findViewById(R.id.ivProfilePic);
            tvParticipantName = v.findViewById(R.id.tvParticipantName);
            tvListingTitle    = v.findViewById(R.id.tvListingTitle);
            tvLastMessage     = v.findViewById(R.id.tvLastMessage);
            vUnreadDot        = v.findViewById(R.id.vUnreadDot);
        }

        void bind(@NonNull ChatRoom room) {
            // Determine the OTHER participant's UID (not the current user)
            String otherUserId = currentUserId.equals(room.getRenterId())
                    ? room.getOwnerId()
                    : room.getRenterId();

            // ── Name ──────────────────────────────────────────────────────────
            String name = userNames.getOrDefault(otherUserId, "Loading...");
            tvParticipantName.setText(name);

            // ── Listing Title ─────────────────────────────────────────────────
            tvListingTitle.setText(room.getListingTitle());

            // ── Last Message ──────────────────────────────────────────────────
            String lastMsg = room.getLastMessage();
            tvLastMessage.setText(lastMsg != null ? lastMsg : "");

            // ── Unread Dot ────────────────────────────────────────────────────
            int unread = room.getUnreadCountForUser(currentUserId);
            boolean hasUnread = unread > 0;
            vUnreadDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);

            // ── Alpha: full for unread, dimmed for read ────────────────────────
            itemView.setAlpha(hasUnread ? 1.0f : 0.55f);

            // ── Profile Picture ───────────────────────────────────────────────
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

            // ── Click ─────────────────────────────────────────────────────────
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onChatRoomClicked(room);
            });
        }
    }
}