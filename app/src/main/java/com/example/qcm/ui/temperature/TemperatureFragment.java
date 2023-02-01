package com.example.qcm.ui.temperature;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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