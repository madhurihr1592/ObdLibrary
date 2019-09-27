package com.obdlibrary.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import com.obdlibrary.R;

public class Bluetooth {

    private static final String TAG = "Bluetooth::";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    public String checkBluetoothAvailable(Context context){
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Log.d(TAG, "Bluetooth not supported");
            //Handle this issue. Report to the user that the device does not support BLE
//            context.showalert(context.getResources().getString(R.string.bluetooth_not_supported), context);
            return context.getResources().getString(R.string.bluetooth_not_supported);
        } else {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        return "";
    }
}
