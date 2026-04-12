package com.usc.rentbnb.ui.onboarding;

public class OnboardingItem {

    public static final int TYPE_LANDING = 0;
    public static final int TYPE_TUTORIAL = 1;
    public static final int TYPE_FINAL = 2;

    private int type;
    private String title;
    private String description;
    private int iconResId;
    private int backgroundResId;

    public OnboardingItem(int type, String title, String description, int iconResId, int backgroundResId) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.backgroundResId = backgroundResId;
    }

    public int getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconResId() { return iconResId; }
    public int getBackgroundResId() { return backgroundResId; }
}
