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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qcm.ui.database.DatabaseDetailFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import com.example.qcm.ui.frequency.FrequencyFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentContainerView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.qcm.databinding.ActivityMainBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.opencv.android.OpenCVLoader;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private Viewport viewport;
    private File curExcel;
    int duration = Toast.LENGTH_LONG;                   // for showToast(), Toast Length
    String uid = "98:D3:41:F6:8D:DE";                   // HC-05 uid
//    String uid = "98:D3:02:96:17:AE";                   // HC-05 uid 2
    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private int pointsPlotted = 1;
    private int time_counter = 0;

    private int graphIntervalCounter = 0;
    private double[] freqTemp = {0, 0};

    private CSVWriter writer;
    private FragmentManager fm;
    private FrequencyFragment frequencyFragment;
    LineGraphSeries<DataPoint> seriesFrequency = new LineGraphSeries<DataPoint>();
    LineGraphSeries<DataPoint> seriesTemp = new LineGraphSeries<DataPoint>();

    private ArrayList<DataPoint> dataPointSeriesFrequency = new ArrayList<>();
    private ArrayList<DataPoint> dataPointSeriesTemp = new ArrayList<>();
    private Workbook curWorkbook;
    private static String LOGTAG_OPENCV = "OpenCV_Log";

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission") // permission must be checked before the call of the function!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        rdata = (TextView) findViewById(R.id.receive_data);
        rdata.setText("Please connect to Bluetooth module to receive data");
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_frequency, R.id.navigation_temperature, R.id.navigation_imaging, R.id.navigation_database)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // List of permissions
        String[] permission_list = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_PRIVILEGED,
                Manifest.permission.CAMERA
        };
        // Ask for permission
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        // Checks OpenCV to initialized
        if (OpenCVLoader.initDebug()) {
            Log.e(LOGTAG_OPENCV, "OpenCV initialized!");
        }

        GraphView graph = (GraphView) findViewById(R.id.graph);

        seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));
        seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));seriesFrequency.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        seriesTemp.appendData(new DataPoint(pointsPlotted++, 100), true, pointsPlotted);
        dataPointSeriesFrequency.add(new DataPoint(pointsPlotted++, 100));
        dataPointSeriesTemp.add(new DataPoint(pointsPlotted++, 100));


        // Bluetooth connection
        connectBluetooth();
//        Demo
//        fm = getSupportFragmentManager();
//        frequencyFragment = (FrequencyFragment)fm.findFragmentById(R.id.frequencyFragment);

