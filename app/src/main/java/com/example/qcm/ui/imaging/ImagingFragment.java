package com.example.qcm.ui.imaging;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;

public class ImagingFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private SurfaceView

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ImagingViewModel imagingViewModel =
                new ViewModelProvider(this).get(ImagingViewModel.class);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_imaging, container, false);

        cameraView = rootView.findViewById(R.id.cameraView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}