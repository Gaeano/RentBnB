package com.usc.rentbnb.network.interceptors;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return chain.proceed(chain.request());
        }

        AtomicReference<String> tokenRef = new AtomicReference<>("");
        CountDownLatch latch = new CountDownLatch(1);

        user.getIdToken(false).addOnSuccessListener(result -> {
            tokenRef.set(result.getToken());
            latch.countDown();
        }).addOnFailureListener(e -> latch.countDown());

        try { latch.await(); } catch (InterruptedException ignored) {}

        Request authenticatedRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer " + tokenRef.get())
                .build();

        return chain.proceed(authenticatedRequest);
    }
}
