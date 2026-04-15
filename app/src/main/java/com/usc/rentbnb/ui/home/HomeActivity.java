package com.usc.rentbnb.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.usc.rentbnb.R;
import com.usc.rentbnb.ui.auth.LoginActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView logout;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        logout = findViewById(R.id.logout_btn);

        auth = FirebaseAuth.getInstance();

        logout.setOnClickListener(v ->{
            auth.signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}