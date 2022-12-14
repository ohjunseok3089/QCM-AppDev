package com.example.qcm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.qcm.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int REQUEST_ENABLE_BT = 10;    // Bluetooth Activation Status
    private BluetoothAdapter bluetoothAdapter;          // Bluetooth Adaptor
    private Set<BluetoothDevice> devices;               // Bluetooth Devices, SET
    private BluetoothDevice bluetoothDevice;            // Bluetooth Device, Object
    private BluetoothSocket bluetoothSocket = null;     // Bluetooth Socket
    private OutputStream outputStream = null;           // Bluetooth Output Stream
    private InputStream inputStream = null;             // Bluetooth Input Stream * Important
    private Thread workerThread = null;                 // To receive a String
    private byte[] readBuffer;                          // Buffered Reader
    private int readBufferPosition;                     // Saves location for buffered reader
    private boolean onBT = false;                       // To check is for bluetooth
    public ProgressDialog asyncDialog;
    public TextView tvBT;
    public TextView rdata;

    int duration = Toast.LENGTH_LONG;                   // for showToast(), Toast Length
    String uid = "98:D3:41:F6:8D:DE";                   // HC-05 uid
    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @SuppressLint("MissingPermission") // permission must be checked before the call of the function!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        rdata = (TextView) findViewById(R.id.receive_data);
        rdata.setText("This is demo textView for received data");
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_frequency, R.id.navigation_temperature, R.id.navigation_imaging, R.id.navigation_database)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        // Bluetooth connection
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println(bluetoothAdapter);

        bluetoothDevice = bluetoothAdapter.getRemoteDevice(uid);
        System.out.println(bluetoothDevice.getName());
        Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " ?????? ??????!", Toast.LENGTH_SHORT).show();
        int cntTry = 0;
        do {
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                System.out.println(bluetoothSocket);
                bluetoothSocket.connect();
//                deviceName.setText(bluetoothDevice.getName());
                System.out.println(bluetoothSocket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }
            cntTry++;
        } while (!bluetoothSocket.isConnected() && cntTry < 3);

        try {
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
//            byte[] bytes = new byte[1024];
//            int bytesRead = inputStream.read(bytes);
            receiveData();
//            String text = new String(bytes, "ASCII");
//            text = text.substring(0, bytesRead);
//            System.out.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            bluetoothSocket.close();
//            System.out.println(bluetoothSocket.isConnected());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        // Bluetooth connection DONE


    }

    @SuppressLint("MissingPermission")
    public void connectBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println(bluetoothAdapter);

        bluetoothDevice = bluetoothAdapter.getRemoteDevice(uid);
        System.out.println(bluetoothDevice.getName());
        Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " ?????? ??????!", Toast.LENGTH_SHORT).show();

        int cntTry = 0;
        do {
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                System.out.println(bluetoothSocket);
                bluetoothSocket.connect();
                System.out.println(bluetoothSocket.isConnected());
            } catch (IOException e) {
                e.printStackTrace();
            }
            cntTry++;
        } while(!bluetoothSocket.isConnected() && cntTry < 3);
        try {
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            byte[] bytes = new byte[1024];
            int bytesRead = inputStream.read(bytes);
//            receiveData();
            String text = new String(bytes, "ASCII");
//            text = text.substring(0, bytesRead);
            System.out.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            bluetoothSocket.close();
//            System.out.println(bluetoothSocket.isConnected());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void receiveData() {
        final Handler handler = new Handler();

        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        int byteAvaliable = inputStream.available();
                        if (byteAvaliable > 0) {
                            byte[] bytes = new byte[byteAvaliable];
                            inputStream.read(bytes);
                            for (int i = 0; i < byteAvaliable; i++) {
                                byte curByte = bytes[i];
                                if (curByte == '\n') {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String text = new String(encodedBytes, "ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            rdata.setText(text);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = curByte;
                                }

                            }
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public void setRequestEnableBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        bluetoothRequestResult.launch(intent);  //
    }

    ActivityResultLauncher<Intent> bluetoothRequestResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Log.e("Activity result", "OK");
                        Intent data = result.getData();

                    }
                }
            }
    );

    @SuppressLint("MissingPermission")
    public void selectDevice() {
        // Toggles device selection menu

        devices = getBondedDevices();
        final int cntPairedDevice = devices.size();

        if (cntPairedDevice == 0) {
            // If there's no paired devices
            showToast("Please pair a QCM device!");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a bluetooth device");

        // List of bluetooth devices
        List<String> listItems = new ArrayList<>();
        for (BluetoothDevice device : devices) {
            listItems.add(device.getName());
        }
        listItems.add("Cancel");

        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == cntPairedDevice) {
                    // If select cancel
                    finish();
                } else {
                    connectToSelectedDevice(items[i].toString());
                }
            }
        });

        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void connectToSelectedDevice(String deviceName) {
        bluetoothDevice = getDeviceFromBondedList(deviceName);

        // Process Dialog
        asyncDialog = new ProgressDialog(MainActivity.this);
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        asyncDialog.setMessage("Connecting Bluetooth...");
        asyncDialog.show();
        asyncDialog.setCancelable(false);

        Thread BTconnect = new Thread(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                try {
                    UUID uuid = UUID.fromString(uid);    // HC-06 UUID
                    // Create Socket
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);

                    // RFCOMM Server connection
                    bluetoothSocket.connect();

                    // INIT Data Stream
                    outputStream = bluetoothSocket.getOutputStream();
                    inputStream = bluetoothSocket.getInputStream();

                    runOnUiThread(new Runnable() {
                        @SuppressLint({"ShowToast", "SetTextI18n"})
                        @Override
                        public void run() {
                            showToast(deviceName + " is successfully connected!");
                            tvBT.setText(deviceName + " Connected");
                            asyncDialog.dismiss();
                        }
                    });

                    onBT = true;
                } catch (Exception e){
                    runOnUiThread(new Runnable() {
                        @SuppressLint({"ShowToast", "SetTextI18n"})
                        @Override
                        public void run() {
                            tvBT.setText("Connection Error! - Please check Bluetooth status");
                            asyncDialog.dismiss();
                            showToast("Bluetooth connection error!");

                        }
                    });
                }
            }
        });
        BTconnect.start();
    }

    @SuppressLint("MissingPermission")
    public BluetoothDevice getDeviceFromBondedList(String deviceName) {
        BluetoothDevice selectedDevice = null;

        for (BluetoothDevice device : devices) {
            if (deviceName.equals(device.getName())){
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }

    @SuppressLint("MissingPermission")
    public Set<BluetoothDevice> getBondedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }



    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, duration).show();
    }
}
