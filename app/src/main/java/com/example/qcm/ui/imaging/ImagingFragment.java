package com.example.qcm.ui.imaging;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
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

import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;
import org.opencv.android.Utils;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private ImageView fl_image_view_2;
    private TextView process_textview;


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
        fl_image_view_2 = rootView.findViewById(R.id.fl_image_view_post_process);
        process_textview = rootView.findViewById(R.id.process_result);

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
        processAndDisplayImage(newFile.getAbsolutePath());

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

    private void testImageProcessing() {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);

        Mat imageMat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC3);
        Utils.bitmapToMat(bmp, imageMat);

        String tempFilePath = saveMatToTempFile(imageMat);

        if (tempFilePath != null) {
            processAndDisplayImage(tempFilePath);
        }
    }

    private String saveMatToTempFile(Mat mat) {
        if (getActivity() == null) {
            return null;
        }

        File tempFile = new File(getActivity().getCacheDir(), "temp_image.jpg");
        boolean result = Imgcodecs.imwrite(tempFile.getAbsolutePath(), mat);
        if (result) {
            return tempFile.getAbsolutePath();
        } else {
            return null;
        }
    }


//    private void processAndDisplayImage(String imagePath) {
//        Mat image = Imgcodecs.imread(imagePath);
////        Core.rotate(image, image, Core.ROTATE_180);
//// Convert drawable resource to bitmap
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
//
//        // Convert bitmap to Mat
//        Mat image = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC3);
//        Utils.bitmapToMat(bmp, image);
//
//        int rows = image.rows();
//        int cols = image.cols();
//
//        int center_x = 1720;
//        int center_y = 1280;
//        int radius = 1000;
//
//        Mat mask = new Mat(rows, cols, CvType.CV_8U, new Scalar(0));
//        Imgproc.circle(mask, new Point(center_x, center_y), radius, new Scalar(255), -1);
//
//        List<Mat> channels = new ArrayList<>();
//        Core.split(image, channels);
//        for (Mat channel : channels) {
//            Core.bitwise_and(channel, mask, channel);
//        }
//
//        Mat greenChannel = channels.get(1);
//
//        int threshold = 110;
//        Mat binaryMask = new Mat();
//        Core.inRange(greenChannel, new Scalar(1), new Scalar(threshold), binaryMask);
//
//        double totalIntensity = Core.sumElems(greenChannel).val[0];
//        int numPixels = Core.countNonZero(binaryMask);
//        double averageIntensity = totalIntensity / numPixels;
//
//        Mat maskedOriginalImageRGB = new Mat();
//        Core.merge(channels, maskedOriginalImageRGB);
//
//        // Save the processed image
//        String processedImagePath = imagePath.replace(".jpg", "_processed.jpg");
//        Imgcodecs.imwrite(processedImagePath, maskedOriginalImageRGB);
//
//        // Convert the saved image to a Bitmap
//        bmp = BitmapFactory.decodeFile(processedImagePath);
//        fl_image_view.setImageBitmap(bmp);
//
//        // Display the statistics (you can adjust this as needed)
//        Toast.makeText(requireContext(),
//                "Total Intensity: " + totalIntensity +
//                        ", Num Pixels: " + numPixels +
//                        ", Avg Intensity: " + averageIntensity,
//                Toast.LENGTH_LONG).show();
//    }

    @SuppressLint("SetTextI18n")
    private void processAndDisplayImage(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);

        int rows = image.rows();
        int cols = image.cols();

        int center_x = 1720;
        int center_y = 1280;
        int radius = 1000; // Fixed
        int threshold = 110;

        Mat mask = new Mat(rows, cols, CvType.CV_8U, new Scalar(0));
        Imgproc.circle(mask, new Point(center_x, center_y), radius, new Scalar(255), -1);

        Mat maskedImage = new Mat();
        Core.bitwise_and(image, image, maskedImage, mask);

        List<Mat> channels = new ArrayList<>();
        Core.split(maskedImage, channels);
        Mat greenChannel = channels.get(1);


        Mat binaryMask = new Mat();
        Core.inRange(greenChannel, new Scalar(1), new Scalar(threshold), binaryMask);

        Mat croppedImage = new Mat();
        maskedImage.copyTo(croppedImage, binaryMask);

        double totalIntensity = Core.sumElems(greenChannel).val[0];
        int numPixels = Core.countNonZero(binaryMask);
        double averageIntensity = totalIntensity / numPixels;

        // Save the processed image
        String processedImagePath = imagePath.replace(".jpg", "_processed.jpg");
        Imgcodecs.imwrite(processedImagePath, croppedImage);

        // Convert the saved image to a Bitmap
        Bitmap bmp = BitmapFactory.decodeFile(processedImagePath);
        fl_image_view_2.setImageBitmap(bmp);

        process_textview.setText("Total Intensity: " + totalIntensity +
                ", Num Pixels: " + numPixels +
                ", Avg Intensity: " + averageIntensity);

        // Display the statistics (you can adjust this as needed)
        Toast.makeText(requireContext(),
                "Total Intensity: " + totalIntensity +
                        ", Num Pixels: " + numPixels +
                        ", Avg Intensity: " + averageIntensity,
                Toast.LENGTH_LONG).show();
    }


//    private void processAndDisplayImage(String a) {
//        // Load the image from res/drawable
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
//        Mat image = new Mat();
//        Utils.bitmapToMat(bmp, image);
//
//        // Rotate the image by 180 degrees
//        Core.rotate(image, image, Core.ROTATE_180);
//
//        int rows = image.rows();
//        int cols = image.cols();
//
//        // Create a circular mask
//        int center_x = 1720;
//        int center_y = 1280;
//        int radius = 1000;
//        Mat mask = new Mat(rows, cols, CvType.CV_8U, new Scalar(0));
//        Imgproc.circle(mask, new Point(center_x, center_y), radius, new Scalar(255), -1);
//
//        // Apply the mask to the image
//        Mat maskedImage = new Mat();
//        Core.bitwise_and(image, image, maskedImage, mask);
//
//        // Extract the green channel
//        List<Mat> channels = new ArrayList<>();
//        Core.split(maskedImage, channels);
//        Mat greenChannel = channels.get(1);
//
//        // Threshold the green channel
//        int threshold = 110;
//        Mat binaryMask = new Mat();
//        Core.inRange(greenChannel, new Scalar(1), new Scalar(threshold), binaryMask);
//
//        // Calculate statistics
//        double totalIntensity = Core.sumElems(greenChannel).val[0];
//        int numPixels = Core.countNonZero(binaryMask);
//        double averageIntensity = totalIntensity / numPixels;
//
//        // Merge the channels to get the RGB result
//        Mat maskedOriginalImageRGB = new Mat();
//        Core.merge(channels, maskedOriginalImageRGB);
//
//        // Convert the processed Mat back to Bitmap for displaying
//        Bitmap processedBitmap = Bitmap.createBitmap(maskedOriginalImageRGB.cols(), maskedOriginalImageRGB.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(maskedOriginalImageRGB, processedBitmap);
//        fl_image_view.setImageBitmap(processedBitmap);
//
//        // Display the statistics
//        Toast.makeText(requireContext(),
//                "Total Intensity: " + totalIntensity +
//                        ", Num Pixels: " + numPixels +
//                        ", Avg Intensity: " + averageIntensity,
//                Toast.LENGTH_LONG).show();
//    }

}
