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
import com.liskovsoft.leankeyboard.activity.BaseSlideOSActivity;
import com.slideos.system.R;
import java.io.File;

public class SystemInfoActivity extends BaseSlideOSActivity {
    
    private ListView mListView;
    private SystemInfoItem[] mInfoItems;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info);
        
        initializeInfoItems();
        setupListView();
    }
    
    private void initializeInfoItems() {
        mInfoItems = new SystemInfoItem[]{
            new SystemInfoItem("Media Storage", getMediaStorageInfo()),
            new SystemInfoItem("App Storage", getAppStorageInfo()),
            new SystemInfoItem("slideOS Version", "Developer Beta 0.8.1"),
            new SystemInfoItem("Android System", getAndroidVersion()),
            new SystemInfoItem("Credits", "View development team information")
        };
    }
    
    private String getMediaStorageInfo() {
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
                
                return formatStorageSize(usedSize) + " used, " + formatStorageSize(availableSize) + " free";
            }
        } catch (Exception e) {
            return "Unable to read storage info";
        }
        return "SD card not available";
    }
    
    private String getAppStorageInfo() {
        try {
            File internalDir = Environment.getDataDirectory();
            StatFs stat = new StatFs(internalDir.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            long availableBlocks = stat.getAvailableBlocks();
            
            long totalSize = totalBlocks * blockSize;
            long availableSize = availableBlocks * blockSize;
            long usedSize = totalSize - availableSize;
            
            return formatStorageSize(usedSize) + " used, " + formatStorageSize(availableSize) + " free";
        } catch (Exception e) {
            return "Unable to read storage info";
        }
    }
    
    private String getAndroidVersion() {
        return "Android " + android.os.Build.VERSION.RELEASE + " (API " + android.os.Build.VERSION.SDK_INT + ")";
    }
    
    private String formatStorageSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    private void setupListView() {
        mListView = findViewById(R.id.system_info_list);
        
        // Create custom adapter
        ArrayAdapter<SystemInfoItem> adapter = new ArrayAdapter<SystemInfoItem>(this, 
            android.R.layout.simple_list_item_1, mInfoItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                
                SystemInfoItem item = getItem(position);
                if (item != null) {
                    // Combine title and subtitle with proper formatting
                    String displayText = item.getTitle() + "\n" + item.getSubtitle();
                    textView.setText(displayText);
                    
                    textView.setTextSize(16);
                    textView.setTextColor(getResources().getColor(android.R.color.white));
                    textView.setPadding(20, 15, 20, 15);
                    textView.setLineSpacing(4, 1.0f);
                }
                
                return view;
            }
        };
        
        mListView.setAdapter(adapter);
        
        // Handle item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleItemClick(position);
            }
        });
        
        // Setup ListView for base class
        setupListView(mListView);
    }
    
    @Override
    protected String getActivityTitle() {
        return "System Info";
    }
    
    private void handleItemClick(int position) {
        if (position == 4) { // Credits
            Intent creditsIntent = new Intent(this, CreditsDetailActivity.class);
            startActivity(creditsIntent);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            // Handle selection
            int position = mListView.getSelectedItemPosition();
            if (position >= 0 && position < mInfoItems.length) {
                handleItemClick(position);
                return true;
            }
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    // SystemInfoItem class to hold title and subtitle
    private static class SystemInfoItem {
        private String mTitle;
        private String mSubtitle;
        
        public SystemInfoItem(String title, String subtitle) {
            mTitle = title;
            mSubtitle = subtitle;
        }
        
        public String getTitle() { return mTitle; }
        public String getSubtitle() { return mSubtitle; }
        
        @Override
        public String toString() {
            return mTitle + "\n" + mSubtitle;
        }
    }
} 