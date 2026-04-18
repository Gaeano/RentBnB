package com.usc.rentbnb.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.IslandResponse;
import com.usc.rentbnb.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Island>> islands = new MutableLiveData<>();
    public LiveData<List<Island>> getIslands() {
        return islands;
    }

    public void fetchIslands() {
        ApiClient.getApiService().getIslands().enqueue(new Callback<IslandResponse>() {
            @Override
            public void onResponse(Call<IslandResponse> call, Response<IslandResponse> response) {
                if (response.isSuccessful() && response.body() != null ) {
                    islands.setValue(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<IslandResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
