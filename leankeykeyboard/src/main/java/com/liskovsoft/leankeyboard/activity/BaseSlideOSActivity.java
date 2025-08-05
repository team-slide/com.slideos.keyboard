package com.liskovsoft.leankeyboard.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.slideos.system.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.Log;
import android.widget.ArrayAdapter;
import com.liskovsoft.leankeyboard.receiver.ThemeChangeReceiver;

public abstract class BaseSlideOSActivity extends AppCompatActivity {
    
    protected TextView mStatusTitle;
    protected TextView mTimeStatus;
    protected TextView mBatteryStatus;
    protected android.widget.ImageView mWifiStatus;
    protected android.widget.ImageView mBluetoothStatus;
    protected Handler mStatusHandler;
    protected ListView mListView;
    protected ThemeChangeReceiver mThemeReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide the action bar completely
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        mStatusHandler = new Handler();
        setupStatusBar();
        startStatusUpdates();
        
        // Apply current theme
        applyCurrentTheme();
        
        // Register theme change receiver
        mThemeReceiver = new ThemeChangeReceiver(this);
        mThemeReceiver.register();
    }
    
    protected void setupStatusBar() {
        // Find status bar views
        mStatusTitle = findViewById(R.id.unified_status_title);
        mTimeStatus = findViewById(R.id.unified_time_status);
        mBatteryStatus = findViewById(R.id.unified_battery_status);
        mWifiStatus = findViewById(R.id.unified_wifi_status);
        mBluetoothStatus = findViewById(R.id.unified_bluetooth_status);
        
        // Set initial title
        if (mStatusTitle != null) {
            mStatusTitle.setText(getActivityTitle());
        }
        
        // Update status immediately
        updateStatusBar();
    }
    
    protected abstract String getActivityTitle();
    
    protected void startStatusUpdates() {
        mStatusHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatusBar();
                // Check for auto theme updates every minute
                checkAutoThemeUpdate();
                mStatusHandler.postDelayed(this, 10000); // Update every 10 seconds for more accurate time
            }
        }, 1000);
    }
    
    private void checkAutoThemeUpdate() {
        // Check if auto theme is enabled and update if needed
        String currentTheme = getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
            .getString("theme_mode", "dark");
        
        if ("auto".equals(currentTheme)) {
            // Force theme refresh for auto mode
            applyCurrentTheme();
        }
    }
    
    protected void updateStatusBar() {
        updateDeviceTime();
        updateBatteryStatus();
        updateConnectionStatus();
    }
    
    protected void updateDeviceTime() {
        try {
            // Get the current system time using System.currentTimeMillis() for more reliable time
            long currentTimeMillis = System.currentTimeMillis();
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(currentTimeMillis);
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentTime = sdf.format(calendar.getTime());
            
            if (mTimeStatus != null) {
                mTimeStatus.setText(currentTime);
            }
        } catch (Exception e) {
            Log.e("BaseSlideOSActivity", "Error updating time: " + e.getMessage());
        }
    }
    
    protected void updateBatteryStatus() {
        try {
            android.content.IntentFilter ifilter = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
            android.content.Intent batteryStatus = registerReceiver(null, ifilter);
            
            if (batteryStatus != null && mBatteryStatus != null) {
                int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
                int batteryPct = level * 100 / scale;
                
                if (batteryPct > 80) {
                    mBatteryStatus.setText("🔋");
                } else if (batteryPct > 50) {
                    mBatteryStatus.setText("🔋");
                } else if (batteryPct > 20) {
                    mBatteryStatus.setText("🔋");
                } else {
                    mBatteryStatus.setText("🔋");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void updateConnectionStatus() {
        try {
            // Check Wi-Fi status
            android.net.wifi.WifiManager wifiManager = 
                (android.net.wifi.WifiManager) getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                mWifiStatus.setImageResource(R.drawable.ic_wifi_on);
            } else {
                mWifiStatus.setImageResource(R.drawable.ic_wifi_off);
            }
            
            // Check Bluetooth status
            android.bluetooth.BluetoothAdapter bluetoothAdapter = 
                android.bluetooth.BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                mBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_on);
            } else {
                mBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_off);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
        protected void setupListView(ListView listView) {
        mListView = listView;
        if (mListView != null) {
            // Use standard Android ListView behavior - no custom configuration needed
            mListView.setSelector(R.color.ipod_classic_blue);
            mListView.setDivider(new android.graphics.drawable.ColorDrawable(getResources().getColor(R.color.subtle_divider)));
            mListView.setDividerHeight(1);

            // Enable smooth scrolling
            mListView.setSmoothScrollbarEnabled(true);
            mListView.setFastScrollEnabled(false); // Disable fast scroll for better DPAD experience

            // Ensure first item is highlighted when ListView is first created
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    if (mListView.getCount() > 0) {
                        mListView.setSelection(0);
                        mListView.requestFocus();
                        mListView.setFocusableInTouchMode(false);
                        // Force the first item to be highlighted
                        mListView.setItemChecked(0, true);
                    }
                }
            });
        }
    }
    

    

    

    

    
    protected void updateStatusBarTitle(String title) {
        if (mStatusTitle != null) {
            mStatusTitle.setText(title);
            
            // Enable scrolling for titles longer than 28 characters
            if (title.length() > 28) {
                mStatusTitle.setSelected(true);
                mStatusTitle.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
                mStatusTitle.setMarqueeRepeatLimit(-1); // -1 means repeat forever
                mStatusTitle.setSingleLine(true);
            } else {
                mStatusTitle.setSelected(false);
                mStatusTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
                mStatusTitle.setSingleLine(true);
            }
        }
    }
    
    protected void applyCurrentTheme() {
        // Load current theme setting
        String currentTheme = getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
            .getString("theme_mode", "dark");
        
        boolean isDark = shouldUseDarkTheme(currentTheme);
        
        // Apply theme to the activity
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setBackgroundColor(getResources().getColor(
                isDark ? android.R.color.black : android.R.color.white
            ));
        }
        
        // Update text colors
        updateActivityTextColors(isDark);
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
    
    private void updateActivityTextColors(boolean isDark) {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            updateTextColorsRecursive((ViewGroup) rootView, isDark);
        }
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
                    textView.setTextColor(getResources().getColor(
                        isDark ? android.R.color.white : android.R.color.black
                    ));
                }
            } else if (child instanceof ViewGroup) {
                updateTextColorsRecursive((ViewGroup) child, isDark);
            }
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mListView != null) {
            int currentPosition = mListView.getSelectedItemPosition();
            int itemCount = mListView.getCount();
            
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (currentPosition > 0) {
                    int newPosition = currentPosition - 1;
                    mListView.smoothScrollToPosition(newPosition);
                    mListView.setSelection(newPosition);
                    mListView.setItemChecked(newPosition, true);
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (currentPosition < itemCount - 1) {
                    int newPosition = currentPosition + 1;
                    mListView.smoothScrollToPosition(newPosition);
                    mListView.setSelection(newPosition);
                    mListView.setItemChecked(newPosition, true);
                    return true;
                }
            }
        }
        
        // Let the base class handle other keys
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStatusHandler != null) {
            mStatusHandler.removeCallbacksAndMessages(null);
        }
        
        // Unregister theme receiver
        if (mThemeReceiver != null) {
            mThemeReceiver.unregister();
        }
    }
} 