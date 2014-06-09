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
    Potent potent;
    ConnectedThread thread;


    // initialization.
    public BluetoothServer(Potent context) throws IOException {
        potent = context;
        thread = null;
        if (!mBluetooth.isEnabled()) {
            throw (new IOException("No bluetooth enabled."));
        }
        Log.i("Bluetoothserver", "initialized");
        run();
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


            thread = new ConnectedThread(socket);
            Log.i("Bluetoothserver", "socket is not null");

            break;

        }


    }

    private void getMessage() {};

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.i("connectedthread", "initializing");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            run();
        }

        public void run() {
            Log.i("connectedthread", "is now running");

            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    // potent.sendToMbed("" + bytes);
                    potent.potentSlider.setProgress(bytes);
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}


