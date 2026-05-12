package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.ViewModel;
import com.usc.rentbnb.models.FAQ;
import java.util.ArrayList;
import java.util.List;

public class AddListingViewModel extends ViewModel {
    public List<FAQ> faqs = new ArrayList<>();
    private final List<ListingDraft> drafts = new ArrayList<>();
    private int currentDraftIndex = 0;

    public AddListingViewModel() {
        drafts.add(new ListingDraft());
    }

    public ListingDraft currentDraft() {
        return drafts.get(currentDraftIndex);
    }

    public void queueCurrentAndStartNew() {
        drafts.add(new ListingDraft());
        currentDraftIndex = drafts.size() - 1;
    }

    public int getDraftCount() {
        return drafts.size();
    }

    public List<ListingDraft> getAllDrafts() {
        return drafts;
    }

    public void resetAll() {
        drafts.clear();
        drafts.add(new ListingDraft());
        currentDraftIndex = 0;
    }
}