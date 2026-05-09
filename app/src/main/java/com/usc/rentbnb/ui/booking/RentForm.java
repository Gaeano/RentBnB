package com.usc.rentbnb.ui.booking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RentForm extends AppCompatActivity {

    private EditText etStartDate, etEndDate, etContactNumber;
    private AutoCompleteTextView actvPaymentMode;
    private TextView tvDurationPrompt;
    private Calendar startCalendar, endCalendar;

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

        // Initialize Views
        tvDurationPrompt = findViewById(R.id.tvDurationPrompt);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etContactNumber = findViewById(R.id.etContactNumber);
        actvPaymentMode = findViewById(R.id.actvPaymentMode);
        Button btnRent = findViewById(R.id.btnRent);

        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        setupDatePickers();
        setupPaymentDropdown();

        btnRent.setOnClickListener(v -> {
            if (validateFields()) {
                String contact = etContactNumber.getText().toString().trim();
                String payment = actvPaymentMode.getText().toString().trim();

                // Get current user's name if possible, or let DigitalReceipt handle it
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String name = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Account User";

                Intent intent = new Intent(RentForm.this, DigitalReceipt.class);
                intent.putExtra("FULL_NAME", name);
                intent.putExtra("CONTACT", contact);
                intent.putExtra("PAYMENT_MODE", payment);
                startActivity(intent);
            }
        });
    }

    private boolean validateFields() {
        if (etStartDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Start Date is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (etEndDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "End Date is required", Toast.LENGTH_SHORT).show();
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
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar currentSelection = isStartDate ? startCalendar : endCalendar;
        
        // Using a style to ensure buttons are visible if theme colors clash
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    currentSelection.set(Calendar.YEAR, year);
                    currentSelection.set(Calendar.MONTH, monthOfYear);
                    currentSelection.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    currentSelection.set(Calendar.HOUR_OF_DAY, 0);
                    currentSelection.set(Calendar.MINUTE, 0);
                    currentSelection.set(Calendar.SECOND, 0);
                    currentSelection.set(Calendar.MILLISECOND, 0);

                    String myFormat = "MM/dd/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    
                    if (isStartDate) {
                        etStartDate.setText(sdf.format(currentSelection.getTime()));
                    } else {
                        etEndDate.setText(sdf.format(currentSelection.getTime()));
                    }
                    
                    calculateAndShowDuration();
                }, 
                currentSelection.get(Calendar.YEAR), 
                currentSelection.get(Calendar.MONTH), 
                currentSelection.get(Calendar.DAY_OF_MONTH));

        // CANNOT select a date prior to today
        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.HOUR_OF_DAY, 0);
        minDate.set(Calendar.MINUTE, 0);
        minDate.set(Calendar.SECOND, 0);
        minDate.set(Calendar.MILLISECOND, 0);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        
        datePickerDialog.show();
    }

    private void calculateAndShowDuration() {
        String startStr = etStartDate.getText().toString();
        String endStr = etEndDate.getText().toString();

        if (!startStr.isEmpty() && !endStr.isEmpty()) {
            long diffInMillis = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
            
            if (diffInMillis < 0) {
                tvDurationPrompt.setText("End date cannot be before start date!");
                tvDurationPrompt.setVisibility(View.VISIBLE);
                return;
            }

            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            // Rental is usually inclusive, e.g., same day = 1 day
            long rentalDays = diffInDays + 1;
            
            tvDurationPrompt.setText("Rented for " + rentalDays + " day(s)");
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
}
