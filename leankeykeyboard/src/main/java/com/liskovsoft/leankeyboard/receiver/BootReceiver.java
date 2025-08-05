package com.liskovsoft.leankeyboard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import com.liskovsoft.leankeyboard.ime.BackgroundKeyboardService;
import com.liskovsoft.leankeyboard.service.SystemStatusBarService;
import com.liskovsoft.leankeyboard.service.KeyboardMonitorService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final String ROCKBOX_PACKAGE = "org.rockbox";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, checking for Rockbox conflict");
            
            // Check if Rockbox is installed to avoid conflicts
            if (!isRockboxInstalled(context)) {
                Log.d(TAG, "Rockbox not detected, starting slideOS Keyboard background service");
                startBackgroundService(context);
            } else {
                Log.d(TAG, "Rockbox detected, skipping slideOS Keyboard background service to avoid conflicts");
            }
        }
    }
    
    private boolean isRockboxInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(ROCKBOX_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    
    private void startBackgroundService(Context context) {
        Intent serviceIntent = new Intent(context, BackgroundKeyboardService.class);
        serviceIntent.setAction("com.slideos.system.START_BACKGROUND_SERVICE");
        context.startService(serviceIntent);
        
        // Start the System Status Bar Service
        Intent statusBarIntent = new Intent(context, SystemStatusBarService.class);
        context.startService(statusBarIntent);
        
        // Start the Keyboard Monitor Service
        Intent monitorIntent = new Intent(context, KeyboardMonitorService.class);
        monitorIntent.setAction("com.slideos.system.START_MONITORING");
        context.startService(monitorIntent);
    }
} 