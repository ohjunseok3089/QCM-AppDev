package com.example.qcm.ui.imaging;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;

import org.opencv.android.OpenCVLoader;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;

import java.util.Collections;
import java.util.List;

public class ImagingFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private ViewGroup rootView;
    private CameraBridgeViewBase mOpenCVCameraView;
    private static String LOGTAG_OPENCV = "OpenCV_Log";

    private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(getContext()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.v(LOGTAG_OPENCV, "OpenCV Loaded");
                    mOpenCVCameraView.enableView();
                } break;
                default: {
                    Log.e(LOGTAG_OPENCV, "OpenCV Loading Failed!");
                    super.onManagerConnected(status);
                } break;
            }

        }
    };
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ImagingViewModel imagingViewModel =
                new ViewModelProvider(this).get(ImagingViewModel.class);
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_imaging, container, false);

        mOpenCVCameraView = (CameraBridgeViewBase) rootView.findViewById(R.id.opencv_camera_view);
        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCVCameraView.setCameraPermissionGranted();
//        mOpenCVCameraView.setMaxFrameSize(2, 2);
        mOpenCVCameraView.setCvCameraViewListener(cvCameraViewListener);
        return rootView;
    }
    protected List<?extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCVCameraView);
    }
    private CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {

        }

        @Override
        public void onCameraViewStopped() {

        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            return null;
        }
    };

    public void onPause() {
        super.onPause();
        if (mOpenCVCameraView != null) {
            mOpenCVCameraView.disableView();
        }
    }
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(LOGTAG_OPENCV, "OpenCV not found, Initializing");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getContext(), mLoaderCallBack);
        } else {
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCVCameraView != null) {
            mOpenCVCameraView.disableView();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}