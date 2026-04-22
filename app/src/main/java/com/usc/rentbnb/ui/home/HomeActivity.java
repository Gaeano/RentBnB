package com.usc.rentbnb.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.usc.rentbnb.R;

public class HomeActivity extends AppCompatActivity {

    private View layoutIslands;
    private View layoutProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Initialize the layout containers
        layoutIslands = findViewById(R.id.layout_islands);
        layoutProducts = findViewById(R.id.layout_products);

        // 2. Find the switch button INSIDE layoutIslands
        if (layoutIslands != null) {
            ImageView btnToProducts = layoutIslands.findViewById(R.id.btn_switch_to_products);
            if (btnToProducts != null) {
                btnToProducts.setOnClickListener(v -> {
                    layoutIslands.setVisibility(View.GONE);
                    layoutProducts.setVisibility(View.VISIBLE);
                });
            }
        }

        // 3. Find the switch button INSIDE layoutProducts
        if (layoutProducts != null) {
            ImageView btnToIslands = layoutProducts.findViewById(R.id.btn_switch_to_islands);
            if (btnToIslands != null) {
                btnToIslands.setOnClickListener(v -> {
                    layoutProducts.setVisibility(View.GONE);
                    layoutIslands.setVisibility(View.VISIBLE);
                });
            }
        }
    }
}