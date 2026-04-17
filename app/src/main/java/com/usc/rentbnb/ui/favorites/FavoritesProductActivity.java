package com.usc.rentbnb.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.home.HomeActivity;

public class FavoritesProductActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites_product);

        // 1. Handle Back Button (Returns to FavoritesActivity)
        ImageView backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        setupHeartToggle(findViewById(R.id.fav_item_card_1));
        setupHeartToggle(findViewById(R.id.fav_item_card_2));
        setupHeartToggle(findViewById(R.id.fav_item_card_3));
    }

    private void setupHeartToggle(View cardView) {
        if (cardView == null) return;

        // Find the specific heart icon inside THIS card (note the ID is item_favorite_heart)
        ImageView heartIcon = cardView.findViewById(R.id.item_favorite_heart);
        if (heartIcon != null) {

            // Assume true since they are in the favorites page
            heartIcon.setTag(true);

            heartIcon.setOnClickListener(v -> {
                boolean isFavorite = (boolean) v.getTag();

                isFavorite = !isFavorite;
                v.setTag(isFavorite);

                if (isFavorite) {
                    heartIcon.setImageResource(R.drawable.ic_favorites_filled);
                } else {
                    heartIcon.setImageResource(R.drawable.ic_favorites);
                }
            });
        }
    }
}