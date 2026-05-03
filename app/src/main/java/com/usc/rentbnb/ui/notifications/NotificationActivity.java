package com.usc.rentbnb.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.usc.rentbnb.repositories.ChatRepository;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.ui.chat.ChatRoomActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private ListenerRegistration chatListener;

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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupRecyclerView();
        fetchNotifications();
        fetchChatNotifications();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) {
            chatListener.remove();
        }
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
                fetchNewListingsAsNotifications();
            }
        });
    }

    private void fetchChatNotifications() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        ChatRepository chatRepo = new ChatRepository();
        chatListener = chatRepo.listenToChatRoomsForUser(currentUserId, new ChatRepository.ChatRoomsListCallback() {
            @Override
            public void onUpdate(List<ChatRoom> chatRooms) {
                for (ChatRoom room : chatRooms) {
                    if (room.getUnreadCountForUser(currentUserId) > 0) {
                        Notification n = new Notification(
                                room.getId(),
                                "chat",
                                room.getId(),
                                room.getListingId(),
                                room.getListingTitle(),
                                room.getListingImageUrl(),
                                room.getLastMessage(),
                                "New Message",
                                room.getOwnerId(),
                                room.getRenterId(),
                                false
                        );

                        // Update or add
                        boolean found = false;
                        for (int i = 0; i < notificationList.size(); i++) {
                            Notification existing = notificationList.get(i);
                            if (existing.getId().equals(n.getId())) {
                                notificationList.set(i, n);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            notificationList.add(0, n);
                        }
                    }
                }
                updateUI();
            }

            @Override
            public void onFailure(String errorMessage) {
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

        if ("rent".equals(type) || "listing".equals(type)) {
            Intent intent = new Intent(this, ListingsDetailsActivity.class);
            intent.putExtra("product_name", notification.getProductName() != null ? notification.getProductName() : "Product");
            intent.putExtra("category", notification.getProductCategory() != null ? notification.getProductCategory() : "Category");
            intent.putExtra("price", notification.getPrice() != null ? notification.getPrice() : "0");
            intent.putExtra("price_unit", notification.getPriceUnit() != null ? notification.getPriceUnit() : "day");
            startActivity(intent);
        } else if ("chat".equals(type)) {
            Intent intent = new Intent(this, ChatRoomActivity.class);
            intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ROOM_ID, notification.getChatRoomId());
            intent.putExtra(ChatRoomActivity.EXTRA_LISTING_ID, notification.getListingId());
            intent.putExtra(ChatRoomActivity.EXTRA_LISTING_TITLE, notification.getProductName());
            intent.putExtra(ChatRoomActivity.EXTRA_OWNER_ID, notification.getOwnerId());
            intent.putExtra(ChatRoomActivity.EXTRA_RENTER_ID, notification.getRenterId());
            startActivity(intent);
        }

        if (!notification.isRead()) {
            if ("listing".equals(type) || "rent".equals(type) || "chat".equals(type)) {
                notification.setRead(true);
                adapter.notifyDataSetChanged();
                return;
            }

            ApiClient.getApiService().markAsRead(notification.getId()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        notification.setRead(true);
                        adapter.notifyDataSetChanged();
                    } else {
                        notification.setRead(true);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    notification.setRead(true);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}