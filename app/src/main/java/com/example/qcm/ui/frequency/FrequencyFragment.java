package com.example.qcm.ui.frequency;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
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
import java.util.Arrays;
import java.util.Objects;

public class FrequencyFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private Viewport viewportFrequency;
    ViewGroup rootView;
    private TextView rdata;
    private double[] freqTemp;

    private LineGraphSeries<DataPoint> series;
    private ArrayList<DataPoint> dataPointSeriesFrequency;


    private Thread workerThread = null;                 // To receive a String

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FrequencyViewModel frequencyViewModel =
                new ViewModelProvider(this).get(FrequencyViewModel.class);
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_frequency, container, false);

        TextView receiveDataTextView = getActivity().findViewById(R.id.receive_data);
        receiveDataTextView.setVisibility(View.VISIBLE);

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
        }

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
//        rdata = (TextView) rootView.findViewById(R.id.receive_data_frequency);
        viewportFrequency = graph.getViewport();
        viewportFrequency.setScrollable(true);
        viewportFrequency.setXAxisBoundsManual(true);

        series = ((MainActivity)getActivity()).getSeriesFrequency();
        dataPointSeriesFrequency = ((MainActivity)getActivity()).getDataPointSeriesFrequency();
        int size = dataPointSeriesFrequency.size();
        System.out.println(size);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Frequency (Hz)");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
        graph.setTitle("Frequency (Hz) / Time (s)");
        series.setDrawDataPoints(true);
        viewportFrequency.setMinX(-100);

        // Display on and off button
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch display_switch = rootView.findViewById(R.id.display_switch_freq);
// Get the custom thumb drawable
        display_switch.setOnClickListener(new View.OnClickListener() {
            TextView messageText = rootView.findViewById(R.id.display_text);

            @Override
            public void onClick(View view) {
                if (graph.getVisibility() == View.VISIBLE) {
                    graph.setVisibility(View.INVISIBLE);
                    display_switch.setChecked(false);
                    messageText.setText("Please click the 'Display On' button.");
                } else {
                    graph.setVisibility(View.VISIBLE);
                    display_switch.setChecked(true);
                    messageText.setText("");
                }
            }
        });

        // Save button
        // TODO



        // Get the toggle button view
        ToggleButton toggleButton = rootView.findViewById(R.id.freq_toggle_button);

        // Set the default design
        toggleButton.setBackgroundResource(R.drawable.save_button);

        // Set the toggle button listener
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Change to the restart button design
                    buttonView.setBackgroundResource(R.drawable.restart_button);
                    // For saving method.

                } else {
                    // Change back to the save button design
                    buttonView.setBackgroundResource(R.drawable.save_button);

                    AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                    builder.setMessage("If you restart the experiment, you cannot revert the changes. Are you going to proceed?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Run your method here when the user clicks "Yes"
                                    ((MainActivity) requireActivity()).restartFreqCollection();
                                    GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
//        rdata = (TextView) rootView.findViewById(R.id.receive_data_frequency);
                                    viewportFrequency = graph.getViewport();
                                    viewportFrequency.setScrollable(true);
                                    viewportFrequency.setXAxisBoundsManual(true);

                                    series = ((MainActivity)getActivity()).getSeriesFrequency();
                                    dataPointSeriesFrequency = ((MainActivity)getActivity()).getDataPointSeriesFrequency();
                                    int size = dataPointSeriesFrequency.size();
                                    System.out.println(size);
                                    graph.addSeries(series);
                                    graph.getGridLabelRenderer().setVerticalAxisTitle("Frequency (Hz)");
                                    graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
                                    graph.setTitle("Frequency (Hz) / Time (s)");
                                    series.setDrawDataPoints(true);
                                    viewportFrequency.setMinX(-100);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing when the user clicks "No"
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
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