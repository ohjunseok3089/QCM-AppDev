//package com.example.qcm;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.app.Application;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Context;
//import android.os.Handler;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.jjoe64.graphview.Viewport;
//import com.jjoe64.graphview.series.DataPoint;
//import com.jjoe64.graphview.series.LineGraphSeries;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Set;
//import java.util.UUID;
//
//public class BluetoothActivity extends Application {
//    private static final int REQUEST_ENABLE_BT = 10;    // Bluetooth Activation Status
//    private BluetoothAdapter bluetoothAdapter;          // Bluetooth Adaptor
//    private Set<BluetoothDevice> devices;               // Bluetooth Devices, SET
//    private BluetoothDevice bluetoothDevice;            // Bluetooth Device, Object
//    private BluetoothSocket bluetoothSocket = null;     // Bluetooth Socket
//    private OutputStream outputStream = null;           // Bluetooth Output Stream
//    private InputStream inputStream = null;             // Bluetooth Input Stream * Important
//    private Thread workerThread = null;                 // To receive a String
//    private byte[] readBuffer;                          // Buffered Reader
//    private int readBufferPosition;                     // Saves location for buffered reader
//    private boolean onBT = false;                       // To check is for bluetooth
//    String uid = "98:D3:41:F6:8D:DE";                   // HC-05 uid
//    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//
//    @SuppressLint("MissingPermission") // permission must be checked before the call of the function!
////    @Override
//    public void setupBluetooth() {
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        System.out.println(bluetoothAdapter);
//
//        bluetoothDevice = bluetoothAdapter.getRemoteDevice(uid);
//        System.out.println(bluetoothDevice.getName());
////        Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + "연결 완료!", Toast.LENGTH_SHORT).show();
//
//        int cntTry = 0;
//        do {
//            try {
//                bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);
//                System.out.println(bluetoothSocket);
//                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
//                bluetoothSocket.connect();
//                Toast.makeText(getApplicationContext(), bluetoothDevice.getName() + " 소켓 연결 완료!", Toast.LENGTH_SHORT).show();
//                System.out.println(bluetoothSocket.isConnected());
//                outputStream = bluetoothSocket.getOutputStream();
//                inputStream = bluetoothSocket.getInputStream();
//            } catch (IOException e){
//                e.printStackTrace();
//            }
//            cntTry++;
//        } while(!bluetoothSocket.isConnected() && cntTry < 3);
//    }
//    public void receiveData(TextView rdata, LineGraphSeries<DataPoint> series, int pointsPlotted, Viewport viewport) {
//        final Handler handler = new Handler();
//
//        readBufferPosition = 0;
//        readBuffer = new byte[1024];
//
//        workerThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (!Thread.currentThread().isInterrupted()) {
////                for (int j = 0; j <= 10; j++) {
//                    try {
//                        int byteAvaliable = inputStream.available();
//                        if (byteAvaliable > 0) {
//                            byte[] bytes = new byte[byteAvaliable];
//                            inputStream.read(bytes);
//                            for (int i = 0; i < byteAvaliable; i++) {
//                                byte curByte = bytes[i];
//                                if (curByte == '\n') {
//                                    byte[] encodedBytes = new byte[readBufferPosition];
//                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                    final String text = new String(encodedBytes, "ASCII");
//                                    readBufferPosition = 0;
//                                    handler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            rdata.setText(text);
//                                            String[] array = text.split(",");
//                                            series.appendData(new DataPoint(pointsPlotted, Double.parseDouble(array[0])), true, pointsPlotted);
//                                            pointsPlotted++;
//                                            viewport.setMaxX(pointsPlotted);
//                                            viewport.setMinX(pointsPlotted - 50);
//                                        }
//                                    });
//                                } else {
//                                    readBuffer[readBufferPosition++] = curByte;
//                                }
//
//                            }
//                        }
//                    } catch (IOException e){
//                        e.printStackTrace();
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        workerThread.start();
//    }
//}
