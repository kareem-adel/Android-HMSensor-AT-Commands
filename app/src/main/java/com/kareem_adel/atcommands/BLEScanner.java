package com.kareem_adel.atcommands;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;

public class BLEScanner extends AppCompatActivity {

    public static BLEScanner mBLEScanner;
    public BLEScannerAdapter bleScannerAdapter;
    public ListView BLEList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBLEScanner = this;
        BLEList = (ListView) findViewById(R.id.BLEList);
        bleScannerAdapter = new BLEScannerAdapter();
        BLEList.setAdapter(bleScannerAdapter);
        BLEList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String BeaconMAC = ((TextView) view.findViewById(R.id.BeaconMAC)).getText().toString();
                Intent intent = new Intent(getApplicationContext(), BLEConfig.class);
                intent.putExtra("BeaconMAC", BeaconMAC);
                startActivity(intent);
            }
        });

        startBTAdapter();
    }


    public static BluetoothAdapter mBluetoothAdapter = null;

    public void startBTAdapter() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 0);
        } else {
            Toast.makeText(this, "Bluetooth Connected", Toast.LENGTH_LONG).show();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }


    public BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bleScannerAdapter.AddDevice(device);
                }
            });
        }
    };


    public void stopBTAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.disable();
        }

        Toast.makeText(this, "Bluetooth Disconnected", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth is enabled successfully !", Toast.LENGTH_SHORT).show();
                    startBTAdapter();
                } else {
                    Toast.makeText(this, "Bluetooth is NOT enabled successfully", Toast.LENGTH_SHORT).show();
                    stopBTAdapter();
                }
                break;
            }
        }
    }

    public class BLEScannerAdapter extends BaseAdapter {
        String[] keys;
        public Hashtable<String, BluetoothDevice> FoundBeacons;

        BLEScannerAdapter() {
            FoundBeacons = new Hashtable<>();
            UpdateKeySet();
        }

        public void UpdateKeySet() {
            keys = FoundBeacons.keySet().toArray(new String[FoundBeacons.size()]);
            Arrays.sort(keys);
        }

        public void AddDevice(BluetoothDevice bluetoothDevice) {
            if (FoundBeacons.get(bluetoothDevice.getAddress()) == null) {
                FoundBeacons.put(bluetoothDevice.getAddress(), bluetoothDevice);
                UpdateKeySet();
            } else {
                FoundBeacons.put(bluetoothDevice.getAddress(), bluetoothDevice);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return FoundBeacons.size();
        }

        @Override
        public Object getItem(int i) {
            return FoundBeacons.get(keys[i]);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        public class Holder {
            TextView BeaconName;
            TextView BeaconMAC;

            Holder(View view) {
                BeaconName = (TextView) view.findViewById(R.id.BeaconName);
                BeaconMAC = (TextView) view.findViewById(R.id.BeaconMAC);
            }
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) BLEScanner.this.getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.ble_item, viewGroup, false);
                view.setTag(new Holder(view));
            }
            BluetoothDevice bluetoothDevice = (BluetoothDevice) getItem(i);
            Holder holder = (Holder) view.getTag();

            holder.BeaconName.setText(bluetoothDevice.getName());
            holder.BeaconMAC.setText(bluetoothDevice.getAddress());

            return view;
        }
    }

}
