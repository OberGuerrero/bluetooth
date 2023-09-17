package co.edu.ue.practica;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.widget.Toast;
import android.Manifest;


public class MainActivity extends AppCompatActivity {

    private Context context;
    private Activity activity;
    private TextView versionAndroid;
    private int versionSDK;
    private ProgressBar pbLevelBattery;
    private TextView tvLevelBattery;
    IntentFilter batteryFilter;
    CameraManager cameraManager;
    String cameraId;
    private Button btnOn;
    private Button btnOff;
    private EditText nameFile;
    private Archivo archivo;
    private TextView tvConection;
    ConnectivityManager conexion;
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        begin();
        batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, batteryFilter);
        btnOn.setOnClickListener(this::onLight);
        btnOff.setOnClickListener(this::offLight);

        // Aquí puedes agregar el código relacionado con el Bluetooth
        Button btnToggleBluetooth = findViewById(R.id.btnBluetooth);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        btnToggleBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBluetooth();
            }
        });
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSION);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void toggleBluetooth() {
        if (bluetoothAdapter == null) {
            // El dispositivo no admite Bluetooth
            Toast.makeText(this, "El dispositivo no admite Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(this, "Bluetooth desactivado", Toast.LENGTH_LONG).show();
        } else {
            bluetoothAdapter.enable();
            Toast.makeText(this, "Bluetooth activado", Toast.LENGTH_LONG).show();
        }
    }

    private void begin(){
        this.context =getApplicationContext();
        this.activity = this;
        this.versionAndroid = findViewById(R.id.tvVersionAndroid);
        this.pbLevelBattery = findViewById(R.id.pbLevelBattery);
        this.tvLevelBattery = findViewById(R.id.tvLevelBatteryLB);
        this.nameFile = findViewById(R.id.etNameFile);
        this.tvConection = findViewById(R.id.tvConection);
        this.btnOn = findViewById(R.id.btnOn);
        this.btnOff = findViewById(R.id.btnOff);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String versionSO = Build.VERSION.RELEASE;
        versionSDK = Build.VERSION.SDK_INT;
        versionAndroid.setText("Versión SO: " + versionSO + "/SDK: " + versionSDK);
        checkConnection();
    }
    private void onLight(View view){
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        try {
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void offLight(View view) {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        try {
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int levelBattery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            pbLevelBattery.setProgress(levelBattery);
            tvLevelBattery.setText("Nivel de la batería: " + levelBattery + "%");
        }
    };

    private void checkConnection(){
        conexion = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conexion.getActiveNetworkInfo();
        boolean stateNet = network != null && network.isConnectedOrConnecting();
        if (stateNet) tvConection.setText("state ON");
        else tvConection.setText("state OFF");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El permiso BLUETOOTH_CONNECT se concedió, puedes activar o desactivar el Bluetooth
                toggleBluetooth();
            } else {
                // El permiso BLUETOOTH_CONNECT fue denegado
                Toast.makeText(this, "Permiso denegado para activar/desactivar Bluetooth",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
