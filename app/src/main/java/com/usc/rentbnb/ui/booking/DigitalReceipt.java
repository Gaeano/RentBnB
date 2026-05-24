package com.usc.rentbnb.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;

import java.util.Locale;

public class DigitalReceipt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_digital_receipt);

        // Initialize Views
        TextView tvReceiptItemName = findViewById(R.id.tvReceiptItemName);
        TextView tvReceiptDates = findViewById(R.id.tvReceiptDates);
        TextView tvReceiptFullName = findViewById(R.id.tvReceiptFullName);
        TextView tvReceiptContact = findViewById(R.id.tvReceiptContact);
        TextView tvReceiptPayment = findViewById(R.id.tvReceiptPaymentMode);
        TextView tvReceiptTotalAmount = findViewById(R.id.tvReceiptTotalAmount);
        MaterialButton btnDownload = findViewById(R.id.btnDownloadReceipt);

        // Fetch User Fallback
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String accountName = "Valued Renter";
        if (currentUser != null) {
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                accountName = currentUser.getDisplayName();
            } else if (currentUser.getEmail() != null) {
                accountName = currentUser.getEmail().split("@")[0];
            }
        }

        // Retrieve Data from Intent
        Intent intent = getIntent();
        String nameFromIntent = intent.getStringExtra("FULL_NAME");
        String contact = intent.getStringExtra("CONTACT");
        String payment = intent.getStringExtra("PAYMENT_MODE");

        // New Dynamic Fields to receive from RentForm
        String itemName = intent.getStringExtra("PRODUCT_NAME");
        String startDate = intent.getStringExtra("START_DATE");
        String endDate = intent.getStringExtra("END_DATE");
        double totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0);

        // Map Data to Views safely
        String finalName = (nameFromIntent != null && !nameFromIntent.isEmpty()) ? nameFromIntent : accountName;
        if (tvReceiptFullName != null) tvReceiptFullName.setText(finalName.toUpperCase());
        if (tvReceiptContact != null && contact != null) tvReceiptContact.setText(contact);
        if (tvReceiptPayment != null && payment != null) tvReceiptPayment.setText("via " + payment.toUpperCase());

        if (tvReceiptItemName != null) {
            tvReceiptItemName.setText(itemName != null ? itemName : "RentBnB Listing");
        }

        if (tvReceiptDates != null) {
            if (startDate != null && endDate != null) {
                tvReceiptDates.setText(String.format("%s to %s", startDate, endDate));
            } else {
                tvReceiptDates.setVisibility(View.GONE);
            }
        }

        if (tvReceiptTotalAmount != null) {
            tvReceiptTotalAmount.setText(String.format(Locale.getDefault(), "₱ %,.2f", totalPrice));
        }

        // Handle Click
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> {
                // TODO: Add logic to save receipt to gallery if required
                Intent nextIntent = new Intent(DigitalReceipt.this, ThankYouPage.class);
                startActivity(nextIntent);
                finish();
            });
        }
    }
}