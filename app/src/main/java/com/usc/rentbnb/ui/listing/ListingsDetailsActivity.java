package com.usc.rentbnb.ui.listing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.usc.rentbnb.R;

import java.util.ArrayList;
import java.util.List;

public class ListingsDetailsActivity extends AppCompatActivity {

    private TextView titleView, categoryView, priceView;
    private FrameLayout btnBack;
    private TextView btnRentNow;
    private ListView activitiesListView;

    // New variables for the clickable placeholders
    private ImageView btnFavorite, btnChat;
    private TextView descriptionText, btnShowAllReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listings_details);

        // 1. Initialize Views
        titleView = findViewById(R.id.listing_title);
        categoryView = findViewById(R.id.listing_category);
        priceView = findViewById(R.id.listing_price);
        btnBack = findViewById(R.id.btn_back_wrapper);
        btnRentNow = findViewById(R.id.btn_rent_now);
        activitiesListView = findViewById(R.id.list_suggested_activities);
        ImageView headerImage = findViewById(R.id.header_image);

        // Initialize the new placeholder views
        btnFavorite = findViewById(R.id.btn_favorite);
        btnChat = findViewById(R.id.btn_chat);
        descriptionText = findViewById(R.id.listing_description);
        btnShowAllReviews = findViewById(R.id.btn_show_all_reviews);

        // 2. Retrieve Data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            String productName = intent.getStringExtra("product_name");
            String category = intent.getStringExtra("category");
            String price = intent.getStringExtra("price");

            // ADD THIS to retrieve the unit
            String priceUnit = intent.getStringExtra("price_unit");

            if (productName != null) titleView.setText(productName);
            if (category != null) categoryView.setText(category);

            // UPDATE THIS to show both price and unit
            if (price != null) {
                if (priceUnit != null && !priceUnit.isEmpty()) {
                    priceView.setText("₱" + price + " / " + priceUnit);
                } else {
                    // Fallback just in case a listing doesn't have a unit
                    priceView.setText("₱" + price);
                }
            }
        }

        // 3. Setup Suggested Activities List
        setupActivitiesList();

        // 4. Setup Click Listeners (Navigation & Main Actions)
        btnBack.setOnClickListener(v -> finish());

        btnRentNow.setOnClickListener(v -> {
            Toast.makeText(ListingsDetailsActivity.this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show();
        });

        // 5. Setup Placeholder Click Listeners
        btnFavorite.setOnClickListener(v -> {
            Toast.makeText(ListingsDetailsActivity.this, "Added to favorites!", Toast.LENGTH_SHORT).show();
            // TODO: Toggle favorite heart icon color/state here later
        });

        btnChat.setOnClickListener(v -> {
            Toast.makeText(ListingsDetailsActivity.this, "Opening chat with owner...", Toast.LENGTH_SHORT).show();
            // TODO: Intent to chat activity here later
        });

        descriptionText.setOnClickListener(v -> {
            Toast.makeText(ListingsDetailsActivity.this, "Expanding description...", Toast.LENGTH_SHORT).show();
            // TODO: Expand the TextView to show full text here later
        });

        btnShowAllReviews.setOnClickListener(v -> {
            Toast.makeText(ListingsDetailsActivity.this, "Opening all reviews...", Toast.LENGTH_SHORT).show();
            // TODO: Open a bottom sheet or new activity with all reviews here later
        });
    }

    private void setupActivitiesList() {
        List<String> activities = new ArrayList<>();
        activities.add("· Island Hopping");
        activities.add("· Snorkeling");
        activities.add("· Sunset Watching");
        activities.add("· Deep Sea Fishing");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_activity_list,
                R.id.activity_text,
                activities
        );

        activitiesListView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(activitiesListView);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}