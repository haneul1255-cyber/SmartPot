package connect_bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smartpot.R;

import SwitchScreen.SwitchMainScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddScreenActivity extends AppCompatActivity {
    private BluetoothTry bluetoothManager;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        bluetoothManager = new BluetoothTry(this);
        
        // 블루투스 활성화를 위한 ActivityResultLauncher 설정
        bluetoothEnableLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "블루투스가 활성화되었습니다", Toast.LENGTH_SHORT).show();
                    checkBluetoothAndListDevices();
                } else {
                    Toast.makeText(this, "블루투스 활성화가 취소되었습니다", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.addscreen);

        View addBluetooth = findViewById(R.id.add_bluetooth);
        View plantSearchEditText = findViewById(R.id.plant_search_edittext);
        View plantListRecyclerVeiw = findViewById(R.id.plant_list_recyclerview);
        View mainScreenBTN = findViewById(R.id.BTNback);
        
        mainScreenBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddScreenActivity.this, SwitchMainScreen.class);
                startActivity(intent);
            }
        });

        if(addBluetooth != null){
            addBluetooth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 클릭 확인을 위한 로그
                    Toast.makeText(AddScreenActivity.this, "블루투스 연결 버튼 클릭됨", Toast.LENGTH_SHORT).show();
                    requestBluetoothPermissionsAndConnect();
                }
            });
        }
    }

    private void requestBluetoothPermissionsAndConnect() {
        // 권한 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                },
                REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            checkBluetoothAndListDevices();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBluetoothAndListDevices();
            } else {
                Toast.makeText(this, "블루투스 권한이 필요합니다", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkBluetoothAndListDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "이 기기는 블루투스를 지원하지 않습니다", Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothEnableLauncher.launch(enableBtIntent);
            return;
        }

        // 블루투스가 활성화되어 있으면 페어링된 기기 목록 표시
        listPairedDevices();
    }

    private void listPairedDevices() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            
            if (pairedDevices.size() > 0) {
                List<String> deviceNames = new ArrayList<>();
                List<BluetoothDevice> deviceList = new ArrayList<>();
                
                for (BluetoothDevice device : pairedDevices) {
                    deviceNames.add(device.getName() != null ? device.getName() : device.getAddress());
                    deviceList.add(device);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("페어링된 블루투스 기기 선택");
                builder.setItems(deviceNames.toArray(new CharSequence[0]), (dialog, which) -> {
                    BluetoothDevice selectedDevice = deviceList.get(which);
                    connectToDevice(selectedDevice);
                });
                builder.setNegativeButton("취소", null);
                builder.show();
            } else {
                Toast.makeText(this, "페어링된 기기가 없습니다", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "블루투스가 비활성화되어 있습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        if (device != null) {
            String deviceName = device.getName() != null ? device.getName() : device.getAddress();
            bluetoothManager.connectBluetooth(deviceName);
            Toast.makeText(this, deviceName + " 연결 시도 중...", Toast.LENGTH_SHORT).show();
        }
    }
}
