package com.iam844.adityajaiswal.wifiscanner;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements WiFiListAdapter.OnItemClicked {

    private static final String TAG = MainActivity.class.getSimpleName();

    WifiManager wifiManager;
    WifiReceiver wifiReceiver;
    WiFiListAdapter wifiListAdapter;
    List wifiList;
    RecyclerView wifiRecyclerView;

    String[] NetworkSSID;
    Button scanWifiBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        executeListener();
    }

    private void initialize() {
        wifiRecyclerView = findViewById(R.id.myRecyclerView);
        scanWifiBtn = findViewById(R.id.scanWifi);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void executeListener() {

        scanWifiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }
                scanWifiList();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        showWifiPasswordDialog(position);
    }

    private void scanWifiList() {
        wifiManager.startScan();
        wifiList = wifiManager.getScanResults();
        setAdapter();
    }

    private void setAdapter() {
        wifiListAdapter = new WiFiListAdapter(getApplicationContext(), wifiList);

        wifiRecyclerView.setAdapter(wifiListAdapter);
        wifiRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        wifiListAdapter.setOnClick(MainActivity.this);
    }

    public void showWifiPasswordDialog(int position) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.wifi_password_dialog, null);

        final EditText inputPassword = mView.findViewById(R.id.wifi_Password);
        Button connectBtn = mView.findViewById(R.id.connect_Btn);
        Button cancelBtn = mView.findViewById(R.id.cancel_Btn);

        final TextView wifiSSID = mView.findViewById(R.id.wifi_SSID);

        String toCutSSID = wifiList.get(position).toString();
        String[] s0 = toCutSSID.split(",");
        NetworkSSID = s0[0].split(": ");
        wifiSSID.setText(NetworkSSID[1]);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String nameSSID = NetworkSSID[1];
                final String namePass = inputPassword.getText().toString();
                connectToWifi(nameSSID, namePass);

                alertDialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void connectToWifi(String ssid, String passphrase) {
        if(isConnectedTo(ssid)){
            Toast.makeText(getApplicationContext(),"Already connected to " + ssid,Toast.LENGTH_SHORT).show();
            return;
        }

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\""+ ssid +"\"";
        wifiConfig.priority=(getMaxConfigurationPriority(wifiManager)+1);
        wifiConfig.preSharedKey = "\""+ passphrase +"\"";
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.priority = getMaxConfigurationPriority(wifiManager);

        //Add network
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        boolean successful = wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        if (successful == true) {
            Toast.makeText(getApplicationContext(),"Successfully connected",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(),"Not connected",Toast.LENGTH_SHORT).show();
        }

    }

    private int getMaxConfigurationPriority(final WifiManager wifiManager) {

        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int maxPriority = 0;

        for(final WifiConfiguration config : configurations) {
            if(config.priority > maxPriority)
                maxPriority = config.priority;
        }

        return maxPriority;
    }

    public boolean isConnectedTo(String ssid){
        if(wifiManager.getConnectionInfo().getSSID() == ssid){
            return true;
        }
        return false;
    }
}
