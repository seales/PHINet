package com.ndnhealthnet.androidudpclient.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ndnhealthnet.androidudpclient.R;
import com.ndnhealthnet.androidudpclient.Utility.ConstVar;
import com.ndnhealthnet.androidudpclient.Utility.Utils;

import java.util.ArrayList;
import java.util.Set;

/**
 * Lists Paired Bluetooth devices and gives user option to discover more.
 * User may choose a device to connect to and poll regularly for biometric data.
 */
public class PairedSensorsListActivity  extends ListActivity {

    Button backBtn, discoverSensorBtn;
    TextView loggedInText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairedsensorlist);

        ArrayList<String> sensorList = getPairedDeviceNames();

        final SensorAdapter adapter = new SensorAdapter(this, sensorList);
        setListAdapter(adapter);

        String currentUserID = Utils.getFromPrefs(getApplicationContext(),
                ConstVar.PREFS_LOGIN_USER_ID_KEY, "");

        loggedInText = (TextView) findViewById(R.id.loggedInTextView);
        loggedInText.setText(currentUserID); // place username on screen

        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // TODO - pass back results ("onActivityFinish")

                finish();
            }
        });

        discoverSensorBtn = (Button) findViewById(R.id.discoverSensorBtn);
        discoverSensorBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                ArrayList<String> discoveredSensorNames = discoverSensors();

                // TODO - append to ListView
            }
        });
    }

    /**
     * Method returns list of (address, name) of all paired bluetooth devices
     *
     * @return list of paired bluetooth devices
     */
    private ArrayList<String> getPairedDeviceNames() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<String> pairedDeviceNames = new ArrayList<>();

        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {

                pairedDeviceNames.add(device.getName() + "\n" + device.getAddress());
            }
        }

        return pairedDeviceNames;
    }

    /**
     * Method discovers sensors then returns list of (name, address)
     *
     * @return list of discovered bluetooth devices
     */
    private ArrayList<String> discoverSensors() {
        final ArrayList<String> discoveredSensorNames = new ArrayList<>();

        // Create a BroadcastReceiver for ACTION_FOUND
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    discoveredSensorNames.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        return discoveredSensorNames;
    }

    /**
     * Used by paired sensor list view.
     */
    private class SensorAdapter extends ArrayAdapter<String> {

        Activity activity = null;
        ArrayList<String> discoveredSensors;

        public SensorAdapter(ListActivity li, ArrayList<String> allSensors)
        {
            super(li, 0, allSensors);
            discoveredSensors = allSensors;
            activity = li;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                convertView = activity.getLayoutInflater()
                        .inflate(R.layout.list_item_sensor, null);
            }

            final String sensorName = " "; // TODO - set

            // creates individual button in ListView for each patient
            Button sensorButton = (Button)convertView.findViewById(R.id.listSensorButton);
            sensorButton.setText(sensorName);
            sensorButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(PairedSensorsListActivity.this);
                    builder.setTitle("Connect to sensor?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // TODO - return to previous activity
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                }
            });
            return convertView;
        }
    }
}