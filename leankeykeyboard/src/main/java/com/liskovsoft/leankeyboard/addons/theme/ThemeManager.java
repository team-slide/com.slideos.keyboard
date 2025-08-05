package com.liskovsoft.leankeyboard.addons.theme;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.liskovsoft.leankeyboard.ime.LeanbackKeyboardView;
import com.liskovsoft.leankeyboard.utils.LeanKeyPreferences;
import com.slideos.system.R;

public class ThemeManager {
    private static final String TAG = ThemeManager.class.getSimpleName();
    private static ThemeManager sInstance;
    private final Context mContext;
    private final RelativeLayout mRootView;
    private final LeanKeyPreferences mPrefs;
    private int mCurrentAccentColor = -1;

    public ThemeManager(Context context, RelativeLayout rootView) {
        mContext = context;
        mRootView = rootView;
        mPrefs = LeanKeyPreferences.instance(mContext);
        sInstance = this;
    }
    
    public static ThemeManager getInstance(Context context) {
        if (sInstance == null) {
            // Create a minimal instance for accent color management
            sInstance = new ThemeManager(context, null);
        }
        return sInstance;
    }

    public void updateKeyboardTheme() {
        String currentThemeId = mPrefs.getCurrentTheme();

        if (LeanKeyPreferences.THEME_DEFAULT.equals(currentThemeId)) {
            applyKeyboardColors(
                    R.color.keyboard_background,
                    R.color.candidate_background,
                    R.color.enter_key_font_color,
                    R.color.key_text_default
            );
            applyShiftDrawable(-1);
        } else {
            applyForTheme((String themeId) -> {
                Resources resources = mContext.getResources();
                int keyboardBackgroundResId = resources.getIdentifier("keyboard_background_" + themeId.toLowerCase(), "color", mContext.getPackageName());
                int candidateBackgroundResId = resources.getIdentifier("candidate_background_" + themeId.toLowerCase(), "color", mContext.getPackageName());
                int enterFontColorResId = resources.getIdentifier("enter_key_font_color_" + themeId.toLowerCase(), "color", mContext.getPackageName());
                int keyTextColorResId = resources.getIdentifier("key_text_default_" + themeId.toLowerCase(), "color", mContext.getPackageName());

                applyKeyboardColors(
                        keyboardBackgroundResId,
                        candidateBackgroundResId,
                        enterFontColorResId,
                        keyTextColorResId
                );

                int shiftLockOnResId = resources.getIdentifier("ic_ime_shift_lock_on_" + themeId.toLowerCase(), "drawable", mContext.getPackageName());

                applyShiftDrawable(shiftLockOnResId);
            });
        }
    }

    public void updateSuggestionsTheme() {
        String currentTheme = mPrefs.getCurrentTheme();

        if (LeanKeyPreferences.THEME_DEFAULT.equals(currentTheme)) {
            applySuggestionsColors(
                    R.color.candidate_font_color
            );
        } else {
            applyForTheme((String themeId) -> {
                Resources resources = mContext.getResources();
                int candidateFontColorResId = resources.getIdentifier("candidate_font_color_" + themeId.toLowerCase(), "color", mContext.getPackageName());
                applySuggestionsColors(candidateFontColorResId);
            });
        }
    }

