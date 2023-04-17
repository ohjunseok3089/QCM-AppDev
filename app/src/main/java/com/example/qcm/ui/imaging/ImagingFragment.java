package com.example.qcm.ui.imaging;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qcm.MainActivity;
import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;

import org.opencv.android.OpenCVLoader;
import org.opencv.imgcodecs.Imgcodecs;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ImagingFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    private FragmentDashboardBinding binding;
    private ViewGroup rootView;
    private CameraBridgeViewBase mOpenCVCameraView;
    private static String LOGTAG_OPENCV = "OpenCV_Log";
    private Mat mRgba;
    private Mat mGray;
    private int mCameraId = 0;
    private ImageView take_picture_button;
    private int take_image = 0;
    private String image_name;


    private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(getContext()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(LOGTAG_OPENCV, "OpenCV Loaded");
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
        mOpenCVCameraView.setMaxFrameSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCVCameraView.setCameraPermissionGranted();
        mOpenCVCameraView.setCvCameraViewListener(this);
        mOpenCVCameraView.enableFpsMeter();
        image_name = ((MainActivity) getActivity()).getCurExcelName();
        take_picture_button = rootView.findViewById(R.id.takePicture);
        take_picture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (take_image == 0) {
                    take_image = 1;
                } else {
                    take_image = 0;
                }
            }
        });

        return rootView;
    }
    protected List<?extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCVCameraView);
    }


    public void onPause() {
        super.onPause();
        if (mOpenCVCameraView != null) {
            mOpenCVCameraView.disableView();
        }
    }
    public void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(LOGTAG_OPENCV, "OpenCV found, Initializing");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        } else {
            Log.d(LOGTAG_OPENCV, "OpenCV not found, Initializing");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getContext(), mLoaderCallBack);
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

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        take_image = take_picture_function_rgb(take_image, mRgba);

        return mRgba;
    }

    private int take_picture_function_rgb(int take_image, Mat mRgba) {
        if (take_image == 1) {
            Mat save_mat = new Mat();
            Core.flip(mRgba.t(), save_mat, 1);
            Imgproc.cvtColor(save_mat, save_mat, Imgproc.COLOR_RGBA2BGRA);

            File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/testing");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentTime = sdf.format(new Date());
            String fileName = Environment.getExternalStorageDirectory().getPath() + "/testing" + image_name + "_" + currentTime + ".jpg";
            Imgcodecs.imwrite(fileName, save_mat);
            take_image = 0;
        }
        return take_image;
    }
}