package com.iam844.adityajaiswal.wifiscanner;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class WiFiListAdapter extends BaseAdapter {

    Context context;
    List<ScanResult> wiFiList;
    LayoutInflater inflater;
    public String SSIDname;
    public String newSSID;

    public WiFiListAdapter(Context context, List<ScanResult> wiFiList) {
        this.context = context;
        this.wiFiList = wiFiList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return wiFiList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        Holder holder;

        if (view == null) {

            view = inflater.inflate(R.layout.wifi_list_item, null);
            holder = new Holder();

            holder.tvDetails = view.findViewById(R.id.textWifiName);
            view.setTag(holder);

        } else {
            holder = (Holder) view.getTag();
        }

        holder.tvDetails.setText(wiFiList.get(position).SSID);



        SSIDname = wiFiList.get(position).SSID;

        String[] s0 = SSIDname.split(",");
        Log.d("S0",""+s0[0]);

        newSSID = s0[0];
        return view;
    }

}



