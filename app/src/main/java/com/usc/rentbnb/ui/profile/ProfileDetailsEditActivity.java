package com.usc.rentbnb.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.usc.rentbnb.R;

public class ProfileDetailsEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_details_edit);

        View topBar = findViewById(R.id.top_bar_edit);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + 16, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btn_back_edit);
        btnBack.setOnClickListener(v -> finish());

        // Check if Company or Indiv
        boolean isCompany = getIntent().getBooleanExtra("IS_COMPANY", true);

        Fragment fragmentToLoad = isCompany ? new CompProfileEditFragment() : new IndivProfileEditFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.profile_edit_fragment_container, fragmentToLoad)
                .commit();
    }
}