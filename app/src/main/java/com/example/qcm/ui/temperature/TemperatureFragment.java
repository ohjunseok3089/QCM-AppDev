package com.example.qcm.ui.temperature;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;

public class TemperatureFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private Viewport viewportTemperature;
    private TextView rdata;
    private double[] freqTemp;

    private Thread workerThread = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_temperature, container, false);

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
        }

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph_temp);

        viewportTemperature = graph.getViewport();
        viewportTemperature.setScalable(true);
        viewportTemperature.setXAxisBoundsManual(true);

        LineGraphSeries<DataPoint> series = ((MainActivity)getActivity()).getSeriesTemp();
        ArrayList<DataPoint> dataPointSeriesFrequency = ((MainActivity)getActivity()).getDataPointSeriesTemp();

        graph.addSeries(series);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Temperature (K)");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
        graph.setTitle("Temperature (K) / Time (s)");
        viewportTemperature.setMinX(-100);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}