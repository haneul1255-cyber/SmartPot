package connect_bluetooth;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpot.MainActivity;
import com.example.smartpot.R;

import SwitchScreen.SwitchAddScreen;
import SwitchScreen.SwitchMainScreen;

public class AddScreenActivity extends AppCompatActivity {
    private BluetoothTry bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        bluetoothManager=new BluetoothTry(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.addscreen); //addscreen.xml을 inflate

        View addBluetooth=findViewById(R.id.add_bluetooth);
        View plantSearchEditText=findViewById(R.id.plant_search_edittext);
        View plantListRecyclerVeiw=findViewById(R.id.plant_list_recyclerview);
        View mainScreenBTN=findViewById(R.id.BTNback);
        mainScreenBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddScreenActivity.this, SwitchMainScreen.class);
                startActivity(intent);
            }
        });



        if(addBluetooth !=null){
            addBluetooth.setOnClickListener(new View.OnClickListener() {
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                @Override
                public void onClick(View view) {
                    Bluetooth bluetooth = new Bluetooth();
                    bluetooth.connectBluetooth();
                }
            });
        }
    }
}
