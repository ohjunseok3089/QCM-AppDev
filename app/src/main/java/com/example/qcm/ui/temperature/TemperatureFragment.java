package com.example.qcm.ui.temperature;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
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
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_temperature, container, false);

        TextView receiveDataTextView = getActivity().findViewById(R.id.receive_data);
        receiveDataTextView.setVisibility(View.VISIBLE);

        boolean isBluetoothConnected = ((MainActivity) getActivity()).checkBluetooth();
        if (!isBluetoothConnected) {
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

        viewportTemperature = graph.getViewport();
        viewportTemperature.setScalable(true);
        viewportTemperature.setXAxisBoundsManual(true);

        series = ((MainActivity)getActivity()).getSeriesTemp();
        dataPointSeriesFrequency = ((MainActivity)getActivity()).getDataPointSeriesTemp();

        graph.addSeries(series);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Temperature (K)");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
        graph.setTitle("Temperature (K) / Time (s)");
        series.setDrawDataPoints(true);
        viewportTemperature.setMinX(-100);

        // Display on and off button
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        Switch display_switch = rootView.findViewById(R.id.display_switch_temp);
// Get the custom thumb drawable
        display_switch.setOnClickListener(new View.OnClickListener() {
            TextView messageText = rootView.findViewById(R.id.display_text_temp);

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
        // Get the toggle button view
        ToggleButton toggleButton = rootView.findViewById(R.id.temp_toggle_button);

        // Set the default design
        toggleButton.setBackgroundResource(R.drawable.save_button);

        // Set the toggle button listener
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setBackgroundResource(R.drawable.restart_button);
                } else {
                    buttonView.setBackgroundResource(R.drawable.save_button);
                }

                if (toggleButton.isPressed()){
                    if (isChecked) {
                        buttonView.setBackgroundResource(R.drawable.restart_button);
                        // Change to the restart button design
                        // For saving method.
                        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                        builder.setMessage("Would you like to start saving data points?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which){
                                        try {
                                            ((MainActivity)getActivity()).saveExcelFile();
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing when the user clicks "No"
                                        buttonView.setBackgroundResource(R.drawable.save_button);

                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        // Change back to the save button design
                        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                        builder.setMessage("If you restart the experiment, you cannot revert the changes. Are you going to proceed?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Run your method here when the user clicks "Yes"
                                        ((MainActivity) requireActivity()).restartFreqCollection();
                                        graph.removeAllSeries();
                                        buttonView.setBackgroundResource(R.drawable.save_button);
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