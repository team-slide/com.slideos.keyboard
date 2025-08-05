package com.liskovsoft.leankeyboard.activity.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import com.liskovsoft.leankeyboard.activity.BaseSlideOSActivity;
import com.slideos.system.R;

public class CustomColorActivity extends BaseSlideOSActivity {
    
    private View mColorPreview;
    private EditText mHexInput;
    private SeekBar mRedSeekBar, mGreenSeekBar, mBlueSeekBar;
    private EditText mRedInput, mGreenInput, mBlueInput;
    private Button mApplyButton, mCancelButton;
    
    private int mCurrentColor = Color.BLUE;
    private boolean mIsUpdatingFromSeekBar = false;
    private boolean mIsUpdatingFromHex = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_color);
        
        initializeViews();
        setupListeners();
        loadCurrentColor();
    }
    
    private void initializeViews() {
        mColorPreview = findViewById(R.id.color_preview);
        mHexInput = findViewById(R.id.hex_input);
        mRedSeekBar = findViewById(R.id.red_seekbar);
        mGreenSeekBar = findViewById(R.id.green_seekbar);
        mBlueSeekBar = findViewById(R.id.blue_seekbar);
        mRedInput = findViewById(R.id.red_input);
        mGreenInput = findViewById(R.id.green_input);
        mBlueInput = findViewById(R.id.blue_input);
        mApplyButton = findViewById(R.id.apply_button);
        mCancelButton = findViewById(R.id.cancel_button);
    }
    
    private void setupListeners() {
        // Hex input listener
        mHexInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!mIsUpdatingFromSeekBar) {
                    updateFromHex(s.toString());
                }
            }
        });
        
        // RGB input listeners
        mRedInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!mIsUpdatingFromSeekBar) {
                    updateFromRGBInput();
                }
            }
        });
        
        mGreenInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!mIsUpdatingFromSeekBar) {
                    updateFromRGBInput();
                }
            }
        });
        
        mBlueInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!mIsUpdatingFromSeekBar) {
                    updateFromRGBInput();
                }
            }
        });
        
        // SeekBar listeners
        mRedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mIsUpdatingFromSeekBar = true;
                    mRedInput.setText(String.valueOf(progress));
                    updateColorFromSeekBars();
                    mIsUpdatingFromSeekBar = false;
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        mGreenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mIsUpdatingFromSeekBar = true;
                    mGreenInput.setText(String.valueOf(progress));
                    updateColorFromSeekBars();
                    mIsUpdatingFromSeekBar = false;
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        mBlueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mIsUpdatingFromSeekBar = true;
                    mBlueInput.setText(String.valueOf(progress));
                    updateColorFromSeekBars();
                    mIsUpdatingFromSeekBar = false;
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // Button listeners
        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("custom_color", mCurrentColor);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }
    
    private void loadCurrentColor() {
        // Load saved custom color or default to blue
        mCurrentColor = getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
            .getInt("custom_color_value", Color.BLUE);
        
        updateUIFromColor(mCurrentColor);
    }
    
    private void updateFromHex(String hexString) {
        if (mIsUpdatingFromSeekBar) return;
        
        mIsUpdatingFromHex = true;
        
        try {
            // Remove # if present
            if (hexString.startsWith("#")) {
                hexString = hexString.substring(1);
            }
            
            // Ensure it's a valid hex color
            if (hexString.length() == 6) {
                mCurrentColor = Color.parseColor("#" + hexString);
                updateUIFromColor(mCurrentColor);
            }
        } catch (Exception e) {
            // Invalid hex color, ignore
        }
        
        mIsUpdatingFromHex = false;
    }
    
    private void updateFromRGBInput() {
        if (mIsUpdatingFromSeekBar) return;
        
        try {
            int red = Integer.parseInt(mRedInput.getText().toString());
            int green = Integer.parseInt(mGreenInput.getText().toString());
            int blue = Integer.parseInt(mBlueInput.getText().toString());
            
            // Clamp values
            red = Math.max(0, Math.min(255, red));
            green = Math.max(0, Math.min(255, green));
            blue = Math.max(0, Math.min(255, blue));
            
            mCurrentColor = Color.rgb(red, green, blue);
            updateUIFromColor(mCurrentColor);
        } catch (NumberFormatException e) {
            // Invalid number, ignore
        }
    }
    
    private void updateColorFromSeekBars() {
        int red = mRedSeekBar.getProgress();
        int green = mGreenSeekBar.getProgress();
        int blue = mBlueSeekBar.getProgress();
        
        mCurrentColor = Color.rgb(red, green, blue);
        updateUIFromColor(mCurrentColor);
    }
    
    private void updateUIFromColor(int color) {
        // Update color preview
        mColorPreview.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        
        // Update hex input
        if (!mIsUpdatingFromHex) {
            String hexColor = String.format("#%06X", (0xFFFFFF & color));
            mHexInput.setText(hexColor);
        }
        
        // Update RGB values
        if (!mIsUpdatingFromSeekBar) {
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            
            mRedSeekBar.setProgress(red);
            mGreenSeekBar.setProgress(green);
            mBlueSeekBar.setProgress(blue);
            
            mRedInput.setText(String.valueOf(red));
            mGreenInput.setText(String.valueOf(green));
            mBlueInput.setText(String.valueOf(blue));
        }
    }
    
    @Override
    protected String getActivityTitle() {
        return "Custom Color";
    }
    

} 