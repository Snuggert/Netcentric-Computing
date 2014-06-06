package com.example.potent_client.app;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jaap on 06.06.14.
 */

public class ConnectedThread extends Thread {
    private final BluetoothSocket serverSocket;
    private final InputStream btInStream;
    private final OutputStream btOutStream;
    public Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler mHandler) {
        serverSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler = mHandler;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        btInStream = tmpIn;
        btOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = btInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            btOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) { }
    }
}