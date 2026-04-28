package com.usc.rentbnb.ui.signup;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.usc.rentbnb.R;

public class CompanySignUpActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ImageView step1Icon, step2Icon, step3Icon;
    private TextView step1Label, step2Label, step3Label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_sign_up);

        viewFlipper = findViewById(R.id.viewFlipper);

        // Stepper UI components
        step1Icon = findViewById(R.id.step1Icon);
        step2Icon = findViewById(R.id.step2Icon);
        step3Icon = findViewById(R.id.step3Icon);

        step1Label = findViewById(R.id.step1Label);
        step2Label = findViewById(R.id.step2Label);
        step3Label = findViewById(R.id.step3Label);

        Button btnNext1 = findViewById(R.id.btnNext1);
        Button btnNext2 = findViewById(R.id.btnNext2);
        Button btnNext3 = findViewById(R.id.btnNext3);
        Button btnNext4 = findViewById(R.id.btnNext4);
        Button btnFinish = findViewById(R.id.btnFinish);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnNext1.setOnClickListener(v -> {
            viewFlipper.showNext();
            updateStepper(1); // Moving to Verification (Mobile)
        });

        btnNext2.setOnClickListener(v -> {
            viewFlipper.showNext();
            updateStepper(2); // Still in Verification (Email)
        });

        btnNext3.setOnClickListener(v -> {
            viewFlipper.showNext();
            updateStepper(3); // Moving to You're in! (Details)
        });

        btnNext4.setOnClickListener(v -> {
            viewFlipper.showNext();
            updateStepper(4); // Still in You're in! (Permits)
        });

        btnFinish.setOnClickListener(v -> {
            // Handle completion
            finish();
        });

        btnBack.setOnClickListener(v -> {
            if (viewFlipper.getDisplayedChild() > 0) {
                viewFlipper.showPrevious();
                updateStepper(viewFlipper.getDisplayedChild());
            } else {
                finish();
            }
        });

        // Initialize first step
        updateStepper(0);
    }

    private void updateStepper(int stepIndex) {
        // Reset all
        step1Icon.setAlpha(0.5f);
        step2Icon.setAlpha(0.5f);
        step3Icon.setAlpha(0.5f);

        // Logic mapping ViewFlipper child index to Stepper UI
        if (stepIndex == 0) {
            // Basic Info child
            step1Icon.setAlpha(1.0f);
        } else if (stepIndex == 1 || stepIndex == 2) {
            // Mobile Number or Email Verification children
            step2Icon.setAlpha(1.0f);
        } else if (stepIndex == 3 || stepIndex == 4) {
            // Details or Business Permits children
            step3Icon.setAlpha(1.0f);
        }
    }
}