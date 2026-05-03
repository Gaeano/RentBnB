package com.usc.rentbnb.ui.signup;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.home.HomeActivity;

import androidx.appcompat.app.AppCompatActivity;

public class IndividualSignUpActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ImageView step1Icon, step2Icon, step3Icon;
    private TextView step1Label, step2Label, step3Label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_sign_up);

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
        Button btnFinish = findViewById(R.id.btnFinish);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnNext1.setOnClickListener(v -> {
            viewFlipper.showNext();
            updateStepper(1);
        });

        btnNext2.setOnClickListener(v -> {
            viewFlipper.showNext();
            updateStepper(2);
        });

        btnNext3.setOnClickListener(v -> {
            viewFlipper.showNext();
            // Final step (Details) usually doesn't change the 3-step indicator
            // but we can highlight the last one or stay on it.
            updateStepper(2);
        });

        btnFinish.setOnClickListener(v -> {
            // Handle completion
            startActivity(new Intent(IndividualSignUpActivity.this, HomeActivity.class));
        });

        btnBack.setOnClickListener(v -> {
            if (viewFlipper.getDisplayedChild() > 0) {
                viewFlipper.showPrevious();
                updateStepper(viewFlipper.getDisplayedChild());
            } else {
                finish();
            }
        });
    }

    private void updateStepper(int stepIndex) {
        // Reset all
        step1Icon.setAlpha(0.5f);
        step2Icon.setAlpha(0.5f);
        step3Icon.setAlpha(0.5f);

        // Update based on current view
        switch (stepIndex) {
            case 0:
                step1Icon.setAlpha(1.0f);
                step1Label.setText("Basic Info");
                step2Label.setText("Mobile Number");
                step3Label.setText("Email Verification");
                break;
            case 1:
                step2Icon.setAlpha(1.0f);
                // Note: The image shows labels changing slightly or icons changing.
                // For simplicity, we just change alpha to indicate active step.
                break;
            case 2:
                step3Icon.setAlpha(1.0f);
                break;
        }
    }
}