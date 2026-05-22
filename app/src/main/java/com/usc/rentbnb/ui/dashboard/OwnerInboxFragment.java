package com.usc.rentbnb.ui.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;

public class OwnerInboxFragment extends Fragment {

    private EditText etSearchInbox;
    private RecyclerView rvInbox;
    private View emptyStateInbox;
    private TextView tvNewMessages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearchInbox = view.findViewById(R.id.search_bar);
        rvInbox = view.findViewById(R.id.rvInbox);
        emptyStateInbox = view.findViewById(R.id.emptyStateInbox);
        tvNewMessages = view.findViewById(R.id.tvNewMessages);

        rvInbox.setLayoutManager(new LinearLayoutManager(getContext()));

        // TODO: Initialize ChatThreadAdapter and attach to rvInbox

        // For demo purposes, show empty state
        checkAndShowInboxEmptyState(true); // Change to false when you have data
    }

    /**
     * Check if the inbox is empty and show/hide empty state accordingly
     * @param isEmpty true if the inbox is empty, false otherwise
     */
    private void checkAndShowInboxEmptyState(boolean isEmpty) {
        if (isEmpty) {
            showInboxEmptyState();
        } else {
            hideInboxEmptyState();
        }
    }

    /**
     * Show empty state for inbox with smooth fade-in animation
     */
    private void showInboxEmptyState() {
        if (emptyStateInbox != null) {
            emptyStateInbox.setAlpha(0f);
            emptyStateInbox.setVisibility(View.VISIBLE);
            emptyStateInbox.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(null);
        }

        // Hide RecyclerView
        if (rvInbox != null) {
            rvInbox.setVisibility(View.GONE);
        }

        // Hide new messages badge
        if (tvNewMessages != null) {
            tvNewMessages.setVisibility(View.GONE);
        }
    }

    /**
     * Hide empty state for inbox with smooth fade-out animation
     */
    private void hideInboxEmptyState() {
        if (emptyStateInbox != null && emptyStateInbox.getVisibility() == View.VISIBLE) {
            emptyStateInbox.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            emptyStateInbox.setVisibility(View.GONE);
                        }
                    });
        }

        // Show RecyclerView
        if (rvInbox != null) {
            rvInbox.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Call this method when inbox data is loaded
     * @param hasMessages true if there are messages, false otherwise
     * @param newMessageCount count of new/unread messages
     */
    public void onInboxDataLoaded(boolean hasMessages, int newMessageCount) {
        checkAndShowInboxEmptyState(!hasMessages);

        // Update new messages badge
        if (tvNewMessages != null && hasMessages && newMessageCount > 0) {
            tvNewMessages.setText(newMessageCount + " new");
            tvNewMessages.setVisibility(View.VISIBLE);
        } else if (tvNewMessages != null) {
            tvNewMessages.setVisibility(View.GONE);
        }
    }
}