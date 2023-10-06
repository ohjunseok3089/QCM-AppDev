package com.example.qcm.ui.frequency;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qcm.MainActivity;
import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FrequencyFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private Viewport viewportFrequency;
    private ViewGroup rootView;
    private TextView rdata;
    private double[] freqTemp;

    private LineGraphSeries<DataPoint> series;
    private GraphView graph;
    private ArrayList<DataPoint> dataPointSeriesFrequency;


    private Thread workerThread = null;                 // To receive a String
    private String fileName;


    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FrequencyViewModel frequencyViewModel =
                new ViewModelProvider(this).get(FrequencyViewModel.class);
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_frequency, container, false);


        boolean isBluetoothConnected = ((MainActivity) getActivity()).checkBluetooth();

        // Check Bluetooth
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
        // Check expr is created
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

        graph = (GraphView) rootView.findViewById(R.id.graph);

        viewportFrequency = graph.getViewport();
        viewportFrequency.setScrollable(true);
        viewportFrequency.setXAxisBoundsManual(true);

        series = ((MainActivity)getActivity()).getSeriesFrequency();
        dataPointSeriesFrequency = ((MainActivity)getActivity()).getDataPointSeriesFrequency();

        graph.addSeries(series);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Frequency (Hz)");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
        graph.setTitle("Frequency (Hz) / Time (s)");
        graph.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                double x = viewportFrequency.getMinX(false) + (viewportFrequency.getMaxX(false) - viewportFrequency.getMinX(false)) * (motionEvent.getX() / graph.getWidth());
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
                    TextView textView = rootView.findViewById(R.id.touch_val_freq);
                    textView.setText((int) nearest.getX() + "s | " + nearest.getY() + "Hz");
                }

                return false;
            }
        });
        series.setDrawDataPoints(true);

        // Text receiver
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setOnDataFetchedListener(frequencyViewModel);
        }

        // rdata setter
        frequencyViewModel.getData().observe(getViewLifecycleOwner(), array -> {
            if (array != null && array.length == 2) {
                TextView rdata_freq = rootView.findViewById(R.id.rt_freqency_freq);
                TextView rdata_temp = rootView.findViewById(R.id.rt_temp_freq);
                rdata_freq.setText(array[0] + "Hz");
                rdata_temp.setText(array[1] + "K");
                viewportFrequency.setMaxX(((MainActivity)getActivity()).getDataSize());
            } else {
                Log.d("HomeFragment", "LiveData observed. Data format unexpected.");
            }
        });

        // Trigger fetching data from MainActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).fetchData();
        }


        Button saveButton = rootView.findViewById(R.id.freq_save_button);
        Button restartButton = rootView.findViewById(R.id.freq_restart_button);
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
                                viewportFrequency.setMinX(0);
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