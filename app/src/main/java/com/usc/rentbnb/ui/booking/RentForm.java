package com.usc.rentbnb.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.BookingRequest;
import com.usc.rentbnb.models.BookingResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RentForm extends AppCompatActivity {

    private static final String TAG = "RentForm";
    private EditText etStartDate, etEndDate, etContactNumber;
    private AutoCompleteTextView actvPaymentMode;
    private TextView tvDurationPrompt, tvBoatName, tvOwnerName, tvPrice;
    private ImageButton btnBack;
    private Long startDateMillis, endDateMillis;
    private Listing currentListing;
    private double calculatedTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rent_form);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        currentListing = (Listing) getIntent().getSerializableExtra("listing_object");

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        tvBoatName = findViewById(R.id.tvBoatName);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDurationPrompt = findViewById(R.id.tvDurationPrompt);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etContactNumber = findViewById(R.id.etContactNumber);
        actvPaymentMode = findViewById(R.id.actvPaymentMode);
        Button btnRent = findViewById(R.id.btnRent);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (currentListing != null) {
            tvBoatName.setText(currentListing.getProductName());
            tvOwnerName.setText("👤 Owner ID: " + currentListing.getOwnerId());
            tvPrice.setText("₱" + String.format(Locale.US, "%,.0f", currentListing.getPrice()) + " / " + currentListing.getPriceUnit());
        }

        setupDatePickers();
        setupPaymentDropdown();

        btnRent.setOnClickListener(v -> {
            if (validateFields()) {
                submitBooking();
            }
        });
    }

    private boolean validateFields() {
        if (startDateMillis == null || endDateMillis == null) {
            Toast.makeText(this, "Please select rental dates", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etContactNumber.getText().toString().trim().isEmpty()) {
            etContactNumber.setError("Contact Number is required");
            return false;
        }
        if (actvPaymentMode.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a Payment Mode", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setupDatePickers() {
        View.OnClickListener dateListener = v -> {
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(DateValidatorPointForward.now());

            MaterialDatePicker<Pair<Long, Long>> dateRangePicker =
                    MaterialDatePicker.Builder.dateRangePicker()
                            .setTitleText("Select Dates")
                            .setCalendarConstraints(constraintsBuilder.build())
                            .build();

            dateRangePicker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER");

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {
                startDateMillis = selection.first;
                endDateMillis = selection.second;

                SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                displayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                etStartDate.setText(displayFormat.format(startDateMillis));
                etEndDate.setText(displayFormat.format(endDateMillis));

                calculateAndShowDuration();
            });
        };

        etStartDate.setOnClickListener(dateListener);
        etEndDate.setOnClickListener(dateListener);
    }

    private void calculateAndShowDuration() {
        if (startDateMillis != null && endDateMillis != null) {
            long diffInMillis = endDateMillis - startDateMillis;
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long rentalDays = diffInDays + 1;
            
            if (currentListing != null) {
                calculatedTotal = rentalDays * currentListing.getPrice();
                tvDurationPrompt.setText("Rented for " + rentalDays + " day(s). Total: ₱" + String.format(Locale.US, "%,.0f", calculatedTotal));
            } else {
                tvDurationPrompt.setText("Rented for " + rentalDays + " day(s)");
            }
            tvDurationPrompt.setVisibility(View.VISIBLE);
        } else {
            tvDurationPrompt.setVisibility(View.GONE);
        }
    }

    private void setupPaymentDropdown() {
        String[] paymentOptions = {"GCash", "PayPal", "Cash"};
        if (actvPaymentMode != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, paymentOptions);
            actvPaymentMode.setAdapter(adapter);
        }
    }

    private void submitBooking() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || currentListing == null) {
            Toast.makeText(this, "Error: User or Listing not found", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        apiFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String renterId = user.getUid();
        String ownerId = currentListing.getOwnerId();
        String listingId = currentListing.getId();
        String startDate = apiFormat.format(startDateMillis);
        String endDate = apiFormat.format(endDateMillis);
        String status = "pending";

        BookingRequest request = new BookingRequest(renterId, ownerId, listingId, startDate, endDate, calculatedTotal, status);

        ApiClient.getApiService().createBooking(request).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RentForm.this, "Booking Requested Successfully!", Toast.LENGTH_LONG).show();
                    
                    Intent intent = new Intent(RentForm.this, DigitalReceipt.class);
                    intent.putExtra("FULL_NAME", user.getDisplayName() != null ? user.getDisplayName() : "User");
                    intent.putExtra("CONTACT", etContactNumber.getText().toString());
                    intent.putExtra("PAYMENT_MODE", actvPaymentMode.getText().toString());
                    intent.putExtra("TOTAL_PRICE", calculatedTotal);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Booking failed: " + response.code() + " - " + errorBody);
                        Toast.makeText(RentForm.this, "Booking failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                Toast.makeText(RentForm.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
