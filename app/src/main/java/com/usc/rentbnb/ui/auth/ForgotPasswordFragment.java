package com.usc.rentbnb.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.usc.rentbnb.R;

public class ForgotPasswordFragment extends Fragment {

    private TextInputEditText emailField;
    private MaterialButton resetPasswordBtn, backToLogin;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        emailField = view.findViewById(R.id.email_input);
        resetPasswordBtn = view.findViewById(R.id.reset_btn);
        backToLogin = view.findViewById(R.id.return_login_btn);

        resetPasswordBtn.setOnClickListener(v ->{
            String emailText = emailField.getText().toString().trim();

            if (emailText.isEmpty()){
                Toast.makeText(getActivity(), "Please enter your email", Toast.LENGTH_LONG).show();
                return;
            }

            resetPasswordBtn.setEnabled(false);
            resetPasswordBtn.setText("Sending...");

            FirebaseAuth auth = FirebaseAuth.getInstance();

            auth.sendPasswordResetEmail(emailText).addOnCompleteListener(task ->{
                if (task.isSuccessful()){
                    Toast.makeText(getActivity(), "Password reset link sent to your email", Toast.LENGTH_LONG).show();
                    resetPasswordBtn.setEnabled(true);
                    resetPasswordBtn.setText("Resend Email");
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Password reset failed.";
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });

        backToLogin.setOnClickListener(v ->{
           requireActivity().finish();
        });

        return view;

    }
}