package com.usc.rentbnb.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.usc.rentbnb.R;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.ui.notifications.NotificationActivity;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles incoming FCM push notifications and FCM token refreshes.
 *
 * REQUIRED AndroidManifest.xml additions (add inside <application>):
 *
 * <service
 *     android:name=".services.MyFirebaseMessagingService"
 *     android:exported="false">
 *     <intent-filter>
 *         <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *     </intent-filter>
 * </service>
 *
 * Also add this permission if targeting API 33+:
 * <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG        = "FCMService";
    private static final String CHANNEL_ID = "rentbnb_default";

    // ---------------------------------------------------------------------------
    // Token management
    // ---------------------------------------------------------------------------

    /**
     * Called when the FCM token is refreshed (e.g. on first install, after token
     * expiry, or after clearing app data). Saves the new token to the backend.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token refreshed: " + token);
        saveFcmTokenToBackend(token);
    }

    /**
     * Called on app startup from wherever FirebaseAuth login is confirmed.
     * Fetches the current token and saves it. Safe to call repeatedly.
     */
    public static void refreshAndSaveToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token != null && !token.isEmpty()) {
                        saveFcmTokenToBackend(token);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Failed to get FCM token", e));
    }

    private static void saveFcmTokenToBackend(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, String> body = new HashMap<>();
        body.put("fcmToken", token);

        ApiClient.getApiService().saveFcmToken(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token saved to backend.");
                } else {
                    Log.w(TAG, "Failed to save FCM token: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.w(TAG, "Failed to save FCM token (network): " + t.getMessage());
            }
        });
    }

    // ---------------------------------------------------------------------------
    // Receive push notification
    // ---------------------------------------------------------------------------

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "FCM message received from: " + remoteMessage.getFrom());

        String title = null;
        String body  = null;

        // Notification payload (shown automatically when app is in background)
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body  = remoteMessage.getNotification().getBody();
        }

        // Data payload — always delivered, used when app is in foreground
        Map<String, String> data = remoteMessage.getData();
        if (!data.isEmpty()) {
            if (title == null) title = data.get("title");
            if (body == null)  body  = data.get("body");
        }

        if (title == null) title = "RentBnb";
        if (body == null)  body  = "You have a new notification.";

        showSystemNotification(title, body, data);
    }

    // ---------------------------------------------------------------------------
    // Show system notification
    // ---------------------------------------------------------------------------

    private void showSystemNotification(String title, String body, Map<String, String> data) {
        createNotificationChannel();

        // Tapping the notification opens NotificationActivity
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, flags);

        int smallIcon = R.drawable.ic_notifications;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            // Use a unique ID per push to stack notifications rather than replace
            int notifId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
            manager.notify(notifId, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "RentBnb Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Booking requests, messages, and rental updates.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}