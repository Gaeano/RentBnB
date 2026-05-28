package com.usc.rentbnb.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.HistoryAdapter;
import com.usc.rentbnb.models.Booking;
import com.usc.rentbnb.models.BookingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.ui.review.ReviewBookingActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryStatusFragment extends Fragment {

    private static final String ARG_STATUS = "status";

    private String targetStatus;
    private RecyclerView rvHistoryList;
    private HistoryAdapter historyAdapter;
    private LinearLayout emptyStateLayout;
    private List<Booking> allBookings = new ArrayList<>();
    private String currentQuery = "";
    private View loadingOverlay;

    public static HistoryStatusFragment newInstance(String status) {
        HistoryStatusFragment fragment = new HistoryStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetStatus = getArguments().getString(ARG_STATUS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_history_list, container, false);
        loadingOverlay = requireActivity().findViewById(R.id.loading_overlay);

        return  root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistoryList = view.findViewById(R.id.rvHistoryList);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);

        rvHistoryList.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyAdapter = new HistoryAdapter();
        
        historyAdapter.setOnReviewClickListener(booking -> {
            Intent intent = new Intent(requireContext(), ReviewBookingActivity.class);
            intent.putExtra("EXTRA_BOOKING_ID", booking.getId());
            intent.putExtra("EXTRA_LISTING_ID", booking.getListingId());
            intent.putExtra("EXTRA_LISTING_TITLE", booking.getListingTitle());
            startActivity(intent);
        });

        historyAdapter.setOnCancelClickListener(booking -> {
            new MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
                    .setTitle("Cancel Booking")
                    .setMessage("Are you sure you want to cancel this booking request?")
                    .setPositiveButton("Yes, Cancel", (d, w) -> cancelBooking(booking))
                    .setNegativeButton("No", null)
                    .show();
        });

        rvHistoryList.setAdapter(historyAdapter);

        fetchHistoryData();
    }

    public void setSearchQuery(String query) {
        this.currentQuery = query;
        if (isAdded()) {
            filterAndDisplay();
        }
    }

    private void fetchHistoryData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        loadingOverlay.setVisibility(View.VISIBLE); // show spinner


        ApiClient.getApiService().getMyBookings(user.getUid()).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (!isAdded()) return;
                loadingOverlay.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("HISTORY", "Code: " + response.code());
                    allBookings = response.body().getData();
                    filterAndDisplay();
                } else {
                    Log.e("HISTORY", "Code: " + response.code());
                    showEmpty("Failed to fetch bookings.");
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                if (!isAdded()) return;
                loadingOverlay.setVisibility(View.GONE);
                Log.e("HISTORY", t.getMessage());
                showEmpty("Network error.");
            }
        });
    }

    private void cancelBooking(Booking booking) {
        loadingOverlay.setVisibility(View.VISIBLE);

        Map<String, String> body = new HashMap<>();
        body.put("status", "CANCELLED");

        ApiClient.getApiService().updateBookingStatus(booking.getId(), body)
                .enqueue(new Callback<BookingResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BookingResponse> call, @NonNull Response<BookingResponse> response) {
                        if (!isAdded()) return;
                        loadingOverlay.setVisibility(View.GONE);

                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Booking cancelled successfully.", Toast.LENGTH_SHORT).show();
                            // Remove from local list and update UI
                            allBookings.remove(booking);
                            filterAndDisplay();
                        } else {
                            Toast.makeText(requireContext(), "Failed to cancel booking.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        loadingOverlay.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterAndDisplay() {
        List<Booking> filteredList = new ArrayList<>();
        for (Booking booking : allBookings) {
            boolean statusMatch = false;
            if ("Active".equalsIgnoreCase(targetStatus)) {
                statusMatch = "Active".equalsIgnoreCase(booking.getStatus());
            } else if ("Pending_owner_approval".equalsIgnoreCase(targetStatus)){
                statusMatch = "Pending_owner_approval".equalsIgnoreCase(booking.getStatus());
            } else if ("Completed".equalsIgnoreCase(targetStatus)) {
                statusMatch = "Completed".equalsIgnoreCase(booking.getStatus());
            } else if ("Cancelled".equalsIgnoreCase(targetStatus)) {
                statusMatch = "CANCELLED".equalsIgnoreCase(booking.getStatus()) || "REJECTED".equalsIgnoreCase(booking.getStatus());
            } else if ("Overdue".equalsIgnoreCase(targetStatus)) {
                statusMatch = "Overdue".equalsIgnoreCase(booking.getStatus()) || "Cancelled".equalsIgnoreCase(booking.getStatus());
            }

            if (statusMatch) {
                if (currentQuery.isEmpty()) {
                    filteredList.add(booking);
                } else {
                    if (booking.getListingTitle() != null && booking.getListingTitle().toLowerCase().contains(currentQuery.toLowerCase())) {
                        filteredList.add(booking);
                    }
                }
            }
        }

        if (filteredList.isEmpty()) {
            showEmpty(currentQuery.isEmpty() ? "No " + targetStatus + " bookings found" : "No matching items found.");
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvHistoryList.setVisibility(View.VISIBLE);
            historyAdapter.setBookings(filteredList);
            historyAdapter.notifyDataSetChanged();
        }
    }

    private void showEmpty(String message) {
        if (rvHistoryList == null || emptyStateLayout == null) return;
        
        rvHistoryList.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        TextView messageTextView = emptyStateLayout.findViewById(R.id.empty_state_message);
        TextView descriptionTextView = emptyStateLayout.findViewById(R.id.empty_state_description);
        
        if (messageTextView != null) {
            messageTextView.setText(message);
        }
        if (descriptionTextView != null) {
            descriptionTextView.setVisibility(View.VISIBLE);
        }
    }
}