package com.usc.rentbnb.ui.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.rentbnb.R;

public class OwnerCalendarFragment extends Fragment {

    private Spinner spinnerListingsFilter;
    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private RecyclerView rvAgenda;
    private View emptyStateAgenda;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerListingsFilter = view.findViewById(R.id.spinnerListingsFilter);
        calendarView = view.findViewById(R.id.calendarView);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        rvAgenda = view.findViewById(R.id.rvAgenda);
        emptyStateAgenda = view.findViewById(R.id.emptyStateAgenda);

        rvAgenda.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set initial date
        updateSelectedDateText(System.currentTimeMillis());

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String dateText = getMonthName(month) + " " + dayOfMonth + ", " + year;
            tvSelectedDate.setText(dateText);

            // TODO: Fetch bookings for this specific date and update the rvAgenda Adapter
            // For demo purposes, show empty state
            checkAndShowAgendaEmptyState(true); // Change based on actual data
        });

        // Initial empty state check
        checkAndShowAgendaEmptyState(true); // Change to false when you have data
    }

    private void updateSelectedDateText(long dateInMillis) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);

        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        String dateText = getMonthName(month) + " " + day + ", " + year;
        tvSelectedDate.setText(dateText);
    }

    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month];
    }

    /**
     * Check if the agenda list is empty and show/hide empty state accordingly
     * @param isEmpty true if the list is empty, false otherwise
     */
    private void checkAndShowAgendaEmptyState(boolean isEmpty) {
        if (isEmpty) {
            showAgendaEmptyState();
        } else {
            hideAgendaEmptyState();
        }
    }

    /**
     * Show empty state for agenda with smooth fade-in animation
     */
    private void showAgendaEmptyState() {
        if (emptyStateAgenda != null) {
            emptyStateAgenda.setAlpha(0f);
            emptyStateAgenda.setVisibility(View.VISIBLE);
            emptyStateAgenda.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(null);
        }

        // Hide RecyclerView
        if (rvAgenda != null) {
            rvAgenda.setVisibility(View.GONE);
        }
    }

    /**
     * Hide empty state for agenda with smooth fade-out animation
     */
    private void hideAgendaEmptyState() {
        if (emptyStateAgenda != null && emptyStateAgenda.getVisibility() == View.VISIBLE) {
            emptyStateAgenda.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            emptyStateAgenda.setVisibility(View.GONE);
                        }
                    });
        }

        // Show RecyclerView
        if (rvAgenda != null) {
            rvAgenda.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Call this method when agenda data is loaded for a specific date
     * @param hasBookings true if there are bookings for the date, false otherwise
     */
    public void onAgendaDataLoaded(boolean hasBookings) {
        checkAndShowAgendaEmptyState(!hasBookings);
    }
}