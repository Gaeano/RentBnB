package com.usc.rentbnb.network;

import com.usc.rentbnb.models.AuthResponse;
import com.usc.rentbnb.models.CreateListingRequest;
import com.usc.rentbnb.models.CreateListingResponse;
import com.usc.rentbnb.models.FAQ;
import com.usc.rentbnb.models.IslandResponse;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.models.NotificationResponse;
import com.usc.rentbnb.models.RegisterRequest;
import com.usc.rentbnb.models.WeatherResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("islands")
    Call<IslandResponse> getIslands();

    @POST("auth/register")
    Call<AuthResponse> registerUser(
            @Header("Authorization") String token,
            @Body RegisterRequest registerRequest
    );

    @POST("auth/google")
    Call<AuthResponse> googleSignIn(
            @Header("Authorization") String token
    );

    @POST("listings")
    Call<CreateListingResponse> createListing(@Body CreateListingRequest createListingRequest);

    @GET("listings")
    Call<ListingResponse> getListings(@Query("island") String island);

    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
        @Query("lat") double lat,
        @Query("lon") double lon
    );

    // Notifications API
    @GET("notifications")
    Call<NotificationResponse> getNotifications();

    @PATCH("notifications/{id}/read")
    Call<ResponseBody> markAsRead(@Path("id") String notificationId);

    @PATCH("notifications/read-all")
    Call<ResponseBody> markAllAsRead();

    @DELETE("notifications/{id}")
    Call<ResponseBody> deleteNotification(@Path("id") String notificationId);

    // FAQ API
    @GET("users/me/faqs")
    Call<List<FAQ>> getDefaultFaqs();

    @POST("users/me/faqs")
    Call<FAQ> addDefaultFaq(@Body FAQ faq);

    @PATCH("users/me/faqs/{id}")
    Call<FAQ> updateDefaultFaq(@Path("id") String faqId, @Body FAQ faq);

    @DELETE("users/me/faqs/{id}")
    Call<ResponseBody> deleteDefaultFaq(@Path("id") String faqId);
}
