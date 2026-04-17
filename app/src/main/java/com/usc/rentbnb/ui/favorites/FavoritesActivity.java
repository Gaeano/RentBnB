package com.usc.rentbnb.ui.favorites;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.usc.rentbnb.R;

public class FavoritesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);


        setupHeartToggle(findViewById(R.id.fav_card_1));
        setupHeartToggle(findViewById(R.id.fav_card_2));
        setupHeartToggle(findViewById(R.id.fav_card_3));
        setupHeartToggle(findViewById(R.id.fav_card_4));
        setupHeartToggle(findViewById(R.id.fav_card_5));
    }

    // Helper method to handle the clicking and swapping of the icons
    private void setupHeartToggle(View cardView) {
        if (cardView == null) return;

        // Find the specific heart icon inside THIS card
        ImageView heartIcon = cardView.findViewById(R.id.favorite_heart_icon);
        if (heartIcon != null) {

            // Since this is the favorites page, we assume they start as "favorited" (true)
            heartIcon.setTag(true);

            heartIcon.setOnClickListener(v -> {
                // Get current state
                boolean isFavorite = (boolean) v.getTag();

                // Toggle the state (if true, becomes false. If false, becomes true)
                isFavorite = !isFavorite;
                v.setTag(isFavorite);

                // Update the image based on the new state
                if (isFavorite) {
                    heartIcon.setImageResource(R.drawable.ic_favorites_filled);
                } else {
                    heartIcon.setImageResource(R.drawable.ic_favorites);
                }
            });
        }
    }
}