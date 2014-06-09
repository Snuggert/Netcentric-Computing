package com.example.potent_client.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
    int REQUEST_ENABLE_BT = 1;

    ArrayAdapter<String> deviceList;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice btServer;
    ListView showList;
    Handler mHandler;

    private final BroadcastReceiver broadCastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent

                // Add the name and address to an array adapter to show in a ListView
                deviceList.add(device.getName() + "\n" + device.getAddress());
                Log.i("broadcastReceiver", "Bluetooth device found: Now " + deviceList.getCount() + " items in deviceList");
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i("ConnectBluetooth", "BC received: Connected");
                Intent mainIntent = new Intent(ConnectBluetooth.this, PotentClient.class);
                mainIntent.putExtra("Device", device); //Optional parameters
                ConnectBluetooth.this.startActivity(mainIntent);

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                // TODO
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_list);
        Log.i("Connectbluetooth", "Activity started");

        //Intent intent = getIntent();

        showList = (ListView)findViewById(R.id.bluetoothList);
        deviceList = new ArrayAdapter<String>(this, R.layout.simple_row);
        showList.setAdapter(deviceList);
        Log.i("deviceList", "initiated");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.i("bluetoothAdapter", "gotten");
        Log.i("deviceList", "Tests added");


        showList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                String address = item.split("\n")[1];
                Log.i("Chosen address to connect:", "-" + address +"-");

                connectToServer(address);
            }
        });

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(broadCastReceiver, filter);
        registerReceiver(broadCastReceiver, filter1);
        registerReceiver(broadCastReceiver, filter2);


        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.i("BT Adapter", "Not enabled");
        }
        else {
            Log.i("BT Adapter", "Enabled");
            discover();
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            // Bluetooth enabled

           /* ArrayAdapter deviceList;
            deviceList = new ArrayAdapter(this, R.layout.bluetooth_list);

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    deviceList.add(device.getName() + "\n" + device.getAddress());
                }
            }*/
            Log.i("BT Adapter", "Enabled now");
            discover();

        }
    }

    private void discover() {
        if (!bluetoothAdapter.startDiscovery()) {
            // Something went wrong
            Log.i("startDiscovery", "Failed");
        }
        else {
            Log.i("startDiscovery", "Succeeded");

        }
    }

    private void connectToServer(String address) {
        btServer = bluetoothAdapter.getRemoteDevice(address);

        new ConnectThread(bluetoothAdapter, btServer, mHandler);


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
}

