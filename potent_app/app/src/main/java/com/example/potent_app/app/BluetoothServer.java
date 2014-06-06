package com.example.potent_app.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by root on 5-6-14.
 */
public class BluetoothServer implements Runnable {
    public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";

    BluetoothAdapter mBluetooth = BluetoothAdapter.getDefaultAdapter();
    private BluetoothServerSocket mServerSocket;

    // initialization.
    public BluetoothServer(Context context) throws IOException {
        if (!mBluetooth.isEnabled()) {
            throw (new IOException("No bluetooth enabled."));
        }
    }

    // This loop runs all the time, looking for new data in from the Accessory
    @Override
    public void run() {
        BluetoothSocket socket = null;
        try {
            mServerSocket = mBluetooth.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
                    UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
        } catch (IOException e) {
            Log.e("Server Socket listening failed", "", e);
        }
        while (true) {
            try {
                socket = mServerSocket.accept();
            } catch (IOException e) {
                Log.e("Socket acceptance failed", "", e);
            }
            if (socket != null) {

            }
        }

    }
}
