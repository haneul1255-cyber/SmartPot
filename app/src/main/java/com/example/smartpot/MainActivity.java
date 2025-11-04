package com.example.smartpot;

import static connect_bluetooth.Bluetooth.BT_MESSAGE_READ;
import static connect_bluetooth.Bluetooth.REQUEST_PERMISSION_BT;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.UnsupportedEncodingException;

import connect_bluetooth.BluetoothTry;
import SwitchScreen.SwitchAddScreen;


public class MainActivity extends AppCompatActivity {
BluetoothAdapter mBluetoothAdapter;
Handler mBluetoothHandler;
    private BluetoothTry bluetoothManager;
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ✅ Bluetooth 권한 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_PERMISSION_BT);
        }

        mBluetoothHandler = new Handler(msg -> {
            if (msg.what == BT_MESSAGE_READ) {
                try {
                    String readMessage = new String((byte[]) msg.obj, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return true;
        });


        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bluetoothManager = new BluetoothTry(this);

        View addPotLayout=findViewById(R.id.add_pot);
        if (addPotLayout!=null){
            addPotLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, SwitchAddScreen.class);
                    startActivity(intent);
                }
            });
        }

    }
    // ✅ 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_BT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Bluetooth 권한 허용됨", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth 권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}