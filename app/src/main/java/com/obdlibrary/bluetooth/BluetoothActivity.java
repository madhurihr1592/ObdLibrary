package com.obdlibrary.bluetooth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.AvailableCommandNames;
import com.obdlibrary.R;
import com.obdlibrary.bluetoothAdapters.LeDeviceListAdapter;
import com.obdlibrary.obd.*;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "BluetoothActivityLog";
    private static final long UPDATE_PERIOD = 1000;
    private BluetoothManager mBluetoothManager;
    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<>();
    private LeDeviceListAdapter leDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Dialog dialog;
    public boolean isDialogShown;

    private boolean isServiceBound;
    private boolean preRequisites = true;
    private AbstractGatewayService service;

    private final Runnable mQueueCommands = new Runnable() {
        public void run() {
            if (service != null && service.isRunning() && service.queueEmpty()) {
                queueCommands();
            }
            // run again in period defined in preferences
            new Handler().postDelayed(mQueueCommands, UPDATE_PERIOD);
        }
    };


    private ServiceConnection serviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d(TAG, className.toString() + " service is bound");
            isServiceBound = true;
            service = ((AbstractGatewayService.AbstractGatewayServiceBinder) binder).getService();
            service.setContext(BluetoothActivity.this);
//            Log.d(TAG, "Starting live data");
            Log.d(TAG, "Starting live data New");

            new ObdServiceAsync(mLeDeviceClicked).execute();//added on 20th aug

            //commented on 20th aug
            /*try {
                service.startService(mLeDeviceClicked);
//                Toast.makeText(BluetoothActivity.this,"CONNECTED",Toast.LENGTH_LONG).show();
                onLiveDataStarted();
            } catch (IOException ioe) {
                Log.e(TAG, "Failure Starting live data");
//                Toast.makeText(BluetoothActivity.this,"ERROR CONNECTING!!",Toast.LENGTH_LONG).show();
                doUnbindService();
                onConnectionFailure();
            }*/

            //////comment
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        // This method is *only* called when the connection to the service is lost unexpectedly
        // and *not* when the client unbinds (http://developer.android.com/guide/components/bound-services.html)
        // So the isServiceBound attribute should also be set to false when we unbind from the service.
        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, className.toString() + " service is unbound");
            isServiceBound = false;
            onServiceDisconnectedCallback();
        }
    };

    public void onLiveDataStarted() {

    }

    public void onServiceDisconnectedCallback() {

    }

    public void onConnectionFailure() {

    }

    private BluetoothDevice mLeDeviceClicked;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mBluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Log.d(TAG, "Bluetooth not supported");
            //Handle this issue. Report to the user that the device does not support BLE
            showalert(this.getResources().getString(R.string.bluetooth_not_supported));
            return;
        } else {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        if (!checkBluetoothSupport(mBluetoothAdapter)) {
            showalert(this.getResources().getString(R.string.bluetooth_not_supported));
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is currently disabled...enabling");
            showBluetoothEnableDialog();
        } else {
//            initializeLeAdapter();
            Log.d(TAG, "Bluetooth enabled...starting services");
        }
//        scanLeDevice();
    }

    private void showBluetoothEnableDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.bluetooth_enable))
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        mBluetoothAdapter.enable();
                    }
                });
        final AlertDialog alert = builder.create();
        if (!isFinishing())
            alert.show();
    }


    public void showPairedLeDevicesAndConnect() {
        leDeviceListAdapter.clear();
        boolean isDeviceFound = false;
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>(bondedDevices);
        for (int i = 0; i < bluetoothDevices.size(); i++) {
            if (bluetoothDevices.get(i).getName() != null) {
                Log.d("BluetoothActivity::", "Bonded Name: " + bluetoothDevices.get(i).getName());
                Log.d("BluetoothActivity::", "Bonded mac: " + bluetoothDevices.get(i).getAddress());
                Log.d("BluetoothActivity::", "Bonded uuid: " + Arrays.toString(bluetoothDevices.get(i).getUuids()));

                ParcelUuid[] uuids = bluetoothDevices.get(i).getUuids();

                if (uuids != null) {
                    for (int k = 0; k < uuids.length; k++) {
//                    if (uuids[k] == new ParcelUuid(com.kia.obd.obd.BluetoothManager.MY_UUID)){
                        if (uuids[k].toString().equalsIgnoreCase("00001101-0000-1000-8000-00805F9B34FB")) {
                            isDeviceFound = true;
                            leDeviceListAdapter.addDevice(bluetoothDevices.get(i));
                            leDeviceListAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                } else {
                    Log.d("BluetoothActivity::", "UUID is null");
                }
            }
        }

        Log.d("BluetoothActivity::", "isDeviceFound" + isDeviceFound);

        if (!isDeviceFound) {
            Toast.makeText(this, getResources().getString(R.string.no_device_found), Toast.LENGTH_LONG).show();
            changeStatus(this.getResources().getString(R.string.connect));
        }
//        Log.d("BluetoothActivity::","showPairedLeDevices: "+mLeDevices.size());
//        Log.d("BluetoothActivity::","isDialogShown: "+isDialogShown);

        if (mLeDevices.size() != 0)
            if (!isDialogShown) {
                showdialog();
            }
    }

    public void changeStatus(String message) {

    }

    private void queueCommands() {
        if (isServiceBound) {
            for (ObdCommand Command : ObdConfig.getCommands()) {
                service.queueJob(new ObdCommandJob(Command));
            }
        }
    }

    public void stateUpdate(final ObdCommandJob job) {
        stateUpdate(job, isServiceBound);
    }

    public void stateUpdate(ObdCommandJob job, boolean isServiceBound) {

    }

    public void stopLiveData() {
        Log.d(TAG, "Stopping live data..");
        doUnbindService();
    }

    public static String LookUpCommand(String txt) {
        for (AvailableCommandNames item : AvailableCommandNames.values()) {
            if (item.getValue().equals(txt)) return item.name();
        }
        return txt;
    }

    public void showdialog() {
        Log.d(TAG, "showdialog");
        isDialogShown = true;
        dialog = new Dialog(this);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_device);

        dialog.setCancelable(false);

        ListView mdeviceList = (ListView) dialog.findViewById(R.id.device_list);
        TextView mCancelBtn = (TextView) dialog.findViewById(R.id.cancel_btn);
        mdeviceList.setAdapter(leDeviceListAdapter);

        mdeviceList.setOnItemClickListener(this);

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isDialogShown = false;
                mLeDevices.clear();
                leDeviceListAdapter.notifyDataSetChanged();
                if (!isFinishing()) {
                    dialog.dismiss();
                }
                onServiceDisconnectedCallback();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                isDialogShown = false;
                mLeDevices.clear();
                leDeviceListAdapter.notifyDataSetChanged();
                onServiceDisconnectedCallback();
            }
        });

        if (!isFinishing())
            dialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        BluetoothDevice leDeviceClicked = mLeDevices.get(i);
        Log.d(TAG, "leDeviceClicked :: " + leDeviceClicked);
        if (leDeviceClicked == null)
            return;

        if (dialog != null)
            if (!isFinishing())
                dialog.dismiss();
        onItemClicked(true, leDeviceClicked);
    }

    public void onItemClicked(boolean itemClicked, BluetoothDevice leDeviceClicked) {
        //sleep for 500 ms and then connect. this avoids gatt 133 error code
//        connectDevice(leDeviceClicked);
        mLeDeviceClicked = leDeviceClicked;
//        pairDevice(leDeviceClicked);

        doBindService();
        // start command execution
        new Handler().post(mQueueCommands);
    }

    private void doBindService() {
        Log.d(TAG, "doBindService called:: " + isServiceBound);
        if (!isServiceBound) {
            Log.d(TAG, "Binding OBD service..");
            if (preRequisites) {
                Intent serviceIntent = new Intent(this, ObdGatewayService.class);
                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
            } else {
                Intent serviceIntent = new Intent(this, MockObdGatewayService.class);
                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
            }
        }
    }

    private void doUnbindService() {
        if (isServiceBound) {
            if (service.isRunning()) {
                service.stopService();
            }
            Log.d(TAG, "Unbinding OBD service..");
            unbindService(serviceConn);
            isServiceBound = false;
        }
    }


    private void pairDevice(BluetoothDevice device) {
        Log.d(TAG, "pairDevice");
        try {
            /*Method m = device.getClass()
                    .getMethod("createBond", (Class[]) null);
            boolean isBonded = (Boolean)m.invoke(device, (Object[]) null);*/
            boolean isBonded = device.createBond();
            Log.d(TAG, "pairDevice done" + isBonded);
            /*doBindService();
            // start command execution
            new Handler().post(mQueueCommands);*/
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {

        if (bluetoothAdapter == null) {
//            Toast.makeText(this, this.getResources().getString(R.string.bluetooth_not_supported),Toast.LENGTH_SHORT).show();
            showalert(this.getResources().getString(R.string.bluetooth_not_supported));
            return false;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Bluetooth LE is not supported");
            showalert(this.getResources().getString(R.string.ble_not_supported));
//            Toast.makeText(this, this.getResources().getString(R.string.ble_not_supported),Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Log.w(TAG, "Bluetooth LE is supported");
        }
        return true;
    }

    private void showalert(String message) {
        Log.d("BluetoothActivity::", "show alert" + message);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        finishAffinity();
                    }
                });
        final AlertDialog alert = builder.create();
        if (!isFinishing())
            alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLiveData();
    }

    public class ObdServiceAsync extends AsyncTask<BluetoothDevice, Integer, Object> {

        private BluetoothDevice bluetoothDevice;

        public ObdServiceAsync(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
        }

        @Override
        protected Object doInBackground(BluetoothDevice... bluetoothDevices) {
            Log.d("ObdGatewayService::", "ObdConnectionAsync: doInBackground: ");
            try {
                service.startService(bluetoothDevice);
                return new String("Success");
            } catch (IOException ioe) {
                Log.e(TAG, "Failure Starting live data");
                return ioe;
            }
        }

        @Override
        protected void onPostExecute(Object object) {
            super.onPostExecute(object);
            Log.d("ObdGatewayService::", "ObdConnectionAsync: onPostExecute: ");
            if (object instanceof Exception) {
                doUnbindService();
                onConnectionFailure();
            } else if (object instanceof String) {
                onLiveDataStarted();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

}
