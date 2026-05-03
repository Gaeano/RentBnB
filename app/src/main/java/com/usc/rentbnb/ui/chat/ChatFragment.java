package com.usc.rentbnb.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ChatListAdapter;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.repositories.ChatRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatFragment — the main chat inbox screen.
 *
 * Shows two tabs inside a single fragment:
 *   Tab 0 "As Renter"  — chat rooms where currentUser is the renter
 *   Tab 1 "As Owner"   — chat rooms where currentUser is the owner
 *                         (grouped by listing, handled in adapter)
 *
 * Real-time updates are powered by a Firestore snapshot listener.
 * The listener is removed in onDestroyView() to prevent memory leaks.
 *
 * Layout file: fragment_chat.xml
 */
public class ChatFragment extends Fragment {

    // ─── UI ───────────────────────────────────────────────────────────────────
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TextView tvInboxHeader;

    // ─── Data ─────────────────────────────────────────────────────────────────
    private ChatListAdapter adapter;
    private ChatRepository chatRepository;
    private ListenerRegistration chatRoomsListener;

    /** All chat rooms for the current user (both as renter and owner). */
    private final List<ChatRoom> allRooms = new ArrayList<>();

    // ─── Auth ─────────────────────────────────────────────────────────────────
    private String currentUserId;

    // ─── Active Tab ───────────────────────────────────────────────────────────
    private static final int TAB_RENTER = 0;
    private static final int TAB_OWNER  = 1;
    private int activeTab = TAB_RENTER;

    // =========================================================================
    // Fragment Lifecycle
    // =========================================================================

    public ChatFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return; // Should not happen if auth guard is in place
        currentUserId = user.getUid();

        chatRepository = new ChatRepository();

        initViews(view);
        applyWindowInsets();
        setupTabs();
        setupRecyclerView();
        startListeningToChatRooms();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // CRITICAL: remove Firestore listener to avoid memory leaks
        if (chatRoomsListener != null) {
            chatRoomsListener.remove();
            chatRoomsListener = null;
        }
    }

    // =========================================================================
    // Initialization
    // =========================================================================

    private void initViews(@NonNull View view) {
        tvInboxHeader = view.findViewById(R.id.tvInboxHeader);
        tabLayout     = view.findViewById(R.id.tabLayoutChats);
        recyclerView  = view.findViewById(R.id.recyclerViewChats);
        tvEmpty       = view.findViewById(R.id.tvEmptyChats);
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(tvInboxHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + 8,
                    v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("As Renter"));
        tabLayout.addTab(tabLayout.newTab().setText("As Owner"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                activeTab = tab.getPosition();
                filterAndDisplay();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new ChatListAdapter(currentUserId);
        adapter.setOnChatRoomClickListener(this::openChatRoom);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    // =========================================================================
    // Firestore Listener
    // =========================================================================

    /**
     * Starts a real-time listener for all chat rooms where the user is a participant.
     * On each update: caches all rooms, resolves other-participant names/photos,
     * and filters to the active tab.
     */
    private void startListeningToChatRooms() {
        chatRoomsListener = chatRepository.listenToChatRoomsForUser(
                currentUserId,
                new ChatRepository.ChatRoomsListCallback() {
                    @Override
                    public void onUpdate(List<ChatRoom> chatRooms) {
                        allRooms.clear();
                        allRooms.addAll(chatRooms);
                        filterAndDisplay();
                        resolveParticipantProfiles(chatRooms);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        showEmpty(true);
                    }
                }
        );
    }

    /**
     * Looks up display names and photo URLs for all other participants
     * in the given chat room list, caching them in the adapter.
     */
    private void resolveParticipantProfiles(@NonNull List<ChatRoom> rooms) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (ChatRoom room : rooms) {
            // The "other" participant from the current user's perspective
            String otherId = currentUserId.equals(room.getRenterId())
                    ? room.getOwnerId()
                    : room.getRenterId();

            db.collection("users").document(otherId).get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;
                        String name  = doc.getString("displayName");
                        String photo = doc.getString("photoUrl");
                        if (name != null)  adapter.putUserName(otherId, name);
                        if (photo != null) adapter.putUserPhoto(otherId, photo);
                    });
        }
    }

    // =========================================================================
    // Filtering
    // =========================================================================

    /**
     * Filters allRooms by the active tab role and pushes the result to the adapter.
     */
    private void filterAndDisplay() {
        List<ChatRoom> filtered = new ArrayList<>();
        for (ChatRoom room : allRooms) {
            if (activeTab == TAB_RENTER && currentUserId.equals(room.getRenterId())) {
                filtered.add(room);
            } else if (activeTab == TAB_OWNER && currentUserId.equals(room.getOwnerId())) {
                filtered.add(room);
            }
        }
        adapter.setChatRooms(filtered);
        showEmpty(filtered.isEmpty());
    }

    private void showEmpty(boolean empty) {
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // =========================================================================
    // Navigation
    // =========================================================================

    /**
     * Opens ChatRoomActivity for the selected chat room.
     * Passes all required extras so ChatRoomActivity can hydrate without extra Firestore reads.
     */
    private void openChatRoom(@NonNull ChatRoom room) {
        // Mark as read immediately on tap
        chatRepository.markAsRead(room.getId(), currentUserId);

        Intent intent = new Intent(requireContext(), ChatRoomActivity.class);
        intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ROOM_ID,    room.getId());
        intent.putExtra(ChatRoomActivity.EXTRA_LISTING_ID,      room.getListingId());
        intent.putExtra(ChatRoomActivity.EXTRA_LISTING_TITLE,   room.getListingTitle());
        intent.putExtra(ChatRoomActivity.EXTRA_OWNER_ID,        room.getOwnerId());
        intent.putExtra(ChatRoomActivity.EXTRA_RENTER_ID,       room.getRenterId());
        intent.putExtra(ChatRoomActivity.EXTRA_CURRENT_MODE,    room.getMode());
        startActivity(intent);
    }
}