package com.liskovsoft.leankeyboard.activity.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.liskovsoft.leankeyboard.activity.BaseSlideOSActivity;
import com.liskovsoft.leankeyboard.addons.theme.ThemeManager;
import com.slideos.system.R;

public class AppearanceSettingsActivity extends BaseSlideOSActivity {
    
    private String mCurrentTheme = "dark"; // Default to dark
    private String mCurrentAccentColor = "accent_blue"; // Default to blue
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appearance_settings);
        
        initializeViews();
        loadCurrentSettings();
        setupClickListeners();
        
        // Apply current theme immediately
        refreshActivityTheme();
        
        // Set initial focus on theme option
        setInitialFocus();
    }
    
    private void setInitialFocus() {
        // Set focus on theme option initially
        TextView themeOption = findViewById(R.id.theme_option);
        if (themeOption != null) {
            themeOption.requestFocus();
        }
    }
    
    private void initializeViews() {
        // Initialize color views
        int[] colorIds = {
            R.id.color_blue, R.id.color_green, R.id.color_orange, R.id.color_purple,
            R.id.color_red, R.id.color_teal, R.id.color_yellow, R.id.color_pink,
            R.id.color_custom
        };
        
        for (int colorId : colorIds) {
            FrameLayout colorView = findViewById(colorId);
            if (colorView != null) {
                colorView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectColor(v);
                    }
                });
            }
        }
    }
    
    private void loadCurrentSettings() {
        // Load current theme setting
        mCurrentTheme = getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
            .getString("theme_mode", "dark");
        
        // Load current accent color
        mCurrentAccentColor = getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
            .getString("accent_color", "accent_blue");
        
        // Set the selected color
        updateColorSelection();
        
        // Update theme display
        updateThemeDisplay();
    }
    
    private void setupClickListeners() {
        // Theme selection is now handled via DPAD navigation
    }
    
    private void selectColor(View colorView) {
        String colorTag = (String) colorView.getTag();
        
        if ("accent_custom".equals(colorTag)) {
            // Open custom color picker
            Intent customColorIntent = new Intent(this, CustomColorActivity.class);
            startActivityForResult(customColorIntent, 1001);
        } else {
            // Select preset color
            mCurrentAccentColor = colorTag;
            updateColorSelection();
            
            // Save preference
            getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
                .edit()
                .putString("accent_color", mCurrentAccentColor)
                .apply();
            
            // Apply accent color
            applyAccentColor(colorTag);
            
            showToast("Accent color updated");
        }
    }
    
    private void updateColorSelection() {
        // Clear all selections
        int[] colorIds = {
            R.id.color_blue, R.id.color_green, R.id.color_orange, R.id.color_purple,
            R.id.color_red, R.id.color_teal, R.id.color_yellow, R.id.color_pink,
            R.id.color_custom
        };
        
        for (int colorId : colorIds) {
            FrameLayout colorView = findViewById(colorId);
            if (colorView != null) {
                colorView.setSelected(false);
            }
        }
        
        // Select current color
        int selectedColorId = getColorIdFromTag(mCurrentAccentColor);
        if (selectedColorId != 0) {
            FrameLayout selectedView = findViewById(selectedColorId);
            if (selectedView != null) {
                selectedView.setSelected(true);
            }
        }
    }
    
    private int getColorIdFromTag(String colorTag) {
        switch (colorTag) {
            case "accent_blue": return R.id.color_blue;
            case "accent_green": return R.id.color_green;
            case "accent_orange": return R.id.color_orange;
            case "accent_purple": return R.id.color_purple;
            case "accent_red": return R.id.color_red;
            case "accent_teal": return R.id.color_teal;
            case "accent_yellow": return R.id.color_yellow;
            case "accent_pink": return R.id.color_pink;
            case "accent_custom": return R.id.color_custom;
            default: return R.id.color_blue;
        }
    }
    
    private void applyAccentColor(String colorTag) {
        int colorResId = getColorResourceId(colorTag);
        if (colorResId != 0) {
            // Save the accent color preference
            getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
                .edit()
                .putString("accent_color", colorTag)
                .apply();
            
            // Update the ipod_classic_blue color resource dynamically
            // This will affect all UI elements that use this color
            ThemeManager.getInstance(this).setAccentColor(getResources().getColor(colorResId));
            
            // Update current accent color
            mCurrentAccentColor = colorTag;
            
            // Show feedback
            String colorName = colorTag.replace("accent_", "").toUpperCase();
            showToast("Accent color set to " + colorName);
        }
    }
    
    private int getColorResourceId(String colorTag) {
        switch (colorTag) {
            case "accent_blue": return R.color.accent_blue;
            case "accent_green": return R.color.accent_green;
            case "accent_orange": return R.color.accent_orange;
            case "accent_purple": return R.color.accent_purple;
            case "accent_red": return R.color.accent_red;
            case "accent_teal": return R.color.accent_teal;
            case "accent_yellow": return R.color.accent_yellow;
            case "accent_pink": return R.color.accent_pink;
            default: return R.color.accent_blue;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            int customColor = data.getIntExtra("custom_color", Color.BLUE);
            
            // Save custom color
            getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
                .edit()
                .putString("accent_color", "accent_custom")
                .putInt("custom_color_value", customColor)
                .apply();
            
            mCurrentAccentColor = "accent_custom";
            updateColorSelection();
            
            // Apply custom color
            ThemeManager.getInstance(this).setAccentColor(customColor);
            
            showToast("Custom color applied");
        }
    }
    
    private void toggleTheme() {
        // Toggle between dark, light, and auto themes
        if ("dark".equals(mCurrentTheme)) {
            mCurrentTheme = "light";
            showToast("Light mode enabled");
        } else if ("light".equals(mCurrentTheme)) {
            mCurrentTheme = "auto";
            showToast("Auto mode enabled");
        } else {
            mCurrentTheme = "dark";
            showToast("Dark mode enabled");
        }
        
        // Save theme preference
        getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
            .edit()
            .putString("theme_mode", mCurrentTheme)
            .apply();
        
        // Apply theme
        ThemeManager.getInstance(this).applyTheme(mCurrentTheme);
        
        // Update theme display
        updateThemeDisplay();
        
        // Refresh the entire activity UI
        refreshActivityTheme();
    }
    
    private void refreshActivityTheme() {
        // Apply theme colors to the current activity
        boolean isDark = shouldUseDarkTheme();
        
        // Update main layout background
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setBackgroundColor(getResources().getColor(
                isDark ? android.R.color.black : android.R.color.white
            ));
        }
        
        // Update all TextView colors recursively
        updateTextViewColors(isDark);
        
        // Update ListView colors if present
        updateListViewColors(isDark);
        
        // Update status bar colors (keep them white for visibility)
        updateStatusBarColors();
        
        // Force a redraw
        if (rootView != null) {
            rootView.invalidate();
        }
        
        // Also update the window background
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(
            isDark ? android.R.color.black : android.R.color.white
        ));
    }
    
    private void updateStatusBarColors() {
        // Keep status bar elements white for visibility
        TextView statusTitle = findViewById(R.id.unified_status_title);
        TextView timeStatus = findViewById(R.id.unified_time_status);
        TextView batteryStatus = findViewById(R.id.unified_battery_status);
        
        if (statusTitle != null) {
            statusTitle.setTextColor(getResources().getColor(android.R.color.white));
        }
        if (timeStatus != null) {
            timeStatus.setTextColor(getResources().getColor(android.R.color.white));
        }
        if (batteryStatus != null) {
            batteryStatus.setTextColor(getResources().getColor(android.R.color.white));
        }
    }
    
    private boolean shouldUseDarkTheme() {
        if ("dark".equals(mCurrentTheme)) {
            return true;
        } else if ("light".equals(mCurrentTheme)) {
            return false;
        } else if ("auto".equals(mCurrentTheme)) {
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
    
    private void updateTextViewColors(boolean isDark) {
        // Find all TextViews and update their colors
        View rootView = findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            updateTextViewColorsRecursive((ViewGroup) rootView, isDark);
        }
    }
    
    private void updateTextViewColorsRecursive(ViewGroup parent, boolean isDark) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                // Skip the status bar elements
                if (textView.getId() != R.id.unified_status_title && 
                    textView.getId() != R.id.unified_time_status &&
                    textView.getId() != R.id.unified_battery_status) {
                    textView.setTextColor(getResources().getColor(
                        isDark ? android.R.color.white : android.R.color.black
                    ));
                }
            } else if (child instanceof ViewGroup) {
                updateTextViewColorsRecursive((ViewGroup) child, isDark);
            }
        }
    }
    
    private void updateListViewColors(boolean isDark) {
        // Update ListView background and text colors
        // This would be handled by the adapter in a real implementation
    }
    
    private void updateThemeDisplay() {
        TextView themeOption = findViewById(R.id.theme_option);
        if (themeOption != null) {
            String themeText = getThemeDisplayName(mCurrentTheme);
            themeOption.setText(themeText);
        }
    }
    
    private String getThemeDisplayName(String theme) {
        switch (theme) {
            case "dark": return "Dark";
            case "light": return "Light";
            case "auto": return "Auto";
            default: return "Dark";
        }
    }
    
    private void clearColorSelection() {
        int[] colorIds = {
            R.id.color_blue, R.id.color_green, R.id.color_orange, R.id.color_purple,
            R.id.color_red, R.id.color_teal, R.id.color_yellow, R.id.color_pink,
            R.id.color_custom
        };
        
        for (int colorId : colorIds) {
            FrameLayout colorView = findViewById(colorId);
            if (colorView != null) {
                colorView.setSelected(false);
            }
        }
    }
    
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected String getActivityTitle() {
        return "Appearance";
    }
    

    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle DPAD center/enter for direct selection (iPod Classic style)
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            // Check if theme option is focused
            TextView themeOption = findViewById(R.id.theme_option);
            if (themeOption != null && themeOption.hasFocus()) {
                toggleTheme();
                return true;
            }
            
            // Check if any color is focused
            int[] colorIds = {
                R.id.color_blue, R.id.color_green, R.id.color_orange, R.id.color_purple,
                R.id.color_red, R.id.color_teal, R.id.color_yellow, R.id.color_pink,
                R.id.color_custom
            };
            
            for (int colorId : colorIds) {
                FrameLayout colorView = findViewById(colorId);
                if (colorView != null && colorView.hasFocus()) {
                    selectColor(colorView);
                    return true;
                }
            }
            
            // If nothing is focused, focus and select the first color
            FrameLayout firstColorView = findViewById(colorIds[0]);
            if (firstColorView != null) {
                firstColorView.requestFocus();
                selectColor(firstColorView);
                return true;
            }
        }
        
        // Handle DPAD navigation with boustrophedon (snake-like) pattern
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || 
            keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            // Find currently focused view
            View focusedView = getCurrentFocus();
            
            if (focusedView != null) {
                // Handle navigation between theme option and color grid
                if (focusedView.getId() == R.id.theme_option) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        // Move to first color
                        FrameLayout firstColor = findViewById(R.id.color_blue);
                        if (firstColor != null) {
                            firstColor.requestFocus();
                            return true;
                        }
                    }
                } else {
                    // Handle boustrophedon navigation through color grid
                    // Layout: Blue(0) -> Green(1) -> Orange(2) -> Purple(3)
                    //         Pink(7) <- Yellow(6) <- Teal(5) <- Red(4)
                    //         Custom(8)
                    int[] colorIds = {
                        R.id.color_blue, R.id.color_green, R.id.color_orange, R.id.color_purple,
                        R.id.color_red, R.id.color_teal, R.id.color_yellow, R.id.color_pink,
                        R.id.color_custom
                    };
                    
                    int currentIndex = -1;
                    for (int i = 0; i < colorIds.length; i++) {
                        if (focusedView.getId() == colorIds[i]) {
                            currentIndex = i;
                            break;
                        }
                    }
                    
                    if (currentIndex != -1) {
                        int newIndex = currentIndex;
                        
                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                            if (currentIndex == 8) {
                                // From custom, go to pink (end of second row)
                                newIndex = 7;
                            } else if (currentIndex >= 4) {
                                // From second row, go to corresponding position in first row
                                newIndex = currentIndex - 4;
                            } else {
                                // From first row, go to theme option
                                TextView themeOption = findViewById(R.id.theme_option);
                                if (themeOption != null) {
                                    themeOption.requestFocus();
                                    return true;
                                }
                            }
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            if (currentIndex < 4) {
                                // From first row, go to corresponding position in second row
                                newIndex = currentIndex + 4;
                            } else if (currentIndex < 8) {
                                // From second row, go to custom
                                newIndex = 8;
                            }
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            if (currentIndex == 0) {
                                // From blue, go to theme option
                                TextView themeOption = findViewById(R.id.theme_option);
                                if (themeOption != null) {
                                    themeOption.requestFocus();
                                    return true;
                                }
                            } else if (currentIndex <= 3) {
                                // First row: left to right
                                newIndex = currentIndex - 1;
                            } else if (currentIndex <= 7) {
                                // Second row: right to left
                                newIndex = currentIndex + 1;
                            } else if (currentIndex == 8) {
                                // From custom, go to yellow
                                newIndex = 6;
                            }
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            if (currentIndex <= 3) {
                                // First row: left to right
                                if (currentIndex < 3) {
                                    newIndex = currentIndex + 1;
                                } else {
                                    // From purple, go to red (start of second row)
                                    newIndex = 4;
                                }
                            } else if (currentIndex <= 7) {
                                // Second row: right to left
                                if (currentIndex > 4) {
                                    newIndex = currentIndex - 1;
                                } else {
                                    // From red, go to custom
                                    newIndex = 8;
                                }
                            } else if (currentIndex == 8) {
                                // From custom, go to pink
                                newIndex = 7;
                            }
                        }
                        
                        // Clamp to valid range
                        newIndex = Math.max(0, Math.min(colorIds.length - 1, newIndex));
                        
                        // Focus new color and scroll if necessary
                        FrameLayout newColor = findViewById(colorIds[newIndex]);
                        if (newColor != null) {
                            newColor.requestFocus();
                            // Ensure the focused item is visible
                            newColor.post(new Runnable() {
                                @Override
                                public void run() {
                                    newColor.requestFocusFromTouch();
                                }
                            });
                            return true;
                        }
                    }
                }
            } else {
                // If nothing is focused, focus the theme option
                TextView themeOption = findViewById(R.id.theme_option);
                if (themeOption != null) {
                    themeOption.requestFocus();
                    return true;
                }
            }
        }
        
        return super.onKeyDown(keyCode, event);
    }
} 