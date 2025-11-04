package connect_bluetooth;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BluetoothTry {
    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;

    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothTry(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connectBluetooth(String deviceName) {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(enableBtIntent);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName)) {
                    bluetoothDevice = device;
                    break;
                }
            }
        }

        if (bluetoothDevice == null) {
            Toast.makeText(context, "해당 이름의 기기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            Toast.makeText(context, "블루투스 연결 성공!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "연결 실패", Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnectBluetooth() {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                bluetoothSocket.close();
                Toast.makeText(context, "블루투스 연결 해제", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }
}