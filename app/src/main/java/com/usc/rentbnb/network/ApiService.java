package com.usc.rentbnb.network;

import com.google.gson.JsonObject;
import com.usc.rentbnb.models.AuthResponse;
import com.usc.rentbnb.models.BookingResponse;
import com.usc.rentbnb.models.BookingRequest;
import com.usc.rentbnb.models.ConfirmReturnResponse;
import com.usc.rentbnb.models.CreateListingRequest;
import com.usc.rentbnb.models.CreateListingResponse;
import com.usc.rentbnb.models.EarningsResponse;
import com.usc.rentbnb.models.InquilinoOpeningRequest;
import com.usc.rentbnb.models.InquilinoReplyRequest;
import com.usc.rentbnb.models.InquilinoResponse;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.models.IslandResponse;
import com.usc.rentbnb.models.FAQ;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.models.NotificationResponse;
import com.usc.rentbnb.models.RegisterRequest;
import com.usc.rentbnb.models.ReviewRequest;
import com.usc.rentbnb.models.ReviewsResponse;
import com.usc.rentbnb.models.WeatherResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {
    // auth
    @GET("islands")
    Call<IslandResponse> getIslands();

    @POST("auth/register")
    Call<AuthResponse> registerUser(
            @Body RegisterRequest registerRequest
    );

    @POST("auth/google")
    Call<AuthResponse> googleSignIn();

    @POST("auth/fcm-token")
    Call<ResponseBody> saveFcmToken(@Body Map<String, String> body);

    @GET("auth/users/me")
    Call<AuthResponse> getUserData();

    @PUT("auth/update")
    Call<AuthResponse> updateProfile(@Body RegisterRequest updatedData);

    // listings
    @POST("listings")
    Call<CreateListingResponse> createListing(@Body CreateListingRequest createListingRequest);

    @GET("listings")
    Call<ListingResponse> getListings(@Query("island") String island);

    @GET("listings/owner/{ownerId}")
    Call<ListingResponse> getOwnerListings(@Path("ownerId") String ownerId);

    @PATCH("listings/{listingId}/status")
    Call<ResponseBody> updateListingStatus(
            @Path("listingId") String listingId,
            @Body Map<String, String> status
    );

    // weatjer
    @GET("weather")
    Call<WeatherResponse> getCurrentWeather(
        @Query("lat") double lat,
        @Query("lon") double lon
    );

    // cloudinary
    @Multipart
    @POST
    Call<JsonObject> uploadImageToCloudinary(
            @Url String url,
            @Part("upload_preset") RequestBody uploadPreset,
            @Part MultipartBody.Part file
    );

    //favs
    @GET("favorites/users/{userId}/islands")
    Call<IslandResponse> getFavoriteIslands(
            @Path("userId") String userId
    );

    @GET("favorites/users/{userId}/listings")
    Call<ListingResponse>getFavoriteListings(
            @Path("userId") String userId
    );

    // add favl isting
    @POST("favorites/users/{userId}/listings")
    Call<Void> addFavoriteListing(
            @Path("userId") String userId,
            @Body Listing newFavorite
    );

    //remove fav listing
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
    @GET("bookings/users/{userId}/history")
    Call<BookingResponse> getMyBookings(@Path("userId") String userId);

    @GET("bookings/users/{userId}/lent-history")
    Call<BookingResponse> getMyLentItems(@Path("userId") String userId);

    // reviews
    @POST("reviews/add")
    Call<Void> addReview(@Body ReviewRequest request);

    @GET("reviews/listing/{listingId}")
    Call<ReviewsResponse> getListingReview(@Path("listingId") String listingId);

    // notifs
    @GET("notifications")
    Call<NotificationResponse> getNotifications();

    @PATCH("notifications/{id}/read")
    Call<ResponseBody> markAsRead(@Path("id") String notificationId);

    @PATCH("notifications/read-all")
    Call<ResponseBody> markAllAsRead();

    @DELETE("notifications/{id}")
    Call<ResponseBody> deleteNotification(@Path("id") String notificationId);

    // Inquilino (AI Chatbot)
    @POST("chat/inquilino/opening")
    Call<InquilinoResponse> generateInquilinoOpening(
            @Body InquilinoOpeningRequest request
    );

    @POST("chat/inquilino/reply")
    Call<InquilinoResponse> generateInquilinoReply(
            @Body InquilinoReplyRequest request
    );

    // Booking Endpoints
    @POST("bookings")
    Call<BookingResponse> createBooking(@Body BookingRequest bookingRequest);

    @GET("bookings/owner/{ownerId}")
    Call<BookingResponse> getOwnerBookings(@Path("ownerId") String ownerId);

    @GET("bookings/owner/{ownerId}/earnings")
    Call<EarningsResponse> getOwnerEarnings(@Path("ownerId") String ownerId);

    @GET("bookings/user/{userId}")
    Call<BookingResponse> getBookingsByUser(@Path("userId") String userId);

    @PUT("bookings/{bookingId}/status")
    Call<BookingResponse> updateBookingStatus(
            @Path("bookingId") String bookingId,
            @Body Map<String, String> status
    );

    @POST("bookings/{bookingId}/confirm-return")
    Call<ConfirmReturnResponse> confirmReturn(@Path("bookingId") String bookingId);

    @POST("bookings/{bookingId}/apply-penalty")
    Call<BookingResponse> applyPenalty(
            @Path("bookingId") String bookingId,
            @Body Map<String, Object> body
    );

    // FAQs
    @GET("faqs/default")
    Call<List<FAQ>> getDefaultFaqs();

    @POST("faqs/default")
    Call<FAQ> addDefaultFaq(@Body FAQ faq);

    @PUT("faqs/default/{id}")
    Call<FAQ> updateDefaultFaq(@Path("id") String id, @Body FAQ faq);

    @DELETE("faqs/default/{id}")
    Call<ResponseBody> deleteDefaultFaq(@Path("id") String id);
}
