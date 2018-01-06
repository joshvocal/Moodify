package me.joshvocal.moodify.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.joshvocal.moodify.utils.BitmapUtils;
import me.joshvocal.moodify.utils.ImageHelper;
import me.joshvocal.moodify.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    private static final String FILE_PROVIDER_AUTHORITY =
            "me.joshvocal.moodify.android.fileprovider";

    // The image selected to detect.
    private Bitmap mResultsBitmap;
    private ImageView mSelfieImageView;
    private TextView mEmotionTextView;

    // Floating Action Buttons
    private FloatingActionButton mCameraIconButton;
    private FloatingActionButton mFinishedIconButton;
    private FloatingActionButton mSaveImageButton;

    private EmotionServiceClient mEmotionServiceClient;

    private String mCurrentPhotoPath;
    private String mCurrentEmotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideActionBarElevation();
        setEmotionServiceClient();
        bindViews();
    }

    private void setEmotionServiceClient() {
        if (mEmotionServiceClient == null) {
            mEmotionServiceClient =
                    new EmotionServiceRestClient(getString(R.string.emotion_api_key));
        }
    }

    private void hideActionBarElevation() {
        getSupportActionBar().setElevation(0);
    }

    private void bindViews() {
        mSelfieImageView = (ImageView) findViewById(R.id.selfie_image_view);
        mEmotionTextView = (TextView) findViewById(R.id.emotion_text_view);
        mCameraIconButton = (FloatingActionButton) findViewById(R.id.camera_icon_button);
        mCameraIconButton.setOnClickListener(this);
        mFinishedIconButton = (FloatingActionButton) findViewById(R.id.finished_icon_button);
        mFinishedIconButton.setOnClickListener(this);
        mSaveImageButton = (FloatingActionButton) findViewById(R.id.save_icon_button);
        mSaveImageButton.setOnClickListener(this);
    }

    private void moodifyMe() {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            launchCamera();
        }

    }

    private void launchCamera() {
        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mCurrentPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void processAndSetImage() {
        mSaveImageButton.setVisibility(View.VISIBLE);
        // Resample the saved image to fit the ImageView
        mResultsBitmap = BitmapUtils.resamplePic(this, mCurrentPhotoPath);
        processImage();
        // Set the new bitmap to the ImageView
        mSelfieImageView.setImageBitmap(mResultsBitmap);
    }

    private void processImage() {
        // Convert image to stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mResultsBitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        // Create async task to process data.
        AsyncTask<InputStream, String, List<RecognizeResult>> processAsync =
                new AsyncTask<InputStream, String, List<RecognizeResult>>() {

                    ProgressDialog mProgressDialog = new ProgressDialog(MainActivity.this);

                    @Override
                    protected void onPreExecute() {
                        mProgressDialog.show();
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        mProgressDialog.setMessage(values[0]);
                    }

                    @Override
                    protected List<RecognizeResult> doInBackground(InputStream... params) {
                        publishProgress("Determining emotion...");
                        List<RecognizeResult> result = null;

                        try {
                            result = mEmotionServiceClient.recognizeImage(params[0]);
                        } catch (EmotionServiceException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return result;
                    }

                    @Override
                    protected void onPostExecute(List<RecognizeResult> recognizeResults) {
                        mProgressDialog.dismiss();

                        // Could not determine the emotion so end early.
                        if (recognizeResults == null || recognizeResults.size() == 0) {
                            Toast.makeText(MainActivity.this, "Could not determine emotion", Toast.LENGTH_SHORT).show();
                            mEmotionTextView.setText("");
                            return;
                        }

                        // Set the image view and text view with the emotion determined
                        for (RecognizeResult result : recognizeResults) {
                            mCurrentEmotion = determineEmotionFromPicture(result);
                            mEmotionTextView.setText(mCurrentEmotion);
                            mSelfieImageView.setImageBitmap(ImageHelper.drawRectangleOnBitmap(
                                    mResultsBitmap, result.faceRectangle));
                        }
                    }
                };

        processAsync.execute(inputStream);
    }

    private String determineEmotionFromPicture(RecognizeResult result) {
        // Create a list to hold the values of emotions from the picture.
        List<Double> list = new ArrayList<>();
        Scores scores = result.scores;

        // Add all emotion scores to the list.
        list.add(scores.anger);
        list.add(scores.happiness);
        list.add(scores.contempt);
        list.add(scores.disgust);
        list.add(scores.fear);
        list.add(scores.neutral);
        list.add(scores.sadness);
        list.add(scores.surprise);

        // Sort list
        Collections.sort(list);

        // Get max value from list
        double maxNum = list.get(list.size() - 1);

        // Check which emotion has the highest value
        if (maxNum == scores.anger) {
            return "Anger";
        } else if (maxNum == scores.happiness) {
            return "Happiness";
        } else if (maxNum == scores.contempt) {
            return "Contempt";
        } else if (maxNum == scores.disgust) {
            return "Disgust";
        } else if (maxNum == scores.fear) {
            return "Fear";
        } else if (maxNum == scores.neutral) {
            return "Neutral";
        } else if (maxNum == scores.sadness) {
            return "Sadness";
        } else if (maxNum == scores.surprise) {
            return "Surprise";
        } else {
            return "Can't detect";
        }
    }

    public void saveMe() {
        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, mCurrentPhotoPath);
        // Save the image
        BitmapUtils.saveImage(this, mResultsBitmap);
    }

    private boolean hasSelfieBeenTaken() {
        // Check if there is an image and an emotion.
        return mResultsBitmap != null && !mEmotionTextView.getText().equals("");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView
            processAndSetImage();
        } else {
            // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(this, mCurrentPhotoPath);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_icon_button:
                moodifyMe();
                break;
            case R.id.save_icon_button:
                saveMe();
                break;
            case R.id.finished_icon_button:
                if (hasSelfieBeenTaken()) {
                    // Start playing songs related to the mood of the picture
                    Intent intent = new Intent(this, PlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("emotion", mCurrentEmotion);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                } else {
                    // Could not determine an emotion from the picture
                    Toast.makeText(this, "Could not determine an emotion", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }
}
