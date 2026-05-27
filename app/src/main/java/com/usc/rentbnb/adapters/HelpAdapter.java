package com.usc.rentbnb.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.HelpMessage;

import java.util.ArrayList;
import java.util.List;

public class HelpAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER   = 1;
    private static final int VIEW_TYPE_AI     = 2;
    private static final int VIEW_TYPE_TYPING = 3;

    private final List<Object> items = new ArrayList<>();
    private boolean isTyping = false;

    // Sentinel object for the typing row
    private static final Object TYPING_SENTINEL = new Object();

    // ---------------------------------------------------------------------------
    // Data
    // ---------------------------------------------------------------------------

    public void addMessage(HelpMessage message) {
        // Remove any typing indicator before inserting the new message
        if (isTyping) {
            isTyping = false;
            items.remove(TYPING_SENTINEL);
        }
        items.add(message);
        notifyDataSetChanged();
    }

    public void setTyping(boolean typing) {
        if (typing == isTyping) return;
        isTyping = typing;
        if (typing) {
            items.add(TYPING_SENTINEL);
        } else {
            items.remove(TYPING_SENTINEL);
        }
        notifyDataSetChanged();
    }

    // ---------------------------------------------------------------------------
    // Adapter overrides
    // ---------------------------------------------------------------------------

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item == TYPING_SENTINEL) return VIEW_TYPE_TYPING;
        HelpMessage msg = (HelpMessage) item;
        return msg.isFromUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_USER:
                return new UserVH(inf.inflate(R.layout.item_chat_user, parent, false));
            case VIEW_TYPE_AI:
                return new AiVH(inf.inflate(R.layout.item_chat_ai, parent, false));
            case VIEW_TYPE_TYPING:
                return new TypingVH(inf.inflate(R.layout.item_chat_typing, parent, false));
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (item == TYPING_SENTINEL) {
            ((TypingVH) holder).bind();
            return;
        }
        HelpMessage msg = (HelpMessage) item;
        if (holder instanceof UserVH) ((UserVH) holder).bind(msg);
        else if (holder instanceof AiVH) ((AiVH) holder).bind(msg);
    }

    @Override
    public int getItemCount() { return items.size(); }

    // ---------------------------------------------------------------------------
    // ViewHolders — uses the same item layouts as ChatAdapter
    // item_chat_user:  tvMessageText, tvTimestamp  (sent bubble — right aligned)
    // item_chat_ai:    tvMessageText, tvTimestamp, tvAiLabel  (AI bubble — left)
    // item_chat_typing: tvTypingIndicator
    // ---------------------------------------------------------------------------

    static class UserVH extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;
        UserVH(View v) {
            super(v);
            tvText = v.findViewById(R.id.tvMessageText);
            tvTime = v.findViewById(R.id.tvTimestamp);
        }
        void bind(HelpMessage msg) {
            tvText.setText(msg.getText());
            if (tvTime != null) tvTime.setText(msg.getTimestamp());
        }
    }

    static class AiVH extends RecyclerView.ViewHolder {
        TextView tvText, tvTime, tvLabel;
        AiVH(View v) {
            super(v);
            tvText  = v.findViewById(R.id.tvMessageText);
            tvTime  = v.findViewById(R.id.tvTimestamp);
            tvLabel = v.findViewById(R.id.tvAiLabel);
        }
        void bind(HelpMessage msg) {
            tvText.setText(msg.getText());
            if (tvTime  != null) tvTime.setText(msg.getTimestamp());
            if (tvLabel != null) tvLabel.setText("Inquilino");
        }
    }

    static class TypingVH extends RecyclerView.ViewHolder {
        TextView tvTyping;
        TypingVH(View v) {
            super(v);
            tvTyping = v.findViewById(R.id.tvTypingIndicator);
        }
        void bind() {
            if (tvTyping != null) tvTyping.setText("Inquilino is typing...");
        }
    }
}