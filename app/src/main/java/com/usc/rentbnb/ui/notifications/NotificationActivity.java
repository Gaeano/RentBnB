package com.usc.rentbnb.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.NotificationAdapter;
import com.usc.rentbnb.models.Notification;
import com.usc.rentbnb.models.NotificationResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.ui.listing.ListingsDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private ImageView btnNotificationMenu;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        View root = findViewById(R.id.notificationRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnNotificationMenu = findViewById(R.id.btnNotificationMenu);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnNotificationMenu.setOnClickListener(this::showNotificationMenu);

        setupRecyclerView();
        fetchNotifications();
    }

    private void showNotificationMenu(View v) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, v);
        popup.getMenu().add("Mark all as read");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Mark all as read")) {
                markAllAsRead();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void markAllAsRead() {
        ApiClient.getApiService().markAllAsRead().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Update local state even if server has partial success
                android.content.SharedPreferences prefs = getSharedPreferences("RentBnB_Prefs", MODE_PRIVATE);
                java.util.Set<String> readIds = new java.util.HashSet<>(prefs.getStringSet("read_notification_ids", new java.util.HashSet<>()));

                for (Notification n : notificationList) {
                    n.setRead(true);
                    readIds.add(n.getId());
                }
                
                prefs.edit().putStringSet("read_notification_ids", readIds).apply();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fallback local update
                android.content.SharedPreferences prefs = getSharedPreferences("RentBnB_Prefs", MODE_PRIVATE);
                java.util.Set<String> readIds = new java.util.HashSet<>(prefs.getStringSet("read_notification_ids", new java.util.HashSet<>()));

                for (Notification n : notificationList) {
                    n.setRead(true);
                    readIds.add(n.getId());
                }

                prefs.edit().putStringSet("read_notification_ids", readIds).apply();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notificationList, this);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void fetchNotifications() {
        ApiClient.getApiService().getNotifications().enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                notificationList = new ArrayList<>();
                
                android.content.SharedPreferences prefs = getSharedPreferences("RentBnB_Prefs", MODE_PRIVATE);
                java.util.Set<String> readIds = prefs.getStringSet("read_notification_ids", new java.util.HashSet<>());

                // ADD MOCK NOTIFICATIONS FOR RENTER UI
                Notification mockRent = new Notification(
                        "mock_rent_1",
                        "rent",
                        "Yamaha NMAX",
                        "Motorcycle",
                        "yamaha_nmax",
                        "150",
                        "day"
                );
                mockRent.setRead(readIds.contains(mockRent.getId()));
                notificationList.add(mockRent);
                
                Notification mockChat = new Notification(
                        "mock_chat_1",
                        "chat",
                        "Username"
                );
                mockChat.setRead(readIds.contains(mockChat.getId()));
                notificationList.add(mockChat);

                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> serverList = response.body().getData();
                    if (serverList != null) {
                        notificationList.addAll(serverList);
                    }
                    
                    fetchNewListingsAsNotifications();
                } else {
                    fetchNewListingsAsNotifications();
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                notificationList = new ArrayList<>();
                android.content.SharedPreferences prefs = getSharedPreferences("RentBnB_Prefs", MODE_PRIVATE);
                java.util.Set<String> readIds = prefs.getStringSet("read_notification_ids", new java.util.HashSet<>());

                Notification mockRent = new Notification(
                        "mock_rent_1",
                        "rent",
                        "Yamaha NMAX",
                        "Motorcycle",
                        "yamaha_nmax",
                        "150",
                        "day"
                );
                mockRent.setRead(readIds.contains(mockRent.getId()));
                notificationList.add(mockRent);

                Notification mockChat = new Notification(
                        "mock_chat_1",
                        "chat",
                        "Username"
                );
                mockChat.setRead(readIds.contains(mockChat.getId()));
                notificationList.add(mockChat);

                fetchNewListingsAsNotifications();
            }
        });
    }

    private void fetchNewListingsAsNotifications() {
        ApiClient.getApiService().getListings(null).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Listing> listings = response.body().getData();
                    if (listings != null) {
                        android.content.SharedPreferences prefs = getSharedPreferences("RentBnB_Prefs", MODE_PRIVATE);
                        java.util.Set<String> readIds = prefs.getStringSet("read_notification_ids", new java.util.HashSet<>());

                        for (Listing listing : listings) {
                            if (listing.isNew()) {
                                Notification n = new Notification(
                                        listing.getId(),
                                        "listing",
                                        listing.getProductName(),
                                        listing.getCategory(),
                                        (listing.getImageUrls() != null && !listing.getImageUrls().isEmpty()) ? listing.getImageUrls().get(0) : null,
                                        String.valueOf(listing.getPrice()),
                                        listing.getPriceUnit()
                                );
                                n.setRead(readIds.contains(n.getId()));

                                // Check if already exists by message to avoid duplicates if called multiple times
                                boolean exists = false;
                                for (Notification existing : notificationList) {
                                    if (listing.getProductName().equals(existing.getProductName())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    notificationList.add(0, n); // Add to top
                                }
                            }
                        }
                    }
                }
                updateUI();
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                updateUI();
            }
        });
    }

    private void updateUI() {
        if (notificationList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            adapter.setNotifications(notificationList);
        }
    }

    @Override
    public void onNotificationClick(Notification notification) {
        String type = notification.getType();
        
        // Navigation Logic
        if ("rent".equals(type) || "listing".equals(type)) {
            Intent intent = new Intent(this, ListingsDetailsActivity.class);
            intent.putExtra("listing_id", notification.getListingId());
            intent.putExtra("product_name", notification.getProductName() != null ? notification.getProductName() : "Product");
            intent.putExtra("category", notification.getProductCategory() != null ? notification.getProductCategory() : "Category");
            intent.putExtra("price", notification.getPrice() != null ? notification.getPrice() : "0");
            intent.putExtra("price_unit", notification.getPriceUnit() != null ? notification.getPriceUnit() : "day");
            startActivity(intent);
        } else if ("chat".equals(type)) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("navigate_to_chat", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        if (!notification.isRead()) {
            // Mark as read in DB
            ApiClient.getApiService().markAsRead(notification.getId()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    notification.setRead(true);
                    adapter.notifyDataSetChanged();
                    
                    // Also mark locally as read to ensure badge sync
                    markAsReadLocally(notification.getId());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    // Fallback local update
                    notification.setRead(true);
                    adapter.notifyDataSetChanged();
                    markAsReadLocally(notification.getId());
                }
            });
        }
    }

    private void markAsReadLocally(String id) {
        android.content.SharedPreferences prefs = getSharedPreferences("RentBnB_Prefs", MODE_PRIVATE);
        java.util.Set<String> readIds = new java.util.HashSet<>(prefs.getStringSet("read_notification_ids", new java.util.HashSet<>()));
        if (readIds.add(id)) {
            prefs.edit().putStringSet("read_notification_ids", readIds).apply();
        }
    }
}
