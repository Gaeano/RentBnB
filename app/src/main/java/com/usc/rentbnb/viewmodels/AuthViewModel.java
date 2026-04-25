package com.usc.rentbnb.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.models.AuthResponse;
import com.usc.rentbnb.models.RegisterRequest;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.network.ApiService;
import com.usc.rentbnb.repositories.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private static final String TAG = "AuthViewModel";

    public AuthViewModel(){
        authRepository = new AuthRepository();
    }

    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }
    public LiveData<String> getErrorLiveData(){
        return errorLiveData;
    }
    public LiveData<Boolean> getLiveLoadingData(){
        return loadingLiveData;
    }

    private ApiService apiService = ApiClient.getApiService();

    public void login(String email, String password){
        loadingLiveData.setValue(true);

        authRepository.login(email, password).addOnCompleteListener(task -> {
            loadingLiveData.setValue(false);
            if (task.isSuccessful()){
                userLiveData.setValue(authRepository.getCurrentUser());
                Log.d(TAG, "Login successful");
            } else {
                errorLiveData.setValue(task.getException().getMessage());
                Log.e(TAG, "Login failed" + task.getException().getMessage());
            }
        });

    }

    public void signUp(String email, String password, String fullName) {
        loadingLiveData.setValue(true);

        authRepository.signUp(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                handleError("Sign up failed", task.getException());
                return;
            }

            authRepository.updateProfile(fullName).addOnCompleteListener(updateTask -> {
                if (!updateTask.isSuccessful()) {
                    handleError("Profile update failed", updateTask.getException());
                    return;
                }

                FirebaseUser user = authRepository.getCurrentUser();

                if (user == null) {
                    handleError("User session lost", null);
                    return;
                }

                user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                    if (!tokenTask.isSuccessful()) {
                        handleError("Token retrieval failed", tokenTask.getException());
                        return;
                    }

                    String token = "Bearer " + tokenTask.getResult().getToken();
                    apiService.registerUser(token, new RegisterRequest(fullName)).enqueue(new Callback<AuthResponse>() {
                        @Override
                        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                            loadingLiveData.setValue(false);
                            if (response.isSuccessful()) {
                                userLiveData.setValue(user);
                                Log.d(TAG, "User added to db    ");
                            } else {
                                errorLiveData.setValue("Backend Error: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<AuthResponse> call, Throwable t) {
                            loadingLiveData.setValue(false);
                            errorLiveData.setValue("Network Failure: " + t.getMessage());
                            Log.e(TAG, t.getMessage());
                        }
                    });
                });
            });
        });
    }

    private void handleError(String message, Exception e) {
        loadingLiveData.setValue(false);
        String error = (e != null) ? e.getMessage() : "Unknown error";
        errorLiveData.setValue(message + ": " + error);
        Log.e(TAG, message + ": " + error);
    }

    public void signInWithGoogle(String idToken){
        loadingLiveData.setValue(true);

        authRepository.loginWithGoogle(idToken).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                handleError("Sign in with Google failed", task.getException());
                return;
            }
            FirebaseUser user = authRepository.getCurrentUser();

            if (user == null) {
                handleError("User session lost", null);
                return;
            }

            user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                if (!tokenTask.isSuccessful()) {
                    handleError("Token retrieval failed", tokenTask.getException());
                    return;
                }

                String token = "Bearer " + tokenTask.getResult().getToken();
                apiService.googleSignIn(token).enqueue(new Callback<AuthResponse>(){

                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        loadingLiveData.setValue(false);
                        if(response.isSuccessful()){
                            userLiveData.setValue(user);
                            Log.d(TAG, "User added to db    ");
                        } else {
                            loadingLiveData.setValue(true);
                            errorLiveData.setValue("Backend Error: " + response.code());
                            Log.e(TAG, "Backend Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        loadingLiveData.setValue(false);
                        errorLiveData.setValue("Network Failure: " + t.getMessage());
                        Log.e(TAG, t.getMessage());
                    }
                });


            });
    });
    }

    public void logout(){
        authRepository.logout();
    }


}
