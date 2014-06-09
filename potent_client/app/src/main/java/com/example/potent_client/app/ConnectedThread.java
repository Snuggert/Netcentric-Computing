package com.example.potent_client.app;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by jaap on 06.06.14.
 */

public class ConnectedThread extends Thread implements Runnable {
    private final BluetoothSocket serverSocket;
    private final InputStream btInStream;
    private final OutputStream btOutStream;

    public ConnectedThread(BluetoothSocket socket) {
        serverSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
            Log.i("ConnectedThread", "In and outputstream connected");
        } catch (IOException e) { }

        btInStream = tmpIn;
        btOutStream = tmpOut;
        Log.i("ConnectedThread", "Thread created");
    }

    public void run() {
        Log.i("ConnectedThread", "Running started");
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