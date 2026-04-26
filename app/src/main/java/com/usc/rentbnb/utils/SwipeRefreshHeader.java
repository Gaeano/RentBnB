package com.usc.rentbnb.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshKernel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.usc.rentbnb.R;

public class SwipeRefreshHeader extends LinearLayout implements RefreshHeader {

    public SwipeRefreshHeader(Context context) {
        super(context);
        initView(context);
    }

    public SwipeRefreshHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        // Inflate your custom XML here
        LayoutInflater.from(context).inflate(R.layout.layout_custom_refresh_header, this, true);
    }

    @NonNull
    @Override
    public View getView() {
        return this; // Return this view to the SmartRefreshLayout
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate; // Makes the header pull down smoothly
    }

    @Override
    public boolean autoOpen(int duration, float dragRate, boolean animationOnly) {
        return false;
    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {}

    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
        return 0; // Delay in milliseconds before closing
    }

    @Override
    public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {}

    @Override
    public void setPrimaryColors(int... colors) {}

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight) {}

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {}

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {}

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {}

    @Override
    public boolean isSupportHorizontalDrag() { return false; }
}