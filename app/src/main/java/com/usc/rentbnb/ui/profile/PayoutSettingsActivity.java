package com.usc.rentbnb.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.usc.rentbnb.R;
import com.usc.rentbnb.models.User;
import com.usc.rentbnb.viewmodels.UserProfileViewModel;

public class PayoutSettingsActivity extends AppCompatActivity {

    private EditText etGcashNumber, etPaypalEmail, etPaypalName;
    private UserProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout_settings);

        View payoutRoot = findViewById(R.id.payoutRoot);
        ViewCompat.setOnApplyWindowInsetsListener(payoutRoot, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        initViews();
        setUpObservers();
        
        viewModel.loadUserData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSavePayout).setOnClickListener(v -> savePayoutMethods());
    }

    private void initViews() {
        etGcashNumber = findViewById(R.id.etGcashNumber);
        etPaypalEmail = findViewById(R.id.etPaypalEmail);
        etPaypalName = findViewById(R.id.etPaypalName);
    }

    private void setUpObservers() {
        viewModel.getUserProfile().observe(this, user -> {
            if (user != null && user.getPayoutMethods() != null) {
                User.PayoutMethods methods = user.getPayoutMethods();
                if (methods.getGcash() != null) {
                    etGcashNumber.setText(methods.getGcash().getMobileNumber());
                }
                if (methods.getPaypal() != null) {
                    etPaypalEmail.setText(methods.getPaypal().getEmail());
                    etPaypalName.setText(methods.getPaypal().getAccountHolderName());
                }
            }
        });

        viewModel.getErrorData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, "Payout methods saved", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void savePayoutMethods() {
        String gcashNum = etGcashNumber.getText().toString().trim();
        String paypalEmail = etPaypalEmail.getText().toString().trim();
        String paypalName = etPaypalName.getText().toString().trim();

        // Prevent saving if everything is completely empty
        if (gcashNum.isEmpty() && paypalEmail.isEmpty() && paypalName.isEmpty()) {
            Toast.makeText(this, "Please add at least one payout method", Toast.LENGTH_SHORT).show();
            return;
        }

        User.PayoutMethods methods = new User.PayoutMethods();

        // 1. Set GCash (send empty string if left blank)
        methods.setGcash(new User.GcashDetails(gcashNum));

        // 2. Handle PayPal
        if (!paypalEmail.isEmpty() && !paypalName.isEmpty()) {
            methods.setPaypal(new User.PaypalDetails(paypalEmail, paypalName));
        } else if (!paypalEmail.isEmpty() || !paypalName.isEmpty()) {
            Toast.makeText(this, "Please fill in all PayPal fields, or leave both blank", Toast.LENGTH_SHORT).show();
            return;
        } else {
            // FIX: Explicitly send empty strings so the backend doesn't crash from missing JSON keys
            methods.setPaypal(new User.PaypalDetails("", ""));
        }

        viewModel.updatePayoutMethods(methods);
    }
}
