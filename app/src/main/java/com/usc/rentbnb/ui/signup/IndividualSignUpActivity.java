package com.usc.rentbnb.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.auth.GoogleAuthHelper;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.ui.onboarding.OnboardingActivity;
import com.usc.rentbnb.viewmodels.AuthViewModel;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class IndividualSignUpActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ImageView step1Icon, step2Icon, step3Icon;
    private TextView step1Label, step2Label, step3Label;

    // Step 1 fields (Index 0)
    private TextInputLayout tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etPassword, etConfirmPassword;

    // Step 2 fields (Details - Index 1)
    private TextInputLayout tilAge, tilGender, tilCity, tilProvince, tilCompleteAddress;
    private TextInputEditText etAge, etGender, etCity, etProvince, etCompleteAddress;

    // Step 3 fields (Mobile - Index 2)
    private TextInputLayout tilMobileNumber;
    private TextInputEditText etMobileNumber;
    private Button btnNextMobile;

    // Step 4 fields (Verification - Index 3)
    private TextView tvVerificationSentTo;
    private Button btnResendEmail;

    private AuthViewModel authViewModel;
    private GoogleAuthHelper googleAuthHelper;
    private FirebaseAuth mAuth;
    private boolean isPerformingAuthAction = false;
    private static final String TAG = "IndividualSignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_individual_sign_up);

        mAuth = FirebaseAuth.getInstance();
        viewFlipper = findViewById(R.id.viewFlipper);

        // Stepper UI components
        step1Icon = findViewById(R.id.step1Icon);
        step2Icon = findViewById(R.id.step2Icon);
        step3Icon = findViewById(R.id.step3Icon);

        step1Label = findViewById(R.id.step1Label);
        step2Label = findViewById(R.id.step2Label);
        step3Label = findViewById(R.id.step3Label);

        initViews();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupObservers();

        googleAuthHelper = new GoogleAuthHelper(this, new GoogleAuthHelper.GoogleAuthCallback() {
            @Override
            public void onSuccess(String idToken) {
                isPerformingAuthAction = true;
                authViewModel.signInWithGoogle(idToken);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(IndividualSignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        Button btnNext1 = findViewById(R.id.btnNext1);
        Button btnNextDetails = findViewById(R.id.btnNextDetails);
        Button btnNextVerification = findViewById(R.id.btnNext3); // I've Verified button
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnGoogle = findViewById(R.id.btnGoogle);
        TextView tvLoginRedirect = findViewById(R.id.tvLoginRedirect);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        // Step 1 -> Step 2 (Details)
        btnNext1.setOnClickListener(v -> {
            if (validateStep1()) {
                viewFlipper.setDisplayedChild(1);
                updateStepper(0);
            }
        });

        // Step 2 -> Step 3 (Mobile)
        btnNextDetails.setOnClickListener(v -> {
            if (validateStepDetails()) {
                viewFlipper.setDisplayedChild(2);
                updateStepper(1);
            }
        });

        // Step 3 -> Step 4 (Account Creation & Verification)
        btnNextMobile.setOnClickListener(v -> {
            if (validateStepMobile()) {
                signUpAttempt();
            }
        });

        // Step 4 -> Finish
        btnNextVerification.setOnClickListener(v -> {
            checkEmailVerification();
        });

        btnResendEmail.setOnClickListener(v -> {
            resendVerificationEmail();
        });

        btnBack.setOnClickListener(v -> {
            int currentChild = viewFlipper.getDisplayedChild();
            if (currentChild > 0) {
                if (currentChild == 3) {
                    viewFlipper.setDisplayedChild(2);
                    updateStepper(1);
                } else if (currentChild == 2) {
                    viewFlipper.setDisplayedChild(1);
                    updateStepper(0);
                } else {
                    viewFlipper.setDisplayedChild(0);
                    updateStepper(0);
                }
            } else {
                finish();
            }
        });

        btnGoogle.setOnClickListener(v -> {
            googleAuthHelper.launchGoogleSignIn();
        });

        tvLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(IndividualSignUpActivity.this, LoginActivity.class));
        });

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(IndividualSignUpActivity.this, LoginActivity.class));
        });
    }

    private void initViews() {
        // Step 1
        tilFullName = findViewById(R.id.tilFullName);
        etFullName = findViewById(R.id.etFullName);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.etPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        // Step 2 (Details)
        tilAge = findViewById(R.id.tilAge);
        etAge = findViewById(R.id.etAge);
        tilGender = findViewById(R.id.tilGender);
        etGender = findViewById(R.id.etGender);
        tilCity = findViewById(R.id.tilCity);
        etCity = findViewById(R.id.etCity);
        tilProvince = findViewById(R.id.tilProvince);
        etProvince = findViewById(R.id.etProvince);
        tilCompleteAddress = findViewById(R.id.tilCompleteAddress);
        etCompleteAddress = findViewById(R.id.etCompleteAddress);

        // Step 3 (Mobile)
        tilMobileNumber = findViewById(R.id.tilMobileNumber);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        btnNextMobile = findViewById(R.id.btnNext2);

        // Step 4 (Verification)
        tvVerificationSentTo = findViewById(R.id.tvVerificationSentTo);
        btnResendEmail = findViewById(R.id.btnResendEmail);
    }

    private void setupObservers() {
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null && isPerformingAuthAction) {
                isPerformingAuthAction = false;
                if (viewFlipper.getDisplayedChild() == 2) { // Mobile step completion
                    viewFlipper.setDisplayedChild(3); // Move to Verification
                    updateStepper(2);
                    tvVerificationSentTo.setText("We sent a verification link to " + user.getEmail() + ".\nPlease click it to continue.");
                    user.sendEmailVerification().addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, "Initial verification email failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // This handles Google sign-in (from Step 1)
                    Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, OnboardingActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        authViewModel.getErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getLiveLoadingData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                btnNextMobile.setEnabled(false);
                btnNextMobile.setText("Processing...");
            } else {
                btnNextMobile.setEnabled(true);
                btnNextMobile.setText("next");
            }
        });
    }

    private void signUpAttempt() {
        String fullNameText = etFullName.getText().toString();
        String emailText = etEmail.getText().toString().trim();
        String passwordText = etPassword.getText().toString().trim();

        isPerformingAuthAction = true;
        authViewModel.signUp(emailText, passwordText, fullNameText);
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(IndividualSignUpActivity.this, HomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Email not verified yet. Please check your inbox.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            btnResendEmail.setEnabled(false);
            btnResendEmail.setText("Sending...");
            user.sendEmailVerification().addOnCompleteListener(task -> {
                btnResendEmail.setEnabled(true);
                btnResendEmail.setText("Resend Email");
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Verification link sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to send link: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateStep1() {
        boolean isValid = true;
        clearErrors(tilFullName, tilEmail, tilPassword, tilConfirmPassword);

        if (TextUtils.isEmpty(etFullName.getText())) {
            tilFullName.setError("Full name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(etEmail.getText())) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        }
        if (TextUtils.isEmpty(etPassword.getText())) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (etPassword.getText().length() < 7) {
            tilPassword.setError("Password must be at least 7 characters");
            isValid = false;
        }
        if (TextUtils.isEmpty(etConfirmPassword.getText())) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid;
    }

    private boolean validateStepDetails() {
        boolean isValid = true;
        clearErrors(tilAge, tilGender, tilCity, tilProvince, tilCompleteAddress);

        if (TextUtils.isEmpty(etAge.getText())) {
            tilAge.setError("Age is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(etGender.getText())) {
            tilGender.setError("Gender is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(etCity.getText())) {
            tilCity.setError("City is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(etProvince.getText())) {
            tilProvince.setError("Province is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(etCompleteAddress.getText())) {
            tilCompleteAddress.setError("Complete address is required");
            isValid = false;
        }

        return isValid;
    }

    private boolean validateStepMobile() {
        clearErrors(tilMobileNumber);
        if (TextUtils.isEmpty(etMobileNumber.getText())) {
            tilMobileNumber.setError("Mobile number is required");
            return false;
        }
        return true;
    }

    private void clearErrors(TextInputLayout... layouts) {
        for (TextInputLayout layout : layouts) {
            layout.setError(null);
            layout.setErrorEnabled(false);
        }
    }

    private void updateStepper(int stepIndex) {
        step1Icon.setAlpha(0.5f);
        step2Icon.setAlpha(0.5f);
        step3Icon.setAlpha(0.5f);

        switch (stepIndex) {
            case 0:
                step1Icon.setAlpha(1.0f);
                break;
            case 1:
                step2Icon.setAlpha(1.0f);
                break;
            case 2:
                step3Icon.setAlpha(1.0f);
                break;
        }
    }
}
