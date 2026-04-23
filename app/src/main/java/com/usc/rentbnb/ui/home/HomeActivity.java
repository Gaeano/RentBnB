package com.usc.rentbnb.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Island;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.listing.AddListingActivity;
import com.usc.rentbnb.islands.IslandDetailsActivity;
import com.usc.rentbnb.viewmodels.HomeViewModel;

public class HomeActivity extends AppCompatActivity {

    private TextView feedTitleView;
    private TextView[] filterChips;
    private HomeViewModel homeViewModel;

    private boolean showingIslands = true;

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
}