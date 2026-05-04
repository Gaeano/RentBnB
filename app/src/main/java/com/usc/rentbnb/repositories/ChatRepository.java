package com.usc.rentbnb.repositories;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.models.Message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    private static final String COL_CHAT_ROOMS = "chatRooms";
    private static final String COL_MESSAGES   = "messages";
    private final FirebaseFirestore db;

    public ChatRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void createChatRoom(
            @NonNull String renterId,
            @NonNull String ownerId,
            @NonNull String listingId,
            @NonNull String listingTitle,
            @NonNull String listingImageUrl,
            @NonNull ChatRoomCallback callback) {

        List<String> participants = Arrays.asList(renterId, ownerId);
        Map<String, Integer> unreadCount = new HashMap<>();
        unreadCount.put(renterId, 0);
        unreadCount.put(ownerId, 0);

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("renterId", renterId);
        roomData.put("ownerId", ownerId);
        roomData.put("listingId", listingId);
        roomData.put("listingTitle", listingTitle);
        roomData.put("listingImageUrl", listingImageUrl);
        roomData.put("mode", ChatRoom.MODE_AI);
        roomData.put("lastMessage", "");
        roomData.put("lastMessageTimestamp", Timestamp.now());
        roomData.put("participantIds", participants);
        roomData.put("unreadCount", unreadCount);

        db.collection(COL_CHAT_ROOMS)
                .add(roomData)
                .addOnSuccessListener(docRef -> {
                    ChatRoom room = new ChatRoom(
                            docRef.getId(), renterId, ownerId,
                            listingId, listingTitle, listingImageUrl, participants
                    );
                    room.setUnreadCount(unreadCount);
                    callback.onSuccess(room);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void findExistingChatRoom(
            @NonNull String renterId,
            @NonNull String ownerId,
            @NonNull String listingId,
            @NonNull ExistingRoomCallback callback) {

        db.collection(COL_CHAT_ROOMS)
                .whereEqualTo("renterId", renterId)
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("listingId", listingId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        ChatRoom room = querySnapshot.getDocuments().get(0).toObject(ChatRoom.class);
                        if (room != null) room.setId(querySnapshot.getDocuments().get(0).getId());
                        callback.onResult(room);
                    } else {
                        callback.onResult(null);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    public ListenerRegistration listenToChatRoomsForUser(
            @NonNull String userId,
            @NonNull ChatRoomsListCallback callback) {

        return db.collection(COL_CHAT_ROOMS)
                .whereArrayContains("participantIds", userId)
                .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        callback.onFailure(error != null ? error.getMessage() : "Unknown error");
                        return;
                    }
                    List<ChatRoom> rooms = snapshots.toObjects(ChatRoom.class);
                    for (int i = 0; i < rooms.size(); i++) {
                        rooms.get(i).setId(snapshots.getDocuments().get(i).getId());
                    }
                    callback.onUpdate(rooms);
                });
    }

    public void switchChatMode(
            @NonNull String chatRoomId,
            @NonNull String newMode,
            @NonNull SimpleCallback callback) {

        db.collection(COL_CHAT_ROOMS)
                .document(chatRoomId)
                .update("mode", newMode)
                .addOnSuccessListener(unused -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public void markAsRead(@NonNull String chatRoomId, @NonNull String userId) {
        Map<String, Object> update = new HashMap<>();
        update.put("unreadCount." + userId, 0);
        db.collection(COL_CHAT_ROOMS)
                .document(chatRoomId)
                .update(update);
    }

    public void sendMessage(
            @NonNull String chatRoomId,
            @NonNull String senderId,
            @NonNull String text,
            @NonNull String senderType,
            @NonNull String recipientId,
            @NonNull SimpleCallback callback) {

        Timestamp now = Timestamp.now();

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("chatRoomId", chatRoomId);
        msgData.put("senderId", senderId);
        msgData.put("text", text);
        msgData.put("timestamp", now);
        msgData.put("senderType", senderType);

        DocumentReference roomRef = db.collection(COL_CHAT_ROOMS).document(chatRoomId);

        roomRef.collection(COL_MESSAGES)
                .add(msgData)
                .addOnSuccessListener(msgRef -> {
                    Map<String, Object> roomUpdate = new HashMap<>();
                    roomUpdate.put("lastMessage", text);
                    roomUpdate.put("lastMessageTimestamp", now);
                    roomUpdate.put("unreadCount." + recipientId, FieldValue.increment(1));

                    roomRef.update(roomUpdate)
                            .addOnSuccessListener(u -> callback.onResult(true))
                            .addOnFailureListener(e -> callback.onResult(false));
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public ListenerRegistration listenToMessages(
            @NonNull String chatRoomId,
            @NonNull MessagesCallback callback) {

        return db.collection(COL_CHAT_ROOMS)
                .document(chatRoomId)
                .collection(COL_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        callback.onFailure(error != null ? error.getMessage() : "Unknown error");
                        return;
                    }
                    List<Message> messages = snapshots.toObjects(Message.class);
                    for (int i = 0; i < messages.size(); i++) {
                        messages.get(i).setId(snapshots.getDocuments().get(i).getId());
                    }
                    callback.onUpdate(messages);
                });
    }

    public interface ChatRoomCallback {
        void onSuccess(ChatRoom chatRoom);
        void onFailure(String errorMessage);
    }

    public interface ExistingRoomCallback {
        void onResult(ChatRoom chatRoom);
    }

    public interface ChatRoomsListCallback {
        void onUpdate(List<ChatRoom> chatRooms);
        void onFailure(String errorMessage);
    }

    public interface MessagesCallback {
        void onUpdate(List<Message> messages);
        void onFailure(String errorMessage);
    }

    public interface SimpleCallback {
        void onResult(boolean success);
    }
}