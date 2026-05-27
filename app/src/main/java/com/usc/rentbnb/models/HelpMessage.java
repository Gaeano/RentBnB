package com.usc.rentbnb.models;

/**
 * In-memory only message model for the Help Center.
 * Not persisted to Firestore or any backend — messages exist only
 * for the current session and are discarded when the Activity is destroyed.
 */
public class HelpMessage {
    public static final String TYPE_USER = "USER";
    public static final String TYPE_AI   = "AI";

    private final String text;
    private final String type;
    private final String timestamp;

    public HelpMessage(String text, String type) {
        this.text      = text;
        this.type      = type;
        this.timestamp = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    public String getText()      { return text; }
    public String getType()      { return type; }
    public String getTimestamp() { return timestamp; }

    public boolean isFromUser() { return TYPE_USER.equals(type); }
    public boolean isFromAi()   { return TYPE_AI.equals(type); }
}