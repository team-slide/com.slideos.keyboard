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
import android.widget.Toast;
import com.liskovsoft.leankeyboard.activity.BaseSlideOSActivity;
import com.slideos.system.R;
import java.io.File;

public class StorageConfirmationActivity extends BaseSlideOSActivity {
    
    public static final String EXTRA_CONFIRMATION_TYPE = "confirmation_type";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MESSAGE = "message";
    
    public static final int TYPE_MEDIA_STORAGE = 1;
    public static final int TYPE_ROCKBOX_SETTINGS = 2;
    
    private ListView mListView;
    private String[] mOptions;
    private int mConfirmationType;
    private String mTitle;
    private String mMessage;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_confirmation);
        
        // Get intent extras
        Intent intent = getIntent();
        mConfirmationType = intent.getIntExtra(EXTRA_CONFIRMATION_TYPE, TYPE_MEDIA_STORAGE);
        mTitle = intent.getStringExtra(EXTRA_TITLE);
        mMessage = intent.getStringExtra(EXTRA_MESSAGE);
        
        // Set default values if not provided
        if (mTitle == null) {
            mTitle = "Reset Media Storage";
        }
        if (mMessage == null) {
            mMessage = "This will delete everything from SD card except Rockbox folders. This action cannot be undone.";
        }
        
        setupUI();
        setupListView();
    }
    
    private void setupUI() {
        // Set title and message
        TextView titleView = findViewById(R.id.confirmation_title);
        TextView messageView = findViewById(R.id.confirmation_message);
        
        if (titleView != null) {
            titleView.setText(mTitle);
        }
        if (messageView != null) {
            messageView.setText(mMessage);
        }
        
        // Set status bar title
        TextView statusTitle = findViewById(R.id.unified_status_title);
        if (statusTitle != null) {
            statusTitle.setText("Confirm Action");
        }
    }
    
    private void setupListView() {
        mListView = findViewById(R.id.confirmation_list);
        mOptions = new String[]{"Yes, proceed", "No, cancel"};
        
        // Create custom adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, mOptions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextSize(18);
                textView.setTextColor(getResources().getColor(android.R.color.white));
                textView.setPadding(20, 15, 20, 15);
                
                return view;
            }
        };
        
        mListView.setAdapter(adapter);
        
        // Handle item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleOptionClick(position);
            }
        });
        
        // Setup ListView for base class
        setupListView(mListView);
    }
    
    @Override
    protected String getActivityTitle() {
        return "Confirm Action";
    }
    
    private void handleOptionClick(int position) {
        switch (position) {
            case 0: // Yes, proceed
                performAction();
                break;
            case 1: // No, cancel
                finish();
                break;
        }
    }
    
    private void performAction() {
        switch (mConfirmationType) {
            case TYPE_MEDIA_STORAGE:
                performMediaStorageReset();
                break;
            case TYPE_ROCKBOX_SETTINGS:
                performRockboxSettingsReset();
                break;
        }
    }
    
    private void performMediaStorageReset() {
        try {
            File sdcardDir = new File("/sdcard");
            if (sdcardDir.exists() && sdcardDir.isDirectory()) {
                deleteFilesExceptRockbox(sdcardDir);
                Toast.makeText(this, "Media storage reset complete", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SD card not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error resetting media storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private void performRockboxSettingsReset() {
        try {
            File sdcardDir = new File("/sdcard");
            if (sdcardDir.exists() && sdcardDir.isDirectory()) {
                deleteRockboxFolders(sdcardDir);
                Toast.makeText(this, "Rockbox settings reset complete", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SD card not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error resetting Rockbox settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private void deleteFilesExceptRockbox(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                // Skip rockbox and .rockbox folders
                if (!fileName.equals("rockbox") && !fileName.equals(".rockbox")) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }
    
    private void deleteRockboxFolders(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                // Delete only rockbox and .rockbox folders
                if (fileName.equals("rockbox") || fileName.equals(".rockbox")) {
                    deleteDirectoryRecursively(file);
                }
            }
        }
    }
    
    private void deleteDirectoryRecursively(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursively(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            // Handle selection
            int position = mListView.getSelectedItemPosition();
            if (position >= 0 && position < mOptions.length) {
                handleOptionClick(position);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
} 