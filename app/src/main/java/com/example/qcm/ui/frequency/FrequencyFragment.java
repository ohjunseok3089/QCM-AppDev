package com.example.qcm.ui.frequency;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
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
        viewportFrequency.setMinX(-100);
        return rootView;
    }

        @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}