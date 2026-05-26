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

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MESSAGE_SENT     = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_DATE_HEADER      = 3;
    private static final int VIEW_TYPE_TYPING           = 4;

    private final List<Message> messageList = new ArrayList<>();
    private final List<Object> displayList = new ArrayList<>();
    private final String currentUserId;
    private String ownerName = "Owner";
    
    private boolean isTyping = false;
    private String typingName = "";

    public ChatAdapter(@NonNull String currentUserId) {
        this.currentUserId = currentUserId;
    }

    private void rebuildDisplayList() {
        displayList.clear();
        String lastDate = "";

        for (Message msg : messageList) {
            String msgDate = msg.getFormattedDateOnly();
            if (!msgDate.equals(lastDate)) {
                displayList.add(new DateHeader(msgDate));
                lastDate = msgDate;
            }
            displayList.add(msg);
        }

        if (isTyping) {
            displayList.add(new TypingIndicator(typingName));
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = displayList.get(position);
        if (item instanceof DateHeader) return VIEW_TYPE_DATE_HEADER;
        if (item instanceof TypingIndicator) return VIEW_TYPE_TYPING;
        
        Message msg = (Message) item;
        if (msg.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_MESSAGE_SENT:
                return new SentViewHolder(inflater.inflate(R.layout.item_chat_sent, parent, false));
            case VIEW_TYPE_MESSAGE_RECEIVED:
                return new ReceivedViewHolder(inflater.inflate(R.layout.item_chat_received, parent, false));
            case VIEW_TYPE_DATE_HEADER:
                return new DateHeaderViewHolder(inflater.inflate(R.layout.item_chat_date_header, parent, false));
            case VIEW_TYPE_TYPING:
                return new TypingViewHolder(inflater.inflate(R.layout.item_chat_typing, parent, false));
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = displayList.get(position);

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).bind((Message) item);
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).bind((Message) item, ownerName);
        } else if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).bind((DateHeader) item);
        } else if (holder instanceof TypingViewHolder) {
            ((TypingViewHolder) holder).bind((TypingIndicator) item);
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public void setMessages(@NonNull List<Message> messages) {
        // Calculate diff for smoother updates and to prevent "jumping"
        final List<Object> oldDisplayList = new ArrayList<>(displayList);
        
        messageList.clear();
        messageList.addAll(messages);
        rebuildDisplayList();

        androidx.recyclerview.widget.DiffUtil.calculateDiff(new androidx.recyclerview.widget.DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return oldDisplayList.size(); }
            @Override
            public int getNewListSize() { return displayList.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                Object oldItem = oldDisplayList.get(oldPos);
                Object newItem = displayList.get(newPos);
                if (oldItem instanceof Message && newItem instanceof Message) {
                    return ((Message) oldItem).getId().equals(((Message) newItem).getId());
                }
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                return oldDisplayList.get(oldPos).equals(displayList.get(newPos));
            }
        }).dispatchUpdatesTo(this);
    }

    public void setTypingState(boolean typing, String name) {
        this.isTyping = typing;
        this.typingName = name;
        rebuildDisplayList();
        notifyDataSetChanged();
    }

    public void setOwnerName(String name) {
        this.ownerName = name;
        notifyDataSetChanged();
    }

    // ─── ViewHolders ──────────────────────────────────────────────────────────

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;
        SentViewHolder(View v) {
            super(v);
            tvText = v.findViewById(R.id.tvMessageText);
            tvTime = v.findViewById(R.id.tvTimestamp);
        }
        void bind(Message msg) {
            tvText.setText(msg.getText());
            tvTime.setText(msg.getFormattedTime());
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvText, tvTime;
        View bubble;
        ReceivedViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvSenderName);
            tvText = v.findViewById(R.id.tvMessageText);
            tvTime = v.findViewById(R.id.tvTimestamp);
            bubble = v.findViewById(R.id.llBubble);
        }
        void bind(Message msg, String ownerDisplayName) {
            if (msg.isFromAi()) {
                tvName.setText("Inquilino");
                bubble.setBackgroundResource(R.drawable.bg_bubble_ai);
            } else {
                tvName.setText(ownerDisplayName);
                bubble.setBackgroundResource(R.drawable.bg_bubble_owner);
            }
            tvText.setText(msg.getText());
            tvTime.setText(msg.getFormattedTime());
        }
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        DateHeaderViewHolder(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvDateHeader);
        }
        void bind(DateHeader header) {
            tvDate.setText(header.date);
        }
    }

    static class TypingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTyping;
        TypingViewHolder(View v) {
            super(v);
            tvTyping = v.findViewById(R.id.tvTypingIndicator);
        }
        void bind(TypingIndicator indicator) {
            String text = indicator.name + " is typing...";
            tvTyping.setText(text);
        }
    }

    // ─── Helper Objects ───────────────────────────────────────────────────────
    static class DateHeader {
        String date;
        DateHeader(String date) { this.date = date; }
    }

    static class TypingIndicator {
        String name;
        TypingIndicator(String name) { this.name = name; }
    }
}