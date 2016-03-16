package cz.kralicinor.btchecker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity {

    private byte[] runs;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        final ListView pairedDevicesList = ( ListView ) findViewById( R.id.mainPairedDevicesList );
        final TextView nextCheck = ( TextView ) findViewById( R.id.mainPairedDevicesNextCheck );

        final BluetoothAdapter bluetoothAdapter;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if ( !bluetoothAdapter.isEnabled() ) {
            bluetoothAdapter.enable();
            Toast.makeText( this, "Enabling bluetooth...", Toast.LENGTH_SHORT ).show();
        }

        runs = new byte[]{ 0 };
        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if ( runs[ 0 ] == 0 || runs[ 0 ] >= 15 ) {
                        List< String > btDevNames = new ArrayList<>();
                        for ( BluetoothDevice device : bluetoothAdapter.getBondedDevices() ) {
                            if ( device.getBondState() == BluetoothDevice.BOND_BONDED ) {
                                btDevNames.add( device.getName() );
                            }
                        }
                        final ArrayAdapter< String > arrayAdapter = new ArrayAdapter<>( getApplicationContext(), R.layout.main_devices_list_item, btDevNames );
                        if ( pairedDevicesList != null ) {
                            pairedDevicesList.post( new Runnable() {
                                @Override
                                public void run() {
                                    pairedDevicesList.setAdapter( arrayAdapter );
                                }
                            } );
                        }
                        runs[ 0 ] = 0;
                    }
                    if ( nextCheck != null ) {
                        nextCheck.post( new Runnable() {
                            @Override
                            public void run() {
                                nextCheck.setText( String.valueOf( runs[ 0 ] ) );
                            }
                        } );
                    }
                }
                finally {
                    runs[ 0 ]++;
                    handler.postDelayed( this, 1000 );
                }
            }
        };
        runnable.run();
    }
}
