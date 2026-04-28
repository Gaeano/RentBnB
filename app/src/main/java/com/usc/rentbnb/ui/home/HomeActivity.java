package com.usc.rentbnb.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.WeatherResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.ui.listing.AddListingActivity;
import com.usc.rentbnb.utils.NavigationHelper;
import com.usc.rentbnb.viewmodels.HomeViewModel;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView tabIslands, tabRentals;
    private EditText searchBar;
    private HomeViewModel homeViewModel;

    private boolean showingRentals = true;
    private WeatherResponse.WeatherData currentWeather;
    private int currentWeatherIconRes = R.drawable.ic_sun;
    private String currentWeatherMessage = "Checking the skies...";

    private NavigationHelper navigationHelper;
    private FusedLocationProviderClient fusedLocationClient;

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        LinearLayout homeHeader = findViewById(R.id.homeHeader);
        ViewCompat.setOnApplyWindowInsetsListener(homeHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + 8, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        tabIslands = findViewById(R.id.tab_islands);
        tabRentals = findViewById(R.id.tab_rentals);
        searchBar = findViewById(R.id.search_bar);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupFilterChips();
        setupTitleToggle();
        setupBottomNavigation(homeHeader);
        setupSearchLogic();

        navigationHelper.setInitialState();
        homeViewModel.fetchIslands();
        homeViewModel.fetchListings();

        switchFeed(true);
        fetchUserLocation();

        findViewById(R.id.weather_button).setOnClickListener(v -> showWeatherDialog());
    }

    private void setupSearchLogic() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                searchRunnable = () -> homeViewModel.filterData(query, showingRentals);
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void setupTitleToggle() {
        tabRentals.setOnClickListener(v -> { if (!showingRentals) switchFeed(true); });
        tabIslands.setOnClickListener(v -> { if (showingRentals) switchFeed(false); });
    }

    private void switchFeed(boolean toRentals) {
        showingRentals = toRentals;

        if (toRentals) {
            tabRentals.setBackgroundResource(R.drawable.bg_tab_active);
            tabRentals.setTextColor(ContextCompat.getColor(this, R.color.teal_primary));
            tabIslands.setBackgroundResource(android.R.color.transparent);
            tabIslands.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
        } else {
            tabIslands.setBackgroundResource(R.drawable.bg_tab_active);
            tabIslands.setTextColor(ContextCompat.getColor(this, R.color.teal_primary));
            tabRentals.setBackgroundResource(android.R.color.transparent);
            tabRentals.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
        }

        // Apply current search text to new tab
        homeViewModel.filterData(searchBar.getText().toString(), showingRentals);

        Fragment fragment = toRentals ? new RentalsFragment() : new IslandsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.homeFeedContainer, fragment)
                .commit();
    }

    private void setupFilterChips() {
        TextView chipNearYou = findViewById(R.id.chip_near_you);
        TextView chipTrending = findViewById(R.id.chip_trending);
        TextView chipNew = findViewById(R.id.chip_new);
        TextView chipTopRated = findViewById(R.id.chip_top_rated);

        TextView[] chips = {chipNearYou, chipTrending, chipNew, chipTopRated};
        for (TextView chip : chips) {
            if (chip != null) {
                chip.setOnClickListener(v -> {
                    v.setSelected(!v.isSelected());
                    v.setBackgroundResource(v.isSelected() ? R.drawable.chip_background_selected : R.drawable.chip_background);
                });
            }
        }
    }

    private void setupBottomNavigation(View homeHeader) {
        navigationHelper = new NavigationHelper(this, R.id.homeFeedContainer, homeHeader);
        View addListingFab = findViewById(R.id.navFab);
        if (addListingFab != null) {
            addListingFab.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, AddListingActivity.class));
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
                    String condition = currentWeather.getCondition() != null ? currentWeather.getCondition() : "Clear";
                    switch (condition) {
                        case "Rain":
                            currentWeatherMessage = "It's going to rain soon";
                            currentWeatherIconRes = R.drawable.ic_rain; break;
                        case "Snow":
                            currentWeatherMessage = "Snow expected soon";
                            currentWeatherIconRes = R.drawable.ic_snow; break;
                        case "Cloudy":
                            currentWeatherMessage = "Nice and cool today";
                            currentWeatherIconRes = R.drawable.ic_cloud; break;
                        case "Foggy":
                            currentWeatherMessage = "Low visibility, take care";
                            currentWeatherIconRes = R.drawable.ic_fog; break;
                        default:
                            currentWeatherMessage = "Perfect day for rentals";
                            currentWeatherIconRes = R.drawable.ic_sun; break;
                    }
                    ivWeatherIcon.setImageResource(currentWeatherIconRes);
                    ivWeatherIcon.setAlpha(1.0f);
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) { t.printStackTrace(); }
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
        }
        dialog.show();
    }

    private void fetchUserLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) fetchWeather(location.getLatitude(), location.getLongitude());
            else fetchWeather(10.3157, 123.8854);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocation();
        }
    }
}