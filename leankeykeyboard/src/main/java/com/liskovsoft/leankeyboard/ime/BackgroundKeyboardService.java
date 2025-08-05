package com.liskovsoft.leankeyboard.ime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class BackgroundKeyboardService extends Service {
    private static final String TAG = "BackgroundKeyboardService";
    private static final int BACK_KEYCODE = KeyEvent.KEYCODE_BACK;
    private static final long HOLD_THRESHOLD = 800; // 800ms hold time for keyboard spawn/dismiss
    
    private Handler mHandler;
    private boolean mIsBackHeld = false;
    private long mBackStartTime = 0;
    private boolean mKeyboardShown = false;
    private boolean mIsKeyboardActive = false; // Track if keyboard is currently active/visible
    private boolean mRootListenerActive = false;
    private Thread mRootListenerThread;
    private boolean mShouldConsumeBackRelease = false; // Flag to consume BACK release after long press
    private InputMethodManager mInputMethodManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Log.d(TAG, "Background keyboard service created");
        
        // Start root-based key event listener
        startRootKeyListener();
        
        // Set system settings to always show soft keyboard
        setSystemKeyboardSettings();
        
        // Start focus monitoring
        startFocusMonitoring();
    }
    
    private void startRootKeyListener() {
        if (mRootListenerThread != null && mRootListenerThread.isAlive()) {
            return; // Already running
        }
        
        mRootListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // First, find the input device for the BACK button
                    String inputDevice = findBackButtonDevice();
                    if (inputDevice == null) {
                        Log.e(TAG, "Could not find BACK button input device");
                        mRootListenerActive = false;
                        return;
                    }
                    
                    Log.d(TAG, "Found BACK button device: " + inputDevice);
                    
                    // Use getevent to listen for key events at system level
                    Process process = Runtime.getRuntime().exec("su -c 'getevent -lt " + inputDevice + "'");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    
                    String line;
                    while ((line = reader.readLine()) != null && !Thread.interrupted()) {
                        if (line.contains("EV_KEY") && line.contains("KEY_BACK")) {
                            if (line.contains("DOWN")) {
                                handleBackDown();
                            } else if (line.contains("UP")) {
                                boolean consumed = handleBackUp();
                                if (consumed) {
                                    // Block the BACK release event by not letting it propagate
                                    Log.d(TAG, "Consuming BACK release event");
                                    // The event is already consumed by our processing
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error in root key listener: " + e.getMessage());
                    // Fall back to non-root method
                    mRootListenerActive = false;
                }
            }
        });
        
        mRootListenerThread.setDaemon(true);
        mRootListenerThread.start();
        mRootListenerActive = true;
        Log.d(TAG, "Root-based key listener started");
    }
    
    private String findBackButtonDevice() {
        try {
            // List input devices and find one that has BACK button
            Process process = Runtime.getRuntime().exec("su -c 'getevent -p'");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            String currentDevice = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("add device")) {
                    // Extract device path
                    int start = line.indexOf("'") + 1;
                    int end = line.lastIndexOf("'");
                    if (start > 0 && end > start) {
                        currentDevice = line.substring(start, end);
                    }
                } else if (line.contains("KEY_BACK") && currentDevice != null) {
                    return currentDevice;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error finding BACK button device: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Background keyboard service started");
        
        if (intent != null) {
            String action = intent.getAction();
            if ("com.slideos.system.START_BACKGROUND_SERVICE".equals(action)) {
                Log.d(TAG, "Background service started");
            } else if ("com.slideos.system.KEYBOARD_STATE_CHANGED".equals(action)) {
                // Update keyboard state when it changes
                mIsKeyboardActive = intent.getBooleanExtra("isActive", false);
                Log.d(TAG, "Keyboard state changed: " + mIsKeyboardActive);
            } else if (intent.hasExtra("keyCode")) {
                // Handle key event from activity (fallback method)
                int keyCode = intent.getIntExtra("keyCode", 0);
                KeyEvent keyEvent = intent.getParcelableExtra("keyEvent");
                if (keyEvent != null && !mRootListenerActive) {
                    onKeyEvent(keyEvent);
                }
            }
        }
        
        return START_STICKY; // Restart service if killed
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        
        // Only handle BACK for keyboard spawn/dismiss
        if (keyCode == BACK_KEYCODE) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                handleBackDown();
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                return handleBackUp();
            }
        }
        return false;
    }
    
    private void handleBackDown() {
        if (!mIsBackHeld) {
            mIsBackHeld = true;
            mBackStartTime = System.currentTimeMillis();
            mShouldConsumeBackRelease = false; // Reset consumption flag
            
            // Schedule keyboard spawn/dismiss after hold threshold
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsBackHeld) {
                        if (!mIsKeyboardActive) {
                            // Keyboard not active, spawn it
                            showKeyboard();
                            mShouldConsumeBackRelease = true; // Consume the release
                        } else {
                            // Keyboard is active, prepare to dismiss on release
                            Log.d(TAG, "BACK held long enough to dismiss keyboard on release");
                            mShouldConsumeBackRelease = true; // Consume the release
                        }
                    }
                }
            }, HOLD_THRESHOLD);
        }
    }
    
    private boolean handleBackUp() {
        if (mIsBackHeld) {
            mIsBackHeld = false;
            long holdDuration = System.currentTimeMillis() - mBackStartTime;
            
            if (holdDuration >= HOLD_THRESHOLD) {
                // Long press - handle keyboard spawn/dismiss
                if (mIsKeyboardActive) {
                    // Keyboard was active, dismiss it
                    hideKeyboard();
                }
                // If keyboard wasn't active, it was already shown in the delayed runnable
                
                Log.d(TAG, "Long BACK press handled - consuming release event");
                return true; // Consume the BACK release event
            } else {
                // Short press - let the normal BACK handling take care of navigation
                Log.d(TAG, "Short BACK press - allowing normal navigation");
                return false; // Don't consume, let other apps handle it
            }
        }
        return false;
    }
    
    private void showKeyboard() {
        Log.d(TAG, "Showing keyboard via background service");
        mKeyboardShown = true;
        mIsKeyboardActive = true;
        
        // Send broadcast to show keyboard
        Intent intent = new Intent("com.slideos.system.SHOW_KEYBOARD");
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
        
        // Also try to show IME directly
        Intent imeIntent = new Intent(this, LeanbackImeService.class);
        imeIntent.setAction("SHOW_KEYBOARD");
        startService(imeIntent);
    }
    
    private void hideKeyboard() {
        Log.d(TAG, "Hiding keyboard via background service");
        mKeyboardShown = false;
        mIsKeyboardActive = false;
        
        // Send broadcast to hide keyboard
        Intent intent = new Intent("com.slideos.system.HIDE_KEYBOARD");
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
        
        // Also try to hide IME directly
        Intent imeIntent = new Intent(this, LeanbackImeService.class);
        imeIntent.setAction("HIDE_KEYBOARD");
        startService(imeIntent);
    }
    
    private void setSystemKeyboardSettings() {
        try {
            // Force Android to show soft keyboard even with physical keyboard
            Runtime.getRuntime().exec("su -c 'settings put secure show_ime_with_hard_keyboard 1'");
            Runtime.getRuntime().exec("su -c 'settings put secure show_soft_keyboard 1'");
            Runtime.getRuntime().exec("su -c 'settings put secure always_show_soft_keyboard 1'");
            
            // Additional settings to ensure keyboard shows with hardware input devices
            Runtime.getRuntime().exec("su -c 'settings put secure show_ime_with_hard_keyboard 1'");
            Runtime.getRuntime().exec("su -c 'settings put secure show_soft_keyboard 1'");
            Runtime.getRuntime().exec("su -c 'settings put secure always_show_soft_keyboard 1'");
            
            // Disable hardware keyboard detection that might hide soft keyboard
            Runtime.getRuntime().exec("su -c 'settings put secure show_ime_with_hard_keyboard 1'");
            Runtime.getRuntime().exec("su -c 'settings put secure show_soft_keyboard 1'");
            Runtime.getRuntime().exec("su -c 'settings put secure always_show_soft_keyboard 1'");
            
            // Force IME to always be available
            Runtime.getRuntime().exec("su -c 'settings put secure default_input_method com.slideos.system/.ime.LeanbackImeService'");
            
            Log.d(TAG, "System keyboard settings updated for hardware input compatibility");
        } catch (IOException e) {
            Log.e(TAG, "Failed to update system keyboard settings: " + e.getMessage());
        }
    }
    
    private void startFocusMonitoring() {
        // Use a combination of approaches for reliable text input detection
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkForTextInputFocus();
                checkForHardwareInputDevices();
                // Continue monitoring
                mHandler.postDelayed(this, 2000); // Check every 2 seconds
            }
        }, 2000);
        
        Log.d(TAG, "Focus monitoring started");
    }
    
    private void checkForHardwareInputDevices() {
        try {
            // Check for connected hardware input devices
            Process process = Runtime.getRuntime().exec("su -c 'getevent -p'");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            boolean hasHardwareKeyboard = false;
            boolean hasDpad = false;
            boolean hasGamepad = false;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("KEY_A") || line.contains("KEY_B") || line.contains("KEY_C") || 
                    line.contains("KEY_D") || line.contains("KEY_E") || line.contains("KEY_F") ||
                    line.contains("KEY_G") || line.contains("KEY_H") || line.contains("KEY_I") ||
                    line.contains("KEY_J") || line.contains("KEY_K") || line.contains("KEY_L") ||
                    line.contains("KEY_M") || line.contains("KEY_N") || line.contains("KEY_O") ||
                    line.contains("KEY_P") || line.contains("KEY_Q") || line.contains("KEY_R") ||
                    line.contains("KEY_S") || line.contains("KEY_T") || line.contains("KEY_U") ||
                    line.contains("KEY_V") || line.contains("KEY_W") || line.contains("KEY_X") ||
                    line.contains("KEY_Y") || line.contains("KEY_Z")) {
                    hasHardwareKeyboard = true;
                }
                
                if (line.contains("KEY_DPAD_UP") || line.contains("KEY_DPAD_DOWN") || 
                    line.contains("KEY_DPAD_LEFT") || line.contains("KEY_DPAD_RIGHT")) {
                    hasDpad = true;
                }
                
                if (line.contains("KEY_BUTTON_A") || line.contains("KEY_BUTTON_B") || 
                    line.contains("KEY_BUTTON_X") || line.contains("KEY_BUTTON_Y")) {
                    hasGamepad = true;
                }
            }
            
            // If hardware input devices are detected, ensure soft keyboard is available
            if (hasHardwareKeyboard || hasDpad || hasGamepad) {
                Log.d(TAG, "Hardware input devices detected - ensuring soft keyboard availability");
                ensureSoftKeyboardAvailability();
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error checking hardware input devices: " + e.getMessage());
        }
    }
    
    private void ensureSoftKeyboardAvailability() {
        try {
            // Force the system to show soft keyboard even with hardware input devices
            Runtime.getRuntime().exec("su -c 'settings put secure show_ime_with_hard_keyboard 1'");
            Runtime.getRuntime().exec("su -c 'settings put secure show_soft_keyboard 1'");
            Runtime.getRuntime().exec("su -c 'settings put secure always_show_soft_keyboard 1'");
            
            // Ensure our IME is set as default
            Runtime.getRuntime().exec("su -c 'settings put secure default_input_method com.slideos.system/.ime.LeanbackImeService'");
            
            // Force IME to be enabled
            Runtime.getRuntime().exec("su -c 'ime enable com.slideos.system/.ime.LeanbackImeService'");
            Runtime.getRuntime().exec("su -c 'ime set com.slideos.system/.ime.LeanbackImeService'");
            
            Log.d(TAG, "Soft keyboard availability ensured for hardware input devices");
        } catch (IOException e) {
            Log.e(TAG, "Error ensuring soft keyboard availability: " + e.getMessage());
        }
    }
    
    private void checkForTextInputFocus() {
        try {
            // Method 1: Check if any IME is currently active
            if (mInputMethodManager != null) {
                boolean isImeActive = mInputMethodManager.isActive();
                if (isImeActive && !mIsKeyboardActive) {
                    Log.d(TAG, "IME is active - showing keyboard");
                    showKeyboard();
                    return;
                }
            }
            
            // Method 2: Use root to check if any text input field is focused
            checkForTextInputFocusViaRoot();
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking text input focus: " + e.getMessage());
        }
    }
    
    private void checkForTextInputFocusViaRoot() {
        try {
            // Use a more targeted approach to detect text input focus
            // Check if any app is requesting text input
            Process process = Runtime.getRuntime().exec("su -c 'dumpsys input_method | grep -E \"mInputStarted|mShowRequested|mInputShown\"'");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            boolean hasActiveInput = false;
            boolean showRequested = false;
            boolean inputShown = false;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("mInputStarted=true")) {
                    hasActiveInput = true;
                }
                if (line.contains("mShowRequested=true")) {
                    showRequested = true;
                }
                if (line.contains("mInputShown=true")) {
                    inputShown = true;
                }
            }
            
            // Show keyboard if input is started but not shown, or if show is requested
            if (hasActiveInput && (showRequested || !inputShown) && !mIsKeyboardActive) {
                Log.d(TAG, "Text input field detected via IME dump - showing keyboard");
                showKeyboard();
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Error checking IME state: " + e.getMessage());
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Stop root listener thread
        if (mRootListenerThread != null && mRootListenerThread.isAlive()) {
            mRootListenerThread.interrupt();
        }
        
        Log.d(TAG, "Background keyboard service destroyed");
    }
} 