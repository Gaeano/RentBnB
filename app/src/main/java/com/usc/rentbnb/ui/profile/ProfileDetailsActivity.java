package com.usc.rentbnb.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.usc.rentbnb.R;

public class ProfileDetailsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_details);

        // 1. Grab the top bar
        View topBar = findViewById(R.id.top_bar);

        // 2. Apply your insets logic here to protect the "My Account" text and back button
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + 16;
            v.setLayoutParams(params);
            return insets;
        });

        // Handle the Back Button
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Check the Intent to decide which Fragment to load
        boolean isCompany = getIntent().getBooleanExtra("IS_COMPANY", false);
        Fragment fragmentToLoad = isCompany ? new CompProfileDetailsFragment() : new IndivProfileDetailsFragment();

        // Load the appropriate fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.profile_fragment_container, fragmentToLoad)
                .commit();
    }


}