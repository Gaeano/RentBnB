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
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.usc.rentbnb.R;

public class GoogleAuthHelper {

    private final Activity activity;
    private final FirebaseAuth auth;
    private final CredentialManager credentialManager;
    private final GoogleAuthCallback callback;

    private static final String TAG = "GoogleAuthHelper";

    // 1. The Interface: This is how the helper talks back to your Activity
    public interface GoogleAuthCallback {
        void onSuccess(FirebaseUser user, boolean isNewUser);
        void onError(String errorMessage);
    }

    // 2. The Constructor
    public GoogleAuthHelper(Activity activity, GoogleAuthCallback callback) {
        this.activity = activity;
        this.callback = callback;
        this.auth = FirebaseAuth.getInstance();
        this.credentialManager = CredentialManager.create(activity);
    }

    // 3. The universal launch method
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
                        handleSignInResult(result);
                        Log.d(TAG, "Successfully acquired google credentials");
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        callback.onError("Google Sign-In cancelled or failed. No account found");
                        Log.e(TAG, e.getMessage());
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
                firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
                Log.d(TAG, "Successfully got google ID credential");
            } catch (Exception e) {
                callback.onError("Failed to parse Google credentials.");
                Log.e(TAG, "" + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential).addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();

                boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();

                callback.onSuccess(user, isNewUser);
                Log.d(TAG, "Successfully Signed in using credential");

            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Firebase Auth Failed";
                callback.onError(error);
                Log.e(TAG, "Firebase Auth Failed");
            }
        });
    }
}