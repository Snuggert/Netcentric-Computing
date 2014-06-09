package com.example.potent_app.app;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.adkPort.AdkPort;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jelte on 9-6-14.
 */
public class BTService extends Service {
    private final IBinder mBinder = new LocalBinder();
    public BluetoothServer mBluetoothServer = null;
    public int currentTarget;
    public int currentPosition;
    public AdkPort mbed;
    boolean started = false;
    public Potent mPotent;

    public class LocalBinder extends Binder {
        BTService getService() {
            // Return this instance of BTService so clients can call public methods
            return BTService.this;
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
    }

    private void startMbed() {
        try {
            mbed = new AdkPort(this);
        } catch (IOException e) {
            return;
        }


        mbed.attachOnNew(new AdkPort.MessageNotifier(){
            @Override
            public void onNew()
            {
                byte[] in = mbed.readB();
                switch(in[0])
                {
                    case 'P':
                        currentPosition = byteToInt(in[1]) + byteToInt(in[2]) * 256;

                        byte[] bytes = intToByteArr(currentPosition);
                        if (mBluetoothServer != null && mBluetoothServer.conThread != null) {
                            mBluetoothServer.conThread.write(bytes);
                        }

                        break;
                    default:
                        break;
                }
            }
        });

        Thread thread = new Thread(mbed);
        thread.start();
        mbed.sendString("GO");

    }

    @Override
    public int onStartCommand(Intent intent, int x, int y) {

        try{
            Log.i("BTService", "Setting up server");
            mBluetoothServer = new BluetoothServer(this);
            Log.i("BTService", "Set up server");


        }catch(IOException e){
            Log.d("nope", "", e);
        }
        started = true;

        return START_STICKY_COMPATIBILITY;
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

    public int byteToInt(byte b) {
        int t = ((Byte)b).intValue();
        if (t < 0)
        {
            t += 256;
        }
        return t;
    }

    public byte[] intToByteArr(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

}
