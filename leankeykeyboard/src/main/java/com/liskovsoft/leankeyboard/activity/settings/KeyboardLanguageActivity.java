package com.liskovsoft.leankeyboard.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.liskovsoft.leankeyboard.activity.BaseSlideOSActivity;
import com.liskovsoft.leankeyboard.addons.keyboards.KeyboardInfo;
import com.liskovsoft.leankeyboard.addons.keyboards.intkeyboards.ResKeyboardInfo;
import com.slideos.system.R;
import java.util.List;

public class KeyboardLanguageActivity extends BaseSlideOSActivity {
    
    private ListView mListView;
    private List<KeyboardInfo> mKeyboardInfos;
    private ArrayAdapter<String> mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard_settings);
        
        loadKeyboardLanguages();
        setupListView();
    }
    
    private void loadKeyboardLanguages() {
        // Load all available keyboard languages from the original implementation
        mKeyboardInfos = ResKeyboardInfo.getAllKeyboardInfos(this);
    }
    
    private void setupListView() {
        mListView = findViewById(R.id.settings_list);
        
        // Create language names array
        String[] languageNames = new String[mKeyboardInfos.size() + 1]; // +1 for "Back"
        for (int i = 0; i < mKeyboardInfos.size(); i++) {
            KeyboardInfo info = mKeyboardInfos.get(i);
            String layoutType = info.isAzerty() ? " (AZERTY)" : " (QWERTY)";
            languageNames[i] = info.getLangName() + layoutType;
        }
        languageNames[mKeyboardInfos.size()] = "Back to Keyboard Settings";
        
        // Create custom adapter - let ListView handle highlighting naturally
        mAdapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, languageNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextSize(18);
                textView.setTextColor(getResources().getColor(android.R.color.white));
                textView.setPadding(20, 15, 20, 15);
                
                // Let the ListView's selector handle highlighting naturally
                // This ensures consistent DPAD navigation across all activities
                
                return view;
            }
        };
        
        mListView.setAdapter(mAdapter);
        
        // Handle item clicks and selection
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleLanguageSelection(position);
            }
        });
        
        // Setup ListView for base class
        setupListView(mListView);
        
        // Base class will handle first item highlighting
    }
    
    @Override
    protected String getActivityTitle() {
        return "Keyboard Language & Layout";
    }
    

    
    private void handleLanguageSelection(int position) {
        if (position < mKeyboardInfos.size()) {
            // Language selected
            KeyboardInfo selectedInfo = mKeyboardInfos.get(position);
            setKeyboardLanguage(selectedInfo);
        } else {
            // Back option selected
            finish();
        }
    }
    
    private void setKeyboardLanguage(KeyboardInfo keyboardInfo) {
        // Save the selected language preference
        getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
            .edit()
            .putString("selected_keyboard_language", keyboardInfo.getLangCode())
            .putBoolean("selected_keyboard_azerty", keyboardInfo.isAzerty())
            .apply();
        
        // Update the IME's character set by restarting the keyboard service
        // This ensures the new language/layout is applied immediately
        Intent serviceIntent = new Intent(this, com.liskovsoft.leankeyboard.ime.BackgroundKeyboardService.class);
        serviceIntent.setAction("com.slideos.system.RELOAD_KEYBOARD");
        startService(serviceIntent);
        
        // Show feedback
        String layoutType = keyboardInfo.isAzerty() ? " (AZERTY)" : " (QWERTY)";
        String message = "Language set to " + keyboardInfo.getLangName() + layoutType;
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
        
        // Return to previous screen
        finish();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Let the base class handle DPAD navigation
        return super.onKeyDown(keyCode, event);
    }
} 