package com.usc.rentbnb.ui.chat;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ChatAdapter;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.models.InquilinoOpeningRequest;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.Message;
import com.usc.rentbnb.models.InquilinoReplyRequest;
import com.usc.rentbnb.models.InquilinoResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.repositories.ChatRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ROOM_ID = "extra_chat_room_id";
    public static final String EXTRA_LISTING_ID= "extra_listing_id";
    public static final String EXTRA_LISTING_TITLE = "extra_listing_title";
    public static final String EXTRA_OWNER_ID = "extra_owner_id";
    public static final String EXTRA_RENTER_ID= "extra_renter_id";
    public static final String EXTRA_CURRENT_MODE = "extra_current_mode";

    private TextView tvUserName, tvUserStatus;
    private EditText etMessage;
    private ExtendedFloatingActionButton fabToggleMode;
    private RecyclerView recyclerViewChat;
    private ImageButton btnBack, btnSend;

    private ChatAdapter chatAdapter;
    private ChatRepository chatRepository;
    private ListenerRegistration messagesListener;

    private final List<String> conversationHistory = new ArrayList<>();
    private Listing listing;

    private String chatRoomId, listingId, listingTitle, ownerId, renterId, currentUserId, currentMode;
    private boolean isCurrentUserRenter;
    private String otherParticipantName = "User";


    private boolean openingMessageSent = false;
    private boolean initialMessagesLoaded = false;
    private boolean isRoomEmpty = false;

    private Timestamp lastMessageTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_room);

        extractIntentExtras();
        initAuth();
        chatRepository = new ChatRepository();
        initViews();
        setupWindowInsets();
        setupListeners();
        fetchListingData();
        fetchOtherParticipantName();
        startListeningToMessages();
        markMessagesAsRead();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
            messagesListener = null;
        }
    }

    private void extractIntentExtras() {
        chatRoomId= getIntent().getStringExtra(EXTRA_CHAT_ROOM_ID);
        listingId= getIntent().getStringExtra(EXTRA_LISTING_ID);
        listingTitle = getIntent().getStringExtra(EXTRA_LISTING_TITLE);
        ownerId = getIntent().getStringExtra(EXTRA_OWNER_ID);
        renterId = getIntent().getStringExtra(EXTRA_RENTER_ID);
        currentMode = getIntent().getStringExtra(EXTRA_CURRENT_MODE);

        if (chatRoomId == null) {
            Toast.makeText(this, "Error: Chat Room ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentMode == null) currentMode = ChatRoom.MODE_AI;
    }

    private void initAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }
        currentUserId = user.getUid();
        isCurrentUserRenter = currentUserId.equals(renterId);
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserStatus = findViewById(R.id.tvUserStatus);
        etMessage = findViewById(R.id.etMessage);
        fabToggleMode = findViewById(R.id.fabToggleChatMode);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(llm);

        chatAdapter = new ChatAdapter(currentUserId);
        recyclerViewChat.setAdapter(chatAdapter);

        fabToggleMode.setVisibility(isCurrentUserRenter ? View.VISIBLE : View.GONE);
        updateUiForCurrentMode();
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    private void setupWindowInsets() {
        // 1. Smooth Keyboard Push (Requires android:windowSoftInputMode="adjustResize" in Manifest)
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) return;
            etMessage.setText("");
            handleSendMessage(text);
        });
        fabToggleMode.setOnClickListener(v -> toggleChatMode());
    }

    private void handleSendMessage(@androidx.annotation.NonNull String text) {
        if (isCurrentUserRenter) {
            conversationHistory.add("Renter: " + text);
            chatRepository.sendMessage(chatRoomId, currentUserId, text, Message.TYPE_USER, ownerId, success -> {
                if (!success) {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ChatRoom.MODE_AI.equals(currentMode) && listing != null) {
                    fetchInquilinoReply(text);
                }
                scrollToBottom();
            });
        } else {
            chatRepository.sendMessage(chatRoomId, currentUserId, text, Message.TYPE_OWNER, renterId, success -> {
                if (!success) Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                scrollToBottom();
            });
        }
    }

    private void fetchInquilinoReply(@NonNull String renterQuestion) {
        if (listing == null) return;

        // Show typing indicator before the API call
        setTypingState(true, "Inquilino is generating");

        InquilinoReplyRequest request = new InquilinoReplyRequest(
                chatRoomId, renterId, ownerId, listing.getId(),
                listing.getProductName(),
                listing.getCategory(), listing.getIsland(),
                String.valueOf(listing.getPrice()), listing.getDescription(),
                listing.getOwnerFaq(),
                conversationHistory,
                renterQuestion
        );

        ApiClient.getApiService().generateInquilinoReply(request).enqueue(new Callback<InquilinoResponse>() {
            @Override
            public void onResponse(@NonNull Call<InquilinoResponse> call,
                                   @NonNull Response<InquilinoResponse> response) {
                // Hide typing indicator — the real message will appear via the Firestore listener
                setTypingState(false, "");

                // The controller writes the message to Firestore directly on both success
                // and the busy fallback path. No action needed on the Android side —
                // the messagesListener will pick it up automatically.
                // Only surface a Toast if there was a genuine network/server crash.
                if (!response.isSuccessful() && response.code() >= 500) {
                    // Backend returned an error it could not handle — already wrote busy msg
                    // Nothing to show; the Firestore listener will surface it.
                }
            }

            @Override
            public void onFailure(@NonNull Call<InquilinoResponse> call, @NonNull Throwable t) {
                setTypingState(false, "");
                // Network error — the busy message was not written since the request never reached the server.
                // Write a local fallback directly to Firestore.
                writeLocalFallbackMessage();
            }
        });
    }

    private void maybeFireOpeningMessage() {
        if (openingMessageSent) return;
        if (!isCurrentUserRenter) return;
        if (!ChatRoom.MODE_AI.equals(currentMode)) return;
        if (listing == null) return;
        if (!initialMessagesLoaded) return;
        if (!isRoomEmpty) return;

        openingMessageSent = true;
        setTypingState(true, "Inquilino is generating");

        InquilinoOpeningRequest request = new InquilinoOpeningRequest(
                chatRoomId, renterId, ownerId, listing.getId(),
                listing.getProductName(),
                listing.getCategory(), listing.getIsland(),
                String.valueOf(listing.getPrice()), listing.getDescription(),
                listing.getOwnerFaq()
        );

        ApiClient.getApiService().generateInquilinoOpening(request).enqueue(new Callback<InquilinoResponse>() {
            @Override
            public void onResponse(@NonNull Call<InquilinoResponse> call,
                                   @NonNull Response<InquilinoResponse> response) {
                setTypingState(false, "");
                // Message was written to Firestore by the controller.
                // Firestore listener will surface it automatically.
            }

            @Override
            public void onFailure(@NonNull Call<InquilinoResponse> call, @NonNull Throwable t) {
                setTypingState(false, "");
                writeLocalFallbackMessage();
            }
        });
    }

    private void startListeningToMessages() {
        messagesListener = chatRepository.listenToMessages(chatRoomId, new ChatRepository.MessagesCallback() {
            @Override
            public void onUpdate(List<Message> messages) {
                chatAdapter.setMessages(messages);

                // Scroll after the adapter has laid out the new items
                recyclerViewChat.post(() -> scrollToBottom());

                rebuildConversationHistory(messages);

                // First snapshot received — check if this is a new room
                if (!initialMessagesLoaded) {
                    initialMessagesLoaded = true;
                    isRoomEmpty = messages.isEmpty();
                    if (isRoomEmpty) {
                        maybeFireOpeningMessage();
                    }
                }

                // Auto-handoff: if AI is active but the human owner replied, switch modes
                if (ChatRoom.MODE_AI.equals(currentMode)) {
                    for (Message msg : messages) {
                        if (Message.TYPE_OWNER.equals(msg.getSenderType())) {
                            currentMode = ChatRoom.MODE_OWNER;
                            chatRepository.switchChatMode(chatRoomId, ChatRoom.MODE_OWNER, s -> {});
                            runOnUiThread(() -> {
                                updateUiForCurrentMode();
                                Toast.makeText(ChatRoomActivity.this,
                                        "Owner has joined the chat", Toast.LENGTH_SHORT).show();
                            });
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ChatRoomActivity.this, "Could not load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rebuildConversationHistory(@NonNull List<Message> messages) {
        conversationHistory.clear();
        for (Message msg : messages) {
            String prefix = Message.TYPE_AI.equals(msg.getSenderType()) ? "Inquilino: "
                    : (Message.TYPE_OWNER.equals(msg.getSenderType()) ? "Owner: " : "Renter: ");
            conversationHistory.add(prefix + msg.getText());
        }
    }

    private void fetchListingData() {
        if (listingId == null) return;
        FirebaseFirestore.getInstance().collection("listings").document(listingId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        listing = doc.toObject(Listing.class);
                        if (listing != null) listing.setId(doc.getId());
                        // Trigger opening message if messages are already confirmed empty
                        maybeFireOpeningMessage();
                    }
                });
    }

    private void fetchOtherParticipantName() {
        String otherUserId = isCurrentUserRenter ? ownerId : renterId;
        if (otherUserId == null) return;
        
        FirebaseFirestore.getInstance().collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String name = doc.getString("displayName");
                    if (name != null) {
                        otherParticipantName = name;
                        chatAdapter.setOtherParticipantName(otherParticipantName);
                        if (ChatRoom.MODE_OWNER.equals(currentMode)) {
                            tvUserName.setText(otherParticipantName);
                        }
                    }
                });
    }

    private void markMessagesAsRead() {
        if (chatRoomId != null && currentUserId != null) {
            chatRepository.markAsRead(chatRoomId, currentUserId);
        }
    }

    private void writeLocalFallbackMessage() {
        String fallbackText = "Inquilino is currently unavailable. Please try again in a moment, or switch to talking directly with the owner.";
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.firestore.FieldValue now = com.google.firebase.firestore.FieldValue.serverTimestamp();

        java.util.Map<String, Object> msg = new java.util.HashMap<>();
        msg.put("chatRoomId",  chatRoomId);
        msg.put("senderId",    "INQUILINO");
        msg.put("text",        fallbackText);
        msg.put("timestamp",   now);
        msg.put("senderType",  "AI");

        db.collection("chatRooms").document(chatRoomId)
                .collection("messages").add(msg);
    }

    private void toggleChatMode() {
        String newMode = ChatRoom.MODE_AI.equals(currentMode) ? ChatRoom.MODE_OWNER : ChatRoom.MODE_AI;
        chatRepository.switchChatMode(chatRoomId, newMode, success -> {
            if (!success) {
                Toast.makeText(this, "Failed to switch mode", Toast.LENGTH_SHORT).show();
                return;
            }
            currentMode = newMode;
            fabToggleMode.shrink(new ExtendedFloatingActionButton.OnChangedCallback() {
                @Override
                public void onShrunken(ExtendedFloatingActionButton fab) {
                    updateUiForCurrentMode();
                    fab.extend();
                }
            });
        });
    }

    private void updateUiForCurrentMode() {
        if (ChatRoom.MODE_AI.equals(currentMode)) {
            tvUserName.setText("Inquilino");
            tvUserStatus.setText("AI Assistant · Powered by Gemini");
            tvUserStatus.setTextColor(ContextCompat.getColor(this, R.color.teal_primary));
            etMessage.setHint("Ask Inquilino about this listing...");
            if (isCurrentUserRenter) {
                fabToggleMode.setText("Talk to Owner");
                fabToggleMode.setIconResource(R.drawable.ic_profile);
                fabToggleMode.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal_primary)));
            }
        } else {
            tvUserName.setText(otherParticipantName);
            tvUserStatus.setText(isCurrentUserRenter ? "Connected to Owner" : "Connected to Renter");
            tvUserStatus.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
            etMessage.setHint(isCurrentUserRenter
                    ? "Message " + otherParticipantName + "..."
                    : "Reply to renter...");
            if (isCurrentUserRenter) {
                fabToggleMode.setText("Switch to Inquilino");
                fabToggleMode.setIconResource(android.R.drawable.ic_menu_compass);
                fabToggleMode.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
            }
        }
        if (listingTitle != null) {
            TextView tvSubtitle = findViewById(R.id.tvListingSubtitle);
            if (tvSubtitle != null) tvSubtitle.setText(listingTitle);
        }
    }

    private void scrollToBottom() {
        int count = chatAdapter.getItemCount();
        if (count > 0) recyclerViewChat.smoothScrollToPosition(count - 1);
    }

    public void setTypingState(boolean isTyping, String name) {
        runOnUiThread(() -> {
            if (chatAdapter != null) {
                String displayName = (name == null || name.isEmpty()) ? "Inquilino is generating" : name;
                chatAdapter.setTypingState(isTyping, displayName);
                if (isTyping) recyclerViewChat.post(this::scrollToBottom);
            }
        });
    }

    public String getFormattedTime() {
        if (lastMessageTimestamp == null) return "";
        java.util.Date date = lastMessageTimestamp.toDate();
        java.util.Calendar msgCal  = java.util.Calendar.getInstance();
        java.util.Calendar today   = java.util.Calendar.getInstance();
        java.util.Calendar yesterday = java.util.Calendar.getInstance();
        msgCal.setTime(date);
        yesterday.add(java.util.Calendar.DAY_OF_YEAR, -1);

        if (msgCal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR)
                && msgCal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)) {
            return new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(date);
        } else if (msgCal.get(java.util.Calendar.YEAR) == yesterday.get(java.util.Calendar.YEAR)
                && msgCal.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        } else {
            return new java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(date);
        }
    }
}