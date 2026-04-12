package com.usc.rentbnb.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.usc.rentbnb.R;

public class SignUpActivity extends AppCompatActivity {

    private TextView backBtn, loginBtnRedirect;
    private MaterialButton signUpBtn;
    private ImageView googleBtn;

    private TextInputEditText fullName, email, password, confirmPassword;

    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        //link buttons
        backBtn = findViewById(R.id.back_btn);
        signUpBtn = findViewById(R.id.btn_sign_up);
        googleBtn = findViewById(R.id.btn_google_sign_in);
        loginBtnRedirect = findViewById(R.id.tv_login_link);

        //link userInputs
        fullName = findViewById(R.id.name_textfield);
        email = findViewById(R.id.email_textfield);
        password = findViewById(R.id.password_textfield);
        confirmPassword = findViewById(R.id.confirm_password_textfield);


        auth = FirebaseAuth.getInstance();

        signUpBtn.setOnClickListener(v -> {
            signUpAttempt();
        });

    }

    private void signUpAttempt(){
        String fullNameText = fullName.getText().toString();
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();
        String confirmPasswordText = confirmPassword.getText().toString();

        if (fullName.getText().toString().isEmpty() || email.getText().toString().isEmpty() || password.getText().toString().isEmpty() || confirmPassword.getText().toString().isEmpty()){
            Toast.makeText(SignUpActivity.this, "Please fill up all fields", Toast.LENGTH_LONG).show();
            return;
        }

        if (!passwordText.equals(confirmPasswordText)){
            Toast.makeText(SignUpActivity.this, "Passwords must match",  Toast.LENGTH_LONG).show();
            return;
        }

        if (passwordText.length() < 7){
            Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
            return;
        }

        signUpBtn.setEnabled(false);
        signUpBtn.setText("Signing up...");

        auth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_LONG).show();
                    FirebaseUser user = auth.getCurrentUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fullNameText)
                            .build();
                    user.updateProfile(profileUpdates);

                    //add logic to add to db using backend

                    //uncomment if login page is implemented
//                    Intent intent = new Intent(this, LoginActivity.class);
//                    startActivity(intent);

                } else {
                    signUpBtn.setEnabled(true);
                    signUpBtn.setText("Sign up");
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed.";
                    Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("SignUpActivity", "createUserWithEmail:failure", task.getException());
                }
            }
        });


    }
}