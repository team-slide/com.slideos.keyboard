package com.liskovsoft.leankeyboard.ime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.InputEvent;
import android.view.InputDevice;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BackgroundKeyboardService extends Service {
    private static final String TAG = "BackgroundKeyboardService";
    private static final int ENTER_KEYCODE = KeyEvent.KEYCODE_ENTER;
    private static final int DPAD_CENTER_KEYCODE = KeyEvent.KEYCODE_DPAD_CENTER;
    private static final long HOLD_THRESHOLD = 800; // 800ms hold time for keyboard spawn/dismiss
    
    private Handler mHandler;
    private boolean mIsEnterHeld = false;
    private long mEnterStartTime = 0;
    private boolean mKeyboardShown = false;
    private boolean mIsKeyboardActive = false; // Track if keyboard is currently active/visible
    
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "Background keyboard service created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Background keyboard service started");
        
        if (intent != null) {
            String action = intent.getAction();
            if ("com.liskovsoft.leankeyboard.START_BACKGROUND_SERVICE".equals(action)) {
                Log.d(TAG, "Background service started");
            } else if ("com.liskovsoft.leankeyboard.KEYBOARD_STATE_CHANGED".equals(action)) {
                // Update keyboard state when it changes
                mIsKeyboardActive = intent.getBooleanExtra("isActive", false);
                Log.d(TAG, "Keyboard state changed: " + mIsKeyboardActive);
            } else if (intent.hasExtra("keyCode")) {
                // Handle key event from activity
                int keyCode = intent.getIntExtra("keyCode", 0);
                KeyEvent keyEvent = intent.getParcelableExtra("keyEvent");
                if (keyEvent != null) {
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
        
        // Only handle ENTER and DPAD_CENTER for keyboard spawn/dismiss
        if (keyCode == ENTER_KEYCODE || keyCode == DPAD_CENTER_KEYCODE) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                handleEnterDown(event);
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                handleEnterUp(event);
                return true;
            }
        }
        return false;
    }
    
    private void handleEnterDown(KeyEvent event) {
        if (!mIsEnterHeld) {
            mIsEnterHeld = true;
            mEnterStartTime = System.currentTimeMillis();
            
            // Schedule keyboard spawn/dismiss after hold threshold
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mIsEnterHeld) {
                        if (!mIsKeyboardActive) {
                            // Keyboard not active, spawn it
                            showKeyboard();
                        } else {
                            // Keyboard is active, prepare to dismiss on release
                            Log.d(TAG, "ENTER held long enough to dismiss keyboard on release");
                        }
                    }
                }
            }, HOLD_THRESHOLD);
        }
    }
    
    private void handleEnterUp(KeyEvent event) {
        if (mIsEnterHeld) {
            mIsEnterHeld = false;
            long holdDuration = System.currentTimeMillis() - mEnterStartTime;
            
            if (holdDuration >= HOLD_THRESHOLD) {
                // Long press - handle keyboard spawn/dismiss
                if (mIsKeyboardActive) {
                    // Keyboard was active, dismiss it
                    hideKeyboard();
                }
                // If keyboard wasn't active, it was already shown in the delayed runnable
            } else {
                // Short press - let the normal ENTER handling take care of character input
                Log.d(TAG, "Short ENTER press - allowing normal character input");
            }
        }
    }
    
    private void showKeyboard() {
        Log.d(TAG, "Showing keyboard via background service");
        mKeyboardShown = true;
        mIsKeyboardActive = true;
        
        // Send broadcast to show keyboard
        Intent intent = new Intent("com.liskovsoft.leankeyboard.SHOW_KEYBOARD");
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
        Intent intent = new Intent("com.liskovsoft.leankeyboard.HIDE_KEYBOARD");
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
        
        // Also try to hide IME directly
        Intent imeIntent = new Intent(this, LeanbackImeService.class);
        imeIntent.setAction("HIDE_KEYBOARD");
        startService(imeIntent);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Background keyboard service destroyed");
    }
} 