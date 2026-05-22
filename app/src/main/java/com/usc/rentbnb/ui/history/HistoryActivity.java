package com.usc.rentbnb.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.usc.rentbnb.R;

public class HistoryActivity extends AppCompatActivity {

    private EditText searchBar;
    private TextView chipActive, chipPending, chipCompleted, chipOverdue;
    private String currentStatus = "Active";
    private HistoryStatusFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        ImageView backButton = findViewById(R.id.back_button);
        LinearLayout historyHeader = findViewById(R.id.history_header);
        searchBar = findViewById(R.id.search_bar);

        chipActive = findViewById(R.id.chip_active);
        chipPending = findViewById(R.id.chip_pending);
        chipCompleted = findViewById(R.id.chip_completed);
        chipOverdue = findViewById(R.id.chip_overdue);

        ViewCompat.setOnApplyWindowInsetsListener(historyHeader, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), statusBarHeight + 8, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        backButton.setOnClickListener(v -> finish());

        setupChips();
        setupSearch();

        // Set initial chip style
        updateChipStyles();

        // Load initial fragment
        loadFragment("Active");
    }

    private void setupChips() {
        chipActive.setOnClickListener(v -> selectStatus("Active"));
        chipCompleted.setOnClickListener(v -> selectStatus("Completed"));
        chipOverdue.setOnClickListener(v -> selectStatus("Overdue"));
        chipPending.setOnClickListener(v->selectStatus("Pending"));
    }

    private void selectStatus(String status) {
        if (currentStatus.equals(status)) return;
        currentStatus = status;

        updateChipStyles();
        loadFragment(status);
    }

    private void updateChipStyles() {
        resetChipStyle(chipActive);
        resetChipStyle(chipCompleted);
        resetChipStyle(chipOverdue);
        resetChipStyle(chipPending);

        TextView selectedChip;
        if ("Active".equals(currentStatus)) selectedChip = chipActive;
        else if ("Completed".equals(currentStatus)) selectedChip = chipCompleted;
        else if ("Pending".equals(currentStatus)) selectedChip = chipPending;
        else selectedChip = chipOverdue;

        selectedChip.setBackgroundResource(R.drawable.bg_tab_active);
        selectedChip.setTextColor(ContextCompat.getColor(this, R.color.teal_primary));
    }

    private void resetChipStyle(TextView chip) {
        chip.setBackgroundResource(R.drawable.chip_background);
        chip.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
    }

    private void loadFragment(String status) {
        currentFragment = HistoryStatusFragment.newInstance(status);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.history_fragment_container, currentFragment);
        transaction.commit();

        // Use a small delay or post to ensure fragment is added before setting query if needed,
        // but setSearchQuery already checks isAdded().
        searchBar.post(() -> {
            if (currentFragment != null && searchBar != null) {
                currentFragment.setSearchQuery(searchBar.getText().toString());
            }
        });
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currentFragment != null) {
                    currentFragment.setSearchQuery(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}