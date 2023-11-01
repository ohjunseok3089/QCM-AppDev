package com.example.qcm.ui.imaging;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import java.util.Objects;
import android.util.DisplayMetrics;


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
    private Button makeChangeButton;


    @SuppressLint({"QueryPermissionsNeeded", "ClickableViewAccessibility"})

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
        if (savedInstanceState != null) {
            imageFilePath = savedInstanceState.getString("imageFilePath");
        }
//        processAndDisplayImage(imageFilePath);

        // Retrieve the images from MainActivity and set them to ImageViews


        img_folder = new File(requireContext().getExternalFilesDir("fl_images"), image_name);
        img_location = img_folder.getAbsolutePath();
        cameraBtn = rootView.findViewById(R.id.capture_image);
        galleryBtn = rootView.findViewById(R.id.view_image_button);
        fl_image_view = rootView.findViewById(R.id.fl_image_view);
        fl_image_view_2 = rootView.findViewById(R.id.fl_image_view_post_process);
        process_textview = rootView.findViewById(R.id.process_result);
        makeChangeButton = rootView.findViewById(R.id.make_change_button);

        if (((MainActivity) requireActivity()).getImageFilePath() != null) {
            int screenWidthDp = getScreenWidthDp(getResources());
            int desiredWidthDp = 300;

            // Check if the orientation is portrait
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                desiredWidthDp = screenWidthDp / 2; // 50% of the screen width
            }

            int sizeInPx = dpToPx(desiredWidthDp, getResources());
            imageFilePath = ((MainActivity) requireActivity()).getImageFilePath();

            Bitmap resizedBitmap = resizeBitmap(((MainActivity) requireActivity()).getOriginalImage(), sizeInPx, sizeInPx);
            fl_image_view.setImageBitmap(resizedBitmap);
        }
        if (((MainActivity) requireActivity()).getProcessedImage() != null) {
            int screenWidthDp = getScreenWidthDp(getResources());
            int desiredWidthDp = 300;  // Default width

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                desiredWidthDp = screenWidthDp / 2; // 50% of the screen width
            }

            int sizeInPx = dpToPx(desiredWidthDp, getResources());

            Bitmap resizedBitmap = resizeBitmap(((MainActivity) requireActivity()).getProcessedImage(), sizeInPx, sizeInPx);
            fl_image_view_2.setImageBitmap(resizedBitmap);
            process_textview.setText(((MainActivity) requireActivity()).getProcessedAnalysis());

        }

        cameraBtn.setOnClickListener(v -> {
            requestCameraPermission();
            ((MainActivity) requireActivity()).setImageFilePath(imageFilePath);
        });

        makeChangeButton.setOnClickListener(v -> {
            processAndDisplayImage(imageFilePath);
        });

        fl_image_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    float touchX = event.getX();
                    float touchY = event.getY();

                    // Get the original image dimensions
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imageFilePath, options);
                    int originalImageWidth = options.outWidth;
                    int originalImageHeight = options.outHeight;

                    // Compute scale factors
                    float scaleX = (float) originalImageWidth / fl_image_view.getWidth();
                    float scaleY = (float) originalImageHeight / fl_image_view.getHeight();

                    // Convert touch coordinates to original image coordinates
                    int realX = (int) (touchX * scaleX);
                    int realY = (int) (touchY * scaleY);

                    // Update the text views
                    EditText centerXInput = rootView.findViewById(R.id.center_x_input);
                    EditText centerYInput = rootView.findViewById(R.id.center_y_input);
                    centerXInput.setText(String.valueOf(realX));
                    centerYInput.setText(String.valueOf(realY));

                    return true;
                }
                return false;
            }
        });
        return rootView;
    }

    private int getScreenWidthDp(Resources resources) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) screenWidthDp;
    }

    private float[] getRealCoordinates(ImageView imageView, float touchX, float touchY) {
        float[] coords = new float[2];

        if (imageView.getDrawable() != null) {
            Matrix inverse = new Matrix();
            imageView.getImageMatrix().invert(inverse);
            float[] touchPoint = new float[]{touchX, touchY};
            inverse.mapPoints(coords, touchPoint);
        }

        return coords;
    }
    private void openCamera() {
        Log.d("openCamera", "Method called");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
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

    int dpToPx(int dp, Resources resources) {
        float scale = resources.getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
    Bitmap resizeBitmap(Bitmap original, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            promptForFileNameAndRename();

            Bitmap myBitmap = BitmapFactory.decodeFile(imageFilePath);
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            Bitmap rotatedBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
            ((MainActivity) requireActivity()).setOriginalImage(rotatedBitmap);
            int screenWidthDp = getScreenWidthDp(getResources());
            int desiredWidthDp = 300;  // Default width

// Check if the orientation is portrait
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                desiredWidthDp = screenWidthDp / 2; // 50% of the screen width
            }

            int sizeInPx = dpToPx(desiredWidthDp, getResources());

            Bitmap resizedRotatedBitmap = resizeBitmap(rotatedBitmap, sizeInPx, sizeInPx);
            fl_image_view.setImageBitmap(resizedRotatedBitmap);


//            fl_image_view.setImageBitmap(myBitmap);
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

    @SuppressLint("SetTextI18n")
    private void processAndDisplayImage(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);

        int rows = image.rows();
        int cols = image.cols();

        int radius = 1000; // Fixed

        String center_x_str = ((EditText) rootView.findViewById(R.id.center_x_input)).getText().toString();
        String center_y_str = ((EditText) rootView.findViewById(R.id.center_y_input)).getText().toString();
        String threshold_str = ((EditText) rootView.findViewById(R.id.threshold_input)).getText().toString();

        int center_x = center_x_str.isEmpty() ? 1720 : Integer.parseInt(center_x_str);
        int center_y = center_y_str.isEmpty() ? 1280 : Integer.parseInt(center_y_str);
        int threshold = threshold_str.isEmpty() ? 110 : Integer.parseInt(threshold_str);

        EditText centerXInput = rootView.findViewById(R.id.center_x_input);
        EditText centerYInput = rootView.findViewById(R.id.center_y_input);
        EditText thresholdInput = rootView.findViewById(R.id.threshold_input);

        centerXInput.setText(String.valueOf(center_x));
        centerYInput.setText(String.valueOf(center_y));
        thresholdInput.setText(String.valueOf(threshold));

        if (center_x == 0) center_x = 1720;
        if (center_y == 0) center_y = 1280;
        if (threshold == 0) threshold = 110;

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

        int screenWidthDp = getScreenWidthDp(getResources());
        int desiredWidthDp = 300;  // Default width

// Check if the orientation is portrait
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            desiredWidthDp = screenWidthDp / 2; // 50% of the screen width
        }

        int sizeInPx = dpToPx(desiredWidthDp, getResources());

        Bitmap resizedRotatedBitmap = resizeBitmap(bmp, sizeInPx, sizeInPx);
        fl_image_view_2.setImageBitmap(resizedRotatedBitmap);

        ((MainActivity) requireActivity()).setProcessedImage(bmp);
        ((MainActivity) requireActivity()).setProcessedAnalysis("Total Intensity: " + formatNumber(totalIntensity) + "\n" +
                "Num Pixels: " + formatNumber(numPixels) + "\n" +
                "Avg Intensity: " + formatNumber(averageIntensity));

        process_textview.setText("Total Intensity: " + formatNumber(totalIntensity) + "\n" +
                "Num Pixels: " + formatNumber(numPixels) + "\n" +
                "Avg Intensity: " + formatNumber(averageIntensity));

        // Display the statistics (you can adjust this as needed)
    }

    private String formatNumber(double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            return String.valueOf(value);
        }
        String formatted = String.format("%.2f", value);
        if (formatted.contains("E") || formatted.contains("e")) {
            int indexE = formatted.indexOf('E') == -1 ? formatted.indexOf('e') : formatted.indexOf('E');
            String exponent = formatted.substring(indexE + 1);
            return String.format("%.2f x 10^%s", value / Math.pow(10, Integer.parseInt(exponent)), exponent);
        }
        return formatted;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("imageFilePath", imageFilePath);
    }

}
