package com.usc.rentbnb.ui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.IndividualRegistrationData;
import com.usc.rentbnb.ui.auth.GoogleAuthHelper;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.viewmodels.AuthViewModel;

public class IndividualSignUpActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private FrameLayout loadingOverlay;
    private ImageView step1Icon, step2Icon, step3Icon, googleBtn;
    private TextView step1Label, step2Label, step3Label, resendBtn, loginBtn;

    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private View strengthIndicator, strengthBar1, strengthBar2, strengthBar3, strengthBar4;
    private TextView tvStrengthLabel;
    private EditText etMobile;
    private EditText etAge, etGender, etCity, etProvince, etCompleteAddress;

    private IndividualRegistrationData regData = new IndividualRegistrationData();
    private boolean isGoogleAuth = false;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_sign_up);

        // 1. Link ALL Views First
        viewFlipper = findViewById(R.id.viewFlipper);
        loadingOverlay = findViewById(R.id.loadingOverlay); // The new Loading Spinner

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

        googleBtn = findViewById(R.id.google_btn);
        resendBtn = findViewById(R.id.btn_resend);
        loginBtn = findViewById(R.id.login_btn);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        strengthIndicator = findViewById(R.id.strengthIndicator);
        strengthBar1 = findViewById(R.id.strengthBar1);
        strengthBar2 = findViewById(R.id.strengthBar2);
        strengthBar3 = findViewById(R.id.strengthBar3);
        strengthBar4 = findViewById(R.id.strengthBar4);
        tvStrengthLabel = findViewById(R.id.tvStrengthLabel);

        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etCompleteAddress = findViewById(R.id.etCompleteAddress);

        // 2. Initialize ViewModel and Observers AFTER views are linked
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setUpObservers();

        ViewCompat.setOnApplyWindowInsetsListener(btnBack, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + 16;
            v.setLayoutParams(params);
            return insets;
        });

        // 3. Logic & Listeners
        GoogleAuthHelper googleAuthHelper = new GoogleAuthHelper(this, new GoogleAuthHelper.GoogleAuthCallback() {
            @Override
            public void onSuccess(String idToken) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    isGoogleAuth = true;

                    String fullName = user.getDisplayName();
                    if (fullName != null && fullName.contains(" ")) {
                        int lastSpaceIndex = fullName.lastIndexOf(' ');
                        regData.setFirstName(fullName.substring(0, lastSpaceIndex));
                        regData.setLastName(fullName.substring(lastSpaceIndex + 1));
                    } else {
                        regData.setFirstName(fullName != null ? fullName : "");
                        regData.setLastName("");
                    }
                    regData.setEmail(user.getEmail());

                    viewFlipper.setDisplayedChild(1);
                    updateStepper(1);
                } else {
                    Toast.makeText(IndividualSignUpActivity.this, "Authentication failed: Firebase user is null.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(IndividualSignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        googleBtn.setOnClickListener(v -> googleAuthHelper.launchGoogleSignIn());

        loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnNext1.setOnClickListener(v -> {
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (password.isEmpty() || !password.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }
            regData.setFirstName(etFirstName.getText().toString().trim());
            regData.setLastName(etLastName.getText().toString().trim());
            regData.setEmail(etEmail.getText().toString().trim());
            regData.setPassword(password);

            viewFlipper.showNext();
            updateStepper(1);
        });

        btnNext2.setOnClickListener(v -> {
            regData.setMobileNumber(etMobile.getText().toString().trim());

            if (isGoogleAuth) {
                viewFlipper.setDisplayedChild(3);
                updateStepper(3);
            } else {
                authViewModel.createAccountAndVerifyEmail(regData.getEmail(), regData.getPassword());
            }
        });

        btnNext3.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Manually trigger the loading spinner during the reload task
                loadingOverlay.setVisibility(View.VISIBLE);
                user.reload().addOnCompleteListener(task -> {
                    loadingOverlay.setVisibility(View.GONE);
                    if (task.isSuccessful() && user.isEmailVerified()) {
                        Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();
                        viewFlipper.showNext();
                        updateStepper(2);
                    } else {
                        Toast.makeText(this, "Email not verified yet. Please check your inbox.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        resendBtn.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                resendBtn.setEnabled(false);
                resendBtn.setText("Sending...");
                user.sendEmailVerification().addOnCompleteListener(task -> {
                    resendBtn.setEnabled(true);
                    resendBtn.setText("Send Email Again");
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "New verification link sent!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnFinish.setOnClickListener(v -> {
            regData.setAge(etAge.getText().toString().trim());
            regData.setGender(etGender.getText().toString().trim());
            regData.setCity(etCity.getText().toString().trim());
            regData.setProvince(etProvince.getText().toString().trim());
            regData.setCompleteAddress(etCompleteAddress.getText().toString().trim());

            String fullName = regData.getFirstName() + " " + regData.getLastName();

            authViewModel.finalizeUserRegistration(
                    fullName,
                    regData.getMobileNumber(),
                    regData.getAge(),
                    regData.getGender(),
                    regData.getCity(),
                    regData.getProvince(),
                    regData.getCompleteAddress()
            );
        });

        btnBack.setOnClickListener(v -> {
            if (viewFlipper.getDisplayedChild() > 0) {
                viewFlipper.showPrevious();
                updateStepper(viewFlipper.getDisplayedChild());
            } else {
                finish();
            }
        });

        setupPasswordStrengthWatcher();
    }

    private void setupPasswordStrengthWatcher() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            strengthIndicator.setVisibility(View.GONE);
            return;
        }

        strengthIndicator.setVisibility(View.VISIBLE);
        com.usc.rentbnb.utils.PasswordStrengthHelper.Strength strength = 
                com.usc.rentbnb.utils.PasswordStrengthHelper.calculateStrength(password);

        tvStrengthLabel.setText("Strength: " + strength.label);
        tvStrengthLabel.setTextColor(ContextCompat.getColor(this, strength.colorRes));

        int color = ContextCompat.getColor(this, strength.colorRes);
        int defaultColor = ContextCompat.getColor(this, R.color.divider_color);

        strengthBar1.setBackgroundColor(strength.score >= 1 ? color : defaultColor);
        strengthBar2.setBackgroundColor(strength.score >= 2 ? color : defaultColor);
        strengthBar3.setBackgroundColor(strength.score >= 3 ? color : defaultColor);
        strengthBar4.setBackgroundColor(strength.score >= 4 ? color : defaultColor);
    }

    private void updateStepper(int stepIndex) {
        step1Icon.setAlpha(0.5f);
        step2Icon.setAlpha(0.5f);
        step3Icon.setAlpha(0.5f);

        switch (stepIndex) {
            case 0:
                step1Icon.setAlpha(1.0f);
                step1Label.setText("Basic Info");
                step2Label.setText("Mobile Number");
                step3Label.setText("Email Verification");
                break;
            case 1:
                step2Icon.setAlpha(1.0f);
                break;
            case 2:
            case 3:
                step3Icon.setAlpha(1.0f);
                break;
        }
    }

    private void setUpObservers() {
        // --- NEW LOADING OBSERVER ---
        authViewModel.getLiveLoadingData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                loadingOverlay.setVisibility(View.VISIBLE);
            } else {
                loadingOverlay.setVisibility(View.GONE);
            }
        });

        authViewModel.getAuthStepCompletedLiveData().observe(this, isCompleted -> {
            if (isCompleted != null && isCompleted) {
                Toast.makeText(this, "Verification email sent to " + regData.getEmail(), Toast.LENGTH_LONG).show();
                viewFlipper.setDisplayedChild(2);
                updateStepper(2);
            }
        });

        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Sign up completely successful", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(IndividualSignUpActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        authViewModel.getErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}