    private void applyKeyboardColors(
            int keyboardBackground,
            int candidateBackground,
            int enterFontColor,
            int keyTextColor) {

        RelativeLayout rootLayout = mRootView.findViewById(R.id.root_ime);

        if (rootLayout != null) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(mContext, keyboardBackground));
        }

        View candidateLayout = mRootView.findViewById(R.id.candidate_background);

        if (candidateLayout != null) {
            candidateLayout.setBackgroundColor(ContextCompat.getColor(mContext, candidateBackground));
        }

        Button enterButton = mRootView.findViewById(R.id.enter);

        if (enterButton != null) {
            enterButton.setTextColor(ContextCompat.getColor(mContext, enterFontColor));
        }

        LeanbackKeyboardView keyboardView = mRootView.findViewById(R.id.main_keyboard);

        if (keyboardView != null) {
            keyboardView.setKeyTextColor(ContextCompat.getColor(mContext, keyTextColor));
        }
        
        // Apply theme to all other UI elements
        applyThemeToAllElements(keyboardBackground, keyTextColor);
    }
    
    private void applyThemeToAllElements(int backgroundColor, int textColor) {
        // Apply to status bar if present
        View statusBar = mRootView.findViewById(R.id.unified_status_bar);
        if (statusBar != null) {
            statusBar.setBackgroundColor(ContextCompat.getColor(mContext, backgroundColor));
            
            // Update status bar text colors
            TextView statusTitle = statusBar.findViewById(R.id.status_title);
            if (statusTitle != null) {
                statusTitle.setTextColor(ContextCompat.getColor(mContext, textColor));
            }
            
            TextView timeStatus = statusBar.findViewById(R.id.time_status);
            if (timeStatus != null) {
                timeStatus.setTextColor(ContextCompat.getColor(mContext, textColor));
            }
        }
        
        // Apply to any other text elements
        applyTextColorToChildren(mRootView, textColor);
    }
    
    private void applyTextColorToChildren(View parent, int textColor) {
        if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof TextView) {
                    ((TextView) child).setTextColor(ContextCompat.getColor(mContext, textColor));
                } else if (child instanceof Button) {
                    ((Button) child).setTextColor(ContextCompat.getColor(mContext, textColor));
                } else {
                    applyTextColorToChildren(child, textColor);
                }
            }
        }
    }

    private void applySuggestionsColors(int candidateFontColor) {
        LinearLayout suggestions = mRootView.findViewById(R.id.suggestions);

        if (suggestions != null) {
            int childCount = suggestions.getChildCount();

            Log.d(TAG, "Number of suggestions: " + childCount);

            for (int i = 0; i < childCount; i++) {
                View child = suggestions.getChildAt(i);

                Button candidateButton = child.findViewById(R.id.text);

                if (candidateButton != null) {
                    candidateButton.setTextColor(ContextCompat.getColor(mContext, candidateFontColor));
                }
            }
        }
    }

    private void applyShiftDrawable(int resId) {
        LeanbackKeyboardView keyboardView = mRootView.findViewById(R.id.main_keyboard);

        if (keyboardView != null && resId > 0) {
            Drawable drawable = ContextCompat.getDrawable(mContext, resId);

            keyboardView.setCapsLockDrawable(drawable);
        }
    }

    private void applyForTheme(ThemeCallback callback) {
        String currentThemeId = mPrefs.getCurrentTheme();
        Resources resources = mContext.getResources();
        String[] themes = resources.getStringArray(R.array.keyboard_themes);

        for (String theme : themes) {
            String[] split = theme.split("\\|");
            String themeName = split[0];
            String themeId = split[1];

            if (currentThemeId.equals(themeId)) {
                callback.onThemeFound(themeId);

                break;
            }
        }
    }

    private interface ThemeCallback {
        void onThemeFound(String themeId);
    }
    
    public void setAccentColor(int color) {
        mCurrentAccentColor = color;
        
        // Save to preferences
        mContext.getSharedPreferences("slideOS_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("accent_color_value", color)
            .apply();
        
        // Apply to UI elements that use accent color
        applyAccentColorToUI();
    }
    
    public int getAccentColor() {
        if (mCurrentAccentColor == -1) {
            // Load from preferences
            mCurrentAccentColor = mContext.getSharedPreferences("slideOS_prefs", Context.MODE_PRIVATE)
                .getInt("accent_color_value", mContext.getResources().getColor(R.color.ipod_classic_blue));
        }
        return mCurrentAccentColor;
    }
    
    private void applyAccentColorToUI() {
        if (mRootView == null) return;
        
        // Apply accent color to ListView selectors and other UI elements
        // This will be called when the accent color changes
        Log.d(TAG, "Applying accent color: " + String.format("#%06X", (0xFFFFFF & mCurrentAccentColor)));
    }
    
    public void applyTheme(String themeName) {
        // Save theme preference
        mContext.getSharedPreferences("slideOS_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("theme_mode", themeName)
            .apply();
        
        // Apply theme colors
        if ("dark".equals(themeName)) {
            applyDarkTheme();
        } else if ("light".equals(themeName)) {
            applyLightTheme();
        } else if ("auto".equals(themeName)) {
            // For auto theme, determine based on current time
            if (isNightTime()) {
                applyDarkTheme();
            } else {
                applyLightTheme();
            }
        }
        
        // Broadcast theme change to all activities and services
        android.content.Intent themeChangeIntent = new android.content.Intent("com.slideos.system.THEME_CHANGED");
        themeChangeIntent.putExtra("theme_mode", themeName);
        mContext.sendBroadcast(themeChangeIntent);
        
        // Also update keyboard theme if available
        updateKeyboardTheme();
    }
    
    private boolean isNightTime() {
        // Get current hour (0-23)
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        
        // Consider night time from 6 PM (18:00) to 6 AM (06:00)
        return hour >= 18 || hour < 6;
    }
    
    private void applyDarkTheme() {
        // Apply dark theme colors
        if (mRootView != null) {
            // Set dark background
            mRootView.setBackgroundColor(mContext.getResources().getColor(android.R.color.black));
            
            // Apply dark theme to all child elements
            applyThemeToAllElements(
                mContext.getResources().getColor(android.R.color.black),
                mContext.getResources().getColor(android.R.color.white)
            );
        }
        
        // Update the current activity's theme
        updateCurrentActivityTheme(true);
    }
    
    private void applyLightTheme() {
        // Apply light theme colors
        if (mRootView != null) {
            // Set light background
            mRootView.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
            
            // Apply light theme to all child elements
            applyThemeToAllElements(
                mContext.getResources().getColor(android.R.color.white),
                mContext.getResources().getColor(android.R.color.black)
            );
        }
        
        // Update the current activity's theme
        updateCurrentActivityTheme(false);
    }
    
    private void updateCurrentActivityTheme(boolean isDark) {
        // This method will be called to update the current activity's appearance
        // We'll need to refresh the current activity to see the theme changes
        Log.d(TAG, "Theme changed to: " + (isDark ? "Dark" : "Light"));
        
        // Force a refresh of the current activity
        if (mContext instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) mContext;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Refresh the activity's view
                    View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
                    if (rootView != null) {
                        rootView.invalidate();
                    }
                }
            });
        }
    }
}
