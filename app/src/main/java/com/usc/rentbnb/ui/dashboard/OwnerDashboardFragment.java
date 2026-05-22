package com.usc.rentbnb.ui.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.RentedOutAdapter;
import com.usc.rentbnb.models.Booking;
import com.usc.rentbnb.models.BookingResponse;
import com.usc.rentbnb.models.EarningsResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.adapters.BookingRequestAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerDashboardFragment extends Fragment {

    private TextView tvStatEarnings, tvStatPayouts;
    private RecyclerView rvActionRequired, rvCurrentlyRented;
    private View emptyStateActionRequired;
    private ShapeableImageView ivDashboardAvatar;
    private ImageView ivDashboardNotif;

    private BookingRequestAdapter pendingAdapter;
    private RentedOutAdapter activeAdapter;

    private ListenerRegistration bookingsListener;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        currentUserId = user.getUid();

        initViews(view);
        loadAvatar(user);
        setupRecyclerViews();
        setupNotificationBell();
        startListeningToBookings();
        fetchEarnings();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bookingsListener != null) {
            bookingsListener.remove();
            bookingsListener = null;
        }
    }

    // ---------------------------------------------------------------------------
    // Init
    // ---------------------------------------------------------------------------

    private void initViews(View view) {
        tvStatEarnings           = view.findViewById(R.id.tvStatEarnings);
        tvStatPayouts            = view.findViewById(R.id.tvStatPayouts);
        rvActionRequired         = view.findViewById(R.id.rvActionRequired);
        rvCurrentlyRented        = view.findViewById(R.id.rvCurrentlyRented);
        emptyStateActionRequired = view.findViewById(R.id.emptyStateActionRequired);
        ivDashboardAvatar        = view.findViewById(R.id.ivDashboardAvatar);
        ivDashboardNotif         = view.findViewById(R.id.ivDashboardNotif);
    }

    private void loadAvatar(FirebaseUser user) {
        if (ivDashboardAvatar == null) return;
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || doc == null || !doc.exists()) return;
                    String photoUrl = doc.getString("photoUrl");
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(this)
                                .load(photoUrl)
                                .placeholder(R.drawable.userprofile)
                                .circleCrop()
                                .into(ivDashboardAvatar);
                    }
                });
    }

    private void setupRecyclerViews() {
        pendingAdapter = new BookingRequestAdapter(new BookingRequestAdapter.ActionListener() {
            @Override
            public void onAccept(Booking booking) {
                confirmAndUpdateStatus(booking, "ACTIVE");
            }
            @Override
            public void onDecline(Booking booking) {
                confirmAndUpdateStatus(booking, "REJECTED");
            }
        });

        activeAdapter = new RentedOutAdapter(
                booking -> confirmAndUpdateStatus(booking, "COMPLETED"));

        rvActionRequired.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override public boolean canScrollVertically() { return false; }
        });
        rvCurrentlyRented.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override public boolean canScrollVertically() { return false; }
        });

        rvActionRequired.setAdapter(pendingAdapter);
        rvCurrentlyRented.setAdapter(activeAdapter);
    }

    private void setupNotificationBell() {
        if (ivDashboardNotif == null) return;
        ivDashboardNotif.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Notifications coming soon.", Toast.LENGTH_SHORT).show());
    }

    // ---------------------------------------------------------------------------
    // Real-time bookings listener
    // Firestore whereIn requires API-level compatible list — use Arrays.asList,
    // not List.of which requires API 35+
    // ---------------------------------------------------------------------------

    private void startListeningToBookings() {
        List<String> watchedStatuses = Arrays.asList(
                "PENDING_OWNER_APPROVAL", "ACTIVE", "OVERDUE");

        bookingsListener = FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("ownerId", currentUserId)
                .whereIn("status", watchedStatuses)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null || !isAdded()) return;

                    List<Booking> pending = new ArrayList<>();
                    List<Booking> active  = new ArrayList<>();

                    // Collect unique renterIds for profile resolution
                    Set<String> renterIds = new HashSet<>();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setId(doc.getId());

                        String status = booking.getStatus();
                        if ("PENDING_OWNER_APPROVAL".equals(status)) {
                            pending.add(booking);
                        } else if ("ACTIVE".equals(status) || "OVERDUE".equals(status)) {
                            active.add(booking);
                        }

                        if (booking.getRenterId() != null && !booking.getRenterId().isEmpty()) {
                            renterIds.add(booking.getRenterId());
                        }
                    }

                    pendingAdapter.setItems(pending);
                    activeAdapter.setItems(active);
                    updateActionRequiredEmptyState(pending.isEmpty());

                    // Resolve renter display names and photos
                    resolveRenterProfiles(renterIds, pending, active);
                });
    }

    private void resolveRenterProfiles(Set<String> renterIds,
                                       List<Booking> pending,
                                       List<Booking> active) {
        if (renterIds.isEmpty() || !isAdded()) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (String renterId : renterIds) {
            db.collection("users").document(renterId).get()
                    .addOnSuccessListener(doc -> {
                        if (!isAdded() || doc == null || !doc.exists()) return;
                        String name  = doc.getString("displayName");
                        String photo = doc.getString("photoUrl");

                        for (Booking b : pending) {
                            if (renterId.equals(b.getRenterId())) {
                                if (name  != null) b.setRenterName(name);
                                if (photo != null) b.setRenterPhotoUrl(photo);
                            }
                        }
                        for (Booking b : active) {
                            if (renterId.equals(b.getRenterId())) {
                                if (name  != null) b.setRenterName(name);
                                if (photo != null) b.setRenterPhotoUrl(photo);
                            }
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                pendingAdapter.notifyDataSetChanged();
                                activeAdapter.notifyDataSetChanged();
                            });
                        }
                    });
        }
    }

    // ---------------------------------------------------------------------------
    // Earnings
    // ---------------------------------------------------------------------------

    private void fetchEarnings() {
        ApiClient.getApiService().getOwnerEarnings(currentUserId)
                .enqueue(new Callback<EarningsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EarningsResponse> call,
                                           @NonNull Response<EarningsResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()
                                && response.body().getData() != null) {
                            EarningsResponse.Data data = response.body().getData();
                            if (tvStatEarnings != null)
                                tvStatEarnings.setText(String.format(Locale.getDefault(),
                                        "₱%,.2f", data.getTotalEarnings()));
                            if (tvStatPayouts != null)
                                tvStatPayouts.setText(String.format(Locale.getDefault(),
                                        "₱%,.2f", data.getPendingPayouts()));
                        } else {
                            if (tvStatEarnings != null) tvStatEarnings.setText("₱0.00");
                            if (tvStatPayouts  != null) tvStatPayouts.setText("₱0.00");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EarningsResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        if (tvStatEarnings != null) tvStatEarnings.setText("₱0.00");
                        if (tvStatPayouts  != null) tvStatPayouts.setText("₱0.00");
                    }
                });
    }

    // ---------------------------------------------------------------------------
    // Accept / Decline / Complete
    // ---------------------------------------------------------------------------

    private void confirmAndUpdateStatus(Booking booking, String newStatus) {
        String message;
        switch (newStatus) {
            case "ACTIVE":
                String renterName = booking.getRenterName();
                if (renterName == null || renterName.isEmpty()) renterName = "this renter";
                message = "Accept this booking request from " + renterName + "?";
                break;
            case "REJECTED":
                message = "Decline this booking request?";
                break;
            case "COMPLETED":
                message = "Mark this booking as completed early?";
                break;
            default:
                message = "Update booking status to " + newStatus + "?";
        }

        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> updateBookingStatus(booking, newStatus))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBookingStatus(Booking booking, String newStatus) {
        Map<String, String> body = new HashMap<>();
        body.put("status", newStatus);

        ApiClient.getApiService()
                .updateBookingStatus(booking.getId(), body)
                .enqueue(new Callback<BookingResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BookingResponse> call,
                                           @NonNull Response<BookingResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            String msg;
                            switch (newStatus) {
                                case "ACTIVE":    msg = "Booking accepted.";              break;
                                case "REJECTED":  msg = "Booking declined.";              break;
                                case "COMPLETED": msg = "Booking marked as completed.";   break;
                                default:          msg = "Booking updated.";
                            }
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            // Firestore listener refreshes the list automatically
                        } else if (response.code() == 409) {
                            Toast.makeText(requireContext(),
                                    "Cannot accept: these dates conflict with an existing booking.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(),
                                    "Failed to update booking.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Network error. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------------------------------------------------------------------
    // Empty state
    // ---------------------------------------------------------------------------

    private void updateActionRequiredEmptyState(boolean isEmpty) {
        if (isEmpty) showActionRequiredEmptyState();
        else hideActionRequiredEmptyState();
    }

    private void showActionRequiredEmptyState() {
        if (emptyStateActionRequired != null) {
            emptyStateActionRequired.setAlpha(0f);
            emptyStateActionRequired.setVisibility(View.VISIBLE);
            emptyStateActionRequired.animate().alpha(1f).setDuration(300).setListener(null);
        }
        if (rvActionRequired != null) rvActionRequired.setVisibility(View.GONE);
    }

    private void hideActionRequiredEmptyState() {
        if (emptyStateActionRequired != null
                && emptyStateActionRequired.getVisibility() == View.VISIBLE) {
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
        if (rvActionRequired != null) rvActionRequired.setVisibility(View.VISIBLE);
    }
}