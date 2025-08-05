package com.liskovsoft.leankeyboard.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class KeyboardMonitorService extends Service {
    private static final String TAG = "KeyboardMonitorService";
    private static final long MONITOR_INTERVAL_MS = 2000; // 2 seconds
    private static final String ROCKBOX_PACKAGE = "org.rockbox";
    
    private Handler mHandler;
    private Runnable mMonitorRunnable;
    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;
    private InputMethodManager mInputMethodManager;
    private ScheduledExecutorService mExecutor;
    private boolean mIsMonitoring = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "KeyboardMonitorService created");
        
        mHandler = new Handler(Looper.getMainLooper());
        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mPackageManager = getPackageManager();
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mExecutor = Executors.newSingleThreadScheduledExecutor();
        
        mMonitorRunnable = new Runnable() {
            @Override
            public void run() {
                checkForTextInputOpportunities();
                if (mIsMonitoring) {
                    mHandler.postDelayed(this, MONITOR_INTERVAL_MS);
                }
            }
        };
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "KeyboardMonitorService started");
        
        if (intent != null && "START_MONITORING".equals(intent.getAction())) {
            startMonitoring();
        } else if (intent != null && "STOP_MONITORING".equals(intent.getAction())) {
            stopMonitoring();
        } else {
            // Default: start monitoring
            startMonitoring();
        }
        
        return START_STICKY;
    }
    
    private void startMonitoring() {
        if (!mIsMonitoring) {
            mIsMonitoring = true;
            Log.d(TAG, "Starting keyboard monitoring");
            mHandler.post(mMonitorRunnable);
        }
    }
    
    private void stopMonitoring() {
        if (mIsMonitoring) {
            mIsMonitoring = false;
            Log.d(TAG, "Stopping keyboard monitoring");
            mHandler.removeCallbacks(mMonitorRunnable);
        }
    }
    
    private void checkForTextInputOpportunities() {
        try {
            // Get the current foreground activity
            List<RunningTaskInfo> tasks = mActivityManager.getRunningTasks(1);
            if (tasks.isEmpty()) {
                return;
            }
            
            RunningTaskInfo topTask = tasks.get(0);
            if (topTask.topActivity == null) {
                return;
            }
            
            String packageName = topTask.topActivity.getPackageName();
            String className = topTask.topActivity.getClassName();
            
            // Skip if it's the rockbox app or our own app
            if (ROCKBOX_PACKAGE.equals(packageName) || 
                "com.slideos.system".equals(packageName) ||
                "com.liskovsoft.leankeyboard".equals(packageName)) {
                return;
            }
            
            // Check if this is a search activity
            boolean isSearchActivity = isSearchActivity(packageName, className);
            
            // Check if this activity has text input fields
            boolean hasTextInput = hasTextInputFields(packageName, className);
            
            Log.d(TAG, "Current activity: " + packageName + "/" + className + 
                  " (search: " + isSearchActivity + ", textInput: " + hasTextInput + ")");
            
            // Show keyboard if it's a search activity or has text input
            if (isSearchActivity || hasTextInput) {
                showKeyboard();
            }
            
        } catch (SecurityException e) {
            Log.w(TAG, "Security exception checking for text input opportunities - permissions may be needed", e);
        } catch (Exception e) {
            Log.e(TAG, "Error checking for text input opportunities", e);
        }
    }
    
    private boolean isSearchActivity(String packageName, String className) {
        try {
            // Check if the activity name contains "search"
            if (className.toLowerCase().contains("search")) {
                return true;
            }
            
            // Check for common search-related patterns
            String lowerClassName = className.toLowerCase();
            if (lowerClassName.contains("browser") || 
                lowerClassName.contains("webview") ||
                lowerClassName.contains("chrome") ||
                lowerClassName.contains("firefox") ||
                lowerClassName.contains("youtube") ||
                lowerClassName.contains("playstore") ||
                lowerClassName.contains("market")) {
                return true;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking if activity is search activity", e);
        }
        
        return false;
    }
    
    private boolean hasTextInputFields(String packageName, String className) {
        try {
            // Check if the activity name contains text input related terms
            String lowerClassName = className.toLowerCase();
            if (lowerClassName.contains("edit") || 
                lowerClassName.contains("input") || 
                lowerClassName.contains("text") ||
                lowerClassName.contains("entry") ||
                lowerClassName.contains("form") ||
                lowerClassName.contains("compose") ||
                lowerClassName.contains("message") ||
                lowerClassName.contains("chat") ||
                lowerClassName.contains("note") ||
                lowerClassName.contains("memo")) {
                return true;
            }
            
            // Check for common apps that typically have text input
            String lowerPackageName = packageName.toLowerCase();
            if (lowerPackageName.contains("whatsapp") ||
                lowerPackageName.contains("telegram") ||
                lowerPackageName.contains("messenger") ||
                lowerPackageName.contains("gmail") ||
                lowerPackageName.contains("email") ||
                lowerPackageName.contains("notes") ||
                lowerPackageName.contains("memo") ||
                lowerPackageName.contains("calculator") ||
                lowerPackageName.contains("browser") ||
                lowerPackageName.contains("chrome") ||
                lowerPackageName.contains("firefox")) {
                return true;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking if activity has text input fields", e);
        }
        
        return false;
    }
    
    private void showKeyboard() {
        try {
            // Check if our IME is enabled
            if (!isOurImeEnabled()) {
                Log.d(TAG, "Our IME is not enabled, enabling it");
                enableOurIme();
                // Give it a moment to enable before showing
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showKeyboardInternal();
                    }
                }, 1000);
            } else {
                showKeyboardInternal();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing keyboard", e);
        }
    }
    
    private void showKeyboardInternal() {
        try {
            // Show the keyboard
            Log.d(TAG, "Showing keyboard for text input opportunity");
            
            // Send broadcast to show keyboard
            Intent showKeyboardIntent = new Intent("com.slideos.system.SHOW_KEYBOARD");
            sendBroadcast(showKeyboardIntent);
            
            // Also try to show via InputMethodManager as backup
            if (mInputMethodManager != null) {
                mInputMethodManager.showSoftInputFromInputMethod(null, 0);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in showKeyboardInternal", e);
        }
    }
    
    private boolean isOurImeEnabled() {
        try {
            String enabledIme = android.provider.Settings.Secure.getString(
                getContentResolver(), 
                android.provider.Settings.Secure.ENABLED_INPUT_METHODS
            );
            
            return enabledIme != null && enabledIme.contains("com.liskovsoft.leankeyboard");
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking if our IME is enabled", e);
            return false;
        }
    }
    
    private void enableOurIme() {
        try {
            // Use root to enable our IME if available
            String command = "settings put secure enabled_input_methods com.liskovsoft.leankeyboard/.ime.LeanbackImeService";
            
            mExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        Process process = Runtime.getRuntime().exec("su -c '" + command + "'");
                        process.waitFor();
                        Log.d(TAG, "Enabled our IME via root");
                    } catch (Exception e) {
                        Log.e(TAG, "Error enabling IME via root", e);
                    }
                }
            }, 0, TimeUnit.MILLISECONDS);
            
        } catch (Exception e) {
            Log.e(TAG, "Error enabling our IME", e);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "KeyboardMonitorService destroyed");
        stopMonitoring();
        if (mExecutor != null) {
            mExecutor.shutdown();
        }
        super.onDestroy();
    }
} 