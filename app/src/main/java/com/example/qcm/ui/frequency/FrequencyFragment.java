package com.example.qcm.ui.frequency;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

public class FrequencyFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private Viewport viewportFrequency;
    private TextView rdata;
    private double[] freqTemp;

    private Thread workerThread = null;                 // To receive a String

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FrequencyViewModel frequencyViewModel =
                new ViewModelProvider(this).get(FrequencyViewModel.class);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_frequency, container, false);

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

        LineGraphSeries<DataPoint> series = ((MainActivity)getActivity()).getSeriesFrequency();
        ArrayList<DataPoint> dataPointSeriesFrequency = ((MainActivity)getActivity()).getDataPointSeriesFrequency();
        int size = dataPointSeriesFrequency.size();
        System.out.println(size);
        graph.addSeries(series);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Frequency (Hz)");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
        graph.setTitle("Frequency (Hz) / Time (s)");
        series.setDrawDataPoints(true);
        viewportFrequency.setMinX(-100);

        // Display on and off button
        Button display_button = rootView.findViewById(R.id.display_button);
        display_button.setOnClickListener(new View.OnClickListener() {
            TextView messageText = rootView.findViewById(R.id.display_text);
            @Override
            public void onClick(View view) {
                if (graph.getVisibility() == View.VISIBLE) {
                    graph.setVisibility(View.INVISIBLE);
                    display_button.setText("display on");
                    messageText.setText("Please click the 'Display On' button.");
                } else {
                    graph.setVisibility(View.VISIBLE);
                    display_button.setText("display off");
                    messageText.setText("");
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