package com.example.potent_client.app;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.bluetooth.BluetoothAdapter;
import android.widget.Toast;

import java.util.Set;

import static com.example.potent_client.app.R.layout;


public class PotentClient extends ActionBarActivity implements OnSeekBarChangeListener {
    SeekBar posSlider;
    TextView posField;
    Button connectButton;
    Bundle msgBundle;
    String message;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    msgBundle = msg.getData();
                    message = (String)msg.obj;
                    Log.i("Msg handler", "Got msg: " + message);
                    break;
                default:
                    break;
            }
        }
    };

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
                //btIntent.putExtra("key", value); //Optional parameters
                PotentClient.this.startActivity(btIntent);
            }
        });
        Log.i("PotentClient", "Button initiated");

        Intent intent = getIntent();


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
        // Send position to other device
        int duration = Toast.LENGTH_SHORT;

        String text = "Sending position " + seekBar.getProgress();

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

}
