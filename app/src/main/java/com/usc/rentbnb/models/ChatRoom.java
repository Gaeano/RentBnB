package com.usc.rentbnb.models;

import com.google.firebase.Timestamp;

import java.util.List;

/**
 * Represents a single chat room between a renter and a listing.
 *
 * Firestore path: /chatRooms/{chatRoomId}
 *
 * One chat room exists per (renterId + listingId + ownerId) triple.
 * A renter may have multiple rooms for the same listing if it has different owners.
 */
public class ChatRoom {

    // ─── Mode Constants ──────────────────────────────────────────────────────
    /** Chat is handled by Inquilino (AI). Default on creation. */
    public static final String MODE_AI    = "AI";
    /** Chat has been handed off to the real owner. */
    public static final String MODE_OWNER = "OWNER";

    // ─── Fields ──────────────────────────────────────────────────────────────
    private String id;
    private String renterId;
    private String ownerId;
    private String listingId;
    private String listingTitle;
    private String listingImageUrl;

    /** Current mode: {@link #MODE_AI} or {@link #MODE_OWNER}. */
    private String mode;

    /** Snippet of the last message for the chat list preview. */
    private String lastMessage;

    /**
     * Firestore Timestamp of the last message.
     * Stored natively so Firestore ordering queries work correctly.
     */
    private Timestamp lastMessageTimestamp;

    /**
     * List of participant UIDs (renterId + ownerId).
     * Useful for Firestore security rules and querying chats for a user.
     */
    private List<String> participantIds;

    /**
     * Map of userId → count of unread messages for that user.
     * Key: Firebase Auth UID. Value: number of unread messages.
     *
     * Stored in Firestore as a nested map field, e.g.:
     *   unreadCount: { "uid_renter": 3, "uid_owner": 0 }
     */
    private java.util.Map<String, Integer> unreadCount;

    // ─── Constructors ─────────────────────────────────────────────────────────

    /** Required no-arg constructor for Firestore POJO deserialization. */
    public ChatRoom() {}

    public ChatRoom(String id, String renterId, String ownerId,
                    String listingId, String listingTitle, String listingImageUrl,
                    List<String> participantIds) {
        this.id               = id;
        this.renterId         = renterId;
        this.ownerId          = ownerId;
        this.listingId        = listingId;
        this.listingTitle     = listingTitle;
        this.listingImageUrl  = listingImageUrl;
        this.participantIds   = participantIds;
        this.mode             = MODE_AI; // Always starts in AI mode
        this.lastMessage      = "";
        this.unreadCount      = new java.util.HashMap<>();
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRenterId() { return renterId; }
    public void setRenterId(String renterId) { this.renterId = renterId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }

    public String getListingTitle() { return listingTitle; }
    public void setListingTitle(String listingTitle) { this.listingTitle = listingTitle; }

    public String getListingImageUrl() { return listingImageUrl; }
    public void setListingImageUrl(String listingImageUrl) { this.listingImageUrl = listingImageUrl; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Timestamp getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public java.util.Map<String, Integer> getUnreadCount() { return unreadCount; }
    public void setUnreadCount(java.util.Map<String, Integer> unreadCount) {
        this.unreadCount = unreadCount;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** @return true if Inquilino (AI) is currently handling this chat. */
    public boolean isAiMode() { return MODE_AI.equals(mode); }

    /**
     * Returns the unread message count for a specific user.
     *
     * @param userId Firebase Auth UID
     * @return number of unread messages, or 0 if not found
     */
    public int getUnreadCountForUser(String userId) {
        if (unreadCount == null || !unreadCount.containsKey(userId)) return 0;
        Integer count = unreadCount.get(userId);
        return count != null ? count : 0;
    }
}