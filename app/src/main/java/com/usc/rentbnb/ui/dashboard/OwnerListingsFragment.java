package com.usc.rentbnb.ui.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.usc.rentbnb.R;

public class OwnerListingsFragment extends Fragment {

    private TextView tvTotalListings;
    private EditText etSearchListings;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvInventory;
    private FloatingActionButton fabAddListing;
    private View emptyStateListings;
    private MaterialButton btnEmptyStateAddListing;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_listings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalListings = view.findViewById(R.id.tvTotalListings);
        etSearchListings = view.findViewById(R.id.search_bar);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        rvInventory = view.findViewById(R.id.rvInventory);
        fabAddListing = view.findViewById(R.id.fabAddListing);
        emptyStateListings = view.findViewById(R.id.emptyStateListings);
        btnEmptyStateAddListing = view.findViewById(R.id.btnEmptyStateAddListing);

        rvInventory.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup FAB click listener
        fabAddListing.setOnClickListener(v -> openAddListingScreen());

        // Setup empty state button click listener
        if (btnEmptyStateAddListing != null) {
            btnEmptyStateAddListing.setOnClickListener(v -> openAddListingScreen());
        }

        // TODO: Initialize InventoryAdapter and attach to rvInventory
        // For demo purposes, check if data is empty and show empty state
        checkAndShowEmptyState(true); // Change to false when you have data
    }

    /**
     * Check if the list is empty and show/hide empty state accordingly
     * @param isEmpty true if the list is empty, false otherwise
     */
    private void checkAndShowEmptyState(boolean isEmpty) {
        if (isEmpty) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    /**
     * Show empty state with smooth fade-in animation
     */
    private void showEmptyState() {
        if (emptyStateListings != null) {
            emptyStateListings.setAlpha(0f);
            emptyStateListings.setVisibility(View.VISIBLE);
            emptyStateListings.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(null);
        }

        // Hide RecyclerView
        if (rvInventory != null) {
            rvInventory.setVisibility(View.GONE);
        }

        // Hide total listings badge
        if (tvTotalListings != null) {
            tvTotalListings.setVisibility(View.GONE);
        }
    }

    /**
     * Hide empty state with smooth fade-out animation
     */
    private void hideEmptyState() {
        if (emptyStateListings != null && emptyStateListings.getVisibility() == View.VISIBLE) {
            emptyStateListings.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            emptyStateListings.setVisibility(View.GONE);
                        }
                    });
        }

        // Show RecyclerView
        if (rvInventory != null) {
            rvInventory.setVisibility(View.VISIBLE);
        }

        // Show and update total listings badge
        if (tvTotalListings != null) {
            tvTotalListings.setVisibility(View.VISIBLE);
            // TODO: Update with actual count from adapter
            // tvTotalListings.setText(adapter.getItemCount() + " total");
        }
    }

    /**
     * Open the Add Listing screen
     */
    private void openAddListingScreen() {
        Toast.makeText(getContext(), "Open Add Listing Screen", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to CreateListingActivity
        // Intent intent = new Intent(getActivity(), CreateListingActivity.class);
        // startActivity(intent);
    }

    /**
     * Call this method after successfully adding a listing to refresh the view
     */
    public void onListingAdded() {
        // TODO: Refresh adapter
        checkAndShowEmptyState(false); // Update to check actual data
    }
}