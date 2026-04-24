package com.usc.rentbnb.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.WeatherResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.ui.listing.AddListingActivity;
import com.usc.rentbnb.viewmodels.HomeViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView feedTitleView;
    private TextView[] filterChips;
    private HomeViewModel homeViewModel;

    private boolean showingIslands = true;

    private WeatherResponse.WeatherData currentWeather;
    private int currentWeatherIconRes = R.drawable.ic_sun;
    private String currentWeatherMessage = "Checking the skies...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        LinearLayout homeHeader = findViewById(R.id.homeHeader);
        ViewCompat.setOnApplyWindowInsetsListener(homeHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight + 8,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        feedTitleView = findViewById(R.id.feed_title);

        setupFilterChips();
        setupTitleToggle();
        setupBottomNavigation();

        homeViewModel.fetchIslands();
        homeViewModel.fetchListings();

        switchFeed(true);

        fetchWeather(10.3157, 123.8854); // for testing purposes, del after
        findViewById(R.id.weather_button).setOnClickListener(v -> showWeatherDialog());

        // TODO: Implement search bar logic
    }

    private void switchFeed(boolean toIslands) {
        showingIslands = toIslands;

        String label = toIslands ? "Islands" : "Rentals";
        feedTitleView.setText(label);

        Fragment fragment = toIslands ? new IslandsFragment() : new RentalsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.homeFeedContainer, fragment)
                .commit();
    }

    private void setupTitleToggle() {
        LinearLayout titleRow = findViewById(R.id.feed_title_row);
        titleRow.setOnClickListener(this::showFeedDropdown);
    }

    private void showFeedDropdown(View anchor) {
        View dropdownView = LayoutInflater.from(this)
                .inflate(R.layout.dropdown_feed_menu, null);

        PopupWindow popup = new PopupWindow(
                dropdownView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );
        popup.setElevation(12f);

        dropdownView.findViewById(R.id.menuIslands).setOnClickListener(v -> {
            switchFeed(true);
            popup.dismiss();
        });

        dropdownView.findViewById(R.id.menuRentals).setOnClickListener(v -> {
            switchFeed(false);
            popup.dismiss();
        });

        popup.showAsDropDown(anchor, 0, 4, Gravity.START);
    }

    private void setupFilterChips() {
        TextView chipPopular = findViewById(R.id.chip_popular);
        TextView chipTrending = findViewById(R.id.chip_trending);
        TextView chipNew = findViewById(R.id.chip_new);
        TextView chipLabel1 = findViewById(R.id.chip_label1);
        TextView chipLabel2 = findViewById(R.id.chip_label2);

        filterChips = new TextView[]{chipPopular, chipTrending, chipNew, chipLabel1, chipLabel2};

        for (TextView chip : filterChips) {
            if (chip != null) {
                chip.setOnClickListener(v -> handleChipSelection((TextView) v));
            }
        }
    }

    private void handleChipSelection(TextView selectedChip) {
        for (TextView chip : filterChips) {
            chip.setBackgroundResource(R.drawable.chip_background);
            chip.setTextColor(Color.parseColor("#5F5F5F"));
        }

        selectedChip.setBackgroundResource(R.drawable.chip_background_selected);
        selectedChip.setTextColor(Color.WHITE);
    }

    // TODO: Setup flexible bottom navbar logic (can be accessed from any page w/o having to manually add the logic for each activity)
    private void setupBottomNavigation() {
        View addListingFab = findViewById(R.id.navFab);
        if (addListingFab != null) {
            addListingFab.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, AddListingActivity.class);
                startActivity(intent);
            });
        }
    }

    private void fetchWeather(double userLat, double userLon) {
        ApiClient.getApiService().getCurrentWeather(userLat, userLon).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentWeather = response.body().getData();

                    ImageView ivWeatherIcon = findViewById(R.id.weather_icon);

                    String condition = currentWeather.getCondition();
                    if (condition == null) condition = "Clear";

                    switch (condition) {
                        case "Rain":
                            currentWeatherMessage = "It's going to rain soon";
                            currentWeatherIconRes = R.drawable.ic_rain;
                            break;
                        case "Snow":
                            currentWeatherMessage = "Snow expected soon";
                            currentWeatherIconRes = R.drawable.ic_snow;
                            break;
                        case "Cloudy":
                            currentWeatherMessage = "Nice and cool today";
                            currentWeatherIconRes = R.drawable.ic_cloud;
                            break;
                        case "Foggy":
                            currentWeatherMessage = "Low visibility, take care";
                            currentWeatherIconRes = R.drawable.ic_fog;
                            break;
                        default:
                            currentWeatherMessage = "Perfect day for rentals";
                            currentWeatherIconRes = R.drawable.ic_sun;
                            break;
                    }

                    ivWeatherIcon.setImageResource(currentWeatherIconRes);
                    ivWeatherIcon.setAlpha(1.0f);

                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void showWeatherDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_weather);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setDimAmount(0.7f);
        }

        ImageView icon = dialog.findViewById(R.id.dialog_weather_icon);
        TextView tempText = dialog.findViewById(R.id.dialog_weather_temp);
        TextView conditionText = dialog.findViewById(R.id.dialog_weather_condition);
        TextView messageText = dialog.findViewById(R.id.dialog_weather_message);

        icon.setImageResource(currentWeatherIconRes);
        messageText.setText(currentWeatherMessage);

        if (currentWeather != null) {
            tempText.setText(currentWeather.getTemp() + "°C");
            conditionText.setText(currentWeather.getCondition());
        } else {
            tempText.setText("--°C");
            conditionText.setText("Loading...");
        }

        dialog.show();
    }
}