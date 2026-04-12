package com.usc.rentbnb.ui.onboarding;

public class OnboardingPageModel {
    public enum PageType {
        LANDING, TUTORIAL, FINAL
    }

    private PageType pageType;
    private String title;
    private String description;
    private int iconResId; 
    private int backgroundResId; 

    public OnboardingPageModel(PageType pageType, String title, String description, int iconResId, int backgroundResId) {
        this.pageType = pageType;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.backgroundResId = backgroundResId;
    }

    public PageType getPageType() {
        return pageType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getBackgroundResId() {
        return backgroundResId;
    }
}
