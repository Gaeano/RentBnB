package com.usc.rentbnb.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.viewmodels.AuthViewModel;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.ui.onboarding.OnboardingActivity;

public class SignUpActivity extends AppCompatActivity {

    private TextView loginBtnRedirect;
    private MaterialButton signUpBtn;
    private ImageView googleBtn;

    private TextInputEditText fullName, email, password, confirmPassword;

    FirebaseAuth auth;
    private FirebaseUser currentUser;
    private GoogleAuthHelper googleAuthHelper;

    private AuthViewModel authViewModel;
    private boolean isPerformingAuthAction = false;
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        //link buttons
        signUpBtn = findViewById(R.id.btn_sign_up);
        googleBtn = findViewById(R.id.btn_google_sign_in);
        loginBtnRedirect = findViewById(R.id.tv_login_link);

        //link userInputs
        fullName = findViewById(R.id.name_textfield);
        email = findViewById(R.id.email_textfield);
        password = findViewById(R.id.password_textfield);
        confirmPassword = findViewById(R.id.confirm_password_textfield);



        googleAuthHelper = new GoogleAuthHelper(this, new GoogleAuthHelper.GoogleAuthCallback(){
            @Override
            public void onSuccess(String idToken) {
                isPerformingAuthAction = true;
                authViewModel.signInWithGoogle(idToken);

            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }

        });

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);


        setUpObservers();


        googleBtn.setOnClickListener(v -> {
            googleAuthHelper.launchGoogleSignIn();
        });




        loginBtnRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            Log.d(TAG, "Redirecting to log in activity");
        });


        signUpBtn.setOnClickListener(v -> {
            signUpAttempt();
        });

        checkRememberMeStatus();



    }


    private void signUpAttempt(){
        String fullNameText = fullName.getText().toString();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();
        isPerformingAuthAction = true;

        if (fullName.getText().toString().isEmpty() || email.getText().toString().isEmpty() || password.getText().toString().isEmpty() || confirmPassword.getText().toString().isEmpty()){
            Toast.makeText(SignUpActivity.this, "Please fill up all fields", Toast.LENGTH_LONG).show();
            return;
        }

        if (!passwordText.equals(confirmPasswordText)){
            Toast.makeText(SignUpActivity.this, "Passwords must match",  Toast.LENGTH_LONG).show();
            return;
        }

        if (passwordText.length() < 7){
            Toast.makeText(SignUpActivity.this, "Password must be at least 7 characters", Toast.LENGTH_LONG).show();
            return;
        }

        authViewModel.signUp(emailText, passwordText, fullNameText);

    }
    //this method to be transferreed to the splash screen
    private void checkRememberMeStatus(){
        SharedPreferences sharedPreferences = getSharedPreferences("RentBnBPrefs", MODE_PRIVATE);
        Boolean isRemembered = sharedPreferences.getBoolean("IS_REMEMBERED", false);

        if (currentUser != null){
                currentUser.reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        if (isRemembered){
                            Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        purgeLocalSession(sharedPreferences);
                    }


            });
        }

//        String savedEmail = sharedPreferences.getString("SAVED_EMAIL", "");
//        if (!savedEmail.isEmpty()){
//            emailField.setText(savedEmail);
//            rememberMeBtn.setChecked(true);
//        }


    }
    private void purgeLocalSession(SharedPreferences sharedPreferences) {
        authViewModel.logout(); // Kills the Firebase cache

        // Wipe the Remember Me data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("IS_REMEMBERED", false);
        editor.putString("SAVED_EMAIL", "");
        editor.apply();

        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();

    }

    private void setUpObservers(){
        authViewModel.getUserLiveData().observe(this, user -> {
           if (user != null && isPerformingAuthAction){
               Toast.makeText(SignUpActivity.this, "Sign up successful", Toast.LENGTH_LONG).show();
               Log.d(TAG, "successfully initialized user");
               Intent intent = new Intent(SignUpActivity.this, OnboardingActivity.class);
               startActivity(intent);
               finish();
           }
        });


        authViewModel.getErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null){
                Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.getLiveLoadingData().observe(this, isLoading -> {
            if (isLoading != null && isLoading){
                signUpBtn.setEnabled(false);
                signUpBtn.setText("Signing up...");
            } else {
                signUpBtn.setEnabled(true);
                signUpBtn.setText("Sign up");
            }
        });
    }



}