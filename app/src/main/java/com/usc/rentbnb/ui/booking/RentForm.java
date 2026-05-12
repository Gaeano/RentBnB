package com.usc.rentbnb.ui.booking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.usc.rentbnb.R;

import java.util.Calendar;

public class RentForm extends AppCompatActivity {

    private ViewFlipper vfRentForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rent_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vfRentForm = findViewById(R.id.vfRentForm);
        Button btnRent = findViewById(R.id.btnRent);

        btnRent.setOnClickListener(v -> {
            // Switch to Receipt view
            vfRentForm.setDisplayedChild(1);
        });

        setupDatePickers();
        setupPaymentDropdown();
    }

    private void setupDatePickers() {
        EditText etStartDate = findViewById(R.id.etStartDate);
        EditText etEndDate = findViewById(R.id.etEndDate);

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
    }

    private void showDatePicker(EditText editText) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year1;
                    editText.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void setupPaymentDropdown() {
        String[] paymentOptions = {"GCash", "PayPal", "Cash"};
        AutoCompleteTextView actvPaymentMode = findViewById(R.id.actvPaymentMode);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, paymentOptions);
        
        actvPaymentMode.setAdapter(adapter);
    }

//    @Override
//    public void onBackPressed() {
//        if (vfRentForm.getDisplayedChild() == 1) {
//            vfRentForm.setDisplayedChild(0);
//        } else {
//            super.onBackPressed();
//        }
//    }
}