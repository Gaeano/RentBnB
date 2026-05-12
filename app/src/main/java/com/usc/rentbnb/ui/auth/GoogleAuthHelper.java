package com.usc.rentbnb.ui.auth;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.usc.rentbnb.R;

public class GoogleAuthHelper {

    private final Activity activity;
    private final CredentialManager credentialManager;
    private final GoogleAuthCallback callback;

    private static final String TAG = "GoogleAuthHelper";

    public interface GoogleAuthCallback {
        void onSuccess(String idToken);
        void onError(String errorMessage);
    }

    public GoogleAuthHelper(Activity activity, GoogleAuthCallback callback) {
        this.activity = activity;
        this.callback = callback;
        this.credentialManager = CredentialManager.create(activity);
    }

    public void launchGoogleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                null,
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        Log.d(TAG, "Successfully acquired google credentials");
                        handleSignInResult(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Google Sign-In Error: " + e.getMessage());
                        callback.onError("Google Sign-In cancelled or failed.");
                    }
                }
        );
    }

    private void handleSignInResult(GetCredentialResponse result) {
        Credential credential = result.getCredential();
        if (credential instanceof CustomCredential &&
                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                String idToken = googleIdTokenCredential.getIdToken();
                Log.d(TAG, "Successfully extracted Google ID token. Exchanging with Firebase...");

                // CRITICAL FIX: Route the token to Firebase instead of instantly calling onSuccess
                firebaseAuthWithGoogle(idToken);

            } catch (Exception e) {
                Log.e(TAG, "Parsing Error: " + e.getMessage());
                callback.onError("Failed to parse Google credentials.");
            }
        } else {
            callback.onError("Unexpected credential type received.");
        }
    }

    // --- NEW METHOD: The Firebase Bridge ---
    private void firebaseAuthWithGoogle(String idToken) {
        // 1. Convert Google Token into a Firebase Credential
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        // 2. Log into Firebase using that credential
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Firebase authentication successful for: " + user.getEmail());
                            // 3. ONLY call onSuccess when Firebase confirms the user exists
                            callback.onSuccess(idToken);
                        } else {
                            callback.onError("Firebase login succeeded, but user object is null.");
                        }
                    } else {
                        Log.e(TAG, "Firebase Auth Failed", task.getException());
                        callback.onError("Firebase Authentication Failed: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }
}