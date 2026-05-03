package com.usc.rentbnb.ui.booking;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.usc.rentbnb.R;

public class DigitalReceipt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_digital_receipt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvReceiptHeader).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvReceiptFullName = findViewById(R.id.tvReceiptFullName);
        TextView tvReceiptContact = findViewById(R.id.tvReceiptContact);
        TextView tvReceiptPayment = findViewById(R.id.tvReceiptPaymentMode);
        Button btnDownload = findViewById(R.id.btnDownloadReceipt);

        // Get data from Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("FULL_NAME");
        String contact = intent.getStringExtra("CONTACT");
        String payment = intent.getStringExtra("PAYMENT_MODE");

        if (name != null) tvReceiptFullName.setText(name.toUpperCase());
        if (contact != null) tvReceiptContact.setText(contact);
        if (payment != null) tvReceiptPayment.setText("via " + payment.toLowerCase());

        btnDownload.setOnClickListener(v -> {
            Intent nextIntent = new Intent(DigitalReceipt.this, ThankYouPage.class);
            startActivity(nextIntent);
        });
    }
}