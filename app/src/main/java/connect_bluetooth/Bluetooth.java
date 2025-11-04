package connect_bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevice;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    final static int BT_REQUEST_ENABLE = 1;
    public final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    public final static int REQUEST_PERMISSION_BT = 100;

    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        // ✅ Bluetooth 권한 요청
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.BLUETOOTH_CONNECT,
//                            Manifest.permission.BLUETOOTH_SCAN,
//                            Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_PERMISSION_BT);
//        }
//
//        mBluetoothHandler = new Handler(msg -> {
//            if (msg.what == BT_MESSAGE_READ) {
//                try {
//                    String readMessage = new String((byte[]) msg.obj, "UTF-8");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//            }
//            return true;
//        });
//    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connectBluetooth(){
        if(bluetoothOn()){
            listPairedDevices();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    boolean bluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다", Toast.LENGTH_LONG).show();
            return false;
        }
        if (mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "이미 활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BT_REQUEST_ENABLE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "블루투스 활성화됨", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "블루투스 활성화 취소", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void listPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "BLUETOOTH_CONNECT 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("페어링된 장치 선택");

                mListPairedDevice = new ArrayList<>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevice.add(device.getName());
                }

                final CharSequence[] items = mListPairedDevice.toArray(new CharSequence[0]);
                builder.setItems(items, (dialog, item) -> connectSelectedDevice(items[item].toString()));
                builder.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    void connectSelectedDevice(String selectedDeviceName) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }

        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            Toast.makeText(getApplicationContext(), "연결 성공: " + selectedDeviceName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "연결 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();  // ✅ 빠졌던 부분 추가
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "스트림 생성 실패", Toast.LENGTH_SHORT).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer.clone()).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String str) {
            try {
                mmOutStream.write(str.getBytes());
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "전송됨: " + str, Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show());
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    // ✅ 권한 요청 결과 처리
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSION_BT) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Bluetooth 권한 허용됨", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Bluetooth 권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}
