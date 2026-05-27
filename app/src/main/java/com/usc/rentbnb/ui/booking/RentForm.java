package com.usc.rentbnb.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Booking;
import com.usc.rentbnb.models.BookingRequest;
import com.usc.rentbnb.models.BookingResponse;
import com.usc.rentbnb.models.Listing;
import com.usc.rentbnb.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RentForm extends AppCompatActivity {
    private static final String TAG = "RentForm";

    private ViewFlipper vfCheckout;
    private TextView tvRentFormHeader, tvSummaryTitle, tvSummaryPrice, tvTotalPrice, tvQrTotal;
    private TextInputEditText etStartDate, etEndDate, etContactNumber, etCardNumber;
    private AutoCompleteTextView actvPaymentMode;

    private MaterialButton btnNext, btnConfirmQr, btnConfirmCc;
    private ImageButton btnBack;

    private Listing currentListing;
    private FirebaseUser currentUser;

    private long selectedStartMillis = 0;
    private long selectedEndMillis = 0;

    private String formattedStartDate = "";
    private String formattedEndDate = "";
    private double calculatedTotal = 0.0;
    private int calculatedTotalDays = 0;

    private List<Booking> existingBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_form);

        currentListing = (Listing) getIntent().getParcelableExtra("listing_object");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentListing == null || currentUser == null) {
            Toast.makeText(this, "Session invalid or missing listing data.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentListing.getOwnerId() != null && currentListing.getOwnerId().equals(currentUser.getUid())) {
            Toast.makeText(this, "You cannot rent your own listing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        populateListingSummary();
        setupDropdown();
        setupDatePickers();
        setupClickListeners();
        fetchExistingBookings();
    }

    private void fetchExistingBookings() {
        ApiClient.getApiService().getBookingsByUser(currentUser.getUid()).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    existingBookings = response.body().getData();
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Log.e(TAG, "Failed to fetch existing bookings: " + t.getMessage());
            }
        });
    }

    private void initViews() {
        vfCheckout = findViewById(R.id.vfCheckout);
        tvRentFormHeader = findViewById(R.id.tvRentFormHeader);

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnConfirmQr = findViewById(R.id.btnConfirmQr);
        btnConfirmCc = findViewById(R.id.btnConfirmCc);

        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etContactNumber = findViewById(R.id.etContactNumber);
        etCardNumber = findViewById(R.id.etCardNumber);
        actvPaymentMode = findViewById(R.id.actvPaymentMode);

        tvSummaryTitle = findViewById(R.id.tvSummaryTitle);
        tvSummaryPrice = findViewById(R.id.tvSummaryPrice);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvQrTotal = findViewById(R.id.tvQrTotal);
    }

    private void populateListingSummary() {
        tvSummaryTitle.setText(currentListing.getProductName() != null ? currentListing.getProductName() : "Item");

        String unit = currentListing.getPriceUnit() != null ? currentListing.getPriceUnit() : "day";
        tvSummaryPrice.setText(String.format(Locale.getDefault(), "₱%,.0f / %s", currentListing.getPrice(), unit));
        tvTotalPrice.setText("₱0.00");
    }

    private void setupDropdown() {
        List<String> listingPayments = currentListing.getPaymentMethods();
        if (listingPayments == null) {
            listingPayments = new ArrayList<>();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listingPayments);
        actvPaymentMode.setAdapter(adapter);
    }

    private void setupDatePickers() {
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat sdfApi = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        etStartDate.setOnClickListener(v -> {
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now());

            MaterialDatePicker<Long> startDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Start Date")
                    .setTheme(R.style.CustomDatePickerTheme)
                    .setSelection(selectedStartMillis == 0 ? MaterialDatePicker.todayInUtcMilliseconds() : selectedStartMillis)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            startDatePicker.addOnPositiveButtonClickListener(selection -> {
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(0)
                        .setTheme(R.style.CustomTimePickerTheme)
                        .setTitleText("Select Start Time")
                        .build();

                timePicker.addOnPositiveButtonClickListener(v2 -> {
                    long adjustedMillis = selection + TimeZone.getDefault().getOffset(selection);
                    // Reset to midnight first
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTimeInMillis(adjustedMillis);
                    cal.set(java.util.Calendar.HOUR_OF_DAY, timePicker.getHour());
                    cal.set(java.util.Calendar.MINUTE, timePicker.getMinute());
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);

                    selectedStartMillis = cal.getTimeInMillis();
                    Date date = cal.getTime();

                    etStartDate.setText(sdfDisplay.format(date));
                    formattedStartDate = sdfApi.format(date);
                    etStartDate.setError(null);

                    if (selectedEndMillis != 0 && selectedEndMillis < selectedStartMillis) {
                        selectedEndMillis = 0;
                        etEndDate.setText("");
                        formattedEndDate = "";
                        calculatedTotalDays = 0;
                        tvTotalPrice.setText("₱0.00");
                        Toast.makeText(this, "End date reset because it was before start date.", Toast.LENGTH_SHORT).show();
                    } else if (selectedEndMillis != 0) {
                        calculateTotalPrice();
                    }
                });

                timePicker.show(getSupportFragmentManager(), "START_TIME_PICKER");
            });

            if (!startDatePicker.isAdded()) startDatePicker.show(getSupportFragmentManager(), "START_DATE_PICKER");
        });

        etEndDate.setOnClickListener(v -> {
            if (selectedStartMillis == 0) {
                Toast.makeText(this, "Please select a start date first.", Toast.LENGTH_SHORT).show();
                etStartDate.setError("Required");
                return;
            }

            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.from(selectedStartMillis));

            MaterialDatePicker<Long> endDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select End Date")
                    .setTheme(R.style.CustomDatePickerTheme)
                    .setSelection(selectedEndMillis == 0 ? selectedStartMillis : selectedEndMillis)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            endDatePicker.addOnPositiveButtonClickListener(selection -> {
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(12)
                        .setMinute(0)
                        .setTheme(R.style.CustomTimePickerTheme)
                        .setTitleText("Select End Time")
                        .build();

                timePicker.addOnPositiveButtonClickListener(v2 -> {
                    long adjustedMillis = selection + TimeZone.getDefault().getOffset(selection);
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTimeInMillis(adjustedMillis);
                    cal.set(java.util.Calendar.HOUR_OF_DAY, timePicker.getHour());
                    cal.set(java.util.Calendar.MINUTE, timePicker.getMinute());
                    cal.set(java.util.Calendar.SECOND, 0);
                    cal.set(java.util.Calendar.MILLISECOND, 0);

                    selectedEndMillis = cal.getTimeInMillis();
                    Date date = cal.getTime();

                    etEndDate.setText(sdfDisplay.format(date));
                    formattedEndDate = sdfApi.format(date);
                    etEndDate.setError(null);

                    calculateTotalPrice();
                });

                timePicker.show(getSupportFragmentManager(), "END_TIME_PICKER");
            });

            if (!endDatePicker.isAdded()) endDatePicker.show(getSupportFragmentManager(), "END_DATE_PICKER");
        });
    }

    private void calculateTotalPrice() {
        if (selectedStartMillis == 0 || selectedEndMillis == 0) return;

        long diffInMillis = selectedEndMillis - selectedStartMillis;
        if (diffInMillis < 0) diffInMillis = 0;

        String unit = currentListing.getPriceUnit() != null ? currentListing.getPriceUnit().toLowerCase() : "day";
        
        if (unit.contains("hour")) {
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            // If less than an hour but more than 0, charge for 1 hour
            if (hours == 0 && diffInMillis > 0) hours = 1;
            
            calculatedTotalDays = (int) hours; // Reusing field to store hours
            calculatedTotal = hours * currentListing.getPrice();
        } else {
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            if (days == 0 && diffInMillis >= 0) days = 1;

            calculatedTotalDays = (int) days;
            calculatedTotal = days * currentListing.getPrice();
        }

        String formattedPrice = String.format(Locale.getDefault(), "₱%,.2f", calculatedTotal);
        tvTotalPrice.setText(formattedPrice);
        if (tvQrTotal != null) tvQrTotal.setText(formattedPrice);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            if (vfCheckout.getDisplayedChild() == 1 || vfCheckout.getDisplayedChild() == 2) {
                vfCheckout.setDisplayedChild(0);
                tvRentFormHeader.setText("Confirm Request");
            } else {
                finish();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (validateFormInputs()) {
                String paymentMode = actvPaymentMode.getText().toString().toLowerCase();

                if (paymentMode.contains("qr") || paymentMode.contains("gcash") || paymentMode.contains("paymaya") || paymentMode.contains("maya")) {
                    tvRentFormHeader.setText("QR Payment");
                    vfCheckout.setDisplayedChild(1);
                } else if (paymentMode.contains("paypal")) {
                    tvRentFormHeader.setText("Card Payment");
                    vfCheckout.setDisplayedChild(2);
                } else {
                    // Cash or other direct payment — fetch profile and submit immediately
                    fetchProfileAndSubmit(btnNext, null);
                }
            }
        });

        btnConfirmQr.setOnClickListener(v -> fetchProfileAndSubmit(btnConfirmQr, null));

        btnConfirmCc.setOnClickListener(v -> {
            String cardNumberRaw = etCardNumber != null && etCardNumber.getText() != null
                    ? etCardNumber.getText().toString().trim() : "";

            if (cardNumberRaw.length() < 4) {
                if (etCardNumber != null) etCardNumber.setError("Enter a valid card number");
                return;
            }

            String cardLast4 = cardNumberRaw.substring(cardNumberRaw.length() - 4);
            fetchProfileAndSubmit(btnConfirmCc, cardLast4);
        });
    }

    /**
     * Fetches the current user's Firestore profile to populate renterDetails,
     * then builds and submits the BookingRequest. This is done asynchronously
     * to avoid blocking the main thread. The button is disabled during the
     * fetch to prevent duplicate submissions. If the Firestore fetch fails,
     * the booking is still submitted with whatever data is available from
     * FirebaseAuth (name, email), and the address fields are left empty.
     */
    private void fetchProfileAndSubmit(MaterialButton triggeringButton, String cardLast4) {
        setLoadingState(true, triggeringButton);

        String uid = currentUser.getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isDestroyed()) return;

                    String name = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "";
                    String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
                    String contactNumber = etContactNumber.getText() != null
                            ? etContactNumber.getText().toString().trim() : "";
                    String address = "";
                    String city = "";
                    String province = "";

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String firestoreName = documentSnapshot.getString("displayName");
                        if (firestoreName != null && !firestoreName.isEmpty()) name = firestoreName;

                        String firestoreAddress = documentSnapshot.getString("address");
                        if (firestoreAddress != null) address = firestoreAddress;

                        // location is a nested map: { city: "", province: "" }
                        Object locationObj = documentSnapshot.get("location");
                        if (locationObj instanceof java.util.Map) {
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> locationMap = (java.util.Map<String, Object>) locationObj;
                            Object cityObj = locationMap.get("city");
                            Object provinceObj = locationMap.get("province");
                            if (cityObj instanceof String) city = (String) cityObj;
                            if (provinceObj instanceof String) province = (String) provinceObj;
                        }
                    }

                    submitBookingRequest(triggeringButton, name, email, contactNumber,
                            address, city, province, cardLast4);
                })
                .addOnFailureListener(e -> {
                    if (isDestroyed()) return;

                    Log.w(TAG, "Firestore profile fetch failed, proceeding with Firebase Auth data only: " + e.getMessage());

                    // Degrade gracefully — submit with available data
                    String name = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "";
                    String email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
                    String contactNumber = etContactNumber.getText() != null
                            ? etContactNumber.getText().toString().trim() : "";

                    submitBookingRequest(triggeringButton, name, email, contactNumber,
                            "", "", "", cardLast4);
                });
    }

    private void submitBookingRequest(MaterialButton triggeringButton, String name, String email,
                                      String contactNumber, String address, String city,
                                      String province, String cardLast4) {
        String targetOwner = currentListing.getOwnerId() != null ? currentListing.getOwnerId() : "";
        String paymentMethod = actvPaymentMode.getText() != null
                ? actvPaymentMode.getText().toString() : "";

        BookingRequest.FinancialSummary financialSummary =
                new BookingRequest.FinancialSummary(calculatedTotal);

        BookingRequest.RenterDetails renterDetails = new BookingRequest.RenterDetails(
                name,
                email,
                contactNumber,
                address,
                city,
                province
        );

        BookingRequest request = new BookingRequest(
                currentListing.getId(),
                targetOwner,
                formattedStartDate,
                formattedEndDate,
                calculatedTotalDays,
                financialSummary,
                renterDetails,
                paymentMethod,
                cardLast4
        );

        ApiClient.getApiService().createBooking(request).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                if (isDestroyed()) return;
                setLoadingState(false, triggeringButton);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    navigateToDigitalReceipt();
                } else {
                    try {
                        String errorBody = response.errorBody() != null
                                ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Booking failed: " + response.code() + " - " + errorBody);
                        Toast.makeText(RentForm.this, "Booking failed. Please try again.", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                if (isDestroyed()) return;
                setLoadingState(false, triggeringButton);
                Log.e(TAG, "API Error: " + t.getMessage());
                Toast.makeText(RentForm.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFormInputs() {
        boolean isValid = true;

        if (selectedStartMillis == 0) {
            etStartDate.setError("Required");
            isValid = false;
        }
        if (selectedEndMillis == 0) {
            etEndDate.setError("Required");
            isValid = false;
        }

        // Check for overlaps with same user and same listing
        if (isValid && existingBookings != null) {
            for (Booking b : existingBookings) {
                if (b.getListingId() != null && b.getListingId().equals(currentListing.getId())) {
                    try {
                        SimpleDateFormat sdfApi = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date bStart = sdfApi.parse(b.getStartDate());
                        Date bEnd = sdfApi.parse(b.getEndDate());

                        if (bStart != null && bEnd != null) {
                            long bStartMillis = bStart.getTime();
                            long bEndMillis = bEnd.getTime();

                            // Overlap condition: (StartA <= EndB) and (EndA >= StartB)
                            if (selectedStartMillis <= bEndMillis && selectedEndMillis >= bStartMillis) {
                                Toast.makeText(this, "You already have a booking for these dates.", Toast.LENGTH_LONG).show();
                                isValid = false;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing existing booking date", e);
                    }
                }
            }
        }

        if (etContactNumber.getText() == null
                || etContactNumber.getText().toString().trim().isEmpty()
                || etContactNumber.getText().length() < 10) {
            etContactNumber.setError("Valid contact number required");
            isValid = false;
        } else {
            etContactNumber.setError(null);
        }

        if (actvPaymentMode.getText() == null || actvPaymentMode.getText().toString().trim().isEmpty()) {
            actvPaymentMode.setError("Please select a payment method");
            isValid = false;
        } else {
            actvPaymentMode.setError(null);
        }

        return isValid;
    }

    private void navigateToDigitalReceipt() {
        Intent intent = new Intent(RentForm.this, DigitalReceipt.class);
        intent.putExtra("FULL_NAME", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
        intent.putExtra("CONTACT", "+63 " + etContactNumber.getText().toString());
        intent.putExtra("PAYMENT_MODE", actvPaymentMode.getText().toString());
        intent.putExtra("TOTAL_PRICE", calculatedTotal);

        // Pass missing fields
        intent.putExtra("PRODUCT_NAME", currentListing.getProductName());
        intent.putExtra("START_DATE", etStartDate.getText().toString());
        intent.putExtra("END_DATE", etEndDate.getText().toString());

        startActivity(intent);
        finish();
    }

    private void setLoadingState(boolean isLoading, MaterialButton button) {
        button.setEnabled(!isLoading);
        if (isLoading) {
            button.setText("Processing...");
            button.setAlpha(0.7f);
        } else {
            if (button == btnNext) {
                button.setText("Request Booking");
            } else {
                button.setText("Confirm Payment");
            }
            button.setAlpha(1.0f);
        }
    }
}