package com.usc.rentbnb.ui.auth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;

public class verifyAndForgetActivity extends AppCompatActivity {

    private TextView backBtn;
    private FirebaseAuth auth;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.verify_forgetpassword_activity);


        backBtn.setOnClickListener(v ->{
            backNavigation();
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backNavigation();
            }
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        if (savedInstanceState == null ){

            String mode = getIntent().getStringExtra("FRAGMENT_MODE");

            Fragment activeFragment;
            if ("VERIFY_EMAIL".equals(mode)){
                activeFragment = new VerifyEmailFragment();
            } else {
                activeFragment = new ForgotPasswordFragment();
            }


            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, activeFragment)
                    .commit();
        }
    }

    private void backNavigation(){
        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

}