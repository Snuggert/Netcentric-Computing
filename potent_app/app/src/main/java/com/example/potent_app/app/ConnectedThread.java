package com.example.potent_app.app;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by jelte on 9-6-14.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final BluetoothServer mmServer;

    public ConnectedThread(BluetoothSocket socket, BluetoothServer context) {
        Log.i("connectedthread", "initializing");

        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mmServer = context;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        start();
    }

    public void run() {
        Log.i("connectedthread", "is now running");

        byte[] buffer = new byte[1024];  // buffer store for the stream
        int n_bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                Log.i("connectedthread", "trying to read");
                n_bytes = mmInStream.read(buffer);
                int target = byteArrToInt(buffer);
                Log.i("connectedthread", "read bytes");
                Log.i("connectedthread", "" + byteArrToInt(buffer));
                // Send the obtained bytes to the UI activity
                // potent.sendToMbed("" + bytes);
                mmServer.btService.currentTarget = target;
            } catch (IOException e) {
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            Log.i("connectedthread", "writing bytes");
            mmOutStream.write(bytes);
        } catch (IOException e) {
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }

    public int byteToInt(byte b) {
        int t = ((Byte)b).intValue();
        if (t < 0)
        {
            t += 256;
        }
        return t;
    }

    public int byteArrToInt(byte[] b) {
        return ByteBuffer.wrap(b).getInt();
    }


    public byte[] intToByteArr(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }
}