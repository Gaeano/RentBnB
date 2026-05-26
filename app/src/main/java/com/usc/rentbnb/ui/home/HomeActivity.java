package com.usc.rentbnb.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.usc.rentbnb.repositories.ChatRepository;
import com.usc.rentbnb.models.ChatRoom;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.FilterCriteria;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.models.ListingResponse;
import com.usc.rentbnb.models.Notification;
import com.usc.rentbnb.ui.notifications.NotificationActivity;
import com.usc.rentbnb.models.NotificationResponse;
import com.usc.rentbnb.models.WeatherResponse;
import com.usc.rentbnb.network.ApiClient;
import com.usc.rentbnb.ui.filter.FilterActivity;
import com.usc.rentbnb.ui.listing.AddListingActivity;
import com.usc.rentbnb.utils.NavigationHelper;
import com.usc.rentbnb.viewmodels.HomeViewModel;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView[] filterChips;
    private HomeViewModel homeViewModel;
    private EditText searchBar;

    private boolean showingRentals = true;
    private TextView tabIslands, tabRentals;

    private WeatherResponse.WeatherData currentWeather;
    private int currentWeatherIconRes = R.drawable.ic_sun;
    private String currentWeatherMessage = "Checking the skies...";
    public String userCity = "Cebu City";
    public double userLat = 10.3157;
    public double userLon = 123.8854;

    private NavigationHelper navigationHelper;

    private FusedLocationProviderClient fusedLocationClient;
    private FilterCriteria lastCriteria = null;
    private ListenerRegistration chatListener;

    // saerch debouncing lkogic
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

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

        tabIslands = findViewById(R.id.tab_islands);
        tabRentals = findViewById(R.id.tab_rentals);
        searchBar = findViewById(R.id.search_bar);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);


        setupFilterChips();
        setupTitleToggle();
        setupBottomNavigation(homeHeader);
        setupFilterButton();
        setupSearchLogic();

        if (getIntent().getBooleanExtra("navigate_to_chat", false)) {
            navigationHelper.navigateToChat();
        } else {
            navigationHelper.setInitialState();
        }


        homeViewModel.fetchIslands();
        homeViewModel.fetchListings();

        switchFeed(true);

        fetchUserLocation();

        findViewById(R.id.weather_button).setOnClickListener(v -> showWeatherDialog());

        View notificationBtn = findViewById(R.id.notification_button);
        if (notificationBtn != null) {
            notificationBtn.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        checkUnreadNotifications();
        listenToUnreadChat();

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull androidx.fragment.app.FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f instanceof RentalsFragment) {
                    updateTabUI(true);
                } else if (f instanceof IslandsFragment) {
                    updateTabUI(false);
                }
            }
        }, false);
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
                String query = s.toString().trim();
                searchRunnable = () -> {
                    if (showingRentals) {
                        homeViewModel.filterRentals(query);
                    } else {
                        homeViewModel.filterIslands(query);
                    }
                };

                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void setupTitleToggle() {
        tabRentals.setOnClickListener(v -> {
            if (!showingRentals) {
                switchFeed(true);
            }
        });

        tabIslands.setOnClickListener(v -> {
            if (showingRentals) {
                switchFeed(false);
            }
        });
    }

    private void switchFeed(boolean toRentals) {
        updateTabUI(toRentals);

        String currentQuery = searchBar.getText().toString();
        if (toRentals) {
            homeViewModel.filterRentals(currentQuery);
        } else {
            homeViewModel.filterIslands(currentQuery);
        }

        Fragment fragment = toRentals ? new RentalsFragment() : new IslandsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.homeFeedContainer, fragment)
                .commit();
    }

    private void updateTabUI(boolean isRentals) {
        showingRentals = isRentals;

        if (isRentals) {
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
    }

    private void setupFilterChips() {
        TextView chipWheels = findViewById(R.id.chip_wheels);
        TextView chipWater = findViewById(R.id.chip_water);
        TextView chipOutdoors = findViewById(R.id.chip_outdoors);
        TextView chipElectronics = findViewById(R.id.chip_electronics);
        TextView chipBeachLeisure = findViewById(R.id.chip_beach_leisure);

        filterChips = new TextView[]{chipWheels, chipWater, chipOutdoors, chipElectronics, chipBeachLeisure};

        for (TextView chip : filterChips) {
            if (chip != null) {
                chip.setSelected(false);
                chip.setOnClickListener(v -> handleChipToggle((TextView) v));
            }
        }
    }

    private void handleChipToggle(TextView selectedChip) {
        boolean isNowSelected = !selectedChip.isSelected();
        selectedChip.setSelected(isNowSelected);

        if (isNowSelected) {
            selectedChip.setBackgroundResource(R.drawable.chip_background_selected);
        } else {
            selectedChip.setBackgroundResource(R.drawable.chip_background);
        }
    }

    private void setupBottomNavigation(View homeHeader) {
        navigationHelper = new NavigationHelper(this, R.id.homeFeedContainer, homeHeader);
        View addListingFab = findViewById(R.id.navFab);
        if (addListingFab != null) {
            addListingFab.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, AddListingActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupFilterButton() {
        View filterButton = findViewById(R.id.filter_button);
        if (filterButton == null) return;

        filterButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FilterActivity.class);
            if (lastCriteria != null) {
                intent.putExtra("current_criteria", lastCriteria);
            }
            filterLauncher.launch(intent);
        });
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
                        case "Snow": // useless pero pang chuy rani kay chuy manko
                            currentWeatherMessage = "Snow expected soon";
                            currentWeatherIconRes = R.drawable.ic_snow;
                            break;
                        case "Cloudy":
                            currentWeatherMessage = "Nice and cool today";
                            currentWeatherIconRes = R.drawable.ic_cloud;
                            break;
                        case "Foggy": // busay raman tawn ni ey
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

    private void fetchUserLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // check if user granted permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // no permission = ask permission; show pop-up
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLon = location.getLongitude();
                fetchWeather(userLat, userLon);

                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(userLat, userLon, 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        userCity = addresses.get(0).getLocality();
                        Log.d("LOCATION", "User is in: " + userCity);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.homeFeedContainer);
                if (currentFragment instanceof RentalsFragment) {
                    ((RentalsFragment) currentFragment).onLocationUpdated(userCity, userLat, userLon);
                } else if (currentFragment instanceof IslandsFragment) {
                    ((IslandsFragment) currentFragment).updateLocationTitle(userCity);
                }

                fetchNearbyIslands(userLat, userLon);
            } else {
                fetchWeather(userLat, userLon);
            }
        }).addOnFailureListener(e -> {
            fetchWeather(userLat, userLon);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation();
            } else {
                Log.d("LOCATION", "Permission denied, defaulting to Cebu");
                fetchWeather(10.3157, 123.8854);
            }
        }
    }

    private void fetchNearbyIslands(double lat, double lng) {
        // TODO: pass 'lat' and 'lng' to backend to calculate distance puhon
        // ApiClient.getApiService().getNearbyIslands(lat, lng)...

        Log.d("DATA", "Preparing to fetch islands near " + lat + ", " + lng);

        // fornow because db only has 6 islands, just fetch ALL islands
        homeViewModel.fetchIslands();
    }

    private void checkUnreadNotifications() {
        ApiClient.getApiService().getNotifications().enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                boolean hasUnread = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> notifications = response.body().getData();
                    if (notifications != null) {
                        for (Notification n : notifications) {
                            if (!n.isRead()) {
                                hasUnread = true;
                                break;
                            }
                        }
                    }
                }

                if (hasUnread) {
                    updateNotificationBadge(true);
                } else {
                    checkNewListingsForBadge();
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                checkNewListingsForBadge();
            }
        });
    }

    private void checkNewListingsForBadge() {
        ApiClient.getApiService().getListings(null).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                boolean hasNew = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Listing> listings = response.body().getData();
                    if (listings != null) {
                        for (Listing l : listings) {
                            if (l.isNew()) {
                                hasNew = true;
                                break;
                            }
                        }
                    }
                }
                updateNotificationBadge(hasNew);
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable t) {
                updateNotificationBadge(false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) {
            chatListener.remove();
        }
    }

    private void listenToUnreadChat() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) return;

        ChatRepository chatRepo = new ChatRepository();
        chatListener = chatRepo.listenToChatRoomsForUser(currentUserId, new ChatRepository.ChatRoomsListCallback() {
            @Override
            public void onUpdate(List<ChatRoom> chatRooms) {
                boolean hasUnreadChat = false;
                for (ChatRoom room : chatRooms) {
                    if (room.getUnreadCountForUser(currentUserId) > 0) {
                        hasUnreadChat = true;
                        break;
                    }
                }
                if (hasUnreadChat) {
                    updateNotificationBadge(true);
                }
            }

            @Override
            public void onFailure(String errorMessage) {}
        });
    }

    private void updateNotificationBadge(boolean visible) {
        View badge = findViewById(R.id.notification_badge);
        if (badge != null) {
            badge.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private final ActivityResultLauncher<Intent> filterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    FilterCriteria criteria = (FilterCriteria) result.getData().getSerializableExtra("updated_criteria");

                    lastCriteria = criteria;

                    View filterButton = findViewById(R.id.filter_button);
                    if (filterButton != null) {
                        filterButton.setActivated(!criteria.isEmpty());
                    }

                    homeViewModel.applyFilters(criteria);
                }
            }
    );
}