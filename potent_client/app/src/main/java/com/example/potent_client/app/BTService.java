package com.example.potent_client.app;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.nio.ByteBuffer;

/**
 * Created by jelte on 9-6-14.
 */
public class BTService extends Service {
    private final IBinder mBinder = new LocalBinder();
    public ConnectThread connectThread = null;
    public ConnectedThread connectedThread = null;
    public int currentTarget;
    public int currentPosition;
    boolean started = false;

    public class LocalBinder extends Binder {
        BTService getService() {
            // Return this instance of BTService so clients can call public methods
            return BTService.this;
        }
    }


    public void onStartCommand() {

        started = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("BTService", "Binding");
        if (!started) {
            Log.i("BTService", "creating threads");
            startService(intent);
        }

        return mBinder;
    }

    private int bytetoint(byte b) {
        int t = ((Byte)b).intValue();
        if (t < 0)
        {
            t += 256;
        }
        return t;
    }


    private byte[] inttobyte(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }
}
