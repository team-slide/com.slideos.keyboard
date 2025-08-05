package com.liskovsoft.leankeyboard.activity.settings;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.slideos.system.R;
import com.liskovsoft.leankeyboard.ime.BackgroundKeyboardService;
import com.liskovsoft.leankeyboard.service.SystemStatusBarService;
import com.liskovsoft.leankeyboard.service.KeyboardMonitorService;
import com.liskovsoft.leankeyboard.utils.LeanKeyPreferences;
import com.liskovsoft.leankeyboard.activity.BaseSlideOSActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.IOException;
import static android.content.Context.WIFI_SERVICE;

public class SlideOSSettingsActivity extends BaseSlideOSActivity {
    
    private ListView mListView;
    private String[] mMenuItems;
    private String[] mMenuDescriptions;
    private ArrayAdapter<String> mAdapter;
    private TextView mStatusTitle;
    private ImageView mWifiStatus;
    private ImageView mBluetoothStatus;
    private TextView mBatteryStatus;
    private TextView mTimeStatus;
    private Handler mStatusHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideos_settings);
        
        // Start background keyboard service
        startBackgroundKeyboardService();
        
        // Initialize menu items
        initializeMenuItems();
        
        // Setup ListView
        setupListView();
        
        // Setup complete
    }
    
    private void startBackgroundKeyboardService() {
        Intent serviceIntent = new Intent(this, BackgroundKeyboardService.class);
        serviceIntent.setAction("com.slideos.system.START_BACKGROUND_SERVICE");
        startService(serviceIntent);
        
        // Also start the system status bar service
        Intent statusBarIntent = new Intent(this, SystemStatusBarService.class);
        startService(statusBarIntent);
        
        // Start the keyboard monitor service
        Intent monitorIntent = new Intent(this, KeyboardMonitorService.class);
        monitorIntent.setAction("com.slideos.system.START_MONITORING");
        startService(monitorIntent);
    }
    
    private void initializeMenuItems() {
        mMenuItems = new String[]{
            "System Info",
            "Keyboard",
            "Appearance",
            "Bluetooth",
            "Wi-Fi",
            "Storage",
            getString(R.string.about_desc)
        };
        
        mMenuDescriptions = new String[]{
            "Device information and storage status",
            "Keyboard settings and configuration",
            "Customize theme and accent colors",
            "Open Android Bluetooth settings",
            "Wi-Fi connection manager",
            "Storage management and reset options",
            "About slideOS System and credits"
        };
    }
    
    private void setupListView() {
        mListView = findViewById(R.id.settings_list);
        
        // Create custom adapter with larger text - let ListView handle highlighting naturally
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, mMenuItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextSize(16); // Compact text size for 2.4-inch display
                textView.setTextColor(getResources().getColor(android.R.color.white));
                textView.setPadding(16, 12, 16, 12);
                
                // Let the ListView's selector handle highlighting naturally
                // This allows for proper DPAD navigation and rapid scrolling
                
                return view;
            }
        };
        
        mListView.setAdapter(adapter);
        
        // Store adapter reference for DPAD navigation updates
        mAdapter = adapter;
        
        // Setup ListView for base class (this will configure DPAD navigation properly)
        setupListView(mListView);
        
        // Handle item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleMenuItemClick(position);
            }
        });
        
        // Base class will handle first item highlighting
    }
    
    @Override
    protected String getActivityTitle() {
        return "slideOS System";
    }
    

    

    
    protected void updateStatusBarTitle(String title) {
        // Use the base class method for consistent title handling
        super.updateStatusBarTitle(title);
    }
    

    

    

    

    
    private void handleMenuItemClick(int position) {
        switch (position) {
            case 0: // System Info
                updateStatusBarTitle("System Info");
                Intent systemInfoIntent = new Intent(this, SystemInfoActivity.class);
                startActivity(systemInfoIntent);
                break;
            case 1: // Keyboard
                updateStatusBarTitle("Keyboard Settings");
                Intent keyboardIntent = new Intent(this, KeyboardSettingsActivity.class);
                startActivity(keyboardIntent);
                break;
            case 2: // Appearance
                updateStatusBarTitle("Appearance Settings");
                Intent appearanceIntent = new Intent(this, AppearanceSettingsActivity.class);
                startActivity(appearanceIntent);
                break;
            case 3: // Bluetooth
                updateStatusBarTitle("Bluetooth Settings");
                Intent bluetoothIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(bluetoothIntent);
                break;
            case 4: // Wi-Fi
                updateStatusBarTitle("Wi-Fi Manager");
                Intent wifiIntent = new Intent(this, WifiManagerActivity.class);
                startActivity(wifiIntent);
                break;
            case 5: // Storage
                updateStatusBarTitle("Storage");
                Intent storageIntent = new Intent(this, StorageActivity.class);
                startActivity(storageIntent);
                break;
            case 6: // About
                updateStatusBarTitle("About slideOS");
                Intent creditsIntent = new Intent(this, CreditsActivity.class);
                startActivity(creditsIntent);
                break;
        }
    }
    

    

    
    private void showLanguageSelectionDialog() {
        String[] languages = {
            "English (QWERTY)",
            "Spanish (QWERTY)",
            "French (AZERTY)",
            "German (QWERTZ)",
            "Russian (ЙЦУКЕН)",
            "Japanese (QWERTY)",
            "Chinese (QWERTY)",
            "Korean (QWERTY)"
        };
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Keyboard Language");
        builder.setItems(languages, new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                // Set the selected language and update IME character sets
                setKeyboardLanguage(which);
                Intent layoutIntent = new Intent(SlideOSSettingsActivity.this, KbLayoutActivity.class);
                startActivity(layoutIntent);
            }
        });
        builder.show();
    }
    
    private void setKeyboardLanguage(int languageIndex) {
        // Store the selected language preference
        getSharedPreferences("slideOS_prefs", MODE_PRIVATE)
            .edit()
            .putInt("selected_language", languageIndex)
            .apply();
        
        // Update IME character sets based on language
        updateIMECharacterSets(languageIndex);
    }
    
    private void updateIMECharacterSets(int languageIndex) {
        // This will be handled by the keyboard factory when it loads
        // The language preference will be read by ResKeyboardFactory
        // and appropriate character sets will be loaded
    }
    

    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Let the base class handle all DPAD navigation naturally
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Forward BACK key events to background service for keyboard spawn/dismiss
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent serviceIntent = new Intent(this, BackgroundKeyboardService.class);
            serviceIntent.putExtra("keyCode", keyCode);
            serviceIntent.putExtra("keyEvent", event);
            startService(serviceIntent);
            
            // Check if service wants to consume this BACK event
            // For now, we'll let the service handle it via root access
            // This is a fallback method when root access isn't available
        }
        
        // Handle Menu button for keyboard toggle
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showKeyboardToggleToast();
        }

        return super.onKeyUp(keyCode, event);
    }
    
    private void showKeyboardToggleToast() {
        android.widget.Toast.makeText(this, "Release Menu button to show keyboard", android.widget.Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Update status bar title when returning to this activity
        updateStatusBarTitle("slideOS System");
    }
} 