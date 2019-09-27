package com.obdlibrary.bluetoothAdapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.obdlibrary.R;

import java.util.ArrayList;

/**
 * Created by user on 6/11/17.
 */

public class LeDeviceListAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<BluetoothDevice> mLeDevices;
    private ViewHolder viewholder;

    public LeDeviceListAdapter(Context mContext, ArrayList<BluetoothDevice> mLeDevices) {
        this.mContext = mContext;
        this.mLeDevices = mLeDevices;
    }

    @Override
    public int getCount() {
//        Log.d("LedeviceAdapter::","getcount::" + mLeDevices.size());
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null){
            view = View.inflate(mContext, R.layout.item_le_device, null);
            viewholder = new ViewHolder();
            viewholder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewholder.addresstext = (TextView)view.findViewById(R.id.address_text);
            view.setTag(viewholder);
        }else
            viewholder = (ViewHolder) view.getTag();

        viewholder.deviceName.setText(mLeDevices.get(i).getName());
        viewholder.addresstext.setText(mLeDevices.get(i).getAddress());
        return view;
    }

    public void addDevice(BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    public class ViewHolder {
        TextView deviceName;
        TextView addresstext;
    }
}
