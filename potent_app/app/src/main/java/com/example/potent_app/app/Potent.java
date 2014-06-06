package com.example.potent_app.app;

import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
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
    BluetoothServer mBluetoothServer;

    boolean mbed_attached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potent);

        sliderVal = (TextView)findViewById(R.id.sliderVal);
        posField = (TextView)findViewById(R.id.showPosition);

        potentSlider = (SeekBar)findViewById(R.id.potentSlider);
        potentSlider.setOnSeekBarChangeListener(this);
        potentSlider.setMax(10);
        mBluetoothServer = null;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(discoverable, DISCOVERABLE_ENABLE_BT);

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
                        int position = bytetoint(in[1]) + bytetoint(in[2]) * 256;

                        posField.setText("" + position);
                        byte[] bytes = ByteBuffer.allocate(4).putInt(position).array();
                        if (mBluetoothServer != null && mBluetoothServer.thread != null) {
                            mBluetoothServer.thread.write(bytes);
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
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            // Bluetooth enabled


        }
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
        sendToMbed("" + seekBar.getProgress());

    }

    public void sendToMbed(String mesg) {
        mbed.sendString(mesg);
    }

    @Override
    public void onDestroy() {
        if (mbed != null)
            mbed.onDestroy(this);

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
        if(requestCode == DISCOVERABLE_ENABLE_BT){
            if(resultCode == RESULT_OK){
                try{
                    mBluetoothServer = new BluetoothServer(this);
                }catch(IOException e){
                    Log.d("nope", "", e);
                }
            }else if(resultCode == RESULT_CANCELED){

            }
        }
    }


    private int bytetoint(byte b) {
        int t = ((Byte)b).intValue();
        if (t < 0)
        {
            t += 256;
        }
        return t;
    }
}
