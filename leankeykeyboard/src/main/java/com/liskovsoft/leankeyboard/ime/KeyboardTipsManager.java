package com.liskovsoft.leankeyboard.ime;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.util.Log;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class KeyboardTipsManager {
    private static final String TAG = "KeyboardTipsManager";
    private static final String PREFS_NAME = "keyboard_tips_prefs";
    private static final String KEY_FIRST_INSTALL_TIME = "first_install_time";
    private static final String KEY_TIPS_SHOWN = "tips_shown";
    private static final String KEY_ALWAYS_SHOW_TIPS = "always_show_tips";
    private static final long TIPS_DURATION_MS = 5 * 60 * 1000; // 5 minutes
    private static final long TIP_DISPLAY_DURATION_MS = 4000; // 4 seconds per tip
    private static final long TIP_FADE_DURATION_MS = 800; // 800ms fade in/out

    private Context mContext;
    private Handler mHandler;
    private SharedPreferences mPrefs;
    private boolean mTipsActive = false;
    private int mCurrentTipIndex = 0;
    private TextView mTipView;
    private boolean mIsScrolling = false;
    private int mScrollPosition = 0;
    private String mCurrentTipText = "";

    private final String[] TIPS = {
        "Scroll to type",
        "Press Play button to change between letters, capitals, numbers, symbols",
        "Press Previous Track to delete letters",
        "Press Next Track to add a space",
        "Press Back to close keyboard when finished"
    };

    public KeyboardTipsManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void checkAndStartTips() {
        long firstInstallTime = mPrefs.getLong(KEY_FIRST_INSTALL_TIME, 0);
        boolean alwaysShowTips = mPrefs.getBoolean(KEY_ALWAYS_SHOW_TIPS, false);

        if (firstInstallTime == 0) {
            // First time installation
            firstInstallTime = System.currentTimeMillis();
            mPrefs.edit().putLong(KEY_FIRST_INSTALL_TIME, firstInstallTime).apply();
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceInstall = currentTime - firstInstallTime;

        // Show tips if within first 5 minutes OR if always show tips is enabled
        if (timeSinceInstall <= TIPS_DURATION_MS || alwaysShowTips) {
            startTips();
        }
    }

    public void startTips() {
        if (mTipsActive) {
            return; // Already showing tips
        }

        mTipsActive = true;
        mCurrentTipIndex = 0;
        Log.d(TAG, "Starting keyboard tips");

        // Create tip view
        createTipView();
        
        // Show first tip
        showNextTip();
    }

    private void createTipView() {
        if (mTipView != null) {
            return; // Already created
        }

        mTipView = new TextView(mContext);
        mTipView.setTextColor(Color.WHITE);
        mTipView.setTextSize(14);
        mTipView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        mTipView.setGravity(Gravity.CENTER);
        mTipView.setBackgroundColor(Color.parseColor("#80000000")); // Semi-transparent black
        mTipView.setPadding(20, 12, 20, 12);
        mTipView.setSingleLine(true);
        mTipView.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
        mTipView.setMarqueeRepeatLimit(-1); // MARQUEE_FOREVER equivalent
        mTipView.setSelected(true);
        mTipView.setHorizontallyScrolling(true); // Correct method for Android 4.2.2

        // Set initial visibility to invisible
        mTipView.setAlpha(0.0f);
        
        // Add to keyboard container if available
        if (mContext instanceof LeanbackImeService) {
            LeanbackImeService imeService = (LeanbackImeService) mContext;
            // Use reflection to access private fields
            try {
                java.lang.reflect.Field controllerField = LeanbackImeService.class.getDeclaredField("mKeyboardController");
                controllerField.setAccessible(true);
                Object controller = controllerField.get(imeService);
                
                if (controller != null) {
                    java.lang.reflect.Field containerField = controller.getClass().getDeclaredField("mContainer");
                    containerField.setAccessible(true);
                    Object container = containerField.get(controller);
                    
                    if (container != null) {
                        java.lang.reflect.Method addTipMethod = container.getClass().getMethod("addTipView", View.class);
                        addTipMethod.invoke(container, mTipView);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding tip view to container", e);
            }
        }
    }

    private void showNextTip() {
        if (!mTipsActive || mCurrentTipIndex >= TIPS.length) {
            stopTips();
            return;
        }

        String tipText = TIPS[mCurrentTipIndex];
        mCurrentTipText = tipText;
        
        Log.d(TAG, "Showing tip: " + tipText);

        // Fade in
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(TIP_FADE_DURATION_MS);
        fadeIn.setFillAfter(true);
        
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mTipView.setText(tipText);
                mTipView.setVisibility(View.VISIBLE);
                
                // Check if text needs scrolling
                mTipView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mTipView.getLayout() != null && 
                            mTipView.getLayout().getLineWidth(0) > mTipView.getWidth()) {
                            // Text is too long, enable scrolling
                            mTipView.setSelected(true);
                            mIsScrolling = true;
                        } else {
                            mIsScrolling = false;
                        }
                    }
                });
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Schedule fade out after display duration
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fadeOutTip();
                    }
                }, TIP_DISPLAY_DURATION_MS);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        mTipView.startAnimation(fadeIn);
    }

    private void fadeOutTip() {
        if (!mTipsActive) {
            return;
        }

        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(TIP_FADE_DURATION_MS);
        fadeOut.setFillAfter(true);
        
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mTipView.setVisibility(View.GONE);
                mCurrentTipIndex++;
                
                // Show next tip after a short delay
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showNextTip();
                    }
                }, 500); // 500ms delay between tips
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        mTipView.startAnimation(fadeOut);
    }

    public void stopTips() {
        mTipsActive = false;
        mHandler.removeCallbacksAndMessages(null);
        
        if (mTipView != null) {
            mTipView.setVisibility(View.GONE);
            mTipView.clearAnimation();
            
            // Remove from keyboard container if available
            if (mContext instanceof LeanbackImeService) {
                LeanbackImeService imeService = (LeanbackImeService) mContext;
                // Use reflection to access private fields
                try {
                    java.lang.reflect.Field controllerField = LeanbackImeService.class.getDeclaredField("mKeyboardController");
                    controllerField.setAccessible(true);
                    Object controller = controllerField.get(imeService);
                    
                    if (controller != null) {
                        java.lang.reflect.Field containerField = controller.getClass().getDeclaredField("mContainer");
                        containerField.setAccessible(true);
                        Object container = containerField.get(controller);
                        
                        if (container != null) {
                            java.lang.reflect.Method removeTipMethod = container.getClass().getMethod("removeTipView", View.class);
                            removeTipMethod.invoke(container, mTipView);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error removing tip view from container", e);
                }
            }
        }
        
        Log.d(TAG, "Stopped keyboard tips");
    }

    public void resetTips() {
        mPrefs.edit().remove(KEY_FIRST_INSTALL_TIME).apply();
        Log.d(TAG, "Reset tips - will show on next keyboard launch");
    }

    public void setAlwaysShowTips(boolean alwaysShow) {
        mPrefs.edit().putBoolean(KEY_ALWAYS_SHOW_TIPS, alwaysShow).apply();
        Log.d(TAG, "Always show tips set to: " + alwaysShow);
    }

    public boolean isAlwaysShowTipsEnabled() {
        return mPrefs.getBoolean(KEY_ALWAYS_SHOW_TIPS, false);
    }

    public View getTipView() {
        return mTipView;
    }

    public boolean isTipsActive() {
        return mTipsActive;
    }
} 