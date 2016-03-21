package cz.kralicinor.btchecker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

public class Main extends AppCompatActivity {

    private EditText console;
    private BroadcastReceiver receiver;
    private long lastBtRestart;
    private Runnable runnable;
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        console = (EditText) findViewById(R.id.mainPairedDevicesList);

        printToConsole("Receivers are registered...");

        Intent resultIntent = new Intent(this, Main.class);
        pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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

        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (lastBtRestart == 0 || System.currentTimeMillis() - lastBtRestart >= (1000 * 60 * 60)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printToConsole("Restarting bluetooth...");
                                if (bluetoothAdapter.isEnabled()) {
                                    bluetoothAdapter.disable();
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    bluetoothAdapter.enable();
                                }
                                printToConsole("Bluetooth was restarted...");
                            }
                        }).start();
                        lastBtRestart = System.currentTimeMillis();
                    }
                } finally {
                    setNotification(((1000 * 60 * 60) - (System.currentTimeMillis() - lastBtRestart)) / 1000);
                    handler.postDelayed(runnable, 1000);
                }
            }
        };

        handler.postDelayed(runnable, 15 * 1000);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setNotification(0);
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

    private void printToConsole(final String string) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("y/MM/dd - HH:mm:ss");
        final String timestamp = simpleDateFormat.format(new Date());
        final String consoleText = console.getText().toString();
        console.post(new Runnable() {
            @Override
            public void run() {
                console.setText(timestamp + " | " + string + "\n" + consoleText);
            }
        });
    }

    private void setNotification(long nextRestart) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(android.R.drawable.sym_def_app_icon);
        builder.setContentTitle("BTChecker");
        if (nextRestart > 0) {
            builder.setContentText("Next bt restart in " + nextRestart + " seconds");
        } else {
            builder.setContentText("Next bt restart will be soon...");
        }
        builder.setOngoing(true);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(3468, builder.build());
    }
}
