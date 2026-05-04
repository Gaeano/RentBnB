package com.usc.rentbnb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for the Chat Room screen.
 *
 * Handles 3 distinct message bubble types:
 *   VIEW_TYPE_USER  → item_chat_user.xml  (right-aligned, teal bubble)
 *   VIEW_TYPE_OWNER → item_chat_owner.xml (left-aligned, neutral bubble)
 *   VIEW_TYPE_AI    → item_chat_ai.xml    (left-aligned, Inquilino branded bubble)
 *
 * Timestamps are read from {@link Message#getFormattedTime()} which safely
 * handles null Firestore Timestamps.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ─── View Type Constants ──────────────────────────────────────────────────
    private static final int VIEW_TYPE_USER  = 1;
    private static final int VIEW_TYPE_OWNER = 2;
    private static final int VIEW_TYPE_AI    = 3;

    // ─── State ────────────────────────────────────────────────────────────────
    private final List<Message> messageList;
    private final String currentUserId;

    /** Display name of the owner shown in owner bubbles. Set via {@link #setOwnerName}. */
    private String ownerName = "Owner";

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * @param currentUserId Firebase Auth UID of the logged-in user.
     */
    public ChatAdapter(@NonNull String currentUserId) {
        this.currentUserId = currentUserId;
        this.messageList   = new ArrayList<>();
    }

    // ─── Adapter Overrides ────────────────────────────────────────────────────

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        switch (msg.getSenderType()) {
            case Message.TYPE_AI:    return VIEW_TYPE_AI;
            case Message.TYPE_OWNER: return VIEW_TYPE_OWNER;
            default:                 return VIEW_TYPE_USER;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_AI:
                return new AiViewHolder(inflater.inflate(R.layout.item_chat_ai, parent, false));
            case VIEW_TYPE_OWNER:
                return new OwnerViewHolder(inflater.inflate(R.layout.item_chat_owner, parent, false));
            default:
                return new UserViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messageList.get(position);
        if (holder instanceof UserViewHolder)  ((UserViewHolder) holder).bind(msg);
        else if (holder instanceof OwnerViewHolder) ((OwnerViewHolder) holder).bind(msg, ownerName);
        else if (holder instanceof AiViewHolder)    ((AiViewHolder) holder).bind(msg);
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    // ─── Public Data Methods ──────────────────────────────────────────────────

    /** Replaces entire list. Use for initial load. */
    public void setMessages(@NonNull List<Message> messages) {
        messageList.clear();
        messageList.addAll(messages);
        notifyDataSetChanged();
    }

    /** Appends a single message. Use for real-time updates. */
    public void addMessage(@NonNull Message message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    /** Sets the owner's display name shown in owner bubbles. */
    public void setOwnerName(@NonNull String name) {
        this.ownerName = name;
        notifyDataSetChanged();
    }

    /** Clears all messages. */
    public void clearMessages() {
        int size = messageList.size();
        messageList.clear();
        notifyItemRangeRemoved(0, size);
    }

    // ─── ViewHolder: Renter (right-aligned teal bubble) ───────────────────────
    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessageText;
        private final TextView tvTimestamp;

        UserViewHolder(@NonNull View v) {
            super(v);
            tvMessageText = v.findViewById(R.id.tvMessageText);
            tvTimestamp   = v.findViewById(R.id.tvTimestamp);
        }

        void bind(@NonNull Message msg) {
            tvMessageText.setText(msg.getText());
            // getFormattedTime() is null-safe — returns "" if timestamp is null
            tvTimestamp.setText(msg.getFormattedTime());
        }
    }

    // ─── ViewHolder: Owner (left-aligned neutral bubble) ──────────────────────
    static class OwnerViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSenderName;
        private final TextView tvMessageText;
        private final TextView tvTimestamp;

        OwnerViewHolder(@NonNull View v) {
            super(v);
            tvSenderName  = v.findViewById(R.id.tvSenderName);
            tvMessageText = v.findViewById(R.id.tvMessageText);
            tvTimestamp   = v.findViewById(R.id.tvTimestamp);
        }

        void bind(@NonNull Message msg, @NonNull String ownerName) {
            tvSenderName.setText(ownerName);
            tvMessageText.setText(msg.getText());
            tvTimestamp.setText(msg.getFormattedTime());
        }
    }

    // ─── ViewHolder: Inquilino AI (left-aligned branded bubble) ──────────────
    static class AiViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAiLabel;
        private final TextView tvMessageText;
        private final TextView tvTimestamp;

        AiViewHolder(@NonNull View v) {
            super(v);
            tvAiLabel     = v.findViewById(R.id.tvAiLabel);
            tvMessageText = v.findViewById(R.id.tvMessageText);
            tvTimestamp   = v.findViewById(R.id.tvTimestamp);
        }

        void bind(@NonNull Message msg) {
            tvAiLabel.setText("Inquilino");
            tvMessageText.setText(msg.getText());
            tvTimestamp.setText(msg.getFormattedTime());
        }
    }
}