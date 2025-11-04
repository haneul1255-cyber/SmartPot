package com.example.smartpot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import connect_bluetooth.BluetoothTry;

public class MainActivity extends AppCompatActivity {
    private BluetoothTry bluetoothManager;
    LinearLayout mLinearLayout = findViewById(R.id.add_bluetooth);
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bluetoothManager = new BluetoothTry(this);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        LinearLayout addBluetooth = findViewById(R.id.add_bluetooth);
        addBluetooth.setOnClickListener(v -> {
            bluetoothManager.connectBluetooth("HC-06"); // 연결할 기기 이름
        });
    }
}