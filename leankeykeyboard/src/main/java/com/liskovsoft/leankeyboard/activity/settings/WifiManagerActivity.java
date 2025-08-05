package com.liskovsoft.leankeyboard.activity.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import java.util.ArrayList;
import java.util.List;

public class WifiManagerActivity extends BaseSlideOSActivity {
    
    private ListView mListView;
    private WifiManager mWifiManager;
    private List<WifiNetwork> mWifiNetworks;
    private ArrayAdapter<WifiNetwork> mAdapter;
    private BroadcastReceiver mWifiReceiver;
    private boolean mIsScanning = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_manager);
        
        initializeWifiManager();
        setupListView();
        registerWifiReceiver();
        startWifiScan();
    }
    
    private void initializeWifiManager() {
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiNetworks = new ArrayList<>();
        
        // Ensure Wi-Fi is enabled
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }
    
    private void setupListView() {
        mListView = findViewById(R.id.wifi_list);
        
        // Create custom adapter for Wi-Fi networks
        mAdapter = new ArrayAdapter<WifiNetwork>(this, 
            android.R.layout.simple_list_item_1, mWifiNetworks) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                
                WifiNetwork network = getItem(position);
                if (network != null) {
                    textView.setText(network.getDisplayName());
                    textView.setTextSize(16);
                    textView.setTextColor(getResources().getColor(android.R.color.white));
                    textView.setPadding(20, 15, 20, 15);
                }
                
                return view;
            }
        };
        
        mListView.setAdapter(mAdapter);
        
        // Handle item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleNetworkSelection(position);
            }
        });
        
        // Setup ListView for base class
        setupListView(mListView);
        
        // Add initial items
        addInitialItems();
    }
    
    private void addInitialItems() {
        mWifiNetworks.clear();
        
        // Add header items
        mWifiNetworks.add(new WifiNetwork("Wi-Fi Settings", WifiNetwork.TYPE_HEADER));
        mWifiNetworks.add(new WifiNetwork("Scan for Networks", WifiNetwork.TYPE_SCAN));
        mWifiNetworks.add(new WifiNetwork("Forget All Networks", WifiNetwork.TYPE_FORGET));
        
        // Add separator
        mWifiNetworks.add(new WifiNetwork("Available Networks", WifiNetwork.TYPE_HEADER));
        
        // Add "Scanning..." placeholder
        mWifiNetworks.add(new WifiNetwork("Scanning for networks...", WifiNetwork.TYPE_PLACEHOLDER));
        
        mAdapter.notifyDataSetChanged();
    }
    
    private void registerWifiReceiver() {
        mWifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    handleScanResults();
                } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                    updateWifiConnectionStatus();
                } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                    updateWifiState();
                }
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filter);
    }
    
    private void startWifiScan() {
        if (!mIsScanning) {
            mIsScanning = true;
            mWifiManager.startScan();
            
            // Update UI to show scanning
            updateScanStatus(true);
        }
    }
    
    private void handleScanResults() {
        mIsScanning = false;
        updateScanStatus(false);
        
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        updateNetworkList(scanResults);
    }
    
    private void updateScanStatus(boolean scanning) {
        // Find and update the scanning placeholder
        for (int i = 0; i < mWifiNetworks.size(); i++) {
            WifiNetwork network = mWifiNetworks.get(i);
            if (network.getType() == WifiNetwork.TYPE_PLACEHOLDER) {
                if (scanning) {
                    network.setDisplayName("Scanning for networks...");
                } else {
                    network.setDisplayName("No networks found");
                }
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }
    
    private void updateNetworkList(List<ScanResult> scanResults) {
        // Remove old network entries (keep headers and scan button)
        List<WifiNetwork> newNetworks = new ArrayList<>();
        
        // Keep headers and scan button
        for (WifiNetwork network : mWifiNetworks) {
            if (network.getType() != WifiNetwork.TYPE_NETWORK) {
                newNetworks.add(network);
            }
        }
        
        // Add saved networks first
        List<WifiConfiguration> savedNetworks = mWifiManager.getConfiguredNetworks();
        if (savedNetworks != null && !savedNetworks.isEmpty()) {
            newNetworks.add(new WifiNetwork("Saved Networks", WifiNetwork.TYPE_HEADER));
            for (WifiConfiguration config : savedNetworks) {
                if (config.SSID != null && !config.SSID.isEmpty()) {
                    String ssid = config.SSID;
                    if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                        ssid = ssid.substring(1, ssid.length() - 1);
                    }
                    WifiNetwork savedNetwork = new WifiNetwork(ssid, WifiNetwork.TYPE_SAVED);
                    savedNetwork.setWifiConfiguration(config);
                    newNetworks.add(savedNetwork);
                }
            }
        }
        
        // Add new networks
        if (scanResults != null && !scanResults.isEmpty()) {
            newNetworks.add(new WifiNetwork("Available Networks", WifiNetwork.TYPE_HEADER));
            for (ScanResult result : scanResults) {
                if (result.SSID != null && !result.SSID.isEmpty()) {
                    WifiNetwork network = new WifiNetwork(result);
                    newNetworks.add(network);
                }
            }
        } else {
            // Add "No networks found" message
            newNetworks.add(new WifiNetwork("No networks found", WifiNetwork.TYPE_PLACEHOLDER));
        }
        
        mWifiNetworks.clear();
        mWifiNetworks.addAll(newNetworks);
        mAdapter.notifyDataSetChanged();
    }
    
    private void handleNetworkSelection(int position) {
        WifiNetwork selectedNetwork = mWifiNetworks.get(position);
        
        switch (selectedNetwork.getType()) {
            case WifiNetwork.TYPE_SCAN:
                startWifiScan();
                break;
            case WifiNetwork.TYPE_FORGET:
                forgetAllNetworks();
                break;
            case WifiNetwork.TYPE_NETWORK:
                connectToNetwork(selectedNetwork);
                break;
            case WifiNetwork.TYPE_SAVED:
                connectToSavedNetwork(selectedNetwork);
                break;
            case WifiNetwork.TYPE_HEADER:
            case WifiNetwork.TYPE_PLACEHOLDER:
                // Do nothing for headers and placeholders
                break;
        }
    }
    
    private void forgetAllNetworks() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Forget All Networks");
        builder.setMessage("This will remove all saved Wi-Fi networks. Are you sure?");
        
        builder.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
                if (configurations != null) {
                    for (WifiConfiguration config : configurations) {
                        mWifiManager.removeNetwork(config.networkId);
                    }
                    mWifiManager.saveConfiguration();
                    Toast.makeText(WifiManagerActivity.this, "All networks forgotten", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("No", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void connectToSavedNetwork(WifiNetwork network) {
        WifiConfiguration config = network.getWifiConfiguration();
        if (config != null) {
            boolean success = mWifiManager.enableNetwork(config.networkId, true);
            if (success) {
                Toast.makeText(this, "Connecting to " + network.getDisplayName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to connect to " + network.getDisplayName(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void connectToNetwork(WifiNetwork network) {
        ScanResult scanResult = network.getScanResult();
        if (scanResult != null) {
            // For open networks, connect directly
            if (scanResult.capabilities.contains("WEP") || scanResult.capabilities.contains("WPA")) {
                // Show password dialog or use saved configuration
                showPasswordDialog(network);
            } else {
                // Open network - connect directly
                connectToOpenNetwork(scanResult);
            }
        }
    }
    
    private void showPasswordDialog(WifiNetwork network) {
        // Create a simple password input dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Connect to " + network.getDisplayName());
        builder.setMessage("Enter password:");
        
        final android.widget.EditText passwordInput = new android.widget.EditText(this);
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Password");
        builder.setView(passwordInput);
        
        builder.setPositiveButton("Connect", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                String password = passwordInput.getText().toString();
                if (!password.isEmpty()) {
                    connectToSecuredNetwork(network.getScanResult(), password);
                } else {
                    Toast.makeText(WifiManagerActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    private void connectToSecuredNetwork(ScanResult scanResult, String password) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + scanResult.SSID + "\"";
        
        String capabilities = scanResult.capabilities;
        if (capabilities.contains("WPA2") || capabilities.contains("WPA")) {
            config.preSharedKey = "\"" + password + "\"";
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        } else if (capabilities.contains("WEP")) {
            config.wepKeys[0] = "\"" + password + "\"";
            config.wepTxKeyIndex = 0;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        }
        
        int networkId = mWifiManager.addNetwork(config);
        if (networkId != -1) {
            boolean success = mWifiManager.enableNetwork(networkId, true);
            if (success) {
                Toast.makeText(this, "Connecting to " + scanResult.SSID, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to connect to " + scanResult.SSID, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to add network configuration", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void connectToOpenNetwork(ScanResult scanResult) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + scanResult.SSID + "\"";
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        
        int networkId = mWifiManager.addNetwork(config);
        if (networkId != -1) {
            boolean success = mWifiManager.enableNetwork(networkId, true);
            if (success) {
                Toast.makeText(this, "Connecting to " + scanResult.SSID, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to connect to " + scanResult.SSID, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void updateWifiConnectionStatus() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
            String connectedSSID = wifiInfo.getSSID();
            if (connectedSSID != null && connectedSSID.startsWith("\"") && connectedSSID.endsWith("\"")) {
                connectedSSID = connectedSSID.substring(1, connectedSSID.length() - 1);
            }
            
            // Update the UI to show connected status
            for (WifiNetwork network : mWifiNetworks) {
                if (network.getType() == WifiNetwork.TYPE_NETWORK && 
                    network.getDisplayName().equals(connectedSSID)) {
                    network.setConnected(true);
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }
    
    private void updateWifiState() {
        if (!mWifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wi-Fi is disabled", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    protected String getActivityTitle() {
        return "Wi-Fi Manager";
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            // Handle selection
            int position = mListView.getSelectedItemPosition();
            if (position >= 0 && position < mWifiNetworks.size()) {
                handleNetworkSelection(position);
                return true;
            }
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWifiReceiver != null) {
            unregisterReceiver(mWifiReceiver);
        }
    }
    
    // WifiNetwork class to represent different types of list items
    private static class WifiNetwork {
        public static final int TYPE_HEADER = 0;
        public static final int TYPE_SCAN = 1;
        public static final int TYPE_NETWORK = 2;
        public static final int TYPE_PLACEHOLDER = 3;
        public static final int TYPE_FORGET = 4;
        public static final int TYPE_SAVED = 5;
        
        private String mDisplayName;
        private int mType;
        private ScanResult mScanResult;
        private WifiConfiguration mWifiConfiguration;
        private boolean mIsConnected;
        private int mSignalStrength;
        
        public WifiNetwork(String displayName, int type) {
            mDisplayName = displayName;
            mType = type;
        }
        
        public WifiNetwork(ScanResult scanResult) {
            mScanResult = scanResult;
            mType = TYPE_NETWORK;
            mDisplayName = scanResult.SSID;
            mSignalStrength = scanResult.level;
        }
        
        public String getDisplayName() {
            if (mType == TYPE_NETWORK) {
                String strength = getSignalStrengthString();
                String security = getSecurityString();
                String status = mIsConnected ? " (Connected)" : "";
                return mDisplayName + " " + strength + " " + security + status;
            } else if (mType == TYPE_SAVED) {
                String status = mIsConnected ? " (Connected)" : " (Saved)";
                return mDisplayName + status;
            }
            return mDisplayName;
        }
        
        private String getSignalStrengthString() {
            if (mSignalStrength >= -50) return "●●●●";
            if (mSignalStrength >= -60) return "●●●○";
            if (mSignalStrength >= -70) return "●●○○";
            if (mSignalStrength >= -80) return "●○○○";
            return "○○○○";
        }
        
        private String getSecurityString() {
            if (mScanResult != null) {
                String capabilities = mScanResult.capabilities;
                if (capabilities.contains("WPA2")) return "[WPA2]";
                if (capabilities.contains("WPA")) return "[WPA]";
                if (capabilities.contains("WEP")) return "[WEP]";
            }
            return "[Open]";
        }
        
        public int getType() { return mType; }
        public ScanResult getScanResult() { return mScanResult; }
        public WifiConfiguration getWifiConfiguration() { return mWifiConfiguration; }
        public void setWifiConfiguration(WifiConfiguration config) { mWifiConfiguration = config; }
        public boolean isConnected() { return mIsConnected; }
        public void setConnected(boolean connected) { mIsConnected = connected; }
        public void setDisplayName(String name) { mDisplayName = name; }
        
        @Override
        public String toString() {
            return getDisplayName();
        }
    }
} 