package com.kareem_adel.atcommands;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.UUID;

public class BLEConfig extends AppCompatActivity {
    String State = "Not Connected";
    BluetoothGatt mBluetoothGatt;
    BluetoothGattCharacteristic TX;
    BluetoothGattCharacteristic RX;
    String BeaconMAC;
    TextView ResultWindow;
    TextView ConnectedTo;
    EditText Command;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleconfig);

        ResultWindow = (TextView) findViewById(R.id.ResultWindow);
        ConnectedTo = (TextView) findViewById(R.id.ConnectedTo);
        Command = (EditText) findViewById(R.id.Command);

        BeaconMAC = (String) getIntent().getExtras().get("BeaconMAC");
        ConnectedTo.setText("Connected to sensor " + BeaconMAC);
        BluetoothDevice bluetoothDevice = BLEScanner.mBluetoothAdapter.getRemoteDevice(BeaconMAC);
        HandleBLEConnection(bluetoothDevice);
    }

    public void HandleBLEConnection(BluetoothDevice bluetoothDevice) {
        mBluetoothGatt = bluetoothDevice.connectGatt(BLEScanner.mBLEScanner.getApplicationContext(), false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    mBluetoothGatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                TX = gatt.getService(UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")).getCharacteristic(UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB"));
                RX = gatt.getService(UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")).getCharacteristic(UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB"));
                gatt.setCharacteristicNotification(RX, true);
                BluetoothGattDescriptor bluetoothGattDescriptor = RX.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
                bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                TX.setValue("AT".getBytes(Charset.forName("UTF-8")));
                gatt.writeCharacteristic(TX);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                final String newResult = new String(characteristic.getValue());
                BLEConfig.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ResultWindow.setText(newResult);
                    }
                });
            }
        });
        refreshDeviceCache(mBluetoothGatt);
        mBluetoothGatt.connect();
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
        }
        return false;
    }

    public void Enter(View view) {
        TX.setValue(Command.getText().toString().getBytes(Charset.forName("UTF-8")));
        mBluetoothGatt.writeCharacteristic(TX);
    }


    @Override
    public void onBackPressed() {
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
        super.onBackPressed();
    }
}
