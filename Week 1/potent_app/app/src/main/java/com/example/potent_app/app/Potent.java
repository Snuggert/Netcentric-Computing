package com.example.potent_app.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.example.adkPort.AdkPort;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.bluetooth.BluetoothAdapter;

import java.io.IOException;
import java.nio.ByteBuffer;


public class Potent extends Activity implements OnSeekBarChangeListener{
    TextView posField, sliderVal;
    SeekBar potentSlider;
    AdkPort mbed;
    BluetoothAdapter mBluetoothAdapter;
    int DISCOVERABLE_ENABLE_BT = 2;
    int RESULT_OK = 120;
    BTService mBTService;
    boolean mBound = false;
    boolean discovering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Potent", "start creating");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potent);

        sliderVal = (TextView)findViewById(R.id.sliderVal);
        posField = (TextView)findViewById(R.id.showPosition);

        potentSlider = (SeekBar)findViewById(R.id.potentSlider);
        potentSlider.setOnSeekBarChangeListener(this);
        potentSlider.setMax(10);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!discovering) {
            discovering = true;

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

            startActivityForResult(discoverable, DISCOVERABLE_ENABLE_BT);
            discovering = false;
        }

    }

    @Override
    protected void onStop(){
        if (mBound) {
            unbindService(mConnection);
        }
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.potent, menu);
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        sliderVal.setText(""+progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //sendToMbed("" + seekBar.getProgress());

    }

    public void sendToMbed(String mesg) {
        mbed.sendString(mesg);
    }

    @Override
    public void onDestroy() {


        super.onDestroy();

    }

    @Override
    public void onPause() {
        if (mbed != null)
            mbed.closeAccessory();

        super.onPause();
    }

    @Override
    public void onResume() {
        if (mbed != null)
            mbed.openAccessory();

        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        Log.i("Potent", "ACTIVITY_RESULT");
        if(requestCode == DISCOVERABLE_ENABLE_BT){
            Log.i("Potent", "DISCOVERABLE_ENABLE_BT");
            Log.i("Potent", "RESULT: " + RESULT_OK + "resultCode: " + resultCode);
            if(resultCode > 0){
                Log.i("Potent", "Binding service");
                Intent intent = new Intent(this, BTService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

                Log.i("Potent", "Bound service");

            }else if(resultCode == RESULT_CANCELED){

            }
        }
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
