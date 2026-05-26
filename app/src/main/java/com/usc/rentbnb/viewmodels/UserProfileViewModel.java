package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usc.rentbnb.callbacks.UserProfileCallback;
import com.usc.rentbnb.models.RegisterRequest;
import com.usc.rentbnb.models.User;
import com.usc.rentbnb.repositories.UserProfileRepository;

public class UserProfileViewModel extends ViewModel {
    private UserProfileRepository userProfileRepository = new UserProfileRepository();
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isloading = new MutableLiveData<>(false);

    private MutableLiveData<Boolean>updateProfileSuccess = new MutableLiveData<>();

    public LiveData<User> getUserProfile() {
        return userLiveData;
    }

    public LiveData<String> getErrorData (){
        return errorLiveData;
    }

    public LiveData<Boolean> getIsloading() {
        return isloading;
    }

    public LiveData<Boolean> getUpdateSuccess(){return updateProfileSuccess;}

    public void loadUserData(){
        isloading.setValue(true);

        userProfileRepository.fetchUserData(new UserProfileCallback() {
            @Override
            public void onSuccess(User user) {
                userLiveData.setValue(user);
                isloading.setValue(false);

            }

            @Override
            public void onError(String errorMessage) {
                errorLiveData.setValue(errorMessage);
                isloading.setValue(false);
            }
        });
    }

    public void updateUserProfile(RegisterRequest updatedData){
        isloading.setValue(true);

        userProfileRepository.updateUserData(updatedData, new UserProfileCallback() {
            @Override
            public void onSuccess(User user) {
                isloading.setValue(false);
                if (user != null){
                    userLiveData.setValue(user);
                    updateProfileSuccess.setValue(true);
                }
            }

            @Override
            public void onError(String errorMessage) {
                errorLiveData.setValue(errorMessage);
                isloading.setValue(false);
            }
        });
    }
}
