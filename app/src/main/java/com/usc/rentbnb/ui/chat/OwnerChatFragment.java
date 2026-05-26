package com.usc.rentbnb.ui.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class OwnerChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmpty;

    private ChatListAdapter adapter;
    private ChatRepository chatRepository;
    private ListenerRegistration chatRoomsListener;
    private String currentUserId;

    public OwnerChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        currentUserId = user.getUid();

        chatRepository = new ChatRepository();

        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmpty = view.findViewById(R.id.tv_empty);

        setupRecyclerView();
        startListeningToOwnerChats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks when the fragment is destroyed by the ViewPager
        if (chatRoomsListener != null) {
            chatRoomsListener.remove();
            chatRoomsListener = null;
        }
    }

    private void setupRecyclerView() {
        adapter = new ChatListAdapter(currentUserId);
        adapter.setOnChatRoomClickListener(this::openChatRoom);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void startListeningToOwnerChats() {
        chatRoomsListener = chatRepository.listenToChatRoomsForUser(
                currentUserId,
                new ChatRepository.ChatRoomsListCallback() {
                    @Override
                    public void onUpdate(List<ChatRoom> allRooms) {
                        if (getActivity() == null) return;

                        getActivity().runOnUiThread(() -> {
                            // 1. Strictly filter for ONLY Owner chats
                            List<ChatRoom> ownerRooms = new ArrayList<>();
                            for (ChatRoom room : allRooms) {
                                if (currentUserId.equals(room.getOwnerId())) {
                                    ownerRooms.add(room);
                                }
                            }

                            // 2. Update UI State
                            boolean isEmpty = ownerRooms.isEmpty();
                            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                            recyclerView.setVisibility(View.VISIBLE); // Never hide the list

                            // 3. Inject data and redraw
                            adapter.setChatRooms(ownerRooms);
                            adapter.notifyDataSetChanged();

                            // 4. Fetch the names/photos of the Renters
                            resolveParticipantProfiles(ownerRooms);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                tvEmpty.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                            });
                        }
                    }
                }
        );
    }

    private void resolveParticipantProfiles(@NonNull List<ChatRoom> rooms) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (ChatRoom room : rooms) {
            // In the Owner tab, the other person is ALWAYS the Renter.
            String renterId = room.getRenterId();

            db.collection("users").document(renterId).get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;

                        boolean needsRedraw = false;
                        String name  = doc.getString("displayName");
                        String photo = doc.getString("photoUrl");

                        if (name != null) {
                            adapter.putUserName(renterId, name);
                            needsRedraw = true;
                        }
                        if (photo != null) {
                            adapter.putUserPhoto(renterId, photo);
                            needsRedraw = true;
                        }

                        if (needsRedraw && getActivity() != null) {
                            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }
                    });
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
        intent.putExtra(ChatRoomActivity.EXTRA_CURRENT_MODE, room.getMode());
        startActivity(intent);
    }

    public void filterChats(String query) {
        if (adapter != null) {
            adapter.filter(query);
        }
    }
}