package com.usc.rentbnb.ui.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ChatListAdapter;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.repositories.ChatRepository;
import com.usc.rentbnb.ui.chat.ChatRoomActivity;

import java.util.ArrayList;
import java.util.List;

public class OwnerInboxFragment extends Fragment {

    private EditText etSearchInbox;
    private RecyclerView rvInbox;
    private View emptyStateInbox;
    private TextView tvNewMessages;
    private ChipGroup chipGroupInbox;

    private ChatListAdapter chatAdapter;
    private ChatRepository chatRepository;
    private ListenerRegistration chatRoomsListener;
    private String currentUserId;

    private final List<ChatRoom> allRooms = new ArrayList<>();
    private String currentFilter = "All";
    private String currentQuery  = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        currentUserId = user.getUid();

        chatRepository = new ChatRepository();

        etSearchInbox   = view.findViewById(R.id.search_bar);
        rvInbox         = view.findViewById(R.id.rvInbox);
        emptyStateInbox = view.findViewById(R.id.emptyStateInbox);
        tvNewMessages   = view.findViewById(R.id.tvNewMessages);

        setupRecyclerView();
        setupSearch();
        setupChipFilters();
        startListeningToChats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatRoomsListener != null) {
            chatRoomsListener.remove();
            chatRoomsListener = null;
        }
    }

    // ---------------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------------

    private void setupRecyclerView() {
        chatAdapter = new ChatListAdapter(currentUserId);
        chatAdapter.setOnChatRoomClickListener(this::openChatRoom);
        rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInbox.setAdapter(chatAdapter);
    }

    private void setupSearch() {
        if (etSearchInbox == null) return;
        etSearchInbox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim().toLowerCase();
                applyFilterAndSearch();
            }
        });
    }

    private void setupChipFilters() {
        if (chipGroupInbox == null) return;
        chipGroupInbox.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = "All";
            } else {
                View chip = group.findViewById(checkedIds.get(0));
                if (chip instanceof Chip) currentFilter = ((Chip) chip).getText().toString();
            }
            applyFilterAndSearch();
        });
    }

    // ---------------------------------------------------------------------------
    // Real-time listener — mirrors OwnerChatFragment exactly
    // ---------------------------------------------------------------------------

    private void startListeningToChats() {
        chatRoomsListener = chatRepository.listenToChatRoomsForUser(
                currentUserId,
                new ChatRepository.ChatRoomsListCallback() {
                    @Override
                    public void onUpdate(List<ChatRoom> rooms) {
                        if (!isAdded()) return;

                        allRooms.clear();
                        for (ChatRoom room : rooms) {
                            // Strictly filter for chats where the current user is the OWNER
                            if (currentUserId.equals(room.getOwnerId())) {
                                allRooms.add(room);
                            }
                        }

                        requireActivity().runOnUiThread(() -> {
                            applyFilterAndSearch();
                            updateUnreadBadge();
                            resolveParticipantProfiles(allRooms);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> checkAndShowInboxEmptyState(true));
                    }
                }
        );
    }

    private void resolveParticipantProfiles(List<ChatRoom> rooms) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (ChatRoom room : rooms) {
            String renterId = room.getRenterId();
            if (renterId == null || renterId.isEmpty()) continue;

            db.collection("users").document(renterId).get()
                    .addOnSuccessListener(doc -> {
                        if (!isAdded() || doc == null || !doc.exists()) return;
                        String name  = doc.getString("displayName");
                        String photo = doc.getString("photoUrl");
                        boolean changed = false;
                        if (name  != null) { chatAdapter.putUserName(renterId, name);   changed = true; }
                        if (photo != null) { chatAdapter.putUserPhoto(renterId, photo); changed = true; }
                        if (changed && getActivity() != null) {
                            getActivity().runOnUiThread(() -> chatAdapter.notifyDataSetChanged());
                        }
                    });
        }
    }

    // ---------------------------------------------------------------------------
    // Filter, search, unread badge
    // ---------------------------------------------------------------------------

    private void applyFilterAndSearch() {
        List<ChatRoom> filtered = new ArrayList<>();

        for (ChatRoom room : allRooms) {
            boolean matchesFilter;
            switch (currentFilter) {
                case "Unread":
                    // Use getMode() as a proxy — rooms in AI mode that haven't been
                    // replied to by the owner are treated as unread.
                    // If ChatRoom exposes an unread count method, swap this line.
                    matchesFilter = ChatRoom.MODE_AI.equals(room.getMode());
                    break;
                case "Booking requests":
                    // Rooms tied to a listing are booking-related
                    matchesFilter = room.getListingId() != null && !room.getListingId().isEmpty();
                    break;
                default:
                    matchesFilter = true;
            }

            // Search by listing title if available, otherwise pass through
            boolean matchesQuery = currentQuery.isEmpty()
                    || matchesRoomQuery(room, currentQuery);

            if (matchesFilter && matchesQuery) filtered.add(room);
        }

        chatAdapter.setChatRooms(filtered);
        chatAdapter.notifyDataSetChanged();
        checkAndShowInboxEmptyState(filtered.isEmpty());
    }

    /**
     * Safe search against available ChatRoom fields.
     * Avoids calling getListingTitle() which is not confirmed on the model.
     * Uses getListingId() as a fallback search target alongside mode.
     */
    private boolean matchesRoomQuery(ChatRoom room, String query) {
        if (room.getListingId() != null && room.getListingId().toLowerCase().contains(query)) return true;
        if (room.getMode() != null && room.getMode().toLowerCase().contains(query)) return true;
        return false;
    }

    private void updateUnreadBadge() {
        // ChatRoom.getUnreadCount(userId) is not confirmed to exist on the model.
        // Badge is hidden until the ChatRoom model is confirmed to expose this method.
        if (tvNewMessages != null) tvNewMessages.setVisibility(View.GONE);
    }

    // ---------------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------------

    private void openChatRoom(ChatRoom room) {
        chatRepository.markAsRead(room.getId(), currentUserId);
        Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ROOM_ID, room.getId());
        intent.putExtra(ChatRoomActivity.EXTRA_LISTING_ID,    room.getListingId());
        intent.putExtra(ChatRoomActivity.EXTRA_OWNER_ID,      room.getOwnerId());
        intent.putExtra(ChatRoomActivity.EXTRA_RENTER_ID,     room.getRenterId());
        intent.putExtra(ChatRoomActivity.EXTRA_CURRENT_MODE,  room.getMode());
        // EXTRA_LISTING_TITLE omitted — getListingTitle() not confirmed on ChatRoom
        startActivity(intent);
    }

    // ---------------------------------------------------------------------------
    // Empty state
    // ---------------------------------------------------------------------------

    private void checkAndShowInboxEmptyState(boolean isEmpty) {
        if (isEmpty) showInboxEmptyState();
        else hideInboxEmptyState();
    }

    private void showInboxEmptyState() {
        if (emptyStateInbox != null) {
            emptyStateInbox.setAlpha(0f);
            emptyStateInbox.setVisibility(View.VISIBLE);
            emptyStateInbox.animate().alpha(1f).setDuration(300).setListener(null);
        }
        if (rvInbox != null) rvInbox.setVisibility(View.GONE);
    }

    private void hideInboxEmptyState() {
        if (emptyStateInbox != null && emptyStateInbox.getVisibility() == View.VISIBLE) {
            emptyStateInbox.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            emptyStateInbox.setVisibility(View.GONE);
                        }
                    });
        }
        if (rvInbox != null) rvInbox.setVisibility(View.VISIBLE);
    }
}