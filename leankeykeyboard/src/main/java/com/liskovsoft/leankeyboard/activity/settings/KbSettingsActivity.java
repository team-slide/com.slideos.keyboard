package com.liskovsoft.leankeyboard.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.GuidedStepSupportFragment;
import com.liskovsoft.leankeyboard.fragments.settings.KbSettingsFragment;
import com.liskovsoft.leankeyboard.receiver.RestartServiceReceiver;
import com.liskovsoft.leankeyboard.ime.BackgroundKeyboardService;

public class KbSettingsActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GuidedStepSupportFragment.addAsRoot(this, new KbSettingsFragment(), android.R.id.content);
        
        // Start background keyboard service
        startBackgroundKeyboardService();
    }
    
    private void startBackgroundKeyboardService() {
        Intent serviceIntent = new Intent(this, BackgroundKeyboardService.class);
        serviceIntent.setAction("com.liskovsoft.leankeyboard.START_BACKGROUND_SERVICE");
        startService(serviceIntent);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Forward ENTER and DPAD_CENTER key events to background service for keyboard spawn/dismiss
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            Intent serviceIntent = new Intent(this, BackgroundKeyboardService.class);
            serviceIntent.putExtra("keyCode", keyCode);
            serviceIntent.putExtra("keyEvent", event);
            startService(serviceIntent);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Forward ENTER and DPAD_CENTER key events to background service for keyboard spawn/dismiss
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            Intent serviceIntent = new Intent(this, BackgroundKeyboardService.class);
            serviceIntent.putExtra("keyCode", keyCode);
            serviceIntent.putExtra("keyEvent", event);
            startService(serviceIntent);
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // restart kbd service
        Intent intent = new Intent(this, RestartServiceReceiver.class);
        sendBroadcast(intent);
    }
}
