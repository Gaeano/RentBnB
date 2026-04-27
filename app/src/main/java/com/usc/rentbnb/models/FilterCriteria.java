package com.usc.rentbnb.models;

import java.util.List;

/**
 * Immutable snapshot of every filter the user has set.
 * Passed from FilterBottomSheet → HomeActivity → HomeViewModel.
 */
public class FilterCriteria {

    // ── Price ────────────────────────────────────────────────────
    public final float minPrice;
    public final float maxPrice;

    // ── Category (multi-select) ───────────────────────────────────
    // Empty list == "all categories"
    public final List<String> categories;

    // ── Price unit (single-select, null == any) ───────────────────
    // Values: "/day" | "/hour" | "/week" | null
    public final String priceUnit;

    // ── Sort (single-select, null == default) ─────────────────────
    // Values: "most_rented" | "newest" | "price_asc" | "price_desc" | null
    public final String sortBy;

    public FilterCriteria(
            float minPrice,
            float maxPrice,
            List<String> categories,
            String priceUnit,
            String sortBy
    ) {
        this.minPrice   = minPrice;
        this.maxPrice   = maxPrice;
        this.categories = categories;
        this.priceUnit  = priceUnit;
        this.sortBy     = sortBy;
    }

    /** True when no filter is active (sheet is in its default cleared state). */
    public boolean isEmpty() {
        return minPrice == 0
                && maxPrice == 10_000
                && (categories == null || categories.isEmpty())
                && priceUnit == null
                && sortBy == null;
    }
}