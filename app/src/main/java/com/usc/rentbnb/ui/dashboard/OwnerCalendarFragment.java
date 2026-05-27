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
            CalendarDayView cell = new CalendarDayView(parent.getContext());
            int cellHeight = (int) (parent.getContext().getResources().getDisplayMetrics().density * 44);
            cell.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, cellHeight));
            return new VH(cell);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            CalendarDay day  = days.get(position);
            CalendarDayView cell = (CalendarDayView) holder.itemView;

            if (day.type == CalendarDay.TYPE_HEADER) {
                cell.bindHeader(day.label);
                return;
            }

            if (day.type == CalendarDay.TYPE_EMPTY) {
                cell.bindEmpty();
                return;
            }

            // TYPE_DAY
            String key = day.toKey();

            // Compute today's key for dot indicator
            String todayKey = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .format(new java.util.Date());

            boolean isBooked   = bookedMap.containsKey(key) && !bookedMap.get(key).isEmpty();
            boolean isSelected = key.equals(selectedKey);
            boolean isToday    = key.equals(todayKey);

            // Determine range position for connected highlight shape
            RangePosition rangePos = RangePosition.NONE;
            if (isBooked && !isSelected) {
                boolean prev = isPrevBooked(day);
                boolean next = isNextBooked(day);
                if (prev && next)      rangePos = RangePosition.MIDDLE;
                else if (!prev && next) rangePos = RangePosition.START;
                else if (prev)          rangePos = RangePosition.END;
                else                    rangePos = RangePosition.SINGLE;
            }

            cell.bindDay(day.label, isSelected, isToday, rangePos);

            cell.setOnClickListener(v -> {
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

        // applyRangeBackground removed — drawing is now handled by CalendarDayView.onDraw()

        @Override
        public int getItemCount() { return days.size(); }

        class VH extends RecyclerView.ViewHolder {
            VH(@NonNull View v) { super(v); }
        }
    }

    // ===========================================================================
    // RangePosition — describes where a day sits within a booked range.
    // Declared here (static nested) because Java does not allow enums inside
    // a non-static inner class (CalendarGridAdapter is non-static).
    // ===========================================================================

    enum RangePosition { NONE, START, MIDDLE, END, SINGLE }

    // ===========================================================================
    // CalendarDayView — custom view that draws range strip + day number + today dot
    // all in a single onDraw() so the dot always clips above the strip.
    // ===========================================================================

    static class CalendarDayView extends View {

        private static final int COLOR_TEAL       = 0xFF3DCFCF;
        private static final int COLOR_TEAL_DARK  = 0xFF29AAAA;
        private static final int COLOR_WHITE      = 0xFFFFFFFF;
        private static final int COLOR_DARK_TEXT  = 0xFF1D1D1D;
        private static final int COLOR_GREY_TEXT  = 0xFF757575;

        private final Paint stripPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dotPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

        private String  label       = "";
        private boolean isHeader    = false;
        private boolean isSelected  = false;
        private boolean isToday     = false;
        // Import the enum via the enclosing class name
        private RangePosition rangePos =
                RangePosition.NONE;

        public CalendarDayView(Context context) {
            super(context);
            init();
        }

        public CalendarDayView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(36f);   // will be scaled to dp in bind
        }

        void bindHeader(String text) {
            this.label      = text;
            this.isHeader   = true;
            this.isSelected = false;
            this.isToday    = false;
            this.rangePos   = RangePosition.NONE;
            invalidate();
        }

        void bindEmpty() {
            this.label      = "";
            this.isHeader   = false;
            this.isSelected = false;
            this.isToday    = false;
            this.rangePos   = RangePosition.NONE;
            invalidate();
        }

        void bindDay(String text, boolean selected, boolean today,
                     RangePosition pos) {
            this.label      = text;
            this.isHeader   = false;
            this.isSelected = selected;
            this.isToday    = today;
            this.rangePos   = pos;
            invalidate();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (label == null || label.isEmpty()) return;

            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;

            float density = getResources().getDisplayMetrics().density;
            float textSizePx = 13f * density;
            textPaint.setTextSize(textSizePx);

            if (isHeader) {
                textPaint.setColor(COLOR_GREY_TEXT);
                textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                canvas.drawText(label, cx, cy - (textPaint.descent() + textPaint.ascent()) / 2f, textPaint);
                return;
            }

            // --- Layer 1: Range strip ---
            if (rangePos != RangePosition.NONE) {
                stripPaint.setColor(COLOR_TEAL);
                float stripTop    = cy - h * 0.35f;
                float stripBottom = cy + h * 0.35f;
                float r = (stripBottom - stripTop) / 2f;

                RectF stripBounds = new RectF(0, stripTop, w, stripBottom);

                switch (rangePos) {
                    case SINGLE:
                        canvas.drawRoundRect(stripBounds, r, r, stripPaint);
                        break;
                    case START:
                        // Rounded left, flat right
                        canvas.drawRoundRect(stripBounds, r, r, stripPaint);
                        canvas.drawRect(cx, stripTop, w, stripBottom, stripPaint);
                        break;
                    case END:
                        // Flat left, rounded right
                        canvas.drawRoundRect(stripBounds, r, r, stripPaint);
                        canvas.drawRect(0, stripTop, cx, stripBottom, stripPaint);
                        break;
                    case MIDDLE:
                        canvas.drawRect(stripBounds, stripPaint);
                        break;
                }
            }

            // --- Layer 2: Selected circle (teal_dark, drawn above strip) ---
            if (isSelected) {
                float circleR = Math.min(w, h) * 0.38f;
                circlePaint.setColor(COLOR_TEAL_DARK);
                canvas.drawCircle(cx, cy, circleR, circlePaint);
            }

            // --- Layer 3: Day number text ---
            // Text is white when on a strip or selected circle, dark otherwise
            boolean onHighlight = isSelected
                    || rangePos != RangePosition.NONE;
            textPaint.setColor(onHighlight ? COLOR_WHITE : COLOR_DARK_TEXT);
            textPaint.setTypeface(android.graphics.Typeface.DEFAULT);
            // Shift text up slightly to leave room for the dot
            float textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f
                    - (isToday ? density * 3f : 0f);
            canvas.drawText(label, cx, textY, textPaint);

            // --- Layer 4: Today dot (drawn last, always on top) ---
            if (isToday) {
                float dotR    = density * 2.5f;
                float dotY    = cy + h * 0.28f;
                // Dot color: white when on highlight, teal_dark when plain
                dotPaint.setColor(onHighlight ? COLOR_WHITE : COLOR_TEAL_DARK);
                canvas.drawCircle(cx, dotY, dotR, dotPaint);
            }
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

            // Duration label — computed from actual timestamps, not totalDays.
            // totalDays in Firestore can store the hour count for sub-day bookings
            // (e.g. 15 for a 15-hour booking) so it cannot be trusted directly.
            holder.tvAgendaStatus.setText(buildDurationLabel(b, selectedDateKey));

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

            // Chat button — finds the existing chat room for this booking's
            // listingId + renterId, then opens ChatRoomActivity.
            if (holder.btnAgendaChat != null) {
                holder.btnAgendaChat.setOnClickListener(v ->
                        openChatRoomForBooking(v, b));
            }
        }

        /**
         * Finds the chat room for this booking's listingId + renterId.
         * If a room already exists, opens it directly.
         * If no room exists (owner initiates first contact from calendar),
         * creates a new room with mode = MODE_OWNER so Inquilino does not
         * generate an opening message — the owner is starting the conversation.
         */
        private void openChatRoomForBooking(android.view.View v, Booking booking) {
            String listingId    = booking.getListingId();
            String renterId     = booking.getRenterId();
            String ownerId      = booking.getOwnerId();
            String listingTitle = booking.getListingTitle();
            if (listingTitle == null || listingTitle.isEmpty())
                listingTitle = booking.getProductName();
            if (listingTitle == null) listingTitle = "";

            String listingImageUrl = booking.getListingImageUrl();
            if (listingImageUrl == null) listingImageUrl = "";

            if (listingId == null || renterId == null || ownerId == null) {
                android.widget.Toast.makeText(v.getContext(),
                        "Chat unavailable for this booking.",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button during async operation to prevent double-taps
            v.setEnabled(false);

            final String finalListingTitle    = listingTitle;
            final String finalListingImageUrl = listingImageUrl;

            FirebaseFirestore.getInstance()
                    .collection("chatRooms")
                    .whereEqualTo("listingId", listingId)
                    .whereEqualTo("renterId",  renterId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshots -> {
                        if (!isAdded()) { v.setEnabled(true); return; }

                        if (snapshots != null && !snapshots.isEmpty()) {
                            // Room already exists — open it
                            v.setEnabled(true);
                            String existingRoomId = snapshots.getDocuments().get(0).getId();
                            String storedMode     = snapshots.getDocuments().get(0).getString("mode");
                            if (storedMode == null)
                                storedMode = com.usc.rentbnb.models.ChatRoom.MODE_OWNER;
                            navigateToChatRoom(v.getContext(), existingRoomId, listingId,
                                    ownerId, renterId, storedMode);
                        } else {
                            // No room — create one with MODE_OWNER so Inquilino stays silent
                            createOwnerInitiatedRoom(v, listingId, finalListingTitle,
                                    finalListingImageUrl, ownerId, renterId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        v.setEnabled(true);
                        android.widget.Toast.makeText(v.getContext(),
                                "Could not open chat. Try again.",
                                android.widget.Toast.LENGTH_SHORT).show();
                    });
        }

        /**
         * Creates a new chat room document with mode = MODE_OWNER.
         * Called only when the owner initiates first contact from the calendar.
         * MODE_OWNER is set at creation time — no second write needed.
         */
        private void createOwnerInitiatedRoom(android.view.View v,
                                              String listingId,
                                              String listingTitle,
                                              String listingImageUrl,
                                              String ownerId,
                                              String renterId) {
            java.util.Map<String, Object> roomData = new java.util.HashMap<>();
            roomData.put("renterId",            renterId);
            roomData.put("ownerId",             ownerId);
            roomData.put("listingId",           listingId);
            roomData.put("listingTitle",        listingTitle);
            roomData.put("listingImageUrl",     listingImageUrl);
            // MODE_OWNER at creation — Inquilino will not generate an opening message
            roomData.put("mode",                com.usc.rentbnb.models.ChatRoom.MODE_OWNER);
            roomData.put("lastMessage",         "");
            roomData.put("lastMessageTimestamp", com.google.firebase.Timestamp.now());
            roomData.put("participantIds",      java.util.Arrays.asList(renterId, ownerId));
            java.util.Map<String, Integer> unread = new java.util.HashMap<>();
            unread.put(renterId, 0);
            unread.put(ownerId,  0);
            roomData.put("unreadCount", unread);

            FirebaseFirestore.getInstance()
                    .collection("chatRooms")
                    .add(roomData)
                    .addOnSuccessListener(docRef -> {
                        v.setEnabled(true);
                        if (!isAdded()) return;
                        navigateToChatRoom(v.getContext(), docRef.getId(), listingId,
                                ownerId, renterId,
                                com.usc.rentbnb.models.ChatRoom.MODE_OWNER);
                    })
                    .addOnFailureListener(e -> {
                        v.setEnabled(true);
                        android.widget.Toast.makeText(v.getContext(),
                                "Could not create chat room. Try again.",
                                android.widget.Toast.LENGTH_SHORT).show();
                    });
        }

        private void navigateToChatRoom(android.content.Context ctx,
                                        String chatRoomId,
                                        String listingId,
                                        String ownerId,
                                        String renterId,
                                        String mode) {
            android.content.Intent intent = new android.content.Intent(
                    ctx, com.usc.rentbnb.ui.chat.ChatRoomActivity.class);
            intent.putExtra(com.usc.rentbnb.ui.chat.ChatRoomActivity.EXTRA_CHAT_ROOM_ID, chatRoomId);
            intent.putExtra(com.usc.rentbnb.ui.chat.ChatRoomActivity.EXTRA_LISTING_ID,   listingId);
            intent.putExtra(com.usc.rentbnb.ui.chat.ChatRoomActivity.EXTRA_OWNER_ID,     ownerId);
            intent.putExtra(com.usc.rentbnb.ui.chat.ChatRoomActivity.EXTRA_RENTER_ID,    renterId);
            intent.putExtra(com.usc.rentbnb.ui.chat.ChatRoomActivity.EXTRA_CURRENT_MODE, mode);
            ctx.startActivity(intent);
        }

        /**
         * Builds a human-readable duration label for the agenda card.
         *
         * Uses the actual startDate and endDate timestamps from the booking.
         * Does NOT use schedule.totalDays because that field may store hours
         * for sub-day bookings (e.g. a 15-hour booking stores totalDays=15).
         *
         * Rules:
         *   < 24h between start and end  → "15 hours" (or "1 hour")
         *   >= 24h                        → "Day X of Y" where Y is calendar days
         */
        private String buildDurationLabel(Booking booking, String currentDateKey) {
            String startStr = booking.getStartDate();
            String endStr   = booking.getEndDate();
            if (startStr == null || endStr == null) {
                return booking.getStatus() != null ? booking.getStatus() : "";
            }

            try {
                // Parse both full datetime strings (yyyy-MM-dd HH:mm:ss or ISO)
                Date startFull = parseDatetime(startStr);
                Date endFull   = parseDatetime(endStr);
                if (startFull == null || endFull == null) {
                    return booking.getStatus() != null ? booking.getStatus() : "";
                }

                long totalMillis = endFull.getTime() - startFull.getTime();
                long totalHours  = totalMillis / (1000L * 60 * 60);

                if (totalHours < 24) {
                    // Sub-day booking — show hours
                    if (totalHours <= 0) totalHours = 1;
                    return totalHours + (totalHours == 1 ? " hour" : " hours");
                } else {
                    // Multi-day booking — show "Day X of Y"
                    // Y = number of calendar days spanned (inclusive)
                    Date startDay = sdf.parse(startStr.substring(0, 10));
                    Date endDay   = sdf.parse(endStr.substring(0, 10));
                    Date curDay   = sdf.parse(currentDateKey);
                    if (startDay == null || endDay == null || curDay == null) {
                        return totalHours / 24 + " days";
                    }
                    long totalDaysSpanned = (endDay.getTime() - startDay.getTime())
                            / (1000L * 60 * 60 * 24) + 1;
                    long currentDayNum = (curDay.getTime() - startDay.getTime())
                            / (1000L * 60 * 60 * 24) + 1;
                    currentDayNum = Math.max(1, Math.min(currentDayNum, totalDaysSpanned));
                    return "Day " + currentDayNum + " of " + totalDaysSpanned;
                }
            } catch (ParseException e) {
                return booking.getStatus() != null ? booking.getStatus() : "";
            }
        }

        /** Parses either "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd'T'HH:mm:ss" datetime strings. */
        private Date parseDatetime(String s) {
            if (s == null) return null;
            String[] formats = {
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
            };
            for (String fmt : formats) {
                try {
                    return new java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault()).parse(s);
                } catch (ParseException ignored) {}
            }
            // Fallback: date only
            try { return sdf.parse(s.substring(0, 10)); } catch (ParseException e) { return null; }
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            ShapeableImageView ivAgendaAvatar;
            TextView tvAgendaName, tvAgendaItem, tvAgendaStatus;
            android.widget.ImageView btnAgendaChat;

            VH(@NonNull View itemView) {
                super(itemView);
                ivAgendaAvatar  = itemView.findViewById(R.id.ivAgendaAvatar);
                tvAgendaName    = itemView.findViewById(R.id.tvAgendaName);
                tvAgendaItem    = itemView.findViewById(R.id.tvAgendaItem);
                tvAgendaStatus  = itemView.findViewById(R.id.tvAgendaStatus);
                btnAgendaChat   = itemView.findViewById(R.id.btnAgendaChat);
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