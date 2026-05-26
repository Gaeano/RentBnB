package com.usc.rentbnb.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ChatListAdapter;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.repositories.ChatRepository;

import java.util.List;

public class ChatFragment extends Fragment {

    private TextView tvInboxHeader;
    private EditText etSearchChats;
    private RecyclerView recyclerViewInbox;
    private LinearLayout layoutEmptyState;

    private ChatListAdapter adapter;
    private ChatRepository chatRepository;
    private ListenerRegistration chatRoomsListener;
    private String currentUserId;

    public ChatFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Auth check
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        currentUserId = user.getUid();
        chatRepository = new ChatRepository();

        // 2. Bind layout views
        tvInboxHeader = view.findViewById(R.id.tvInboxHeader);
        etSearchChats = view.findViewById(R.id.etSearchChats);
        recyclerViewInbox = view.findViewById(R.id.recyclerViewInbox);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        applyWindowInsets();

        // 3. Setup RecyclerView
        adapter = new ChatListAdapter(currentUserId);
        adapter.setOnChatRoomClickListener(this::openChatRoom);
        recyclerViewInbox.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewInbox.setAdapter(adapter);

        // 4. Fetch data and control Lottie visibility
        startListeningToChatRooms();

        // 5. Handle Text Changes for Search
        etSearchChats.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (adapter != null) {
                    adapter.filter(s.toString().trim());
                    updateEmptyStateVisibility(adapter.getItemCount());
                }
            }
        });
    }

    private void startListeningToChatRooms() {
        chatRoomsListener = chatRepository.listenToChatRoomsForUser(
                currentUserId,
                new ChatRepository.ChatRoomsListCallback() {
                    @Override
                    public void onUpdate(List<ChatRoom> allRooms) {
                        // SAFEGUARD: Check if fragment is alive
                        if (!isAdded() || getActivity() == null) return;

                        getActivity().runOnUiThread(() -> {
                            if (!isAdded()) return;

                            // 1. Strictly filter for ONLY Renter chats (since this is the Renter/Standard Chat tab)
                            java.util.List<ChatRoom> filteredRooms = new java.util.ArrayList<>();
                            for (ChatRoom room : allRooms) {
                                if (currentUserId.equals(room.getRenterId())) {
                                    filteredRooms.add(room);
                                }
                            }

                            updateEmptyStateVisibility(filteredRooms.size());
                            adapter.setChatRooms(filteredRooms);
                            resolveParticipantProfiles(filteredRooms);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (isAdded()) updateEmptyStateVisibility(0);
                            });
                        }
                    }
                }
        );
    }

    private void resolveParticipantProfiles(@NonNull List<ChatRoom> rooms) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (ChatRoom room : rooms) {
            String otherUserId = currentUserId.equals(room.getRenterId())
                    ? room.getOwnerId()
                    : room.getRenterId();

            db.collection("users").document(otherUserId).get()
                    .addOnSuccessListener(doc -> {
                        if (!isAdded() || !doc.exists()) return;

                        boolean needsRedraw = false;
                        String name  = doc.getString("displayName");
                        String photo = doc.getString("photoUrl");

                        if (name != null) {
                            adapter.putUserName(otherUserId, name);
                            needsRedraw = true;
                        }
                        if (photo != null) {
                            adapter.putUserPhoto(otherUserId, photo);
                            needsRedraw = true;
                        }

                        if (needsRedraw && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (isAdded()) adapter.notifyDataSetChanged();
                            });
                        }
                    });
        }
    }

    private void updateEmptyStateVisibility(int itemCount) {
        if (itemCount == 0) {
            recyclerViewInbox.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewInbox.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void openChatRoom(@NonNull ChatRoom room) {
        chatRepository.markAsRead(room.getId(), currentUserId);

        Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ROOM_ID, room.getId());
        intent.putExtra(ChatRoomActivity.EXTRA_LISTING_ID, room.getListingId());
        intent.putExtra(ChatRoomActivity.EXTRA_LISTING_TITLE, room.getListingTitle());
        intent.putExtra(ChatRoomActivity.EXTRA_OWNER_ID, room.getOwnerId());
        intent.putExtra(ChatRoomActivity.EXTRA_RENTER_ID, room.getRenterId());

        // Null-safe fallback for chat mode
        String currentMode = room.getMode() != null ? room.getMode() : "chat";
        intent.putExtra(ChatRoomActivity.EXTRA_CURRENT_MODE, currentMode);

        startActivity(intent);
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(tvInboxHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + 8,
                    v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatRoomsListener != null) {
            chatRoomsListener.remove();
            chatRoomsListener = null;
        }
    }
}