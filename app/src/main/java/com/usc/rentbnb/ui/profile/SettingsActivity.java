package com.usc.rentbnb.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.usc.rentbnb.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "RentBnB_Settings";
    private static final String KEY_PUSH_NOTIFS = "push_notifications";
    private static final String KEY_EMAIL_NOTIFS = "email_notifications";

    private SwitchMaterial switchPush;
    private SwitchMaterial switchEmail;
    private ImageButton btnBack;
    private View rowTerms, rowPrivacy, rowVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsRoot), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchPush = findViewById(R.id.switchPushNotifications);
        switchEmail = findViewById(R.id.switchEmailNotifications);
        rowTerms = findViewById(R.id.rowTerms);
        rowPrivacy = findViewById(R.id.rowPrivacy);
        rowVersion = findViewById(R.id.rowVersion);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        switchPush.setChecked(prefs.getBoolean(KEY_PUSH_NOTIFS, true));
        switchEmail.setChecked(prefs.getBoolean(KEY_EMAIL_NOTIFS, false));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchPush.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_PUSH_NOTIFS, isChecked);
        });

        switchEmail.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_EMAIL_NOTIFS, isChecked);
        });

        rowTerms.setOnClickListener(v -> {
            Intent intent = new Intent(this, LegalContentActivity.class);
            intent.putExtra(LegalContentActivity.EXTRA_TYPE, LegalContentActivity.TYPE_TOS);
            startActivity(intent);
        });

        rowPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(this, LegalContentActivity.class);
            intent.putExtra(LegalContentActivity.EXTRA_TYPE, LegalContentActivity.TYPE_PRIVACY);
            startActivity(intent);
        });

        rowVersion.setOnClickListener(v -> {
            Toast.makeText(this, "RentBnB Version 1.0.0", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(key, value).apply();
    }
}