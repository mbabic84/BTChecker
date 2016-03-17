package cz.kralicinor.btchecker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Main extends AppCompatActivity {

    private EditText console;
    private BroadcastReceiver receiver;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private String deviceAddress;
    private UUID uuid = UUID.fromString( "2c15c8b2-ec47-11e5-9ce9-5e5517507c66" );
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        console = ( EditText ) findViewById( R.id.mainPairedDevicesList );

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String pairedDevices = "Paired:";
        for ( BluetoothDevice device : bluetoothAdapter.getBondedDevices() ) {
            if ( device.getName().startsWith( "MI1" ) ) {
                bluetoothDevice = device;
                deviceAddress = device.getAddress();
            }
            pairedDevices += " " + device.getName() + "[" + device.getAddress() + "]";
        }
        printToConsole( pairedDevices );

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                new Thread( new Runnable() {
                    @Override
                    public void run() {
                        if ( bluetoothDevice != null ) {
                            printToConsole( String.valueOf( bluetoothDevice.fetchUuidsWithSdp() ) );
                        }
                        else {
                            printToConsole( "Not found!" );
                        }
                        handler.postDelayed( this, 30000 );
                    }
                } ).start();
            }
        };
        handler.postDelayed( runnable, 10000 );

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive( Context context, Intent intent ) {
                String action = intent.getAction();
                if ( action.equalsIgnoreCase( BluetoothDevice.ACTION_ACL_CONNECTED ) ) {
                    BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
                    printToConsole( "BT+" + device.getName() );
                }
                else if ( action.equalsIgnoreCase( BluetoothDevice.ACTION_ACL_DISCONNECTED ) ) {
                    BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
                    printToConsole( "BT-" + device.getName() );
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver( receiver, new IntentFilter( BluetoothDevice.ACTION_ACL_CONNECTED ) );
        registerReceiver( receiver, new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECTED ) );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver( receiver );
    }

    private void printToConsole( final String string ) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "y/MM/dd - HH:mm:ss" );
        final String timestamp = simpleDateFormat.format( new Date() );
        final String consoleText = console.getText().toString();
        console.post( new Runnable() {
            @Override
            public void run() {
                console.setText( timestamp + " | " + string + "\n" + consoleText );
            }
        } );
    }
}
