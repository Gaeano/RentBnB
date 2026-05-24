package com.usc.rentbnb.ui.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;

public class OwnerDashboardFragment extends Fragment {

    private TextView tvStatEarnings, tvStatPayouts;
    private RecyclerView rvActionRequired, rvCurrentlyRented;
    private View emptyStateActionRequired;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Stats
        tvStatEarnings = view.findViewById(R.id.tvStatEarnings);
        tvStatPayouts = view.findViewById(R.id.tvStatPayouts);

        // Setup RecyclerViews
        rvActionRequired = view.findViewById(R.id.rvActionRequired);
        rvCurrentlyRented = view.findViewById(R.id.rvCurrentlyRented);

        // Get empty state view
        emptyStateActionRequired = view.findViewById(R.id.emptyStateActionRequired);

        // Prevent NestedScrollView stuttering
        rvActionRequired.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override public boolean canScrollVertically() { return false; }
        });
        rvCurrentlyRented.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override public boolean canScrollVertically() { return false; }
        });

        // TODO: Initialize Adapters here and attach to RecyclerViews
        // rvActionRequired.setAdapter(new ActionRequiredAdapter(dataList));

        // For demo, show empty state if no action items
        checkAndShowActionRequiredEmptyState(true); // Change to false when you have data

        fetchDashboardData();
    }

    private void fetchDashboardData() {
        // TODO: Call your Node.js backend (e.g., /api/v1/payments/users/:id/payouts)
        // to calculate earnings and populate the views.

        // Example:
        // tvStatEarnings.setText("$450");
        // tvStatPayouts.setText("$120");
    }

    /**
     * Check if the action required list is empty and show/hide empty state accordingly
     * @param isEmpty true if the list is empty, false otherwise
     */
    private void checkAndShowActionRequiredEmptyState(boolean isEmpty) {
        if (isEmpty) {
            showActionRequiredEmptyState();
        } else {
            hideActionRequiredEmptyState();
        }
    }

    /**
     * Show empty state for action required section with smooth fade-in animation
     */
    private void showActionRequiredEmptyState() {
        if (emptyStateActionRequired != null) {
            emptyStateActionRequired.setAlpha(0f);
            emptyStateActionRequired.setVisibility(View.VISIBLE);
            emptyStateActionRequired.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(null);
        }

        // Hide RecyclerView
        if (rvActionRequired != null) {
            rvActionRequired.setVisibility(View.GONE);
        }
    }

    /**
     * Hide empty state for action required section with smooth fade-out animation
     */
    private void hideActionRequiredEmptyState() {
        if (emptyStateActionRequired != null && emptyStateActionRequired.getVisibility() == View.VISIBLE) {
            emptyStateActionRequired.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            emptyStateActionRequired.setVisibility(View.GONE);
                        }
                    });
        }

        // Show RecyclerView
        if (rvActionRequired != null) {
            rvActionRequired.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Call this method when action required data is loaded to update empty state
     * @param hasActions true if there are action items, false otherwise
     */
    public void onActionRequiredDataLoaded(boolean hasActions) {
        checkAndShowActionRequiredEmptyState(!hasActions);
    }
}