package com.usc.rentbnb.ui.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.ChatAdapter;
import com.usc.rentbnb.adapters.ChatListAdapter;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.repositories.ChatRepository;

import java.util.ArrayList;
import java.util.List;

public class RenterChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private String currentUserId;
    private ChatRepository chatRepository;
    private ChatListAdapter adapter;
    private TextView tvEmpty;
    private ListenerRegistration chatRoomsListener;


    public RenterChatFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_renter_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmpty = view.findViewById(R.id.tv_empty);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;
        currentUserId = user.getUid();
        chatRepository = new ChatRepository();


        setupRecyclerView();
        startListeningToRenterChats();

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

    private void setupRecyclerView(){
        adapter = new ChatListAdapter(currentUserId);
        adapter.setOnChatRoomClickListener(this::openChatRoom);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void startListeningToRenterChats() {
        chatRoomsListener = chatRepository.listenToChatRoomsForUser(
                currentUserId,
                new ChatRepository.ChatRoomsListCallback() {
                    @Override
                    public void onUpdate(List<ChatRoom> allRooms) {
                        if (getActivity() == null) return;

                        getActivity().runOnUiThread(() -> {
                            // 1. Strictly filter for ONLY Renter chats
                            List<ChatRoom> renterRooms = new ArrayList<>();
                            for (ChatRoom room : allRooms) {
                                if (currentUserId.equals(room.getRenterId())) {
                                    renterRooms.add(room);
                                }
                            }

                            // 2. Update UI State
                            boolean isEmpty = renterRooms.isEmpty();

                            Log.d("CHAT", "The value of isEmpty " + isEmpty);
                            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                            recyclerView.setVisibility(View.VISIBLE); // Never hide the list

                            // 3. Inject data and redraw
                            adapter.setChatRooms(renterRooms);
                            adapter.notifyDataSetChanged();

                            // 4. Fetch the names/photos of the Owners
                            resolveParticipantProfiles(renterRooms);
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.e("CHAT", "IM FAILING " + errorMessage);
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
            // In the Renter tab, the other person is ALWAYS the Owner.
            String ownerId = room.getOwnerId();

            db.collection("users").document(ownerId).get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;

                        boolean needsRedraw = false;
                        String name  = doc.getString("displayName");
                        String photo = doc.getString("photoUrl");

                        if (name != null) {
                            adapter.putUserName(ownerId, name);
                            needsRedraw = true;
                        }
                        if (photo != null) {
                            adapter.putUserPhoto(ownerId, photo);
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
}