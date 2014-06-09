package com.example.potent_client.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by jaap on 06.06.14.
 */
public class ConnectThread extends Thread implements Runnable {
    private final BluetoothSocket   serverSocket;
    private final BluetoothAdapter  btAdapter;
    public ConnectedThread          connectedThread;

    public ConnectThread(BluetoothAdapter adapter, BluetoothDevice device) {
        BluetoothSocket tmp = null;
        btAdapter = adapter;

        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.UUIDSTRING));
        } catch (Exception e) {
            this.cancel();
        }
        serverSocket = tmp;
    }

    public void run() {
        btAdapter.cancelDiscovery();

        try {
            serverSocket.connect();
        } catch (IOException connectException) {
            Log.i("Connect Thread Exception", connectException.getMessage() + " ");
            try {
                serverSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        connectedThread = new ConnectedThread(serverSocket);
        connectedThread.start();
    }

    public void cancel() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) { }
        }
    }
}
