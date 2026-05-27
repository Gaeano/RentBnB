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
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.usc.rentbnb.R;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class GoogleAuthHelper {

    private static final String TAG = "GoogleAuthHelper";

    private final Activity activity;
    private final CredentialManager credentialManager;
    private final GoogleAuthCallback callback;

    public interface GoogleAuthCallback {
        void onSuccess(String idToken);
        void onError(String errorMessage);
    }

    public GoogleAuthHelper(Activity activity, GoogleAuthCallback callback) {
        this.activity          = activity;
        this.credentialManager = CredentialManager.create(activity);
        this.callback          = callback;
    }

    public void launchGoogleSignIn() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                // Nonce prevents replay attacks and also forces the account picker
                // to appear even when no account has previously authorised the app.
                // Without this, NoCredentialException is thrown for new users on
                // some versions of Google Play Services.
                .setNonce(generateNonce())
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
                        Log.d(TAG, "Credential acquired successfully");
                        handleSignInResult(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        // Log the full exception class name and message so the real
                        // cause is visible in Logcat even if the Toast is generic.
                        Log.e(TAG, "GetCredentialException type : " + e.getClass().getSimpleName());
                        Log.e(TAG, "GetCredentialException cause: " + e.getMessage());

                        if (e instanceof GetCredentialCancellationException) {
                            // User deliberately dismissed the picker — no Toast needed.
                            Log.d(TAG, "User cancelled the Google Sign-In picker.");
                            return;
                        }

                        if (e instanceof NoCredentialException) {
                            // No Google account is available on this device, or the
                            // SHA-1 fingerprint / web client ID is misconfigured.
                            Log.e(TAG, "NoCredentialException — check: " +
                                    "1) SHA-1 in Firebase console matches app build, " +
                                    "2) google-services.json is up to date, " +
                                    "3) a Web Client ID exists in Google Cloud Console.");
                            callback.onError(
                                    "Google Sign-In is not available. " +
                                            "Please check your Google account settings or try again.");
                            return;
                        }

                        // Any other exception — surface the real message in debug builds
                        String detail = e.getMessage() != null ? e.getMessage() : "Unknown error";
                        callback.onError("Google Sign-In failed: " + detail);
                    }
                }
        );
    }

    private void handleSignInResult(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential
                && credential.getType().equals(
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try {
                GoogleIdTokenCredential googleCred =
                        GoogleIdTokenCredential.createFrom(credential.getData());
                String idToken = googleCred.getIdToken();
                Log.d(TAG, "Google ID token extracted — exchanging with Firebase");
                firebaseAuthWithGoogle(idToken);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse Google credential", e);
                callback.onError("Failed to read Google credentials. Please try again.");
            }
        } else {
            Log.e(TAG, "Unexpected credential type: " + credential.getType());
            callback.onError("Unexpected credential type. Please try again.");
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

        FirebaseAuth.getInstance()
                .signInWithCredential(firebaseCredential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Firebase auth success: " + user.getEmail());
                            callback.onSuccess(idToken);
                        } else {
                            callback.onError(
                                    "Sign-in succeeded but user is null. Please try again.");
                        }
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";
                        Log.e(TAG, "Firebase signInWithCredential failed: " + msg,
                                task.getException());
                        callback.onError("Authentication failed: " + msg);
                    }
                });
    }

    /**
     * Generates a secure random nonce as a hex string.
     * Required by GetGoogleIdOption to prevent replay attacks.
     * Also forces the account picker to appear for users who have not
     * previously authorised this app, preventing NoCredentialException.
     */
    private String generateNonce() {
        try {
            byte[] bytes = new byte[16];
            new SecureRandom().nextBytes(bytes);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            // Fallback to a plain random hex if SHA-256 is unavailable (should never happen)
            return Long.toHexString(new SecureRandom().nextLong());
        }
    }
}