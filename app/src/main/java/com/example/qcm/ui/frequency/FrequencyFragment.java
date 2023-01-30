package com.example.qcm.ui.frequency;

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

public class FrequencyFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private Viewport viewportFrequency;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FrequencyViewModel frequencyViewModel =
                new ViewModelProvider(this).get(FrequencyViewModel.class);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_frequency, container, false);
//        binding = FragmentDashboardBinding.inflate(inflater, container, false);
//        View root = binding.getRoot();

//        final TextView textView = binding.textDashboard;
//        frequencyViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
        viewportFrequency = graph.getViewport();
        viewportFrequency.setScrollable(true);
        viewportFrequency.setXAxisBoundsManual(true);

        return rootView;
    }

    public void setText(String text) {
        rdata = (TextView) findViewById(R.id.receive_data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}