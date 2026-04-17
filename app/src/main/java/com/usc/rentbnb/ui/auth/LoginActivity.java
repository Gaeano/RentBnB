package com.usc.rentbnb.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.AuthViewModel;
import com.usc.rentbnb.ui.home.HomeActivity;
import com.usc.rentbnb.ui.onboarding.OnboardingActivity;

public class LoginActivity extends AppCompatActivity {

    private TextView backBtn, signUpBtn, forgetPasswordBtn;
    private TextInputEditText emailField, passwordField;
    private CheckBox rememberMeBtn;
    private ImageView googleBtn;
    private Button loginBtn;

    private FirebaseUser currentUser;
    private GoogleAuthHelper googleAuthHelper;
    private AuthViewModel authViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        backBtn = findViewById(R.id.tv_back);
        signUpBtn = findViewById(R.id.tv_signup_link);
        loginBtn = findViewById(R.id.btn_login);
        emailField = findViewById(R.id.email_textfield);
        passwordField = findViewById(R.id.password_textfield);
        rememberMeBtn = findViewById(R.id.checkBoxRememberMe);
        forgetPasswordBtn = findViewById(R.id.forget_password_btn);
        googleBtn = findViewById(R.id.btn_google_sign_in);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setUpObservers();




        googleAuthHelper = new GoogleAuthHelper(this, new GoogleAuthHelper.GoogleAuthCallback(){

            @Override
            public void onSuccess(String idToken) {
                Toast.makeText(LoginActivity.this, "Google Sign-In successful", Toast.LENGTH_LONG).show();
                authViewModel.signInWithGoogle(idToken);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        googleBtn.setOnClickListener(v -> {
            googleAuthHelper.launchGoogleSignIn();
        });


        backBtn.setOnClickListener(v -> {
            finish();
        });

        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        loginBtn.setOnClickListener(v ->{
            loginBtnFunctionality();
        });

        forgetPasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, verifyAndForgetActivity.class);
            intent.putExtra("FRAGMENT_MODE", "FORGOT_PASSWORD");
            startActivity(intent);
        });

    }

    private void loginBtnFunctionality(){

        if (emailField.getText().toString().isEmpty() || passwordField.getText().toString().isEmpty()){
            Toast.makeText(LoginActivity.this, "Please fill up all fields", Toast.LENGTH_LONG).show();
            return;
        }
        String emailText = emailField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();

        authViewModel.login(emailText, passwordText);

    }

    private void setUpObservers(){
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null){
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                currentUser = user;
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        authViewModel.getErrorLiveData().observe(this, errorMessage -> {
            if (errorMessage != null){
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getLiveLoadingData().observe(this, isLoading -> {
            if (isLoading != null && isLoading){
                loginBtn.setEnabled(false);
                loginBtn.setText("Logging in...");
            } else {
                loginBtn.setEnabled(true);
                loginBtn.setText("Login");
            }
        });
    }


}