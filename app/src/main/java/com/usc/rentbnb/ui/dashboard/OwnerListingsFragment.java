package com.usc.rentbnb.ui.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.ui.listing.AddListingActivity;
import com.usc.rentbnb.adapters.OwnerListingAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerListingsFragment extends Fragment {

    private TextView tvTotalListings;
    private EditText etSearchListings;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvInventory;
    private FloatingActionButton fabAddListing;
    private View emptyStateListings;
    private MaterialButton btnEmptyStateAddListing;

    private OwnerListingAdapter listingAdapter;
    private final List<Listing> allListings = new ArrayList<>();
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_listings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalListings         = view.findViewById(R.id.tvTotalListings);
        etSearchListings        = view.findViewById(R.id.search_bar);
        chipGroupFilters        = view.findViewById(R.id.chipGroupFilters);
        rvInventory             = view.findViewById(R.id.rvInventory);
        fabAddListing           = view.findViewById(R.id.fabAddListing);
        emptyStateListings      = view.findViewById(R.id.emptyStateListings);
        btnEmptyStateAddListing = view.findViewById(R.id.btnEmptyStateAddListing);

        setupRecyclerView();
        setupSearch();
        setupChipFilters();

        fabAddListing.setOnClickListener(v -> openAddListingScreen());
        if (btnEmptyStateAddListing != null) {
            btnEmptyStateAddListing.setOnClickListener(v -> openAddListingScreen());
        }

        loadListings();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadListings();
    }

    // ---------------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------------

    private void setupRecyclerView() {
        listingAdapter = new OwnerListingAdapter();
        rvInventory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvInventory.setAdapter(listingAdapter);
    }

    private void setupSearch() {
        if (etSearchListings == null) return;
        etSearchListings.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                applySearch();
            }
        });
    }

    private void setupChipFilters() {
        // Chip filters are All / Active / Paused. Since Listing has no status field,
        // the backend getOwnerListings returns all listings for this owner.
        // Chip selection is wired but currently only "All" produces results;
        // "Active" and "Paused" will be functional once a status field is added to Listing.
        if (chipGroupFilters == null) return;
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> applySearch());
    }

    // ---------------------------------------------------------------------------
    // Load
    // ---------------------------------------------------------------------------

    private void loadListings() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        ApiClient.getApiService().getOwnerListings(user.getUid())
                .enqueue(new Callback<ListingResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ListingResponse> call,
                                           @NonNull Response<ListingResponse> response) {
                        if (!isAdded()) return;
                        allListings.clear();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null) {
                            allListings.addAll(response.body().getData());
                        }
                        applySearch();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ListingResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        applySearch();
                    }
                });
    }

    // ---------------------------------------------------------------------------
    // Filter
    // ---------------------------------------------------------------------------

    private void applySearch() {
        List<Listing> filtered = new ArrayList<>();
        for (Listing listing : allListings) {
            boolean matchesQuery = currentQuery.isEmpty()
                    || (listing.getProductName() != null
                    && listing.getProductName().toLowerCase().contains(currentQuery.toLowerCase()))
                    || (listing.getCategory() != null
                    && listing.getCategory().toLowerCase().contains(currentQuery.toLowerCase()));

            if (matchesQuery) filtered.add(listing);
        }

        listingAdapter.setItems(filtered);
        updateTotalBadge(filtered.size());
        checkAndShowEmptyState(filtered.isEmpty());
    }

    private void updateTotalBadge(int count) {
        if (tvTotalListings == null) return;
        if (count > 0) {
            tvTotalListings.setText(count + " total");
            tvTotalListings.setVisibility(View.VISIBLE);
        } else {
            tvTotalListings.setVisibility(View.GONE);
        }
    }

    // ---------------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------------

    private void openAddListingScreen() {
        startActivity(new Intent(requireActivity(), AddListingActivity.class));
    }

    // ---------------------------------------------------------------------------
    // Empty state
    // ---------------------------------------------------------------------------

    private void checkAndShowEmptyState(boolean isEmpty) {
        if (isEmpty) showEmptyState();
        else hideEmptyState();
    }

    private void showEmptyState() {
        if (emptyStateListings != null) {
            emptyStateListings.setAlpha(0f);
            emptyStateListings.setVisibility(View.VISIBLE);
            emptyStateListings.animate().alpha(1f).setDuration(300).setListener(null);
        }
        if (rvInventory != null) rvInventory.setVisibility(View.GONE);
        if (tvTotalListings != null) tvTotalListings.setVisibility(View.GONE);
    }

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
        if (rvInventory != null) rvInventory.setVisibility(View.VISIBLE);
    }
}