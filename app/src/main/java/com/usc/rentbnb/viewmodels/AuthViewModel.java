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
import com.usc.rentbnb.models.CompanyRegistrationData;

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
    private final MutableLiveData<Boolean> authStepCompletedLiveData = new MutableLiveData<>();

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }
    public LiveData<String> getErrorLiveData(){
        return errorLiveData;
    }
    public LiveData<Boolean> getLiveLoadingData(){
        return loadingLiveData;
    }
    public LiveData<Boolean> getAuthStepCompletedLiveData() { return authStepCompletedLiveData; }

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

    public void createAccountAndVerifyEmail(String email, String password){
        loadingLiveData.setValue(true);

        authRepository.signUp(email,password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                handleError("Sign up Failed", task.getException());
                return;
            }

            FirebaseUser user = authRepository.getCurrentUser();
            if(user != null){
                user.sendEmailVerification();
                authStepCompletedLiveData.setValue(true);
            }
        });
    }

    public void finalizeUserRegistration(String displayName, String phone, String age, String gender, String city, String province, String completeAddress){
        loadingLiveData.setValue(true);

        authRepository.updateProfile(displayName).addOnCompleteListener(updateTask -> {
            if (!updateTask.isSuccessful()){
                handleError("Profile update failed", updateTask.getException());
                return;
            }
            RegisterRequest requestData = new RegisterRequest(displayName, phone, age, gender, city, province, completeAddress);
            apiService.registerUser(requestData).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful()){
                        userLiveData.setValue(authRepository.getCurrentUser());
                        Log.d(TAG, "User added to db");
                        loadingLiveData.setValue(false);
                    }else{
                        errorLiveData.setValue("Backend Error " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("Network Failure " + t.getMessage());
                    Log.e(TAG, t.getMessage());
                }
            });


        });

    }

    public void finalizeCompanyRegistration(CompanyRegistrationData regData) {
        loadingLiveData.setValue(true);

        authRepository.updateProfile(regData.getCompanyName()).addOnCompleteListener(updateTask -> {
            if (!updateTask.isSuccessful()) {
                handleError("Profile update failed", updateTask.getException());
                return;
            }

            RegisterRequest requestData = new RegisterRequest();
            requestData.setDisplayName(regData.getCompanyName());
            requestData.setPhone(regData.getPrimaryMobile());
            requestData.setCity(regData.getCity());
            requestData.setProvince(regData.getProvince());
            requestData.setCompleteAddress(regData.getBusinessAddress());
            requestData.setUserType("COMPANY");

            RegisterRequest.CompanyDetails details = new RegisterRequest.CompanyDetails(
                    regData.getCompanyName(),
                    "PENDING_UPLOAD",
                    regData.getBusinessType(),
                    regData.getYearsOfOperation()
            );
            requestData.setCompanyDetails(details);

            apiService.registerUser(requestData).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    loadingLiveData.setValue(false);
                    if (response.isSuccessful()) {
                        userLiveData.setValue(authRepository.getCurrentUser());
                    } else {
                        errorLiveData.setValue("Backend Error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("Network Failure: " + t.getMessage());
                }
            });
        });
    }

    private void handleError(String message, Exception e) {
        loadingLiveData.setValue(false);
        String error = (e != null) ? e.getMessage() : "Unknown error";
        errorLiveData.setValue(message + ": " + error);
        Log.e(TAG, message + ": " + error);
    }

    public void signInWithGoogle(String idToken) {
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

            // The AuthInterceptor will automatically attach the token to this request!
            // Make sure you remove the @Header parameter from this Retrofit method too.
            apiService.googleSignIn().enqueue(new Callback<AuthResponse>(){
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    loadingLiveData.setValue(false);
                    if(response.isSuccessful()){
                        userLiveData.setValue(user);
                        Log.d(TAG, "Google User fully authenticated with custom db");
                    } else {
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
    }




    public void logout(){
        authRepository.logout();
    }


}
