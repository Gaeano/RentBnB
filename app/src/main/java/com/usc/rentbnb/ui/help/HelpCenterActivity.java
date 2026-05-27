package com.usc.rentbnb.ui.help;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.HelpAdapter;
import com.usc.rentbnb.models.HelpMessage;
import com.usc.rentbnb.models.HelpResponse;
import com.usc.rentbnb.network.ApiClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HelpCenterActivity extends AppCompatActivity {

    private RecyclerView rvHelpChat;
    private ScrollView layoutEmptyState;
    private EditText etMessageInput;
    private ImageButton btnSend;

    private HelpAdapter adapter;

    // In-memory conversation history — cleared on Activity destroy
    private final List<String> conversationHistory = new ArrayList<>();

    // Suggested questions shown in the empty state
    private static final String[] SUGGESTED_QUESTIONS = {
            "How do I book a listing?",
            "How do I list my item?",
            "How do I cancel a booking?",
            "How does payment work?",
            "How do I change my name?",
            "What happens if an item is returned late?",
            "How do I contact the owner?"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        setupWindowInsets();
        setupToolbar();
        setupRecyclerView();
        setupInput();
        setupSuggestedQuestions();
    }

    // ---------------------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------------------

    private void setupWindowInsets() {
        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            androidx.core.graphics.Insets bars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvHelpChat      = findViewById(R.id.rvHelpChat);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        rvHelpChat.setLayoutManager(llm);

        adapter = new HelpAdapter();
        rvHelpChat.setAdapter(adapter);
    }

    private void setupInput() {
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSend        = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
            String text = etMessageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                etMessageInput.setText("");
                hideKeyboard();
                sendUserMessage(text);
            }
        });
    }

    private void setupSuggestedQuestions() {
        LinearLayout container = findViewById(R.id.layoutSuggestionsContainer);
        if (container == null) return;

        container.removeAllViews(); // Clear anything old

        // List of our background resource IDs to rotate through
        int[] backgrounds = {
                R.drawable.bg_suggestion_blue,
                R.drawable.bg_suggestion_green,
                R.drawable.bg_suggestion_orange
        };

        // Shuffle the array and pick exactly 3 random questions
        List<String> allQuestions = Arrays.asList(SUGGESTED_QUESTIONS);
        Collections.shuffle(allQuestions);
        List<String> selectedQuestions = allQuestions.subList(0, Math.min(3, allQuestions.size()));

        for (int i = 0; i < selectedQuestions.size(); i++) {
            String question = selectedQuestions.get(i);

            // 1. Inflate or build the custom row layout card programmatically
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setGravity(android.view.Gravity.CENTER_VERTICAL);
            card.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

            // Set margins
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, dpToPx(12));
            card.setLayoutParams(params);

            // Cycle backgrounds smoothly
            card.setBackgroundResource(backgrounds[i % backgrounds.length]);

            // Make ripple effect touch animations work
            card.setClickable(true);
            card.setFocusable(true);
            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            card.setForeground(getDrawable(outValue.resourceId));

            // 2. Build the Text Label (No emojis, no plus icon)
            TextView tvQuestion = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            tvQuestion.setLayoutParams(textParams);
            tvQuestion.setText(question);
            tvQuestion.setTextColor(getColor(R.color.text_dark));
            tvQuestion.setTextSize(14f);
            tvQuestion.setTypeface(null, android.graphics.Typeface.BOLD);

            card.addView(tvQuestion);

            // 3. Click Action to send the message immediately
            card.setOnClickListener(v -> sendUserMessage(question));

            // Append it to the layout parent
            container.addView(card);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // ---------------------------------------------------------------------------
    // Message flow
    // ---------------------------------------------------------------------------

    private void sendUserMessage(String text) {
        // Transition from empty state to chat view on first message
        if (layoutEmptyState.getVisibility() == View.VISIBLE) {
            layoutEmptyState.setVisibility(View.GONE);
            rvHelpChat.setVisibility(View.VISIBLE);
        }

        HelpMessage userMsg = new HelpMessage(text, HelpMessage.TYPE_USER);
        adapter.addMessage(userMsg);
        conversationHistory.add("User: " + text);
        scrollToBottom();

        fetchAiReply(text);
    }

    private void fetchAiReply(String question) {
        adapter.setTyping(true);
        scrollToBottom();

        com.usc.rentbnb.models.HelpRequest request =
                new com.usc.rentbnb.models.HelpRequest(question, conversationHistory);

        ApiClient.getApiService().getHelpAnswer(request)
                .enqueue(new Callback<HelpResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<HelpResponse> call,
                                           @NonNull Response<HelpResponse> response) {
                        adapter.setTyping(false);

                        String answer;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getAnswer() != null) {
                            answer = response.body().getAnswer();
                        } else {
                            answer = "I'm having a little trouble right now. Please try again in a moment!";
                        }

                        conversationHistory.add("Inquilino: " + answer);
                        HelpMessage aiMsg = new HelpMessage(answer, HelpMessage.TYPE_AI);
                        adapter.addMessage(aiMsg);
                        scrollToBottom();
                    }

                    @Override
                    public void onFailure(@NonNull Call<HelpResponse> call, @NonNull Throwable t) {
                        adapter.setTyping(false);
                        String fallback = "I'm having a little trouble right now. Please try again in a moment!";
                        conversationHistory.add("Inquilino: " + fallback);
                        adapter.addMessage(new HelpMessage(fallback, HelpMessage.TYPE_AI));
                        scrollToBottom();
                    }
                });
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private void scrollToBottom() {
        rvHelpChat.post(() -> {
            int count = adapter.getItemCount();
            if (count > 0) rvHelpChat.scrollToPosition(count - 1);
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}