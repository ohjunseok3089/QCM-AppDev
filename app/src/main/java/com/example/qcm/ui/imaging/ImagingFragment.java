package com.example.qcm.ui.imaging;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.qcm.MainActivity;
import com.example.qcm.R;
import com.example.qcm.databinding.FragmentDashboardBinding;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.KeyPoint;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private String img_location;
    private TextView points_val;

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
        points_val = rootView.findViewById(R.id.green_header2);
        mOpenCVCameraView = (CameraBridgeViewBase) rootView.findViewById(R.id.opencv_camera_view);
        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCVCameraView.setCameraPermissionGranted();
        mOpenCVCameraView.setCvCameraViewListener(this);
        take_picture_button = rootView.findViewById(R.id.takePicture);

        image_name = ((MainActivity) getActivity()).getCurExcelName();
        if (image_name == null) {
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



        File img_folder = new File (getContext().getExternalFilesDir("fl_images"), image_name);
        img_location = img_folder.getAbsolutePath();
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
            File folder = new File(img_location);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentTime = sdf.format(new Date());
            String fileName = img_location + "/" + image_name + "_" + currentTime + ".jpg";
            Imgcodecs.imwrite(fileName, save_mat);
            take_image = 0;
//            Toast.makeText(rootView.getContext(),"Saved into " + img_location + "!", Toast.LENGTH_LONG).show();
        }
        return take_image;
    }
}