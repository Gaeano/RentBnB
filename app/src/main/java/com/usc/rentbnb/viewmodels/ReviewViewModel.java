package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.util.MapUtils;
import com.usc.rentbnb.R;
import com.usc.rentbnb.callbacks.BasicCallback;
import com.usc.rentbnb.callbacks.ReviewsCallback;
import com.usc.rentbnb.models.Review;
import com.usc.rentbnb.models.ReviewRequest;
import com.usc.rentbnb.repositories.ReviewsRepository;

import java.util.List;

public class ReviewViewModel extends ViewModel {

    private ReviewsRepository reviewsRepository = new ReviewsRepository();

    private MutableLiveData<List<Review>> reviewLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private MutableLiveData<String> successMessage = new MutableLiveData<>();

    public LiveData<List<Review>> getReviewData(){
        return reviewLiveData;
    }

    public LiveData<Boolean> getIsLoading(){
        return isLoading;
    }
    public LiveData<String> getErrorData(){
        return errorLiveData;
    }

    public LiveData<String> getSuccessMessage(){
        return successMessage;
    }
    public void createReview(ReviewRequest request){
        isLoading.setValue(true);
        reviewsRepository.addReview(request, new BasicCallback() {
            @Override
            public void onSuccess(String message) {
                isLoading.setValue(false);
                successMessage.setValue(message);
            }

            @Override
            public void onError(String errorMssg) {
                isLoading.setValue(false);
                errorLiveData.setValue(errorMssg);
            }
        });
    }

    public void getReviews(String listingId){
        isLoading.setValue(true);
        reviewsRepository.fetchReviews(listingId, new ReviewsCallback() {
            @Override
            public void onSuccess(List<Review> reviews) {
                isLoading.setValue(false);
                reviewLiveData.setValue(reviews);
            }

            @Override
            public void onError(String errorMssg) {
                isLoading.setValue(false);
                errorLiveData.setValue(errorMssg);
            }
        });
    }


}
