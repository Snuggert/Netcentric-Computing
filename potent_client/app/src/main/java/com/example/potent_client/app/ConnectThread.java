package com.example.potent_client.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by jaap on 06.06.14.
 */
public class ConnectThread extends Thread {
    private final BluetoothSocket serverSocket;
    private final BluetoothDevice serverDevice;
    private final BluetoothAdapter btAdapter;
    public Handler mHandler;


    public ConnectThread(BluetoothAdapter adapter, BluetoothDevice device, Handler handler) {
        BluetoothSocket tmp = null;
        serverDevice = device;
        btAdapter = adapter;
        mHandler = handler;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.UUIDSTRING));
            Log.i("ConnectThread", "Create RF done");
        } catch (Exception e) {
            Log.i("ConnectThread", "Create RF failed (wrong UUID?)");
            this.cancel();
        }
        serverSocket = tmp;
        run();
    }

    public void run() {
        Log.i("ConnectThread", "Running started");
        // Cancel discovery because it will slow down the connection
        btAdapter.cancelDiscovery();


        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            Log.i("ConnectThread", "before connecting socket");
            serverSocket.connect();
            Log.i("ConnectThread", "connect to serverSocket done");
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            Log.i("Connect Thread Exception", connectException.getMessage() + " ");
            try {
                Log.i("Connect Thread", "Connecting failed");
                serverSocket.close();
            } catch (IOException closeException) {
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        new ConnectedThread(serverSocket, mHandler);
    }

    public void cancel() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
