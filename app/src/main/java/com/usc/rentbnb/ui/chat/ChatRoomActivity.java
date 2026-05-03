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

    public static final String EXTRA_CHAT_ROOM_ID   = "extra_chat_room_id";
    public static final String EXTRA_LISTING_ID     = "extra_listing_id";
    public static final String EXTRA_LISTING_TITLE  = "extra_listing_title";
    public static final String EXTRA_OWNER_ID       = "extra_owner_id";
    public static final String EXTRA_RENTER_ID      = "extra_renter_id";
    public static final String EXTRA_CURRENT_MODE   = "extra_current_mode";

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
    private String ownerDisplayName = "Owner";

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
        fetchOwnerName();
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
        chatRoomId   = getIntent().getStringExtra(EXTRA_CHAT_ROOM_ID);
        listingId    = getIntent().getStringExtra(EXTRA_LISTING_ID);
        listingTitle = getIntent().getStringExtra(EXTRA_LISTING_TITLE);
        ownerId      = getIntent().getStringExtra(EXTRA_OWNER_ID);
        renterId     = getIntent().getStringExtra(EXTRA_RENTER_ID);
        currentMode  = getIntent().getStringExtra(EXTRA_CURRENT_MODE);
        if (currentMode == null) currentMode = ChatRoom.MODE_AI;
    }

    private void initAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }
        currentUserId = user.getUid();
        isCurrentUserRenter = currentUserId.equals(renterId);
    }

    private void initViews() {
        tvUserName       = findViewById(R.id.tvUserName);
        tvUserStatus     = findViewById(R.id.tvUserStatus);
        etMessage        = findViewById(R.id.etMessage);
        fabToggleMode    = findViewById(R.id.fabToggleChatMode);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        btnBack          = findViewById(R.id.btnBack);
        btnSend          = findViewById(R.id.btnSend);

        chatAdapter = new ChatAdapter(currentUserId);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
        fabToggleMode.setVisibility(isCurrentUserRenter ? View.VISIBLE : View.GONE);
        updateUiForCurrentMode();
    }

    private void setupWindowInsets() {
        ConstraintLayout headerBar = findViewById(R.id.headerBar);
        ViewCompat.setOnApplyWindowInsetsListener(headerBar, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), top + 16, v.getPaddingRight(), v.getPaddingBottom());
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
        recyclerViewChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 0 && fabToggleMode.isExtended()) fabToggleMode.shrink();
                else if (dy < 0 && !fabToggleMode.isExtended()) fabToggleMode.extend();
            }
        });
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

    private void fetchInquilinoReply(@androidx.annotation.NonNull String renterQuestion) {
        if (listing == null) return;

        InquilinoReplyRequest request = new InquilinoReplyRequest(
                chatRoomId, renterId, listing.getProductName(),
                listing.getCategory(), listing.getIsland(),
                String.valueOf(listing.getPrice()), listing.getDescription(),
                listing.getOwnerFaq(),
                conversationHistory,
                renterQuestion
        );

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> {
                    String token = "Bearer " + result.getToken();

                    ApiClient.getApiService().generateInquilinoReply(token, request).enqueue(new Callback<InquilinoResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<InquilinoResponse> call, @NonNull Response<InquilinoResponse> response) {
                            if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                                String realError = response.body() != null ? response.body().getError() : "HTTP Error: " + response.code();
                                Toast.makeText(ChatRoomActivity.this, "Backend Error: " + realError, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<InquilinoResponse> call, @NonNull Throwable t) {
                            Toast.makeText(ChatRoomActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatRoomActivity.this, "Failed to get auth token", Toast.LENGTH_SHORT).show();
                });
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

    private void startListeningToMessages() {
        messagesListener = chatRepository.listenToMessages(chatRoomId, new ChatRepository.MessagesCallback() {
            @Override
            public void onUpdate(List<Message> messages) {
                chatAdapter.setMessages(messages);
                scrollToBottom();
                rebuildConversationHistory(messages);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ChatRoomActivity.this, "Could not load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rebuildConversationHistory(@androidx.annotation.NonNull List<Message> messages) {
        conversationHistory.clear();
        for (Message msg : messages) {
            String prefix = msg.getSenderType().equals(Message.TYPE_AI) ? "Inquilino: " :
                    (msg.getSenderType().equals(Message.TYPE_OWNER) ? "Owner: " : "Renter: ");
            conversationHistory.add(prefix + msg.getText());
        }
    }

    private void fetchListingData() {
        if (listingId == null) return;
        FirebaseFirestore.getInstance().collection("listings").document(listingId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                listing = doc.toObject(Listing.class);
                if (listing != null) listing.setId(doc.getId());
            }
        });
    }

    private void fetchOwnerName() {
        if (ownerId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(ownerId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;
            String name = doc.getString("displayName");
            if (name != null) {
                ownerDisplayName = name;
                chatAdapter.setOwnerName(ownerDisplayName);
                if (ChatRoom.MODE_OWNER.equals(currentMode)) {
                    tvUserName.setText(ownerDisplayName);
                }
            }
        });
    }

    private void markMessagesAsRead() {
        if (chatRoomId != null && currentUserId != null) {
            chatRepository.markAsRead(chatRoomId, currentUserId);
        }
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
                fabToggleMode.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.teal_primary)));
            }
        } else {
            tvUserName.setText(ownerDisplayName);
            tvUserStatus.setText(isCurrentUserRenter ? "Connected to Owner" : "You are the Owner");
            tvUserStatus.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
            etMessage.setHint(isCurrentUserRenter ? "Message " + ownerDisplayName + "..." : "Reply to renter...");
            if (isCurrentUserRenter) {
                fabToggleMode.setText("Switch to Inquilino");
                fabToggleMode.setIconResource(android.R.drawable.ic_menu_compass);
                fabToggleMode.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
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
}