package com.usc.rentbnb.ui.profile;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.usc.rentbnb.R;

public class LegalContentActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "legal_type";
    public static final int TYPE_TOS = 1;
    public static final int TYPE_PRIVACY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_content);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvTitle = findViewById(R.id.tvLegalTitle);
        TextView tvContent = findViewById(R.id.tvLegalContent);

        btnBack.setOnClickListener(v -> finish());

        int type = getIntent().getIntExtra(EXTRA_TYPE, TYPE_TOS);

        if (type == TYPE_TOS) {
            tvTitle.setText("Terms of Service");
            tvContent.setText(getTosText());
        } else {
            tvTitle.setText("Privacy Policy");
            tvContent.setText(getPrivacyText());
        }
    }
    private String getTosText() {
        return "Welcome to RentBnB!\n\n" +
                "1. Acceptance of Terms\n" +
                "By accessing and using this app, you agree to be bound by these terms...\n\n" +
                "2. User Accounts\n" +
                "You are responsible for maintaining the confidentiality of your account...\n\n" +
                "3. Service Description\n" +
                "RentBnB provides a platform for renting and listing items...\n\n" +
                "4. Limitation of Liability\n" +
                "We are not responsible for any damages resulting from the use of our services...";
    }
    private String getPrivacyText() {
        return "Your privacy is important to us.\n\n" +
                "1. Data Collection\n" +
                "We collect personal information that you provide to us, such as name and email...\n\n" +
                "2. Use of Information\n" +
                "We use your information to provide and improve our services...\n\n" +
                "3. Information Sharing\n" +
                "We do not sell your personal information to third parties...\n\n" +
                "4. Security\n" +
                "We take reasonable measures to protect your information from unauthorized access...";
    }
}