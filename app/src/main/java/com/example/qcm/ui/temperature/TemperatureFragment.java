package com.example.qcm.ui.temperature;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qcm.MainActivity;
import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TemperatureFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private Viewport viewportTemperature;
    private TextView rdata;
    private double[] freqTemp;
    ViewGroup rootView;

    private LineGraphSeries<DataPoint> series;
    private GraphView graph;
    private ArrayList<DataPoint> dataPointSeriesFrequency;
    private String fileName;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TemperatureViewModel temperatureViewModel =
                new ViewModelProvider(this).get(TemperatureViewModel.class);
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_temperature, container, false);

//        TextView receiveDataTextView = getActivity().findViewById(R.id.receive_data);
//        receiveDataTextView.setVisibility(View.VISIBLE);

        boolean isBluetoothConnected = ((MainActivity) getActivity()).checkBluetooth();
        if (isBluetoothConnected) {
            // If it's not connected, then alert that you have to connect it to bluetooth.
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Bluetooth is not connected.");
            builder.setMessage("Please go back to home screen and make sure the app is connected to QCM");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();

            // Bluetooth is not connected, go back to previous fragment/screen
            getActivity().onBackPressed();
            return rootView;
        }
        fileName = ((MainActivity) getActivity()).getCurExcelName();
        if (fileName == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Experiment has not been created.");
            builder.setMessage("Please go back to home screen and make sure the experiment is created");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();

            // Bluetooth is not connected, go back to previous fragment/screen
            getActivity().onBackPressed();
            return rootView;
        }

        graph = (GraphView) rootView.findViewById(R.id.graph_temp);

        // Trigger fetching data from MainActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).fetchData();
        }
        viewportTemperature = graph.getViewport();
        viewportTemperature.setScalable(true);
        viewportTemperature.setXAxisBoundsManual(true);

        series = ((MainActivity)getActivity()).getSeriesTemp();
        dataPointSeriesFrequency = ((MainActivity)getActivity()).getDataPointSeriesTemp();

        graph.addSeries(series);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Temperature (K)");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
        graph.setTitle("Temperature (K) / Time (s)");
        graph.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                double x = viewportTemperature.getMinX(false) + (viewportTemperature.getMaxX(false) - viewportTemperature.getMinX(false)) * (motionEvent.getX() / graph.getWidth());
                Iterator<DataPoint> iterator = series.getValues((double) x - 1, (double) x + 1);
                List<DataPoint> dataList = new ArrayList<>();

                while(iterator.hasNext()){
                    dataList.add(iterator.next());
                }

                DataPoint[] points = dataList.toArray(new DataPoint[0]);
                DataPoint nearest = null;
                double nearestDistance = Double.MAX_VALUE;

                for (DataPoint point : points) {
                    double distance = Math.abs(point.getX() - x);
                    if (distance < nearestDistance) {
                        nearest = point;
                        nearestDistance = distance;
                    }
                }

                if (nearest != null) {
                    TextView textView = rootView.findViewById(R.id.touch_val_temp);
                    textView.setText((int) nearest.getX() + "s | " + nearest.getY() + "K");
                }

                return false;
            }
        });

        series.setDrawDataPoints(true);

        // Text receiver
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setOnDataFetchedListener(temperatureViewModel);
        }

        // rdata setter
        temperatureViewModel.getData().observe(getViewLifecycleOwner(), array -> {
            if (array != null && array.length == 2) {
                TextView rdata_freq = rootView.findViewById(R.id.rt_freqency_temp_data);
                TextView rdata_temp = rootView.findViewById(R.id.rt_temp_temp_data);
                rdata_freq.setText(array[0] + "Hz");
                rdata_temp.setText(array[1] + "K");
                viewportTemperature.setMaxX(((MainActivity)getActivity()).getDataSize());
            } else {
                Log.d("HomeFragment", "LiveData observed. Data format unexpected.");
            }
        });
        Button saveButton = rootView.findViewById(R.id.temp_save_button);
        Button restartButton = rootView.findViewById(R.id.temp_restart_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Saving Method
                AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                builder.setMessage("Would you like to save data points?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    ((MainActivity)getActivity()).saveExcelFile();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Do nothing
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Restart Method
                AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                builder.setMessage("If you restart the experiment, you cannot revert the changes. Are you going to proceed?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity)getActivity()).restartCollection();
                                graph.removeAllSeries();
                                series = ((MainActivity)getActivity()).getSeriesFrequency();
                                dataPointSeriesFrequency = ((MainActivity)getActivity()).getDataPointSeriesFrequency();
                                graph.addSeries(series);
                                series.setDrawDataPoints(true);
                                viewportTemperature.setMinX(0);
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Do nothing
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}