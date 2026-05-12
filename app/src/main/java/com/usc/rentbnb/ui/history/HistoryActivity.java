package com.usc.rentbnb.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.HistoryAdapter;
import com.usc.rentbnb.models.Booking;
import com.usc.rentbnb.models.BookingResponse;
import com.usc.rentbnb.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class HistoryActivity extends AppCompatActivity {

    private TextView tabRented, tabLent;
    private boolean isShowingRented = true;

    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private LinearLayout emptyStateLayout;
    private EditText searchBar;
    //private TextView emptyStateMessage;

    private List<Booking> myBookingsList = new ArrayList<>();
    private List<Booking> lentItemsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        ImageView backButton = findViewById(R.id.back_button);
        LinearLayout historyHeader = findViewById(R.id.history_header);

        tabRented = findViewById(R.id.tab_rented);
        tabLent = findViewById(R.id.tab_lent);
        rvHistory = findViewById(R.id.rvHistory);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        searchBar = findViewById(R.id.search_bar);

        ViewCompat.setOnApplyWindowInsetsListener(historyHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + 8, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        backButton.setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter();
        rvHistory.setAdapter(historyAdapter);

        setupTabs();
        setupSearch();

        updateListUI(new ArrayList<>(), "Loading...");

        fetchHistoryData();
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        List<Booking> currentList = isShowingRented ? myBookingsList : lentItemsList;
        if (query.isEmpty()) {
            updateListUI(currentList, isShowingRented ? "You haven't rented any items yet." : "No one has rented your items yet.");
        } else {
            List<Booking> filteredList = new ArrayList<>();
            for (Booking booking : currentList) {
                if (booking.getProductName().toLowerCase().contains(query.toLowerCase()) ||
                    booking.getCategory().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(booking);
                }
            }
            updateListUI(filteredList, "No matching items found.");
        }
    }

    private void setupTabs() {
        tabRented.setOnClickListener(v -> {
            if (!isShowingRented) {
                isShowingRented = true;
                updateTabUI();
                updateListUI(myBookingsList, "You haven't rented any items yet.");
            }
        });

        tabLent.setOnClickListener(v -> {
            if (isShowingRented) {
                isShowingRented = false;
                updateTabUI();
                updateListUI(lentItemsList, "No one has rented your items yet.");
            }
        });
    }

    private void updateTabUI() {
        if (isShowingRented) {
            tabRented.setBackgroundResource(R.drawable.bg_tab_active);
            tabRented.setTextColor(ContextCompat.getColor(this, R.color.teal_primary));
            tabLent.setBackgroundResource(android.R.color.transparent);
            tabLent.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
        } else {
            tabLent.setBackgroundResource(R.drawable.bg_tab_active);
            tabLent.setTextColor(ContextCompat.getColor(this, R.color.teal_primary));
            tabRented.setBackgroundResource(android.R.color.transparent);
            tabRented.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
        }
    }

    private void fetchHistoryData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            updateListUI(new ArrayList<>(), "Please log in to view history.");
            return;
        }
        String userId = user.getUid();

        updateListUI(new ArrayList<>(), "Loading your history...");

        ApiClient.getApiService().getMyBookings(userId).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myBookingsList = response.body().getData();
                    if (isShowingRented) updateListUI(myBookingsList, "You haven't rented any items yet.");
                }
            }
            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
            }
        });

        ApiClient.getApiService().getMyLentItems(userId).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lentItemsList = response.body().getData();
                    if (!isShowingRented) updateListUI(lentItemsList, "No one has rented your items yet.");
                }
            }
            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) { }
        });
    }

    private void updateListUI(List<Booking> bookings, String emptyMessage) {
        if (bookings == null || bookings.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            
            TextView messageTextView = emptyStateLayout.findViewById(R.id.empty_state_message);
            TextView descriptionTextView = emptyStateLayout.findViewById(R.id.empty_state_description);
            
            if (messageTextView != null) {
                messageTextView.setText(emptyMessage);
            }
            if (descriptionTextView != null) {
                descriptionTextView.setVisibility(emptyMessage.contains("Loading") ? View.GONE : View.VISIBLE);
            }
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            historyAdapter.setBookings(bookings);
            historyAdapter.notifyDataSetChanged();
        }
    }
}