package com.example.potent_client.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.potent_client.app.R;

import java.io.IOException;
import java.util.UUID;


/**
 * Created by jaap on 05.06.14.
 */
public class ConnectBluetooth extends Activity {
    ArrayAdapter<String>    deviceList;
    BluetoothAdapter        bluetoothAdapter;
    BluetoothDevice         btServer;
    ListView                showList;
    ConnectThread           connectThread;

    BTService mBTService;

    boolean mBound;

    int REQUEST_ENABLE_BT = 1;

    private final BroadcastReceiver broadCastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                deviceList.add(device.getName() + "\n" + device.getAddress());
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i("ConnectBluetooth", "BC received: Connected");
                sendResult();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                // TODO
            }
        }
    };

    protected void sendResult() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_list);

        showList = (ListView)findViewById(R.id.bluetoothList);
        deviceList = new ArrayAdapter<String>(this, R.layout.simple_row);
        showList.setAdapter(deviceList);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        showList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String address = ((TextView) view).getText().toString().split("\n")[1];

                connectToServer(address);
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(broadCastReceiver, filter);
        registerReceiver(broadCastReceiver, filter1);
        registerReceiver(broadCastReceiver, filter2);

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            discover();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BTService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop(){
        if (mBound) {
            unbindService(mConnection);
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            discover();
        }
    }

    private void discover() {
        if (!bluetoothAdapter.startDiscovery()) {
            Log.i("startDiscovery", "Failed");
        }
        else {
            Log.i("startDiscovery", "Succeeded");
        }
    }

    private void connectToServer(String address) {
        btServer = bluetoothAdapter.getRemoteDevice(address);
        connectThread = new ConnectThread(bluetoothAdapter, btServer);
        connectThread.start();
        mBTService.connectThread = connectThread;
    }

    @Override
    protected void onPause() {
        deviceList.clear();
        bluetoothAdapter.cancelDiscovery();
        super.onPause();
    }

    @Override
    protected void onResume() {
        discover();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadCastReceiver);
        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BTService.LocalBinder binder = (BTService.LocalBinder) service;
            mBTService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i("ServiceConnection", "unbinding");
            mBound = false;
        }
    };
}

