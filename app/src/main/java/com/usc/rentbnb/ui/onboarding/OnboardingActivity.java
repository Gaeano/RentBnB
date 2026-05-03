package com.usc.rentbnb.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.signup.SignupAs;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ImageButton btnNext;
    private LinearLayout indicatorLayout;
    private OnboardingAdapter adapter;
    private List<OnboardingItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        indicatorLayout = findViewById(R.id.indicatorLayout);

        setupItems();
        adapter = new OnboardingAdapter(this, items);
        viewPager.setAdapter(adapter);

        setupIndicators();
        updateIndicators(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                if (position == items.size() - 1) {
                    btnNext.setVisibility(View.GONE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < items.size() - 1) {
                viewPager.setCurrentItem(current + 1);
            }
        });
    }

    private void setupItems() {
        items = new ArrayList<>();

        items.add(new OnboardingItem(
                OnboardingItem.TYPE_LANDING,
                "Explore the water like never before.",
                "Seamless booking, Flexible options, and unforgettable experiences on every trip.",
                0,
                R.drawable.bg_landing
        ));

        items.add(new OnboardingItem(
                OnboardingItem.TYPE_TUTORIAL,
                "Discover and Choose",
                "Find boats using filters, maps, photos, and real reviews—all in one place.",
                R.drawable.ic_discover,
                0
        ));

        items.add(new OnboardingItem(
                OnboardingItem.TYPE_TUTORIAL,
                "Book Your Trip",
                "Check availability, pick your schedule, and book instantly or send a request in just a few taps.",
                R.drawable.ic_book,
                0
        ));

        items.add(new OnboardingItem(
                OnboardingItem.TYPE_TUTORIAL,
                "Pay Securely",
                "Use GCash, Maya, or cards with flexible payments, clear pricing, and instant confirmation.",
                R.drawable.ic_pay,
                0
        ));

        items.add(new OnboardingItem(
                OnboardingItem.TYPE_TUTORIAL,
                "Safe & Reliable",
                "Enjoy verified boats, trusted reviews, GPS tracking, and real-time weather protection.",
                R.drawable.ic_safe,
                0
        ));

        items.add(new OnboardingItem(
                OnboardingItem.TYPE_TUTORIAL,
                "Smart Travel",
                "Get personalized boat picks, smart itineraries, and weather-based suggestions instantly.",
                R.drawable.ic_smart,
                0
        ));

        items.add(new OnboardingItem(
                OnboardingItem.TYPE_FINAL,
                "Ready to set sail?",
                "Start exploring boats and plan your next adventure today.",
                0,
                R.drawable.bg_final
        ));
    }

    private void setupIndicators() {
        ImageView[] indicators = new ImageView[items.size()];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_inactive));
            indicators[i].setLayoutParams(params);
            indicatorLayout.addView(indicators[i]);
        }
    }

    private void updateIndicators(int position) {
        int childCount = indicatorLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView indicator = (ImageView) indicatorLayout.getChildAt(i);
            if (i == position) {
                indicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_active));
            } else {
                indicator.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_inactive));
            }
        }

        if (position == 0 || position == items.size() - 1) {
            indicatorLayout.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
        } else {
            indicatorLayout.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    public void onGetStartedClicked(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("RentBnBPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("IS_FIRST_TIME", false);
        editor.apply();

        Intent intent = new Intent(OnboardingActivity.this, SignupAs.class);
        startActivity(intent);

        finish();
    }
}