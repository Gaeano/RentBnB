package com.usc.rentbnb.ui.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.BookingRequestAdapter;
import com.usc.rentbnb.adapters.RentedOutAdapter;
import com.usc.rentbnb.models.Booking;
import com.usc.rentbnb.models.BookingResponse;
import com.usc.rentbnb.models.ConfirmReturnResponse;
import com.usc.rentbnb.models.EarningsResponse;
import com.usc.rentbnb.network.ApiClient;

import java.util.ArrayList;
import java.util.Arrays;
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

    private static final String TAG = "OwnerDashboard";

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
        if (bookingsListener != null) { bookingsListener.remove(); bookingsListener = null; }
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

        // Check if the user has a Google provider linked
        boolean isGoogleUser = false;
        String googlePhotoUrl = null;

        for (UserInfo info : user.getProviderData()) {
            if ("google.com".equals(info.getProviderId())) {
                isGoogleUser = true;
                if (info.getPhotoUrl() != null) {
                    googlePhotoUrl = info.getPhotoUrl().toString();
                }
                break;
            }
        }

        if (isGoogleUser && googlePhotoUrl != null && !googlePhotoUrl.isEmpty()) {
            // Use Google profile photo directly
            Glide.with(this)
                    .load(googlePhotoUrl)
                    .placeholder(R.drawable.userprofile)
                    .error(R.drawable.userprofile)
                    .circleCrop()
                    .into(ivDashboardAvatar);
        } else {
            // Non-Google user — check Firestore for uploaded photo
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!isAdded()) return;
                        String photoUrl = (doc != null && doc.exists())
                                ? doc.getString("photoUrl") : null;
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.userprofile)
                                    .error(R.drawable.userprofile)
                                    .circleCrop()
                                    .into(ivDashboardAvatar);
                        } else {
                            ivDashboardAvatar.setImageResource(R.drawable.userprofile);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        ivDashboardAvatar.setImageResource(R.drawable.userprofile);
                    });
        }
    }

    private void setupRecyclerViews() {
        pendingAdapter = new BookingRequestAdapter(new BookingRequestAdapter.ActionListener() {
            @Override public void onAccept(Booking booking) { confirmAndUpdateStatus(booking, "ACTIVE"); }
            @Override public void onDecline(Booking booking) { confirmAndUpdateStatus(booking, "REJECTED"); }
        });

        // Tapping a currently-rented card opens the confirm-return / action dialog
        activeAdapter = new RentedOutAdapter(this::showReturnDialog);

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
    // Arrays.asList used — List.of() requires API 35+
    // orderBy omitted — whereIn + orderBy requires composite Firestore index
    // ---------------------------------------------------------------------------

    private void startListeningToBookings() {
        List<String> watched = Arrays.asList(
                "PENDING_OWNER_APPROVAL", "ACTIVE", "OVERDUE", "RETURN_PENDING");

        bookingsListener = FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("ownerId", currentUserId)
                .whereIn("status", watched)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { Log.e(TAG, "Listener error: " + error.getMessage()); return; }
                    if (snapshots == null || !isAdded()) return;

                    List<Booking> pending = new ArrayList<>();
                    List<Booking> active  = new ArrayList<>();
                    Set<String> renterIds = new HashSet<>();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setId(doc.getId());
                        String status = booking.getStatus();

                        if ("PENDING_OWNER_APPROVAL".equals(status)) {
                            pending.add(booking);
                        } else if ("ACTIVE".equals(status) || "OVERDUE".equals(status)
                                || "RETURN_PENDING".equals(status)) {
                            active.add(booking);
                        }

                        if (booking.getRenterId() != null && !booking.getRenterId().isEmpty()) {
                            renterIds.add(booking.getRenterId());
                        }
                    }

                    pending.sort((a, b) -> {
                        String da = a.getStartDate(), db = b.getStartDate();
                        if (da == null && db == null) return 0;
                        if (da == null) return 1;
                        if (db == null) return -1;
                        return da.compareTo(db);
                    });

                    pendingAdapter.setItems(pending);
                    activeAdapter.setItems(active);
                    updateActionRequiredEmptyState(pending.isEmpty());
                    resolveRenterProfiles(renterIds, pending, active);
                });
    }

    private void resolveRenterProfiles(Set<String> renterIds,
                                       List<Booking> pending, List<Booking> active) {
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
        ApiClient.getApiService().getOwnerEarnings(currentUserId).enqueue(new Callback<EarningsResponse>() {
            @Override
            public void onResponse(@NonNull Call<EarningsResponse> call,
                                   @NonNull Response<EarningsResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess() && response.body().getData() != null) {
                    EarningsResponse.Data data = response.body().getData();
                    if (tvStatEarnings != null)
                        tvStatEarnings.setText(String.format(Locale.getDefault(), "₱%,.2f", data.getTotalEarnings()));
                    if (tvStatPayouts != null)
                        tvStatPayouts.setText(String.format(Locale.getDefault(), "₱%,.2f", data.getPendingPayouts()));
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
        String renterName = booking.getRenterName();
        if (renterName == null || renterName.isEmpty()) renterName = "this renter";
        String msg = "ACTIVE".equals(newStatus)
                ? "Accept this booking request from " + renterName + "?"
                : "Decline this booking request?";

        new AlertDialog.Builder(requireContext())
                .setMessage(msg)
                .setPositiveButton("Confirm", (d, w) -> submitStatusUpdate(booking.getId(), newStatus))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitStatusUpdate(String bookingId, String status) {
        Map<String, String> body = new HashMap<>();
        body.put("status", status);

        ApiClient.getApiService().updateBookingStatus(bookingId, body)
                .enqueue(new Callback<BookingResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BookingResponse> call,
                                           @NonNull Response<BookingResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "ACTIVE".equals(status) ? "Booking accepted." : "Booking declined.",
                                    Toast.LENGTH_SHORT).show();
                        } else if (response.code() == 409) {
                            Toast.makeText(requireContext(),
                                    "Cannot accept: dates conflict with an existing booking.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update booking.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Network error.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showReturnDialog(Booking booking) {
        String itemName = booking.getListingTitle();
        if (itemName == null || itemName.isEmpty()) itemName = "this item";

        String statusNote = "OVERDUE".equals(booking.getStatus())
                ? "\n\nNote: This booking is overdue."
                : "RETURN_PENDING".equals(booking.getStatus())
                ? "\n\nReturn already confirmed. Pending penalty review."
                : "";

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Return")
                .setMessage("Has " + itemName + " been returned by the renter?" + statusNote)
                .setPositiveButton("Yes, confirm return", (d, w) -> callConfirmReturn(booking))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void callConfirmReturn(Booking booking) {
        ApiClient.getApiService().confirmReturn(booking.getId())
                .enqueue(new Callback<ConfirmReturnResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ConfirmReturnResponse> call,
                                           @NonNull Response<ConfirmReturnResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            if (response.body().isRequiresPenaltyReview()) {
                                showPenaltyDialog(booking,
                                        response.body().getPenaltyUnit(),
                                        response.body().getPenaltyAmount());
                            } else {
                                Toast.makeText(requireContext(),
                                        "Return confirmed. Payment released to you.", Toast.LENGTH_SHORT).show();
                                fetchEarnings();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to confirm return.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ConfirmReturnResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Network error.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPenaltyDialog(Booking booking, String penaltyUnit, double penaltyRate) {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);

        TextView tvInfo = new TextView(requireContext());
        tvInfo.setText(String.format(Locale.getDefault(),
                "Agreed penalty rate: ₱%,.2f / %s\n\nEnter the total penalty amount to charge, or 0 to waive.",
                penaltyRate, penaltyUnit != null ? penaltyUnit.toLowerCase() : "unit"));
        tvInfo.setTextSize(14f);
        tvInfo.setPadding(0, 0, 0, pad / 2);
        container.addView(tvInfo);

        EditText etAmount = new EditText(requireContext());
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setHint("Penalty amount (₱)");
        etAmount.setText(String.format(Locale.getDefault(), "%.2f", penaltyRate));
        container.addView(etAmount);

        new AlertDialog.Builder(requireContext())
                .setTitle("Apply Penalty Fee")
                .setView(container)
                .setPositiveButton("Charge & Complete", (d, w) -> {
                    String input = etAmount.getText().toString().trim();
                    double amount = 0.0;
                    try { amount = Double.parseDouble(input); } catch (NumberFormatException ignored) {}
                    if (amount < 0) amount = 0.0;
                    submitPenalty(booking.getId(), amount);
                })
                .setNegativeButton("Waive & Complete", (d, w) -> submitPenalty(booking.getId(), 0.0))
                .show();
    }

    private void submitPenalty(String bookingId, double penaltyAmount) {
        Map<String, Object> body = new HashMap<>();
        body.put("penaltyAmount", penaltyAmount);

        ApiClient.getApiService().applyPenalty(bookingId, body)
                .enqueue(new Callback<BookingResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BookingResponse> call,
                                           @NonNull Response<BookingResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            String msg = penaltyAmount > 0
                                    ? String.format(Locale.getDefault(),
                                    "Penalty of ₱%,.2f applied. Booking completed.", penaltyAmount)
                                    : "Penalty waived. Booking completed.";
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                            fetchEarnings();
                        } else {
                            Toast.makeText(requireContext(), "Failed to apply penalty.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Network error.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ---------------------------------------------------------------------------
    // Empty state
    // ---------------------------------------------------------------------------

    private void updateActionRequiredEmptyState(boolean isEmpty) {
        if (isEmpty) showActionRequiredEmptyState(); else hideActionRequiredEmptyState();
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
            emptyStateActionRequired.animate().alpha(0f).setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override public void onAnimationEnd(Animator animation) {
                            emptyStateActionRequired.setVisibility(View.GONE);
                        }
                    });
        }
        if (rvActionRequired != null) rvActionRequired.setVisibility(View.VISIBLE);
    }
}