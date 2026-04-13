package com.usc.rentbnb.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.onboarding.OnboardingActivity;

public class LoginActivity extends AppCompatActivity {

    private TextView backBtn, signUpBtn;
    private TextInputEditText emailField, passwordField;
    private CheckBox rememberMeBtn;
    private ImageView googleBtn;
    private Button loginBtn;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

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

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        checkRememberMeStatus();

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

    }

    private void loginBtnFunctionality(){

        if (emailField.getText().toString().isEmpty() || passwordField.getText().toString().isEmpty()){
            Toast.makeText(LoginActivity.this, "Please fill up all fields", Toast.LENGTH_LONG).show();
            return;
        }
        String emailText = emailField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();

        auth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(task ->{
            if (task.isSuccessful()){
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                currentUser = auth.getCurrentUser();
                SharedPreferences sharedPreferences = getSharedPreferences("RentBnBPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (rememberMeBtn.isChecked()){
                    editor.putBoolean("IS_REMEMBERED", true);
                    editor.putString("SAVED_EMAIL", emailText);
                } else {
                    editor.putBoolean("IS_REMEMBERED", false);
                    editor.putString("SAVED_EMAIL", "");
                }
                editor.apply();

                // uncomment if homePage implemented

            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    //this method to be transferreed to the splash screen
    private void checkRememberMeStatus(){
        SharedPreferences sharedPreferences = getSharedPreferences("RentBnBPrefs", MODE_PRIVATE);
        Boolean isRemembered = sharedPreferences.getBoolean("IS_REMEMBERED", false);

        if (currentUser != null){

            if (isRemembered){
//           Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
//            startActivity(intent);
//            finish();
            } else {
                auth.signOut();
                currentUser = null;
            }

        }

        String savedEmail = sharedPreferences.getString("SAVED_EMAIL", "");
        if (!savedEmail.isEmpty()){
            emailField.setText(savedEmail);
            rememberMeBtn.setChecked(true);
        }



    }
}