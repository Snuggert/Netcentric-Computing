package com.example.potent_app.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by root on 5-6-14.
 */
public class BluetoothServer implements Runnable {
    public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";

    BluetoothAdapter mBluetooth = BluetoothAdapter.getDefaultAdapter();
    private BluetoothServerSocket mServerSocket;
    final BTService btService;
    ConnectedThread conThread;


    // initialization.
    public BluetoothServer(BTService context) throws IOException {
        btService = context;
        conThread = null;
        if (!mBluetooth.isEnabled()) {
            throw (new IOException("No bluetooth enabled."));
        }
        Log.i("Bluetoothserver", "initialized");
        new Thread(this).start();
    }

    // This loop runs all the time, looking for new data in from the Accessory
    @Override
    public void run() {
        BluetoothSocket socket = null;
        Log.i("Bluetoothserver", "Start running");
        try {
            Log.i("Bluetoothserver", "creating server socket");
            mServerSocket = mBluetooth.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
                    UUID.fromString("cb57b1b0-efc3-11e3-ac10-0800200c9a66"));
            Log.i("Bluetoothserver", "created server socket");


        } catch (IOException e) {
            Log.e("Server Socket listening failed", "", e);
        }
        while (true) {
            try {
                Log.i("Bluetoothserver", "accepting connections");

                socket = mServerSocket.accept();
                Log.i("Bluetoothserver", "accepted a connection");

            } catch (IOException e) {
                Log.e("Socket acceptance failed", "", e);
            }

            if (socket == null)
                continue;

            Log.i("Bluetoothserver", "socket is not null");


            conThread = new ConnectedThread(socket, this);
            Log.i("Bluetoothserver", "conThread created");

            break;

        }


    }

    private void getMessage() {};

}


