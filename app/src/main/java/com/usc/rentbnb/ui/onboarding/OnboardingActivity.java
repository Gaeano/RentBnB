package com.usc.rentbnb.ui.onboarding;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.usc.rentbnb.R;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private OnboardingPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        viewPager2 = findViewById(R.id.viewPager2);

        List<OnboardingPageModel> pages = new ArrayList<>();
        // Landing Page
        pages.add(new OnboardingPageModel(
                OnboardingPageModel.PageType.LANDING,
                "RentBnb\nExplore the water like never before.",
                "Seamless booking, flexible options, and unforgettable experiences on every trip.",
                0,
                android.R.color.holo_blue_light // Placeholder for image
        ));

        // Tutorial Pages
        pages.add(new OnboardingPageModel(
                OnboardingPageModel.PageType.TUTORIAL,
                "Discover and Choose",
                "Find boats using filters, maps, photos, and real reviews - all in one place.",
                android.R.drawable.ic_menu_gallery, // Placeholder icon
                0
        ));
        pages.add(new OnboardingPageModel(
                OnboardingPageModel.PageType.TUTORIAL,
                "Book Your Trip",
                "Check availability, pick your schedule, and book instantly or send a request in just a few taps.",
                android.R.drawable.ic_menu_agenda, // Placeholder icon
                0
        ));
        pages.add(new OnboardingPageModel(
                OnboardingPageModel.PageType.TUTORIAL,
                "Pay Securely",
                "Use GCash, Maya, or cards with flexible payments, clear pricing, and instant confirmation.",
                android.R.drawable.ic_menu_manage, // Placeholder icon
                0
        ));
        pages.add(new OnboardingPageModel(
                OnboardingPageModel.PageType.TUTORIAL,
                "Safe & Reliable",
                "Enjoy verified boats, trusted reviews, GPS tracking, and real-time weather protection.",
                android.R.drawable.ic_menu_info_details, // Placeholder icon
                0
        ));
        pages.add(new OnboardingPageModel(
                OnboardingPageModel.PageType.TUTORIAL,
                "Smart Travel",
                "Get personalized boat picks, smart itineraries, and weather-based suggestions instantly.",
                android.R.drawable.ic_menu_mapmode, // Placeholder icon
                0
        ));

        // Final Page
        pages.add(new OnboardingPageModel(
                OnboardingPageModel.PageType.FINAL,
                "Ready to set sail?",
                "Start exploring boats and plan your next adventure today.",
                0,
                android.R.color.holo_blue_dark // Placeholder for image
        ));

        adapter = new OnboardingPagerAdapter(pages, new OnboardingPagerAdapter.OnboardingActionCallback() {
            @Override
            public void onNextClicked(int currentPosition) {
                if (currentPosition < adapter.getItemCount() - 1) {
                    viewPager2.setCurrentItem(currentPosition + 1, true);
                }
            }

            @Override
            public void onGetStartedClicked() {
                // Navigate to next activity or finish Onboarding
                finish();
            }
        });

        viewPager2.setAdapter(adapter);
    }
}