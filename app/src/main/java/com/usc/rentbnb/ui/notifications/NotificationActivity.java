package com.usc.rentbnb.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.NotificationAdapter;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.models.Notification;
import com.usc.rentbnb.models.NotificationResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.repositories.ChatRepository;
import com.usc.rentbnb.ui.chat.ChatRoomActivity;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.ui.listing.ListingsDetailsActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private ImageView btnNotificationMenu;
    private NotificationAdapter adapter;
    private final Map<String, Notification> notifMap = new HashMap<>();

    private ListenerRegistration bookingNotifListener;
    private ListenerRegistration chatListener;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notificationRoot), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        currentUserId = FirebaseAuth.getInstance().getUid();

        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty         = findViewById(R.id.tvEmpty);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ImageView btnMenu = findViewById(R.id.btnNotificationMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(this::showNotificationMenu);
        }

        setupRecyclerView();

        if (currentUserId != null) {
            startListeningToBookingNotifications();
            startListeningToChatNotifications();
        }

        fetchNewListingsAsNotifications();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookingNotifListener != null) bookingNotifListener.remove();
        if (chatListener != null) chatListener.remove();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(new ArrayList<>(), this);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void showNotificationMenu(View v) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, v);
        popup.getMenu().add("Mark all as read");
        popup.setOnMenuItemClickListener(item -> {
            if ("Mark all as read".contentEquals(item.getTitle())) {
                markAllAsRead();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void startListeningToBookingNotifications() {
        bookingNotifListener = FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Notification n = buildFromFirestore(doc);
                        notifMap.put(n.getId(), n);
                    }
                    updateUI();
                });
    }

    private Notification buildFromFirestore(QueryDocumentSnapshot doc) {
        String id        = doc.getId();
        String type      = doc.getString("type");
        String title     = doc.getString("title");
        String body      = doc.getString("body");
        String bookingId = doc.getString("bookingId");
        String listingId = doc.getString("listingId");
        Boolean readVal  = doc.getBoolean("read");
        boolean read     = readVal != null && readVal;

        String createdAt = "";
        if (doc.getTimestamp("createdAt") != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
            createdAt = sdf.format(doc.getTimestamp("createdAt").toDate());
        }

        // For booking notifications, listingImageUrl and productName may also be stored
        String productName  = doc.getString("listingTitle");
        String productImage = doc.getString("listingImageUrl");

        Notification n = new Notification(id, type, title, body, bookingId, listingId, read, createdAt);

        // Populate listing display fields if the document has them
        if (productName != null)  n.setProductName(productName);
        if (productImage != null) n.setProductImage(productImage);

        return n;
    }

    private void startListeningToChatNotifications() {
        ChatRepository chatRepo = new ChatRepository();
        chatListener = chatRepo.listenToChatRoomsForUser(currentUserId,
                new ChatRepository.ChatRoomsListCallback() {
                    @Override
                    public void onUpdate(List<ChatRoom> chatRooms) {
                        // Remove stale chat notifs first so rooms that are now read are cleared
                        notifMap.entrySet().removeIf(e -> e.getKey().startsWith("chat_"));

                        for (ChatRoom room : chatRooms) {
                            int unread = 0;
                            try { unread = room.getUnreadCountForUser(currentUserId); }
                            catch (Exception ignored) {}

                            if (unread <= 0) continue;

                            // Determine the other participant — the person who sent messages
                            String otherUserId = currentUserId.equals(room.getOwnerId())
                                    ? room.getRenterId() : room.getOwnerId();

                            String lastMsg    = tryGet(() -> room.getLastMessage());
                            String roomListingId = room.getListingId();

                            // Build the notification with the info we have, then enrich
                            // asynchronously with the other user's name and photo
                            Notification chatNotif = new Notification(
                                    "chat_" + room.getId(),
                                    "chat",
                                    room.getId(),
                                    roomListingId,
                                    tryGet(() -> room.getListingTitle()),
                                    tryGet(() -> room.getListingImageUrl()),
                                    lastMsg,
                                    null,  // name resolved below
                                    room.getOwnerId(),
                                    room.getRenterId(),
                                    false
                            );

                            notifMap.put(chatNotif.getId(), chatNotif);

                            // Fetch the other user's profile to get name and photo
                            if (otherUserId != null && !otherUserId.isEmpty()) {
                                enrichChatNotifWithUserProfile(chatNotif, otherUserId);
                            }
                        }

                        runOnUiThread(() -> updateUI());
                    }

                    @Override
                    public void onFailure(String errorMessage) {}
                });
    }

    private void enrichChatNotifWithUserProfile(Notification chatNotif, String userId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) return;
                    String name  = doc.getString("displayName");
                    String photo = doc.getString("photoUrl");
                    if (name  != null) chatNotif.setUsername(name);
                    // Store photo in productImage field — adapter reads it for the avatar
                    if (photo != null && !photo.isEmpty()) chatNotif.setProductImage(photo);
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                });
    }

    private void fetchNewListingsAsNotifications() {
        ApiClient.getApiService().getListings(null).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    for (Listing listing : response.body().getData()) {
                        if (listing.isNew()) {
                            String key = "listing_" + listing.getId();
                            if (notifMap.containsKey(key)) continue;
                            String imageUrl = (listing.getImageUrls() != null
                                    && !listing.getImageUrls().isEmpty())
                                    ? listing.getImageUrls().get(0) : null;
                            Notification n = new Notification(
                                    key, "listing",
                                    listing.getProductName(),
                                    listing.getCategory(),
                                    imageUrl,
                                    String.valueOf(listing.getPrice()),
                                    listing.getPriceUnit()
                            );
                            notifMap.put(key, n);
                        }
                    }
                }
                runOnUiThread(() -> updateUI());
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                runOnUiThread(() -> updateUI());
            }
        });
    }

    private void updateUI() {
        List<Notification> list = new ArrayList<>(notifMap.values());

        // Sort: unread first, then by descending creation time where available
        list.sort((a, b) -> {
            if (a.isRead() != b.isRead()) return a.isRead() ? 1 : -1;
            // Secondary sort by createdAt string desc (ISO format sorts lexicographically)
            String ta = a.getTimestamp(), tb = b.getTimestamp();
            if (ta != null && tb != null) return tb.compareTo(ta);
            return 0;
        });

        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            adapter.setNotifications(list);
        }
    }

    private void markAllAsRead() {
        // Update all items in the local map immediately for instant UI feedback
        for (Notification n : notifMap.values()) {
            n.setRead(true);
        }
        updateUI();

        // Call backend to persist
        ApiClient.getApiService().markAllAsRead().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // UI is already updated. Notify HomeActivity to re-check badge on resume.
                setResult(RESULT_OK);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setResult(RESULT_OK);
            }
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        String type = notification.getType() != null ? notification.getType() : "";

        switch (type) {
            case "rent":
            case "NEW_BOOKING_REQUEST":
            case "listing":
            case "BOOKING_ACTIVE":
            case "BOOKING_REJECTED":
            case "BOOKING_COMPLETED":
            case "OVERDUE":
            case "OVERDUE_WARNING": {
                // Navigate to listing details — fetch the full Listing object first
                String listingId = notification.getListingId();
                if (listingId != null && !listingId.isEmpty()) {
                    navigateToListingDetails(listingId);
                }
                break;
            }
            case "chat": {
                Intent intent = new Intent(this, ChatRoomActivity.class);
                intent.putExtra(ChatRoomActivity.EXTRA_CHAT_ROOM_ID, notification.getChatRoomId());
                intent.putExtra(ChatRoomActivity.EXTRA_LISTING_ID, notification.getListingId());
                intent.putExtra(ChatRoomActivity.EXTRA_OWNER_ID, notification.getOwnerId());
                intent.putExtra(ChatRoomActivity.EXTRA_RENTER_ID, notification.getRenterId());
                startActivity(intent);
                break;
            }
            default:
                break;
        }

        // Mark as read
        if (!notification.isRead()) {
            notification.setRead(true);
            adapter.notifyDataSetChanged();

            String notifType = notification.getType() != null ? notification.getType() : "";
            // Only backend notifications have a real Firestore doc ID (not prefixed)
            boolean isBackendNotif = !notification.getId().startsWith("chat_")
                    && !notification.getId().startsWith("listing_");
            if (isBackendNotif) {
                ApiClient.getApiService().markAsRead(notification.getId())
                        .enqueue(new Callback<ResponseBody>() {
                            @Override public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}
                            @Override public void onFailure(Call<ResponseBody> call, Throwable t) {}
                        });
            }
        }
    }

    private void navigateToListingDetails(String listingId) {
        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();

        // Fetch the single listing from the owner's endpoint or fallback approach:
        // Since there is no GET /listings/:id endpoint, we fetch all listings and
        // find the matching one. This is a reasonable approach given the small dataset.
        ApiClient.getApiService().getListings(null).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    for (Listing listing : response.body().getData()) {
                        if (listingId.equals(listing.getId())) {
                            Intent intent = new Intent(NotificationActivity.this,
                                    ListingsDetailsActivity.class);
                            intent.putExtra("listing_object", listing);
                            startActivity(intent);
                            return;
                        }
                    }
                }
                // Listing not found in public listings — may be paused or owner-only
                Toast.makeText(NotificationActivity.this,
                        "Listing is no longer available.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                Toast.makeText(NotificationActivity.this,
                        "Network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String tryGet(java.util.concurrent.Callable<String> fn) {
        try { return fn.call(); } catch (Exception e) { return null; }
    }
}
