package com.usc.rentbnb.models;

import com.google.firebase.Timestamp;

/**
 * Data model representing a single chat message.
 *
 * Firestore path: /chatRooms/{chatRoomId}/messages/{messageId}
 *
 * Uses {@link Timestamp} for safe Firestore server-side ordering.
 * The adapter layer converts Timestamp → display string as needed.
 */
public class Message {

    // ─── Sender Type Constants ────────────────────────────────────────────────
    public static final String TYPE_USER  = "USER";
    public static final String TYPE_OWNER = "OWNER";
    public static final String TYPE_AI    = "AI";

    // ─── Fields ──────────────────────────────────────────────────────────────
    private String id;
    private String chatRoomId;
    private String senderId;
    private String text;
    private String senderType;

    /**
     * Native Firestore Timestamp. Used for ordering and display.
     * Never store as String in Firestore — always use Timestamp so
     * orderBy("timestamp") queries work correctly.
     */
    private Timestamp timestamp;

    // ─── Constructors ─────────────────────────────────────────────────────────

    /** Required no-arg constructor for Firestore POJO deserialization. */
    public Message() {}

    /**
     * Full constructor.
     *
     * @param id         Firestore document ID
     * @param chatRoomId Parent chat room ID
     * @param senderId   Firebase Auth UID of sender (or "INQUILINO" for AI)
     * @param text       Message body
     * @param timestamp  Firestore Timestamp (use {@code Timestamp.now()} for new messages)
     * @param senderType One of TYPE_USER, TYPE_OWNER, TYPE_AI
     */
    public Message(String id, String chatRoomId, String senderId,
                   String text, Timestamp timestamp, String senderType) {
        this.id         = id;
        this.chatRoomId = chatRoomId;
        this.senderId   = senderId;
        this.text       = text;
        this.timestamp  = timestamp;
        this.senderType = senderType;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChatRoomId() { return chatRoomId; }
    public void setChatRoomId(String chatRoomId) { this.chatRoomId = chatRoomId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public boolean isFromUser()  { return TYPE_USER.equals(senderType); }
    public boolean isFromOwner() { return TYPE_OWNER.equals(senderType); }
    public boolean isFromAi()    { return TYPE_AI.equals(senderType); }

    /**
     * Safe timestamp display helper.
     * Returns a human-readable time string, or empty string if timestamp is null.
     */
    public String getFormattedTime() {
        if (timestamp == null) return "";
        java.util.Date date = timestamp.toDate();
        return new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                .format(date);
    }
}