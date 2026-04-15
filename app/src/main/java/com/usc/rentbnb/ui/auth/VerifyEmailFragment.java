package com.usc.rentbnb.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.home.HomeActivity; // Make sure this import matches your project

public class VerifyEmailFragment extends Fragment {

    private MaterialButton continueBtn, resendBtn;
    private FirebaseAuth auth;

    public VerifyEmailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_email, container, false);

        continueBtn = view.findViewById(R.id.btn_continue);
        resendBtn = view.findViewById(R.id.btn_resend);
        auth = FirebaseAuth.getInstance();

        continueBtn.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();

            if (user != null) {
                user.reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (user.isEmailVerified()) {
                            Toast.makeText(getActivity(), "Verification successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            startActivity(intent);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), "Email not verified yet. Please check your inbox.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to refresh status.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        resendBtn.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();

            if (user != null) {
                resendBtn.setEnabled(false);
                resendBtn.setText("Sending...");

                user.sendEmailVerification().addOnCompleteListener(task -> {
                    resendBtn.setEnabled(true);
                    resendBtn.setText("Resend Email");

                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "New verification link sent!", Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Failed to send email.";
                        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        return view;
    }
}