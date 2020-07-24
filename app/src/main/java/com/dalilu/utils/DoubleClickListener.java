package com.dalilu.utils;

import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {

    public static final long DOUBLE_CLICK_TIME = 300;
    private long last_click_time = 0;

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - last_click_time < DOUBLE_CLICK_TIME) {

            onDoubleClick(v);
            last_click_time = 0;
        }

        last_click_time = clickTime;

    }

    public abstract void onDoubleClick(View view);
}
