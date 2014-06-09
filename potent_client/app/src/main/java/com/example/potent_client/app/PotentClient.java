package com.example.potent_client.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


import java.nio.ByteBuffer;

import static com.example.potent_client.app.R.layout;


public class PotentClient extends ActionBarActivity implements OnSeekBarChangeListener {
    SeekBar posSlider;
    TextView posField;
    Button connectButton;
    BTService mBTService;

    boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_potent_client);
        Log.i("PotentClient", "startup");

        connectButton = (Button)findViewById(R.id.connect);
        posField = (TextView)findViewById(R.id.sliderPosition);
        posSlider = (SeekBar)findViewById(R.id.posSlider);
        posSlider.setOnSeekBarChangeListener(this);
        posSlider.setMax(10);
        //posSlider.setEnabled(false);
        Log.i("PotentClient", "View initiated");


        connectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent btIntent = new Intent(PotentClient.this, ConnectBluetooth.class);
                PotentClient.this.startActivityForResult(btIntent, Constants.CONNECT_BT);
            }
        });
        Log.i("PotentClient", "Button initiated");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BTService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop(){
        if (mBound) {
            unbindService(mConnection);
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CONNECT_BT && resultCode == RESULT_OK) {
            Log.i("PotentClient", "Back after activityResult");
            connectButton.setText("CONNECTED");

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.potent_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int pos, boolean b) {
        posField.setText("" + pos);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        byte[] toSend = intToByte(seekBar.getProgress());

        if (mBTService == null || mBTService.connectThread == null ||
                mBTService.connectThread.connectedThread == null)
            return;

        mBTService.connectThread.connectedThread.write(toSend);
    }

    private byte[] intToByte(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BTService.LocalBinder binder = (BTService.LocalBinder) service;
            mBTService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i("ServiceConnection", "unbinding");
            mBound = false;
        }
    };
}
