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
import com.slideos.system.R;

public class KeyboardSettingsActivity extends BaseSlideOSActivity {
    
    private ListView mListView;
    private String[] mMenuItems;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard_settings);
        
        initializeMenuItems();
        setupListView();
    }
    
    private void initializeMenuItems() {
        mMenuItems = new String[]{
            "Activate slideOS Keyboard",
            "Keyboard Language & Layout",
            "Always Show Tips",
            "Back to Main Menu"
        };
    }
    
    private void setupListView() {
        mListView = findViewById(R.id.settings_list);
        
        // Create custom adapter - let ListView handle highlighting naturally
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, mMenuItems) {
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
        
        mListView.setAdapter(adapter);
        
        // Handle item clicks and selection
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleMenuItemClick(position);
            }
        });
        
        // Setup ListView for base class
        setupListView(mListView);
        
        // Update the "Always Show Tips" text to show current status
        updateAlwaysShowTipsText();
        
        // Base class will handle first item highlighting
    }
    
    @Override
    protected String getActivityTitle() {
        return "Keyboard Settings";
    }
    

    
    private void handleMenuItemClick(int position) {
        switch (position) {
            case 0: // Activate slideOS Keyboard
                Intent activationIntent = new Intent(this, KbActivationActivity.class);
                startActivity(activationIntent);
                break;
            case 1: // Keyboard Language & Layout
                Intent languageIntent = new Intent(this, KeyboardLanguageActivity.class);
                startActivity(languageIntent);
                break;
            case 2: // Always Show Tips
                toggleAlwaysShowTips();
                break;
            case 3: // Back to Main Menu
                finish();
                break;
        }
    }
    

    

    
    private void toggleAlwaysShowTips() {
        boolean currentSetting = getSharedPreferences("keyboard_tips_prefs", MODE_PRIVATE)
            .getBoolean("always_show_tips", false);
        
        boolean newSetting = !currentSetting;
        
        // Save the new setting
        getSharedPreferences("keyboard_tips_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("always_show_tips", newSetting)
            .apply();
        
        // Show confirmation
        String message = newSetting ? "Tips will always be shown" : "Tips will only show for new users";
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
        
        // Update the menu item text to reflect current state
        updateAlwaysShowTipsText();
    }
    
    private void updateAlwaysShowTipsText() {
        boolean alwaysShow = getSharedPreferences("keyboard_tips_prefs", MODE_PRIVATE)
            .getBoolean("always_show_tips", false);
        
        String status = alwaysShow ? " (ON)" : " (OFF)";
        mMenuItems[2] = "Always Show Tips" + status;
        
        // Refresh the adapter
        if (mListView.getAdapter() != null) {
            ((ArrayAdapter<String>) mListView.getAdapter()).notifyDataSetChanged();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Let the base class handle DPAD navigation
        return super.onKeyDown(keyCode, event);
    }
} 