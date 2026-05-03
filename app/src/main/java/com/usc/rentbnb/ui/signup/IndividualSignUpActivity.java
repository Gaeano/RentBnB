package com.usc.rentbnb.ui.signup;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.IndividualRegistrationData;
import com.usc.rentbnb.ui.auth.GoogleAuthHelper;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.viewmodels.AuthViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

public class IndividualSignUpActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ImageView step1Icon, step2Icon, step3Icon, googleBtn;
    private TextView step1Label, step2Label, step3Label, resendBtn;

    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private EditText etMobile;
    private EditText etAge, etGender, etCity, etProvince, etCompleteAddress;
    private IndividualRegistrationData regData = new IndividualRegistrationData();
    private boolean isGoogleAuth = false;
    private AuthViewModel authViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_sign_up);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setUpObservers();


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
        googleBtn = findViewById(R.id.google_btn);
        resendBtn = findViewById(R.id.btn_resend);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etMobile = findViewById(R.id.etMobile);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etCompleteAddress = findViewById(R.id.etCompleteAddress);

        ViewCompat.setOnApplyWindowInsetsListener(btnBack, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = statusBarHeight + 16;
            v.setLayoutParams(params);

            return insets;
        });

        GoogleAuthHelper googleAuthHelper = new GoogleAuthHelper(this, new GoogleAuthHelper.GoogleAuthCallback() {
            @Override
            public void onSuccess(String idToken) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    isGoogleAuth = true;

                    String fullName = user.getDisplayName();
                    if (fullName != null && fullName.contains(" ")){
                        regData.setFirstName(fullName.substring(0, fullName.lastIndexOf(' ')));
                        regData.setLastName(fullName.substring(1, fullName.lastIndexOf(' ') + 1));
                    } else {
                        regData.setFirstName(fullName);
                    }
                    regData.setEmail(user.getEmail());

                    viewFlipper.setDisplayedChild(1);
                    updateStepper(1);                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(IndividualSignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });


        googleBtn.setOnClickListener(v -> {
            googleAuthHelper.launchGoogleSignIn();
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
            regData.setPassword(etPassword.getText().toString().trim());

            viewFlipper.showNext();
            updateStepper(1);
        });

        btnNext2.setOnClickListener(v -> {
            regData.setMobileNumber(etMobile.getText().toString().trim());

            //create account
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
                user.reload().addOnCompleteListener(task -> {
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

        btnFinish.setOnClickListener(v -> {
            regData.setAge(etAge.getText().toString().trim());
            regData.setGender(etGender.getText().toString().trim());
            regData.setCity(etCity.getText().toString().trim());
            regData.setProvince(etProvince.getText().toString().trim());
            regData.setCompleteAddress(etCompleteAddress.getText().toString().trim());
            String firstName = regData.getFirstName();
            String lastName = regData.getLastName();

            String fullName = firstName + " " + lastName;
            // Handle completion
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

    private void setUpObservers(){
        authViewModel.getAuthStepCompletedLiveData().observe(this, isCompleted -> {
            if (isCompleted != null && isCompleted){
                Toast.makeText(this, "Verification email sent to " + regData.getEmail(), Toast.LENGTH_LONG).show();
                viewFlipper.setDisplayedChild(2);
                updateStepper(2);
            }
        });

        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null){
                Toast.makeText(this, "Sign up completely successful", Toast.LENGTH_LONG).show();
                Intent intent = new Intent (IndividualSignUpActivity.this, HomeActivity.class);
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