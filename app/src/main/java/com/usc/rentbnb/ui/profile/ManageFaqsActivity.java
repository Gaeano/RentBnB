package com.usc.rentbnb.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;
import com.usc.rentbnb.adapters.FaqAdapter;
import com.usc.rentbnb.models.FAQ;
import com.usc.rentbnb.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageFaqsActivity extends AppCompatActivity implements FaqAdapter.OnFaqClickListener {

    private RecyclerView rvFaqs;
    private TextView tvEmpty;
    private FaqAdapter adapter;
    private List<FAQ> faqList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_faqs);

        View root = findViewById(R.id.faqRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarHeight,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        rvFaqs = findViewById(R.id.rvFaqs);
        tvEmpty = findViewById(R.id.tvEmptyFaqs);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddFaq).setOnClickListener(v -> showAddEditFaqDialog(null));

        setupRecyclerView();
        fetchFaqs();
    }

    private void fetchFaqs() {
        ApiClient.getApiService().getDefaultFaqs().enqueue(new Callback<List<FAQ>>() {
            @Override
            public void onResponse(Call<List<FAQ>> call, Response<List<FAQ>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    faqList = response.body();
                    updateUI();
                } else {
                    mockFaqs(); // Fallback for demo
                }
            }

            @Override
            public void onFailure(Call<List<FAQ>> call, Throwable t) {
                mockFaqs(); // Fallback for demo
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new FaqAdapter(faqList, this);
        rvFaqs.setLayoutManager(new LinearLayoutManager(this));
        rvFaqs.setAdapter(adapter);
    }

    private void mockFaqs() {
        faqList.add(new FAQ("mock_1", "How do I contact you?", "You can message me through the app's chat feature."));
        faqList.add(new FAQ("mock_2", "Is delivery available?", "Yes, I offer delivery for a small fee depending on the distance."));
        updateUI();
    }

    private void updateUI() {
        if (faqList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFaqs.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFaqs.setVisibility(View.VISIBLE);
            adapter.setFaqs(new ArrayList<>(faqList));
        }
    }

    private void showAddEditFaqDialog(FAQ faq) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_faq, null);
        builder.setView(dialogView);

        EditText etQuestion = dialogView.findViewById(R.id.etQuestion);
        EditText etAnswer = dialogView.findViewById(R.id.etAnswer);
        TextView btnSave = dialogView.findViewById(R.id.btnSaveFaq);
        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);

        if (faq != null) {
            tvTitle.setText("Edit FAQ");
            etQuestion.setText(faq.getQuestion());
            etAnswer.setText(faq.getAnswer());
        }

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSave.setOnClickListener(v -> {
            String q = etQuestion.getText().toString().trim();
            String a = etAnswer.getText().toString().trim();

            if (q.isEmpty() || a.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (faq == null) {
                // Add new
                FAQ newFaq = new FAQ(q, a);
                ApiClient.getApiService().addDefaultFaq(newFaq).enqueue(new Callback<FAQ>() {
                    @Override
                    public void onResponse(Call<FAQ> call, Response<FAQ> response) {
                        if (response.isSuccessful()) {
                            faqList.add(response.body());
                            updateUI();
                            Toast.makeText(ManageFaqsActivity.this, "FAQ added", Toast.LENGTH_SHORT).show();
                        } else {
                            // Local fallback for demo
                            faqList.add(new FAQ("local_" + System.currentTimeMillis(), q, a));
                            updateUI();
                        }
                    }

                    @Override
                    public void onFailure(Call<FAQ> call, Throwable t) {
                        // Local fallback for demo
                        faqList.add(new FAQ("local_" + System.currentTimeMillis(), q, a));
                        updateUI();
                        Toast.makeText(ManageFaqsActivity.this, "Network error - Saved locally", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Update existing
                String oldQ = faq.getQuestion();
                String oldA = faq.getAnswer();
                faq.setQuestion(q);
                faq.setAnswer(a);
                
                updateUI(); // Immediate local update

                ApiClient.getApiService().updateDefaultFaq(faq.getId(), faq).enqueue(new Callback<FAQ>() {
                    @Override
                    public void onResponse(Call<FAQ> call, Response<FAQ> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ManageFaqsActivity.this, "FAQ updated", Toast.LENGTH_SHORT).show();
                        } else {
                            // Keep local changes but inform user
                            Toast.makeText(ManageFaqsActivity.this, "Failed to sync update with server", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FAQ> call, Throwable t) {
                        // Keep local changes but inform user
                        Toast.makeText(ManageFaqsActivity.this, "Update saved locally (offline)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onFaqClick(FAQ faq) {
        showAddEditFaqDialog(faq);
    }

    @Override
    public void onDeleteClick(FAQ faq) {
        new AlertDialog.Builder(this)
                .setTitle("Delete FAQ")
                .setMessage("Are you sure you want to delete this default FAQ?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiClient.getApiService().deleteDefaultFaq(faq.getId()).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                faqList.remove(faq);
                                updateUI();
                                Toast.makeText(ManageFaqsActivity.this, "FAQ deleted", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(ManageFaqsActivity.this, "Failed to delete FAQ", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
