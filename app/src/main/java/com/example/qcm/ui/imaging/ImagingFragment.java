package com.example.qcm.ui.imaging;

import static android.app.Activity.RESULT_OK;
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
import android.media.ExifInterface;
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
import android.widget.EditText;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ImagingFragment extends Fragment {

    private ViewGroup rootView;
    private String image_name;
    private String img_location;
    private TextView points_val;
    private Button cameraBtn, galleryBtn;
    private String imageFilePath;
    private Uri photoUri;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private File img_folder;
    private File saved_image;
    private ImageView fl_image_view;

    @SuppressLint("QueryPermissionsNeeded")

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

        img_folder = new File(requireContext().getExternalFilesDir("fl_images"), image_name);
        img_location = img_folder.getAbsolutePath();
        cameraBtn = rootView.findViewById(R.id.capture_image);
        galleryBtn = rootView.findViewById(R.id.view_image_button);
        fl_image_view = rootView.findViewById(R.id.fl_image_view);

        cameraBtn.setOnClickListener(v -> {
            requestCameraPermission();
        });

        return rootView;
    }

    private void openCamera() {
        Log.d("openCamera", "Method called");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.qcm.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ActivityNotFoundException e) {
            // No activity to handle the intent (this should give us more info if the issue is here)
            Log.e("openCamera", "No activity found to handle camera intent", e);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            promptForFileNameAndRename();
            Bitmap myBitmap = BitmapFactory.decodeFile(imageFilePath);
            fl_image_view.setImageBitmap(myBitmap);
        }
    }

    private void promptForFileNameAndRename() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Image Name");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        final EditText input = new EditText(requireContext());
        input.setText(timeStamp);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newFilename = input.getText().toString();
                renameCapturedImage(newFilename);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void renameCapturedImage(String newFilename) {
        File oldFile = new File(imageFilePath);
        File newFile = new File(oldFile.getParent(), newFilename + ".jpg");
        if (oldFile.renameTo(newFile)) {
            imageFilePath = newFile.getAbsolutePath();
            Toast.makeText(requireContext(), "Image saved as " + newFilename, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to rename image", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Get the directory where the file will be saved
        File storageDir = new File(requireContext().getExternalFilesDir("fl_images"), image_name);

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imageFilePath = image.getAbsolutePath();
        return image;
    }
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_LONG).show();
            }
        }
    }
}
