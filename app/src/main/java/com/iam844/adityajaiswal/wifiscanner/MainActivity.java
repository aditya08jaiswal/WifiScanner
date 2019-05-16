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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    WifiManager wifiManager;
    WifiReceiver wifiReceiver;

    WiFiListAdapter wifiListAdapter;
    ListView wifiListView;
    List wifiList;

    String[] s1;

    Button scanWifiBtn;

    private static final String TAG = MainActivity.class.getSimpleName();

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
        wifiListView = findViewById(R.id.myListView);
        scanWifiBtn = findViewById(R.id.scanWifi);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void executeListener() {

        scanWifiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager.setWifiEnabled(true);
                scanWifiList();
            }
        });

        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                showWifiPasswordDialog(view, position);
            }
        });
    }

    private void scanWifiList() {
        wifiManager.startScan();

        wifiList = wifiManager.getScanResults();
        setAdapter();
    }

    private void setAdapter() {
        wifiListAdapter = new WiFiListAdapter(getApplicationContext(), wifiList);
        wifiListView.setAdapter(wifiListAdapter);
    }

    public void showWifiPasswordDialog(View view, int position) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.wifi_password_dialog, null);

        final EditText inputPassword = mView.findViewById(R.id.wifi_Password);
        Button connectBtn = mView.findViewById(R.id.connect_Btn);
        Button cancelBtn = mView.findViewById(R.id.cancel_Btn);

        final TextView wifiSSID = mView.findViewById(R.id.wifi_SSID);

        String newVar = wifiList.get(position).toString();
        String[] s0 = newVar.split(",");
        s1 = s0[0].split(": ");
        wifiSSID.setText(s1[1]);

        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String nameSSID = s1[1];
                final String namePass = inputPassword.getText().toString();
                connectToWifi(nameSSID, namePass);

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

    public static String normalizeAndroidWifiSsid(String ssid) {
        if(ssid == null)
            return ssid;
        else
            return ssid.replace("\"", "");
    }

    /**
     * Call this to connect to a wifi network - it will trigger the WIFI_STATE_CHANGED broadcast.
     *
     * @param ssid
     * @param passphrase
     */
    public void connectToWifi(String ssid, String passphrase) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\""+ ssid +"\"";
        wifiConfig.priority=(getMaxConfigurationPriority(wifiManager)+1);
        wifiConfig.preSharedKey = "\""+ passphrase +"\"";
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.priority = getMaxConfigurationPriority(wifiManager);

        int netId = wifiManager.addNetwork(wifiConfig);

        /*
         * Note: calling disconnect or reconnect should not be required. enableNetwork(net, true)
         * second parameter is defined as boolean enableNetwork (int netId, boolean attemptConnect).
         *
         * Testing on Android Moto E (2nd gen) Andriod 6.0 the .reconnect() call would cause it to
         * reconnect ot the 'normal' wifi because connecting to the group is a bit slow.
         *
         * This method works without the .reconnect() as tested on Android 4.4.2 Samsung Galaxy ACE
         * and Moto E (2nd gen).
         */
        wifiManager.disconnect();
        boolean successful = wifiManager.enableNetwork(netId, true);
    }

    /**
     * Get maximum priority assigned to a network configuration.
     * This helps to prioritize which network to connect to.
     *
     * @param wifiManager
     * @return int: Maximum configuration priority number.
     */
    private int getMaxConfigurationPriority(final WifiManager wifiManager) {
        final List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
        int maxPriority = 0;
        for(final WifiConfiguration config : configurations) {
            if(config.priority > maxPriority)
                maxPriority = config.priority;
        }

        return maxPriority;
    }

}
