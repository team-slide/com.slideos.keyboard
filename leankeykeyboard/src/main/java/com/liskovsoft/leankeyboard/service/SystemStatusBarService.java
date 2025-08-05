package com.liskovsoft.leankeyboard.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.slideos.system.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SystemStatusBarService extends AccessibilityService {
    private static final String TAG = "SystemStatusBarService";
    
    private WindowManager mWindowManager;
    private View mStatusBarView;
    private TextView mStatusTitle;
    private TextView mTimeStatus;
    private ImageView mWifiStatus;
    private ImageView mBluetoothStatus;
    private TextView mBatteryStatus;
    private Handler mHandler;
    
    private String mCurrentAppTitle = "slideOS System";
    private boolean mIsFullScreenApp = false;
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Handle accessibility events
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            String className = event.getClassName() != null ? event.getClassName().toString() : "";
            
            // Check if it's a full screen app or system UI
            mIsFullScreenApp = isFullScreenApp(packageName, className);
            
            // Update status bar visibility
            updateStatusBarVisibility();
            
            // Update title based on current app
            updateStatusBarTitle(packageName, className);
        }
    }
    
    @Override
    public void onInterrupt() {
        // Handle interruption
    }
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        initializeStatusBar();
    }
    
    private void initializeStatusBar() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mHandler = new Handler();
        
        // Create status bar view
        LayoutInflater inflater = LayoutInflater.from(this);
        mStatusBarView = inflater.inflate(R.layout.unified_status_bar, null);
        
        // Initialize views
        mStatusTitle = mStatusBarView.findViewById(R.id.unified_status_title);
        mTimeStatus = mStatusBarView.findViewById(R.id.unified_time_status);
        mWifiStatus = mStatusBarView.findViewById(R.id.unified_wifi_status);
        mBluetoothStatus = mStatusBarView.findViewById(R.id.unified_bluetooth_status);
        mBatteryStatus = mStatusBarView.findViewById(R.id.unified_battery_status);
        
        // Set initial title
        mStatusTitle.setText(mCurrentAppTitle);
        
        // Add status bar to window
        addStatusBarToWindow();
        
        // Start status updates
        startStatusUpdates();
    }
    
    private void addStatusBarToWindow() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                28, // Height in dp
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        
        try {
            mWindowManager.addView(mStatusBarView, params);
            Log.d(TAG, "Status bar overlay added successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error adding status bar overlay: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateStatusBarVisibility() {
        if (mStatusBarView != null) {
            mStatusBarView.setVisibility(mIsFullScreenApp ? View.GONE : View.VISIBLE);
        }
    }
    
    private void updateStatusBarTitle(String packageName, String className) {
        if (packageName.contains("slideos")) {
            mCurrentAppTitle = "slideOS System";
        } else if (packageName.contains("settings")) {
            mCurrentAppTitle = "Settings";
        } else if (packageName.contains("launcher")) {
            mCurrentAppTitle = "Home";
        } else {
            // Try to get app name from package manager
            try {
                mCurrentAppTitle = getPackageManager().getApplicationLabel(
                    getPackageManager().getApplicationInfo(packageName, 0)
                ).toString();
            } catch (Exception e) {
                mCurrentAppTitle = packageName;
            }
        }
        
        if (mStatusTitle != null) {
            mStatusTitle.setText(mCurrentAppTitle);
            
            // Enable scrolling for titles longer than 28 characters
            if (mCurrentAppTitle.length() > 28) {
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
    
    private boolean isFullScreenApp(String packageName, String className) {
        // Check if the current app is full screen or hides status bar
        // Only hide for truly full-screen apps, not regular apps
        return packageName.contains("com.android.systemui") ||
               packageName.contains("com.android.launcher") ||
               className.contains("FullscreenActivity") ||
               className.contains("ImmersiveActivity") ||
               className.contains("VideoPlayerActivity") ||
               className.contains("GameActivity") ||
               packageName.contains("com.android.gallery3d") ||
               packageName.contains("com.google.android.youtube");
    }
    
    private void startStatusUpdates() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateStatusBar();
                mHandler.postDelayed(this, 10000); // Update every 10 seconds for more accurate time
            }
        }, 1000);
    }
    
    private void updateStatusBar() {
        // Update time
        updateDeviceTime();
        
        // Update battery status
        updateBatteryStatus();
        
        // Update connection status
        updateConnectionStatus();
    }
    
    private void updateDeviceTime() {
        try {
            // Get the current system time using System.currentTimeMillis() for more reliable time
            long currentTimeMillis = System.currentTimeMillis();
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(currentTimeMillis);
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentTime = sdf.format(calendar.getTime());
            
            if (mTimeStatus != null) {
                mTimeStatus.setText(currentTime);
                Log.d(TAG, "Updated time: " + currentTime);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating time: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateBatteryStatus() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, ifilter);
            
            if (batteryStatus != null && mBatteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
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
    
    private void updateConnectionStatus() {
        try {
            // Check Wi-Fi status
            android.net.wifi.WifiManager wifiManager = 
                (android.net.wifi.WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mStatusBarView != null && mWindowManager != null) {
            try {
                mWindowManager.removeView(mStatusBarView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
} 