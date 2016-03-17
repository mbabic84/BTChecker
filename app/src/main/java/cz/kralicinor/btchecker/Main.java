package cz.kralicinor.btchecker;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main extends AppCompatActivity {

    private EditText console;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        console = (EditText) findViewById(R.id.mainPairedDevicesList);

        printToConsole("Receivers are registered...");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    printToConsole("BT+" + device.getName());
                } else if (action.equalsIgnoreCase(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    printToConsole("BT-" + device.getName());
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void printToConsole(String string) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("y/MM/dd - HH:mm:ss");
        String timestamp = simpleDateFormat.format(new Date());
        String consoleText = console.getText().toString();
        console.setText(timestamp + " | " + string + "\n" + consoleText);
    }
}
