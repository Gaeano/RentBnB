package com.usc.rentbnb.network;

import com.google.gson.JsonObject;
import com.usc.rentbnb.models.AuthResponse;
import com.usc.rentbnb.models.BookingResponse;
import com.usc.rentbnb.models.CreateListingRequest;
import com.usc.rentbnb.models.CreateListingResponse;
import com.usc.rentbnb.models.InquilinoOpeningRequest;
import com.usc.rentbnb.models.InquilinoReplyRequest;
import com.usc.rentbnb.models.InquilinoResponse;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.IslandResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.models.NotificationResponse;
import com.usc.rentbnb.models.RegisterRequest;
import com.usc.rentbnb.models.WeatherResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

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

    @Multipart
    @POST
    Call<JsonObject> uploadImageToCloudinary(
            @Url String url,
            @Part("upload_preset") RequestBody uploadPreset,
            @Part MultipartBody.Part file
    );

    //get favorite islands and listings
    @GET("favorites/users/{userId}/islands")
    Call<IslandResponse> getFavoriteIslands(
            @Path("userId") String userId
    );

    @GET("favorites/users/{userId}/listings")
    Call<ListingResponse>getFavoriteListings(
            @Path("userId") String userId
    );

    //add favorite listings
    @POST("favorites/users/{userId}/listings")
    Call<Void> addFavoriteListing(
            @Path("userId") String userId,
            @Body Listing newFavorite
    );

    //remove favorite listings
    @DELETE("favorites/users/{userId}/listings/{listingId}")
    Call<Void> removeFavoriteListing(
            @Path("userId") String userId,
            @Path("listingId") String listingId
    );

    //add favorite islands
    @POST("favorites/users/{userId}/islands")
    Call<Void> addFavoriteIsland(
            @Path("userId") String userId,
            @Body Island newFavorite
    );

    //remove favorite islands
    @DELETE("favorites/users/{userId}/islands/{islandId}")
    Call<Void> removeFavoriteIsland(
            @Path("userId") String userId,
            @Path("islandId") String islandId
    );

    // history
    @GET("users/{userId}/history")
    Call<BookingResponse> getMyBookings(@Path("userId") String userId);

    @GET("users/{userId}/lent-history")
    Call<BookingResponse> getMyLentItems(@Path("userId") String userId);

    // notifs
    @GET("notifications")
    Call<NotificationResponse> getNotifications();

    @PATCH("notifications/{id}/read")
    Call<ResponseBody> markAsRead(@Path("id") String notificationId);

    @DELETE("notifications/{id}")
    Call<ResponseBody> deleteNotification(@Path("id") String notificationId);

    // Inquilino (AI Chatbot)
    @POST("chat/inquilino/opening")
    Call<InquilinoResponse> generateInquilinoOpening(
            @Header("Authorization") String token,
            @Body InquilinoOpeningRequest request
    );

    @POST("chat/inquilino/reply")
    Call<InquilinoResponse> generateInquilinoReply(
            @Header("Authorization") String token,
            @Body InquilinoReplyRequest request
    );
}
