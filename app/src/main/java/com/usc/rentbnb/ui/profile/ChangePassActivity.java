package com.usc.rentbnb.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.usc.rentbnb.R;

public class ChangePassActivity extends AppCompatActivity {

    private EditText etCurrent, etNew, etConfirm;
    private TextView tvErrorMatch;
    private View btnSavePass;

    // Strict Java lock for the button
    private boolean isSaveReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_pass);

        // Protect the top bar from system UI
        View topBar = findViewById(R.id.top_bar_pass);
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + 16, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btn_back_pass);
        btnBack.setOnClickListener(v -> finish());

        // Map UI Elements
        etCurrent = findViewById(R.id.et_pass_current);
        etNew = findViewById(R.id.et_pass_new);
        etConfirm = findViewById(R.id.et_pass_confirm);
        tvErrorMatch = findViewById(R.id.tv_error_password_match);
        btnSavePass = findViewById(R.id.btn_save_password);

        // Secure the button initially
        btnSavePass.setClickable(false);
        btnSavePass.setEnabled(false);

        // Attach the typing listeners
        setupTextWatchers();

        btnSavePass.setOnClickListener(v -> {
            // HARD LOCK: Kill the click if the button is disabled
            if (!isSaveReady) return;

            String newPass = etNew.getText().toString();
            String confirm = etConfirm.getText().toString();

            // --- VALIDATION ON CLICK ---
            if (!newPass.equals(confirm)) {
                // Passwords don't match: show the red text and stop proceeding!
                tvErrorMatch.setVisibility(View.VISIBLE);
                return;
            }

            // If we make it here, passwords match! Hide the error (just in case) and save.
            tvErrorMatch.setVisibility(View.GONE);

            // TODO: Validate current password against Firebase and update to the new one
            Toast.makeText(this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupTextWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // UX TRICK: Hide the error text the moment they start typing to fix it
                tvErrorMatch.setVisibility(View.GONE);

                checkFieldsFilled();
            }
        };

        etCurrent.addTextChangedListener(watcher);
        etNew.addTextChangedListener(watcher);
        etConfirm.addTextChangedListener(watcher);
    }

    private void checkFieldsFilled() {
        String current = etCurrent.getText().toString();
        String newPass = etNew.getText().toString();
        String confirm = etConfirm.getText().toString();

        // The button unlocks as long as ALL fields have SOME text in them
        isSaveReady = !current.isEmpty() && !newPass.isEmpty() && !confirm.isEmpty();

        // Update the visual state of the button
        btnSavePass.setEnabled(isSaveReady);
        btnSavePass.setClickable(isSaveReady);
        btnSavePass.setAlpha(isSaveReady ? 1.0f : 0.5f);
    }
}