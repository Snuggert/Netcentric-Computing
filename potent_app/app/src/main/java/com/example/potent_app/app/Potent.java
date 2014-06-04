package com.example.potent_app.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.example.adkPort.AdkPort;
import java.io.IOException;


public class Potent extends ActionBarActivity {
    TextView posField;
    AdkPort mbed;

    boolean mbed_attached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potent);

        posField = (TextView)findViewById(R.id.showPosition);

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

    private int bytetoint(byte b) {
        int t = ((Byte)b).intValue();
        if (t < 0)
        {
            t += 256;
        }
        return t;
    }
}
