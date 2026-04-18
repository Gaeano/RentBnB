package com.usc.rentbnb.models;

import android.util.Log;

import androidx.core.app.NavUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.usc.rentbnb.repositories.AuthRepository;

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

    public void signUp(String email, String password, String fullName){
        loadingLiveData.setValue(true);

        authRepository.signUp(email, password).addOnCompleteListener(task -> {
           if (task.isSuccessful()){
               authRepository.updateProfile(fullName).addOnCompleteListener(updateTask -> {
                   if (updateTask.isSuccessful()){

                       FirebaseUser user = authRepository.getCurrentUser();
                       if (user != null){
                           user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                              String token = tokenTask.getResult().getToken();
                              Log.e("POSTMAN_TOKEN", token);
                           });
                       }

                       userLiveData.setValue(authRepository.getCurrentUser());
                       loadingLiveData.setValue(false);
                       Log.d(TAG, "Sign up successful with display name updated");
                   } else {
                       loadingLiveData.setValue(false);
                       errorLiveData.setValue(updateTask.getException().getMessage());
                       Log.e(TAG, "Sign up failed" + updateTask.getException().getMessage());
                   }
               });
           }
        });
    }

    public void signInWithGoogle(String idToken){
        loadingLiveData.setValue(true);

        authRepository.loginWithGoogle(idToken).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(authRepository.getCurrentUser());
                loadingLiveData.setValue(false);
                Log.d(TAG, "Sign in with Google successful");
            } else {
                loadingLiveData.setValue(false);
                errorLiveData.setValue(task.getException().getMessage());
                Log.e(TAG, "Sign in with Google failed" + task.getException().getMessage());
            }
        });
    }

    public void logout(){
        authRepository.logout();
    }


}
