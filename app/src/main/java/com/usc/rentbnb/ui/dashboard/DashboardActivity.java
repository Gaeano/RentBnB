package com.usc.rentbnb.ui.dashboard;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.usc.rentbnb.R;

public class DashboardActivity extends AppCompatActivity {

    private ImageView navDashboard, navCalendar, navListings, navInbox, navProfile;
    private View dotDashboard, dotCalendar, dotInbox, dotProfile, dotListings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for proper status bar handling
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_dashboard);

        // Setup status bar color and appearance
        setupStatusBar();

        initNavViews();
        setupNavListeners();

        if (savedInstanceState == null) {
            loadFragment(new OwnerDashboardFragment());
            updateNavUI(0);
        }
    }

    private void setupStatusBar() {
        // Make status bar transparent
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Set status bar icons to dark (for light background)
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);
    }

    private void initNavViews() {
        navDashboard = findViewById(R.id.navDashboard);
        navCalendar = findViewById(R.id.navCalendar);
        navListings = findViewById(R.id.navListings);
        navInbox = findViewById(R.id.navInbox);
        navProfile = findViewById(R.id.navProfile);

        dotDashboard = findViewById(R.id.dotDashboard);
        dotCalendar = findViewById(R.id.dotCalendar);
        dotInbox = findViewById(R.id.dotInbox);
        dotProfile = findViewById(R.id.dotProfile);
        dotListings = findViewById(R.id.dotListings);
    }

    private void setupNavListeners() {
        findViewById(R.id.btnNavDashboard).setOnClickListener(v -> {
            loadFragment(new OwnerDashboardFragment());
            updateNavUI(0);
        });

        findViewById(R.id.btnNavCalendar).setOnClickListener(v -> {
            loadFragment(new OwnerCalendarFragment());
            updateNavUI(1);
        });

        findViewById(R.id.btnNavListings).setOnClickListener(v -> {
            loadFragment(new OwnerListingsFragment());
            updateNavUI(2);
        });

        findViewById(R.id.btnNavInbox).setOnClickListener(v -> {
            loadFragment(new OwnerInboxFragment());
            updateNavUI(3);
        });

        findViewById(R.id.btnNavProfile).setOnClickListener(v -> {
            loadFragment(new OwnerProfileFragment());
            updateNavUI(4);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    private void updateNavUI(int activeIndex) {
        int activeColor = Color.parseColor("#FFFFFF");

        ImageView[] icons = {navDashboard, navCalendar, navListings, navInbox, navProfile};
        View[] dots = {dotDashboard, dotCalendar, dotListings, dotInbox, dotProfile};

        for (ImageView icon : icons) {
            icon.setImageTintList(ColorStateList.valueOf(activeColor));
            icon.setAlpha(0.7f);
        }
        for (View dot : dots) {
            if (dot != null) dot.setVisibility(View.GONE);
        }

        switch (activeIndex) {
            case 0:
                navDashboard.setAlpha(1.0f);
                if(dotDashboard != null) dotDashboard.setVisibility(View.VISIBLE);
                break;
            case 1:
                navCalendar.setAlpha(1.0f);
                if(dotCalendar != null) dotCalendar.setVisibility(View.VISIBLE);
                break;
            case 2:
                navListings.setAlpha(1.0f);
                if(dotListings != null) dotListings.setVisibility(View.VISIBLE);
                break;
            case 3:
                navInbox.setAlpha(1.0f);
                if(dotInbox != null) dotInbox.setVisibility(View.VISIBLE);
                break;
            case 4:
                navProfile.setAlpha(1.0f);
                if(dotProfile != null) dotProfile.setVisibility(View.VISIBLE);
                break;
        }
    }
}