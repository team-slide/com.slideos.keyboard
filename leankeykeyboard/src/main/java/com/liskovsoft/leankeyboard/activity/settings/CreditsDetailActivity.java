package com.liskovsoft.leankeyboard.activity.settings;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import com.liskovsoft.leankeyboard.activity.BaseSlideOSActivity;
import com.slideos.system.R;

public class CreditsDetailActivity extends BaseSlideOSActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits_detail);
        setupCredits();
    }
    
    private void setupCredits() {
        TextView versionView = findViewById(R.id.credits_version);
        TextView descriptionView = findViewById(R.id.credits_description);
        TextView developersView = findViewById(R.id.credits_developers);
        TextView attributionView = findViewById(R.id.credits_attribution);
        
        // Set status bar title to "Credits"
        TextView statusTitle = findViewById(R.id.unified_status_title);
        if (statusTitle != null) {
            statusTitle.setText("Credits");
        }
        
        // Set version
        versionView.setText("Developer Beta 0.8.1");
        versionView.setTextSize(20);
        versionView.setTextColor(getResources().getColor(android.R.color.white));
        
        // Set description
        descriptionView.setText("A customised Android experience tailored for the Innioasis Y1 by Team Slide.");
        descriptionView.setTextSize(20);
        descriptionView.setTextColor(getResources().getColor(android.R.color.white));
        
        // Set developers
        developersView.setText("Team Slide Development Team:\n\n" +
                              "Ryan Specter\n" +
                              "Lead Developer\n\n" +
                              "Leonardo Alexandrino de Melo\n" +
                              "Head of slideOS Branding\n\n" +
                              "Melody Cupp\n" +
                              "Innioasis and Team Slide Community Lead");
        developersView.setTextSize(22);
        developersView.setTextColor(getResources().getColor(android.R.color.white));
        
        // Set attribution
        attributionView.setText("Keyboard Component:\n" +
                               "Based on LeanKeyboard by LiskovSoft\n" +
                               "Forked and optimized for slideOS devices\n\n" +
                               "License: Respects original LeanKeyboard license");
        attributionView.setTextSize(18);
        attributionView.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }
    
    @Override
    protected String getActivityTitle() {
        return "Credits";
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
} 