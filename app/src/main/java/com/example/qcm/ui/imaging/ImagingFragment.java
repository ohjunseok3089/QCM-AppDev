package com.example.qcm.ui.imaging;

import static androidx.fragment.app.FragmentManager.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import java.util.Locale;
import java.util.Objects;

public class ImagingFragment extends Fragment {

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
    private Button cameraBtn, galleryBtn;
    private File capturedImage;
    private String imageFilePath;
    private Uri photoUri;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private File img_folder;

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera(File directory){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            try {
                capturedImage = createImageFile(directory);
                if (capturedImage != null) {
                    Uri photoURI = FileProvider.getUriForFile(requireContext(),
                            "com.example.qcm.fileprovider", // replace with your app's package name + ".fileprovider"
                            capturedImage);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private File createImageFile(File directory) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        return File.createTempFile(imageFileName, ".jpg", directory);
    }
    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(img_folder);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.example.qcm.file_paths",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // This method will help to retrieve the image
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // BitMap is data structure of image file which store the image in memory
            // Set the image in imageview for display

        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ImagingViewModel imagingViewModel =
                new ViewModelProvider(this).get(ImagingViewModel.class);
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_imaging, container, false);
        image_name = ((MainActivity) requireActivity()).getCurExcelName();
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
            requireActivity().onBackPressed();
            return rootView;
        }
        img_folder = new File (getContext().getExternalFilesDir("fl_images"), image_name);
        img_location = img_folder.getAbsolutePath();
        cameraBtn = rootView.findViewById(R.id.capture_image);
        galleryBtn = rootView.findViewById(R.id.view_fl_image);

        cameraBtn.setOnClickListener(v -> {
            dispatchTakePictureIntent();
        });

        return rootView;
}

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera(img_folder);
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            // Handle storage actions here
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera(img_folder);
                } else {
                    Toast.makeText(getContext(), "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_STORAGE_PERMISSION:
                // Handle storage permission result if needed
                break;
        }
    }

}