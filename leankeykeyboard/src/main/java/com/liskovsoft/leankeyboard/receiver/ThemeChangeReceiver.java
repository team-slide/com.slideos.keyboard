package com.liskovsoft.leankeyboard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.slideos.system.R;

public class ThemeChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "ThemeChangeReceiver";
    private static final String THEME_CHANGED_ACTION = "com.slideos.system.THEME_CHANGED";
    
    private AppCompatActivity mActivity;
    
    public ThemeChangeReceiver(AppCompatActivity activity) {
        mActivity = activity;
    }
    
    public void register() {
        IntentFilter filter = new IntentFilter(THEME_CHANGED_ACTION);
        mActivity.registerReceiver(this, filter);
    }
    
    public void unregister() {
        try {
            mActivity.unregisterReceiver(this);
        } catch (Exception e) {
            Log.w(TAG, "Error unregistering receiver", e);
        }
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (THEME_CHANGED_ACTION.equals(intent.getAction())) {
            String themeMode = intent.getStringExtra("theme_mode");
            Log.d(TAG, "Theme changed to: " + themeMode);
            
            // Apply theme to the current activity
            if (mActivity != null && !mActivity.isFinishing()) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        applyThemeToActivity(themeMode);
                    }
                });
            }
        }
    }
    
    private void applyThemeToActivity(String themeMode) {
        try {
            boolean isDark = shouldUseDarkTheme(themeMode);
            
            // Apply theme to the activity's root view
            View rootView = mActivity.getWindow().getDecorView().findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.setBackgroundColor(mActivity.getResources().getColor(
                    isDark ? android.R.color.black : android.R.color.white
                ));
                
                // Update text colors recursively
                if (rootView instanceof ViewGroup) {
                    updateTextColorsRecursive((ViewGroup) rootView, isDark);
                }
            }
            
            // Update status bar colors if available
            updateStatusBarColors(isDark);
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme to activity", e);
        }
    }
    
    private boolean shouldUseDarkTheme(String theme) {
        if ("dark".equals(theme)) {
            return true;
        } else if ("light".equals(theme)) {
            return false;
        } else if ("auto".equals(theme)) {
            return isNightTime();
        }
        return true; // Default to dark
    }
    
    private boolean isNightTime() {
        // Get current hour (0-23)
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        
        // Consider night time from 6 PM (18:00) to 6 AM (06:00)
        return hour >= 18 || hour < 6;
    }
    
    private void updateTextColorsRecursive(ViewGroup parent, boolean isDark) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                // Skip status bar elements
                if (textView.getId() != R.id.unified_status_title && 
                    textView.getId() != R.id.unified_time_status &&
                    textView.getId() != R.id.unified_battery_status) {
                    textView.setTextColor(mActivity.getResources().getColor(
                        isDark ? android.R.color.white : android.R.color.black
                    ));
                }
            } else if (child instanceof ViewGroup) {
                updateTextColorsRecursive((ViewGroup) child, isDark);
            }
        }
    }
    
    private void updateStatusBarColors(boolean isDark) {
        try {
            // Update status bar title color
            TextView statusTitle = mActivity.findViewById(R.id.unified_status_title);
            if (statusTitle != null) {
                statusTitle.setTextColor(mActivity.getResources().getColor(android.R.color.white));
            }
            
            // Update time status color
            TextView timeStatus = mActivity.findViewById(R.id.unified_time_status);
            if (timeStatus != null) {
                timeStatus.setTextColor(mActivity.getResources().getColor(android.R.color.white));
            }
            
            // Update battery status color
            TextView batteryStatus = mActivity.findViewById(R.id.unified_battery_status);
            if (batteryStatus != null) {
                batteryStatus.setTextColor(mActivity.getResources().getColor(android.R.color.white));
            }
        } catch (Exception e) {
            Log.w(TAG, "Error updating status bar colors", e);
        }
    }
} 