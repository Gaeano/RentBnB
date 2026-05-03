package com.usc.rentbnb.ui.signup;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.CompanyRegistrationData;
import com.usc.rentbnb.ui.auth.GoogleAuthHelper;
import com.usc.rentbnb.ui.auth.LoginActivity;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.viewmodels.AuthViewModel;

public class CompanySignUpActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private FrameLayout loadingOverlay;
    private ImageView step1Icon, step2Icon, step3Icon, googleBtn;
    private TextView step1Label, step2Label, step3Label, resendBtn, loginBtn;

    private EditText etCompanyName, etEmail, etPassword, etConfirmPassword;
    private EditText etPrimaryMobile, etOptionalMobile1, etOptionalMobile2, etOptionalMobile3;
    private EditText etBusinessType, etYears, etCity, etProvince, etBusinessAddress;
    private EditText etRadius, etCoverage, etSpecificAreas;

    private CompanyRegistrationData regData = new CompanyRegistrationData();
    private boolean isGoogleAuth = false;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_sign_up);

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
        Button btnNext4 = findViewById(R.id.btnNext4);
        Button btnFinish = findViewById(R.id.btnFinish);
        ImageView btnBack = findViewById(R.id.btnBack);

        googleBtn = findViewById(R.id.google_btn);
        resendBtn = findViewById(R.id.btn_resend);
        loginBtn = findViewById(R.id.login_btn);

        etCompanyName = findViewById(R.id.etCompanyName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        etPrimaryMobile = findViewById(R.id.etPrimaryMobile);
        etOptionalMobile1 = findViewById(R.id.etOptionalMobile1);
        etOptionalMobile2 = findViewById(R.id.etOptionalMobile2);
        etOptionalMobile3 = findViewById(R.id.etOptionalMobile3);

        etBusinessType = findViewById(R.id.etBusinessType);
        etYears = findViewById(R.id.etYears);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etBusinessAddress = findViewById(R.id.etBusinessAddress);
        etRadius = findViewById(R.id.etRadius);
        etCoverage = findViewById(R.id.etCoverage);
        etSpecificAreas = findViewById(R.id.etSpecificAreas);

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
                if(user != null){
                    isGoogleAuth = true;
                    regData.setCompanyName(user.getDisplayName() != null ? user.getDisplayName() : "");
                    regData.setEmail(user.getEmail());

                    viewFlipper.setDisplayedChild(1);
                    updateStepper(1);
                } else {
                    Toast.makeText(CompanySignUpActivity.this, "Authentication failed: Firebase user is null.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(CompanySignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        googleBtn.setOnClickListener(v -> googleAuthHelper.launchGoogleSignIn());

        loginBtn.setOnClickListener(v->{
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
            regData.setCompanyName(etCompanyName.getText().toString().trim());
            regData.setEmail(etEmail.getText().toString().trim());
            regData.setPassword(password);

            viewFlipper.showNext();
            updateStepper(1);
        });

        btnNext2.setOnClickListener(v -> {
            String mobile = etPrimaryMobile.getText().toString().trim();
            if (mobile.isEmpty()) {
                etPrimaryMobile.setError("Required");
                return;
            }
            regData.setPrimaryMobile(mobile);
            regData.setOptionalMobile1(etOptionalMobile1.getText().toString().trim());
            regData.setOptionalMobile2(etOptionalMobile2.getText().toString().trim());
            regData.setOptionalMobile3(etOptionalMobile3.getText().toString().trim());

            if(isGoogleAuth){
                viewFlipper.setDisplayedChild(3);
                updateStepper(3);
            } else {
                authViewModel.createAccountAndVerifyEmail(regData.getEmail(), regData.getPassword());
            }
        });

        btnNext3.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                // Manually trigger loading spinner for Firebase reload task
                loadingOverlay.setVisibility(View.VISIBLE);
                user.reload().addOnCompleteListener(task -> {
                    loadingOverlay.setVisibility(View.GONE);
                    if (task.isSuccessful() && user.isEmailVerified()) {
                        Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();
                        viewFlipper.showNext();
                        updateStepper(3);
                    } else {
                        Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        resendBtn.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null){
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

        btnNext4.setOnClickListener(v -> {
            regData.setBusinessType(etBusinessType.getText().toString().trim());
            regData.setYearsOfOperation(etYears.getText().toString().trim());
            regData.setCity(etCity.getText().toString().trim());
            regData.setProvince(etProvince.getText().toString().trim());
            regData.setBusinessAddress(etBusinessAddress.getText().toString().trim());
            regData.setRadius(etRadius.getText().toString().trim());
            regData.setCoverage(etCoverage.getText().toString().trim());
            regData.setSpecificAreas(etSpecificAreas.getText().toString().trim());

            viewFlipper.showNext();
            updateStepper(4);
        });

        btnFinish.setOnClickListener(v -> {
            authViewModel.finalizeCompanyRegistration(regData);
        });

        btnBack.setOnClickListener(v -> {
            if (viewFlipper.getDisplayedChild() > 0) {
                viewFlipper.showPrevious();
                updateStepper(viewFlipper.getDisplayedChild());
            } else {
                finish();
            }
        });

        updateStepper(0);
    }

    private void updateStepper(int stepIndex) {
        step1Icon.setAlpha(0.5f);
        step2Icon.setAlpha(0.5f);
        step3Icon.setAlpha(0.5f);

        if (stepIndex == 0) {
            step1Icon.setAlpha(1.0f);
        } else if (stepIndex == 1 || stepIndex == 2) {
            step2Icon.setAlpha(1.0f);
        } else if (stepIndex == 3 || stepIndex == 4) {
            step3Icon.setAlpha(1.0f);
        }
    }

    private void setUpObservers(){
        // --- NEW LOADING OBSERVER ---
        authViewModel.getLiveLoadingData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                loadingOverlay.setVisibility(View.VISIBLE);
            } else {
                loadingOverlay.setVisibility(View.GONE);
            }
        });

        authViewModel.getAuthStepCompletedLiveData().observe(this, isCompleted -> {
            if (isCompleted != null && isCompleted){
                Toast.makeText(this, "Verification email sent to " + regData.getEmail(), Toast.LENGTH_LONG).show();
                viewFlipper.setDisplayedChild(2);
                updateStepper(2);
            }
        });

        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null){
                Toast.makeText(this, "Company Sign Up successful!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CompanySignUpActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        authViewModel.getErrorLiveData().observe(this, errorMessage -> {
            if(errorMessage != null){
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}