//        viewport = graph.getViewport();
//        viewport.setScrollable(true);
//        viewport.setXAxisBoundsManual(true);
//        graph.addSeries(seriesFrequency);
//        graph.addSeries(seriesTemp);
//        viewport.setMaxX(pointsPlotted);
//        viewport.setMinX(pointsPlotted - 1000);
//        FrequencyFragment frequencyFragment = (FrequencyFragment) getFragmentManager().findFragmentById(R.id.frequencyFragment);
//        bluetoothActivity.receiveData(rdata, series, pointsPlotted, viewport);
        // Bluetooth connection DONE

    }

    /**
     * Bluetooth Related Methods START
     */

    @SuppressLint("MissingPermission") // permission must be checked before the call of the function!
    public void connectBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println(bluetoothAdapter);

        bluetoothDevice = bluetoothAdapter.getRemoteDevice(uid);
        System.out.println(bluetoothDevice.getName());
        Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " bluetooth connection complete!", Toast.LENGTH_SHORT).show();
        int cntTry = 0;
        do {
            try {
                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
                System.out.println(bluetoothSocket);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                bluetoothSocket.connect();
                Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " socket connection complete!", Toast.LENGTH_SHORT).show();
                System.out.println(bluetoothSocket.isConnected());
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                receiveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            cntTry++;
        } while (!bluetoothSocket.isConnected() && cntTry < 3);
    }

    public void disconnectBluetooth() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Bluetooth Related Methods END
     */

    /**
     * Graph Data Related Methods START
     */
    public void receiveData() throws IOException {
        final Handler handler = new Handler();
//        FrequencyFragment fragment = (FrequencyFragment) getFragmentManager().findFragmentById(R.id.);

        readBufferPosition = 0;
        readBuffer = new byte[1024];
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String formattedDate = sdf.format(currentTime);

//        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//        String fileName = "AnalysisData_" + formattedDate + ".csv";
//        String filePath = baseDir + File.separator + fileName;
//        File f = new File(filePath);
//        writer = new CSVWriter(new FileWriter(filePath));
//        String[] header = {"time", "frequency", "temperature"};
//        writer.writeNext(header);

        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
//                for (int j = 0; j <= 10; j++) {
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
//                                            frequencyFragment.setText(text);
                                            String[] array = text.split(",");
                                            rdata.setText("Frequency: " + array[0] + "Hz | Temperature: " + array[1] + "K");
                                            DataPoint dataFreq = new DataPoint(pointsPlotted, Double.parseDouble(array[0]));
                                            DataPoint dataTemp = new DataPoint(pointsPlotted, Double.parseDouble(array[1]));
                                            seriesFrequency.appendData(dataFreq, true, pointsPlotted);
                                            seriesTemp.appendData(dataTemp, true, pointsPlotted);
                                            dataPointSeriesFrequency.add(dataFreq);
                                            dataPointSeriesTemp.add(dataFreq);

                                            freqTemp[0] = Double.parseDouble(array[0]);
                                            freqTemp[1] = Double.parseDouble(array[1]);
                                            if (curWorkbook != null){
                                                try {
                                                    Sheet sheet = curWorkbook.getSheetAt(0);
                                                    Row row = sheet.createRow(pointsPlotted);
                                                    Cell cell1 = row.createCell(0);
                                                    cell1.setCellValue(pointsPlotted);

                                                    Cell cell2 = row.createCell(1);
                                                    cell2.setCellValue(dataFreq.toString());

                                                    Cell cell3 = row.createCell(2);
                                                    cell3.setCellValue(dataTemp.toString());

                                                    FileOutputStream outputStream = new FileOutputStream(curExcel);
                                                    curWorkbook.write(outputStream);
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
//                                            try {
//                                                writer = new CSVWriter(new FileWriter(filePath, true));
//                                            } catch (IOException e) {
//                                                throw new RuntimeException(e);
//                                            }
//                                            writer.writeNext(new String[]{String.valueOf(time_counter), array[0], array[1]});
//                                            try {
//                                                writer.close();
//                                            } catch (IOException e) {
//                                                throw new RuntimeException(e);
//                                            }
                                            pointsPlotted++;
                                            time_counter++;
//                                            viewport.setMaxX(pointsPlotted);
//                                            viewport.setMinX(pointsPlotted - 50);
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
        workerThread.start();
    }

    public LineGraphSeries<DataPoint> getSeriesFrequency() {
        return seriesFrequency;
    }
    public LineGraphSeries<DataPoint> getSeriesTemp() {
        return seriesTemp;
    }
    public ArrayList<DataPoint> getDataPointSeriesFrequency() {
        return dataPointSeriesFrequency;
    }
    public ArrayList<DataPoint> getDataPointSeriesTemp() {
        return dataPointSeriesTemp;
    }
    public double[] getFreqTemp() {
        return freqTemp;
    }

    public void setCurExcelFile(File current){
        curExcel = current;
    }
    public void saveExcelFile() throws IOException {
        // Create a new workbook
        Workbook workbook = WorkbookFactory.create(true);

        // Create a new sheet
        Sheet sheet = workbook.createSheet("Time_Temperature_Frequency");

        Row row1 = sheet.createRow(0);

        Cell cell1 = row1.createCell(0);
        cell1.setCellValue("Time");

        Cell cell2 = row1.createCell(1);
        cell2.setCellValue("Temperature");

        Cell cell3 = row1.createCell(2);
        cell3.setCellValue("Frequency");

        for (int i = 0; i < dataPointSeriesFrequency.size(); i++) {
            Row row = sheet.createRow(i + 1);
            cell1 = row.createCell(0);
            cell1.setCellValue(i + 1);

            cell2 = row.createCell(1);
            cell2.setCellValue(dataPointSeriesTemp.get(i).getY());

            cell3 = row.createCell(2);
            cell3.setCellValue(dataPointSeriesFrequency.get(i).getY());
        }
        curWorkbook = workbook;
        FileOutputStream outputStream = new FileOutputStream(curExcel);
        workbook.write(outputStream);

    }
    public void restartFreqCollection(){
        seriesFrequency = new LineGraphSeries<DataPoint>();
        dataPointSeriesFrequency = new ArrayList<>();
    }

    public void restartTempCollection(){
        seriesTemp = new LineGraphSeries<DataPoint>();
        dataPointSeriesTemp = new ArrayList<>();
    }
    /**
     * Graph Data Related Methods END
     */

    /**
     * Fragment transfer process
     */

    public void switchFragment(Bundle bundle) {
        // Currently, it's just for nav_database_detail.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.navigation_database_detail, bundle);
    }
    public void setRequestEnableBt() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        bluetoothRequestResult.launch(intent);  //
    }

    public boolean checkBluetooth() {
        return this.bluetoothSocket != null && bluetoothSocket.isConnected();
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
