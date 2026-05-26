package com.usc.rentbnb.ui.dashboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.usc.rentbnb.R;
import com.usc.rentbnb.models.Booking;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class OwnerCalendarFragment extends Fragment {

    // Key: "yyyy-MM-dd", Value: list of bookings active on that date
    private final Map<String, List<Booking>> bookedDatesMap = new HashMap<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Calendar state
    private int displayYear;
    private int displayMonth; // 0-based

    private String selectedDateKey;

    // Views
    private TextView tvSelectedDate, tvMonthYear;
    private View btnPrevMonth, btnNextMonth;
    private RecyclerView rvCalendarGrid, rvAgenda;
    private View emptyStateAgenda;

    private CalendarGridAdapter gridAdapter;
    private AgendaAdapter agendaAdapter;

    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        currentUserId = user.getUid();

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvMonthYear    = view.findViewById(R.id.tvMonthYear);
        btnPrevMonth   = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth   = view.findViewById(R.id.btnNextMonth);
        rvCalendarGrid = view.findViewById(R.id.rvCalendarGrid);
        rvAgenda       = view.findViewById(R.id.rvAgenda);
        emptyStateAgenda = view.findViewById(R.id.emptyStateAgenda);

        Calendar today = Calendar.getInstance();
        displayYear  = today.get(Calendar.YEAR);
        displayMonth = today.get(Calendar.MONTH);

        // Select today by default
        selectedDateKey = sdf.format(today.getTime());
        tvSelectedDate.setText(formatDisplayDate(selectedDateKey));

        setupCalendarGrid();
        setupAgenda();
        setupNavButtons();

        loadOwnerBookings();
    }

    // ---------------------------------------------------------------------------
    // Calendar grid
    // ---------------------------------------------------------------------------

    private void setupCalendarGrid() {
        gridAdapter = new CalendarGridAdapter();
        rvCalendarGrid.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 7));
        rvCalendarGrid.setAdapter(gridAdapter);
        renderMonth();
    }

    private void renderMonth() {
        if (tvMonthYear != null) {
            String[] months = {"January","February","March","April","May","June",
                    "July","August","September","October","November","December"};
            tvMonthYear.setText(months[displayMonth] + " " + displayYear);
        }

        List<CalendarDay> days = buildDayList(displayYear, displayMonth);
        gridAdapter.setDays(days, bookedDatesMap, selectedDateKey);
    }

    private List<CalendarDay> buildDayList(int year, int month) {
        List<CalendarDay> days = new ArrayList<>();

        // Day-of-week headers
        String[] headers = {"S","M","T","W","T","F","S"};
        for (String h : headers) days.add(new CalendarDay(h, -1, -1, -1, CalendarDay.TYPE_HEADER));

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int firstDow = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDow; i++) {
            days.add(new CalendarDay("", 0, 0, 0, CalendarDay.TYPE_EMPTY));
        }

        for (int d = 1; d <= daysInMonth; d++) {
            cal.set(year, month, d);
            String key = sdf.format(cal.getTime());
            days.add(new CalendarDay(String.valueOf(d), year, month, d, CalendarDay.TYPE_DAY));
        }

        return days;
    }

    private void setupNavButtons() {
        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                displayMonth--;
                if (displayMonth < 0) { displayMonth = 11; displayYear--; }
                renderMonth();
            });
        }
        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                displayMonth++;
                if (displayMonth > 11) { displayMonth = 0; displayYear++; }
                renderMonth();
            });
        }
    }

    // ---------------------------------------------------------------------------
    // Agenda
    // ---------------------------------------------------------------------------

    private void setupAgenda() {
        agendaAdapter = new AgendaAdapter();
        rvAgenda.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAgenda.setAdapter(agendaAdapter);
        updateAgendaForDate(selectedDateKey);
    }

    private void updateAgendaForDate(String dateKey) {
        if (tvSelectedDate != null) tvSelectedDate.setText(formatDisplayDate(dateKey));
        List<Booking> bookings = bookedDatesMap.getOrDefault(dateKey, new ArrayList<>());
        agendaAdapter.setItems(bookings);
        checkAndShowAgendaEmptyState(bookings.isEmpty());
    }

    // ---------------------------------------------------------------------------
    // Firestore load
    // ---------------------------------------------------------------------------

    private void loadOwnerBookings() {
        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("ownerId", currentUserId)
                .whereIn("status", java.util.Arrays.asList("ACTIVE", "OVERDUE"))
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded() || snapshots == null) return;

                    bookedDatesMap.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Booking booking = doc.toObject(Booking.class);
                        booking.setId(doc.getId());

                        String start = booking.getStartDate();
                        String end   = booking.getEndDate();
                        if (start == null || end == null) continue;

                        // Expand the booking range into individual date keys
                        try {
                            Date startDate = sdf.parse(start.substring(0, 10));
                            Date endDate   = sdf.parse(end.substring(0, 10));
                            if (startDate == null || endDate == null) continue;

                            Calendar cur = Calendar.getInstance();
                            cur.setTime(startDate);

                            Calendar endCal = Calendar.getInstance();
                            endCal.setTime(endDate);

                            while (!cur.after(endCal)) {
                                String key = sdf.format(cur.getTime());
                                if (!bookedDatesMap.containsKey(key)) {
                                    bookedDatesMap.put(key, new ArrayList<>());
                                }
                                bookedDatesMap.get(key).add(booking);
                                cur.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        } catch (ParseException e) {
                            // skip malformed date
                        }
                    }

                    renderMonth();
                    updateAgendaForDate(selectedDateKey);
                });
    }

    // ---------------------------------------------------------------------------
    // Empty state
    // ---------------------------------------------------------------------------

    private void checkAndShowAgendaEmptyState(boolean isEmpty) {
        if (isEmpty) {
            if (emptyStateAgenda != null) {
                emptyStateAgenda.setAlpha(0f);
                emptyStateAgenda.setVisibility(View.VISIBLE);
                emptyStateAgenda.animate().alpha(1f).setDuration(300).setListener(null);
            }
            if (rvAgenda != null) rvAgenda.setVisibility(View.GONE);
        } else {
            if (emptyStateAgenda != null) emptyStateAgenda.setVisibility(View.GONE);
            if (rvAgenda != null) rvAgenda.setVisibility(View.VISIBLE);
        }
    }

    // ---------------------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------------------

    private String formatDisplayDate(String key) {
        if (key == null) return "";
        try {
            Date d = sdf.parse(key);
            if (d == null) return key;
            return new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(d);
        } catch (ParseException e) {
            return key;
        }
    }

    // ===========================================================================
    // Data model for a calendar cell
    // ===========================================================================

    static class CalendarDay {
        static final int TYPE_HEADER = 0;
        static final int TYPE_EMPTY  = 1;
        static final int TYPE_DAY    = 2;

        final String label;
        final int year, month, day, type;

        CalendarDay(String label, int year, int month, int day, int type) {
            this.label = label;
            this.year  = year;
            this.month = month;
            this.day   = day;
            this.type  = type;
        }

        String toKey() {
            return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        }
    }

    // ===========================================================================
    // Calendar grid adapter
    // ===========================================================================

    class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.VH> {

        private List<CalendarDay> days = new ArrayList<>();
        private Map<String, List<Booking>> bookedMap = new HashMap<>();
        private String selectedKey = "";

        void setDays(List<CalendarDay> days, Map<String, List<Booking>> bookedMap, String selectedKey) {
            this.days        = days;
            this.bookedMap   = bookedMap;
            this.selectedKey = selectedKey;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (parent.getContext().getResources().getDisplayMetrics().density * 44)));
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextSize(13f);
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            CalendarDay day = days.get(position);
            TextView tv = (TextView) holder.itemView;

            tv.setText(day.label);
            tv.setBackground(null);
            tv.setTextColor(Color.parseColor("#1D1D1D"));

            if (day.type == CalendarDay.TYPE_HEADER) {
                tv.setTextColor(Color.parseColor("#757575"));
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
                return;
            }

            if (day.type == CalendarDay.TYPE_EMPTY) {
                tv.setText("");
                return;
            }

            // TYPE_DAY
            String key = day.toKey();
            boolean isBooked   = bookedMap.containsKey(key) && !bookedMap.get(key).isEmpty();
            boolean isSelected = key.equals(selectedKey);

            if (isSelected) {
                tv.setBackgroundResource(R.drawable.calendar_day_selected);
                tv.setTextColor(Color.WHITE);
            } else if (isBooked) {
                boolean prevBooked = isPrevBooked(day);
                boolean nextBooked = isNextBooked(day);
                applyRangeBackground(tv, prevBooked, nextBooked);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackground(null);
                tv.setTextColor(Color.parseColor("#1D1D1D"));
            }

            tv.setOnClickListener(v -> {
                String prevSelected = selectedKey;
                selectedKey = key;
                notifyDataSetChanged();
                updateAgendaForDate(key);
            });
        }

        private boolean isPrevBooked(CalendarDay day) {
            Calendar cal = Calendar.getInstance();
            cal.set(day.year, day.month, day.day);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String prevKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            return bookedMap.containsKey(prevKey) && !bookedMap.get(prevKey).isEmpty();
        }

        private boolean isNextBooked(CalendarDay day) {
            Calendar cal = Calendar.getInstance();
            cal.set(day.year, day.month, day.day);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            String nextKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            return bookedMap.containsKey(nextKey) && !bookedMap.get(nextKey).isEmpty();
        }

        private void applyRangeBackground(TextView tv, boolean prevBooked, boolean nextBooked) {
            int teal = Color.parseColor("#3DCFCF");

            if (prevBooked && nextBooked) {
                // Middle of range — full rectangle
                tv.setBackgroundColor(teal);
            } else if (!prevBooked && nextBooked) {
                // Start of range — rounded left, flat right
                tv.setBackground(new RangeDrawable(teal, RangeDrawable.START));
            } else if (prevBooked && !nextBooked) {
                // End of range — flat left, rounded right
                tv.setBackground(new RangeDrawable(teal, RangeDrawable.END));
            } else {
                // Single day
                tv.setBackground(new RangeDrawable(teal, RangeDrawable.SINGLE));
            }
        }

        @Override
        public int getItemCount() { return days.size(); }

        class VH extends RecyclerView.ViewHolder {
            VH(@NonNull View v) { super(v); }
        }
    }

    // ===========================================================================
    // Agenda adapter
    // ===========================================================================

    class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.VH> {

        private final List<Booking> items = new ArrayList<>();

        void setItems(List<Booking> newItems) {
            items.clear();
            if (newItems != null) items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_agenda_booking, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Booking b = items.get(position);

            String renterName = b.getRenterName();
            if (renterName == null || renterName.isEmpty()) {
                if (b.getRenterDetails() != null && b.getRenterDetails().getName() != null)
                    renterName = b.getRenterDetails().getName();
                else renterName = "Renter";
            }
            holder.tvAgendaName.setText(renterName);

            String title = b.getListingTitle();
            if (title == null || title.isEmpty()) title = b.getProductName();
            if (title == null) title = "Item";
            holder.tvAgendaItem.setText(title);

            // Day X of Y
            int totalDays = b.getSchedule() != null ? b.getSchedule().getTotalDays() : 0;
            int currentDay = getDayInBooking(b, selectedDateKey);
            if (totalDays > 0 && currentDay > 0) {
                holder.tvAgendaStatus.setText("Day " + currentDay + " of " + totalDays);
            } else {
                holder.tvAgendaStatus.setText(b.getStatus() != null ? b.getStatus() : "");
            }

            // Load renter avatar
            String photoUrl = b.getRenterPhotoUrl();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(holder.ivAgendaAvatar.getContext())
                        .load(photoUrl)
                        .placeholder(R.drawable.userprofile)
                        .circleCrop()
                        .into(holder.ivAgendaAvatar);
            } else {
                holder.ivAgendaAvatar.setImageResource(R.drawable.userprofile);
            }
        }

        private int getDayInBooking(Booking booking, String dateKey) {
            if (booking.getStartDate() == null || dateKey == null) return -1;
            try {
                Date start   = sdf.parse(booking.getStartDate().substring(0, 10));
                Date current = sdf.parse(dateKey);
                if (start == null || current == null) return -1;
                long diff = current.getTime() - start.getTime();
                return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
            } catch (ParseException e) {
                return -1;
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            ShapeableImageView ivAgendaAvatar;
            TextView tvAgendaName, tvAgendaItem, tvAgendaStatus;

            VH(@NonNull View itemView) {
                super(itemView);
                ivAgendaAvatar  = itemView.findViewById(R.id.ivAgendaAvatar);
                tvAgendaName    = itemView.findViewById(R.id.tvAgendaName);
                tvAgendaItem    = itemView.findViewById(R.id.tvAgendaItem);
                tvAgendaStatus  = itemView.findViewById(R.id.tvAgendaStatus);
            }
        }
    }

    // ===========================================================================
    // Custom drawable for range highlight (start, middle, end, single)
    // ===========================================================================

    static class RangeDrawable extends android.graphics.drawable.Drawable {
        static final int START  = 0;
        static final int END    = 1;
        static final int SINGLE = 2;

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int color;
        private final int shape;

        RangeDrawable(int color, int shape) {
            this.color = color;
            this.shape = shape;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            RectF bounds = new RectF(getBounds());
            paint.setColor(color);

            float r = bounds.height() / 2f;

            if (shape == SINGLE) {
                canvas.drawRoundRect(bounds, r, r, paint);
            } else if (shape == START) {
                // Rounded left, flat right
                canvas.drawRoundRect(bounds, r, r, paint);
                RectF rightHalf = new RectF(bounds.centerX(), bounds.top, bounds.right, bounds.bottom);
                canvas.drawRect(rightHalf, paint);
            } else if (shape == END) {
                // Flat left, rounded right
                canvas.drawRoundRect(bounds, r, r, paint);
                RectF leftHalf = new RectF(bounds.left, bounds.top, bounds.centerX(), bounds.bottom);
                canvas.drawRect(leftHalf, paint);
            }
        }

        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override public void setColorFilter(@Nullable android.graphics.ColorFilter cf) { paint.setColorFilter(cf); }
        @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    }
}