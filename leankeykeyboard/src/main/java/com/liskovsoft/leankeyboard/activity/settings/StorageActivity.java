package com.liskovsoft.leankeyboard.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.liskovsoft.leankeyboard.activity.BaseSlideOSActivity;
import com.liskovsoft.leankeyboard.widget.PieChartView;
import com.slideos.system.R;
import java.io.File;

public class StorageActivity extends BaseSlideOSActivity {
    
    private ListView mListView;
    private String[] mMenuItems;
    private android.widget.FrameLayout mMediaStorageChartContainer;
    private android.widget.FrameLayout mAppStorageChartContainer;
    private TextView mMediaStorageInfo;
    private TextView mAppStorageInfo;
    private PieChartView mMediaChart;
    private PieChartView mAppChart;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        
        initializeMenuItems();
        setupStorageCharts();
        setupListView();
    }
    
    private void initializeMenuItems() {
        mMenuItems = new String[]{
            "Reset Media Storage",
            "Reset Rockbox Settings",
            "Back to Main Menu"
        };
    }
    
    private void setupStorageCharts() {
        // Initialize pie chart containers
        mMediaStorageChartContainer = findViewById(R.id.media_storage_chart_container);
        mAppStorageChartContainer = findViewById(R.id.app_storage_chart_container);
        mMediaStorageInfo = findViewById(R.id.media_storage_info);
        mAppStorageInfo = findViewById(R.id.app_storage_info);
        
        // Create pie chart views
        mMediaChart = new PieChartView(this);
        mAppChart = new PieChartView(this);
        
        // Set colors for the charts
        mMediaChart.setColors(0xFFFF6B6B, 0xFF4ECDC4); // Red for used, Teal for free
        mAppChart.setColors(0xFFFF9F43, 0xFF26DE81);   // Orange for used, Green for free
        
        // Add charts to containers
        if (mMediaStorageChartContainer != null) {
            mMediaStorageChartContainer.addView(mMediaChart);
        }
        if (mAppStorageChartContainer != null) {
            mAppStorageChartContainer.addView(mAppChart);
        }
        
        // Update storage data
        updateStorageData();
    }
    
    private void updateStorageData() {
        // Update Media Storage (SD Card)
        try {
            File sdcardDir = new File("/sdcard");
            if (sdcardDir.exists()) {
                StatFs stat = new StatFs(sdcardDir.getPath());
                long blockSize = stat.getBlockSize();
                long totalBlocks = stat.getBlockCount();
                long availableBlocks = stat.getAvailableBlocks();
                
                long totalSize = totalBlocks * blockSize;
                long availableSize = availableBlocks * blockSize;
                long usedSize = totalSize - availableSize;
                
                // Update pie chart
                if (mMediaChart != null) {
                    mMediaChart.setStorageData(usedSize, totalSize);
                }
                
                // Update info text
                if (mMediaStorageInfo != null) {
                    String info = "Free: " + formatStorageSize(availableSize) + " / Total: " + formatStorageSize(totalSize);
                    mMediaStorageInfo.setText(info);
                    mMediaStorageInfo.setTextSize(20); // Slightly smaller than title
                }
            }
        } catch (Exception e) {
            if (mMediaStorageInfo != null) {
                mMediaStorageInfo.setText("SD card not available");
            }
        }
        
        // Update App Storage (Internal)
        try {
            File internalDir = Environment.getDataDirectory();
            StatFs stat = new StatFs(internalDir.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            long availableBlocks = stat.getAvailableBlocks();
            
            long totalSize = totalBlocks * blockSize;
            long availableSize = availableBlocks * blockSize;
            long usedSize = totalSize - availableSize;
            
            // Update pie chart
            if (mAppChart != null) {
                mAppChart.setStorageData(usedSize, totalSize);
            }
            
            // Update info text
            if (mAppStorageInfo != null) {
                String info = "Free: " + formatStorageSize(availableSize) + " / Total: " + formatStorageSize(totalSize);
                mAppStorageInfo.setText(info);
                mAppStorageInfo.setTextSize(20); // Slightly smaller than title
            }
        } catch (Exception e) {
            if (mAppStorageInfo != null) {
                mAppStorageInfo.setText("Unable to read storage info");
            }
        }
    }
    
    private String formatStorageSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    private void setupListView() {
        mListView = findViewById(R.id.storage_list);
        
        // Create custom adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_list_item_1, mMenuItems) {
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
                handleMenuItemClick(position);
            }
        });
        
        // Setup ListView for base class
        setupListView(mListView);
        
        // Ensure ListView has proper height for scrolling
        mListView.post(new Runnable() {
            @Override
            public void run() {
                // Calculate total height needed for all items
                int totalHeight = 0;
                for (int i = 0; i < mListView.getCount(); i++) {
                    View listItem = mListView.getAdapter().getView(i, null, mListView);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
                
                // Set minimum height to ensure proper scrolling
                android.view.ViewGroup.LayoutParams params = mListView.getLayoutParams();
                params.height = Math.max(totalHeight, 300); // Minimum 300dp height
                mListView.setLayoutParams(params);
            }
        });
    }
    
    @Override
    protected String getActivityTitle() {
        return "Storage";
    }
    
    private void handleMenuItemClick(int position) {
        switch (position) {
            case 0: // Reset Media Storage
                resetMediaStorage();
                break;
            case 1: // Reset Rockbox Settings
                resetRockboxSettings();
                break;
            case 2: // Back to Main Menu
                finish();
                break;
        }
    }
    
    private void resetMediaStorage() {
        // Launch confirmation activity
        Intent intent = new Intent(this, StorageConfirmationActivity.class);
        intent.putExtra(StorageConfirmationActivity.EXTRA_CONFIRMATION_TYPE, StorageConfirmationActivity.TYPE_MEDIA_STORAGE);
        intent.putExtra(StorageConfirmationActivity.EXTRA_TITLE, "Reset Media Storage");
        intent.putExtra(StorageConfirmationActivity.EXTRA_MESSAGE, 
            "This will delete everything from SD card except Rockbox folders.\n\n" +
            "This action cannot be undone and will permanently remove all media files, " +
            "documents, and other data from your SD card.");
        startActivity(intent);
    }
    
    private void resetRockboxSettings() {
        // Launch confirmation activity
        Intent intent = new Intent(this, StorageConfirmationActivity.class);
        intent.putExtra(StorageConfirmationActivity.EXTRA_CONFIRMATION_TYPE, StorageConfirmationActivity.TYPE_ROCKBOX_SETTINGS);
        intent.putExtra(StorageConfirmationActivity.EXTRA_TITLE, "Reset Rockbox Settings");
        intent.putExtra(StorageConfirmationActivity.EXTRA_MESSAGE, 
            "This will delete all Rockbox settings and configuration files.\n\n" +
            "This will reset your Rockbox player to default settings, " +
            "removing all custom themes, playlists, and configuration.");
        startActivity(intent);
    }
    

    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            // Handle selection
            int position = mListView.getSelectedItemPosition();
            if (position >= 0 && position < mMenuItems.length) {
                handleMenuItemClick(position);
                return true;
            }
        }
        
        return super.onKeyDown(keyCode, event);
    }
} 