package com.example.meghan.simplecameraapp;



import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Camera Main Activity";
    private static final int REQUEST_SAVE_IMAGE_PERMISSION_REQUEST_CODE = 123;
    Button mTakePictureButton;
    ImageView mCameraPicture;

    private static int TAKE_PICTURE = 0;

    private String mImagePath;
    private Bitmap mImage;

    private static final String IMAGE_FILEPATH_KEY = "image filepath key";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mImagePath = savedInstanceState.getString(IMAGE_FILEPATH_KEY);
        }

        mCameraPicture = (ImageView) findViewById(R.id.camera_picture);
        mTakePictureButton = (Button) findViewById(R.id.take_picture_button);
        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
    }

    private void takePhoto() {

        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check to see if there is a camera on this device.
        if (pictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(MainActivity.this, "Your device does not have a camera", Toast.LENGTH_LONG).show();
        }

        else {
            // Create a File object from the specified filename
            String imageFilename = "simple_camera_app_" + new Date().getTime();  //Create a unique filename including a timestamp

            File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = null;
            Uri imageFileUri = null;

            try {

                imageFile = File.createTempFile(imageFilename, ".jpg", storageDirectory);
                Log.i(TAG, "image file " + imageFile);

                mImagePath = imageFile.getAbsolutePath();
                Log.i(TAG, "image file path  " + mImagePath);

                imageFileUri = FileProvider.getUriForFile(MainActivity.this, "com.clara.simplecameraapp", imageFile);

            } catch (IOException ioe) {
                Log.e(TAG, "Error creating file for photo storage", ioe);
                return;
            }

            //So if the file creation worked, should have a value for imageFileUri. Include this URI as an extra
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);

            //And then request the camera is launched
            startActivityForResult(pictureIntent, TAKE_PICTURE);

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "On Activity Result");

        if (resultCode == RESULT_OK && requestCode == TAKE_PICTURE) {
            scaleBitmap();
            saveToMediaStore();

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged");

        if (hasFocus && mImagePath != null) {
            scaleBitmap();
            mCameraPicture.setImageBitmap(mImage);
        }
    }



    private void saveToMediaStore() {
        //Add image to device's MediaStore - this makes the image accessible to the
        //gallery app, and other apps that can read from the MediaStore

        //Need to request permission on Nougat and above

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            MediaStore.Images.Media.insertImage(getContentResolver(), mImage, "SimpleCameraApp", "Photo taken by SimpleCameraApp");
        } else {
            //This request opens a dialog box for the user to accept the permission request.
            // When the user clicks ok or cancel, the onRequestPermission method (below) is called with the results
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_SAVE_IMAGE_PERMISSION_REQUEST_CODE);
        }

    }


    // This ts the callback for requestPermissions for adding image to media store

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_SAVE_IMAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Now should be able to save
                MediaStore.Images.Media.insertImage(getContentResolver(), mImage, "SimpleCameraApp", "Photo taken by SimpleCameraApp");
            }
        }
    }

    //Handle rotation. Save the image pathname.
    // Bitmap objects are parcelable so can be put in a bundle, but the bitmap might be huge and take up more
    // memory that the app is allocated. So, put the file path and re-decode the bitmap on rotation.
    @Override
    public void onSaveInstanceState(Bundle outBundle) {
        outBundle.putString(IMAGE_FILEPATH_KEY, mImagePath);
    }


    private void scaleBitmap() {
        // * Scale picture taken to fit into the ImageView */
        //Step 1: what size is the ImageView?
        int imageViewHeight = mCameraPicture.getHeight();
        int imageViewWidth = mCameraPicture.getWidth();

        //Step 2: decode file to find out how large the image is.

        // BitmapFactory is used to create bitmaps from pixels in a file.
        // Many options and settings, so use a BitmapFactory.Options object to store our desired settings.
        // Set the inJustDecodeBounds flag to true,
        // which means just the *information about* the picture is decoded and stored in bOptions
        // Not all of the pixels have to be read and stored in this process.
        // When we've done this, we can query bOptions to find out the original picture's height and width.

        BitmapFactory.Options bOptions = new BitmapFactory.Options();
        bOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mImagePath, bOptions);

        int pictureHeight = bOptions.outHeight;
        int pictureWidth = bOptions.outWidth;

        //Step 3. Can use the original size and target size to calculate scale factor
        int scaleFactor = Math.min(pictureHeight / imageViewHeight, pictureWidth / imageViewWidth);

        //Step 4. Decode the image file into a new bitmap, scaled to fit the ImageView
        bOptions.inJustDecodeBounds = false;   //setting this to be false will actualy decode the file to a Bitmap
        bOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mImagePath, bOptions);
        mImage = bitmap;
    }
}







/*
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button mTakePictureButton;
    ImageView mCameraPicture;

    private static int TAKE_PICTURE = 0;
    private String mImageFilename = "temp_photo_file";
    private Uri mImageFileUri;

    private static final String PICTURE_TO_DISPLAY = "there is a picture to display";
    private boolean mIsPictureToDisplay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) { //Help handle rotation.
            mIsPictureToDisplay = savedInstanceState.getBoolean(PICTURE_TO_DISPLAY, false);
        }

        mCameraPicture = (ImageView) findViewById(R.id.camera_picture); //refences to Imageview and buttons
        mTakePictureButton = (Button) findViewById(R.id.take_picture_button);
        mTakePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //TODO Why does it return a thumbnail picture, not the full size
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Check to see if there is a camera on this device
                if (pictureIntent.resolveActivity(getPackageManager()) != null) { //No app able to take picture, will get null back so don't crash such as Apple watch
                    // Create a File object from the specified filename
                    File imageFile = new File(Environment.getExternalStorageDirectory(), mImageFilename); //File is a path on a file system
                    // And a URI that Android will use to find this file resource
                    // Remember a URI specifies how to find a particular resource (in this case, on the file system),
                    // and where to find it (the file path)
                    mImageFileUri = Uri.fromFile(imageFile); //URI - what kind of resourse and where is it?
                    //Include this URI as an extra
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageFileUri); //Key, as somewhere to put the output.
                    //And then request the camera is launched
                    startActivityForResult(pictureIntent, TAKE_PICTURE);
                } else {
                    Toast.makeText(MainActivity.this, "Your device does not have a camera",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    //Whom is returning data to us.
        if (resultCode == RESULT_OK && requestCode == TAKE_PICTURE) { //Is there a picture to display? checking

            //relaced code
            //Bitmap image = data.getParcelableExtra("data");
            //mCameraPicture.setImageBitmap(image);

            mIsPictureToDisplay = true;
        } else {
            mIsPictureToDisplay = false;
        }
    }

    @Override //gets called when the user interface is ready and in this window we will set the picture.
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && mIsPictureToDisplay) {
            Bitmap image = scaleBitmap();
            mCameraPicture.setImageBitmap(image);
            //Add image to device's MediaStore - this makes the image accessible to the
            //gallery app, and other apps that can read from the MediaStore
            MediaStore.Images.Media.insertImage(getContentResolver(), image,
                    "SimpleCameraApp", "Photo taken by SimpleCameraApp");
        }
    }

    @Override //Help handle rotation
    public void onSaveInstanceState(Bundle outBundle) {
        outBundle.putBoolean(PICTURE_TO_DISPLAY, mIsPictureToDisplay);
    }

*/
/*
    private Bitmap scaleBitmap() {
        //  Scale picture taken to fit into the ImageView
        //Step 1: what size is the ImageView?

*/
/*
        int imageViewHeight = mCameraPicture.getHeight();
        int imageViewWidth = mCameraPicture.getWidth();

        //Step 2: decode file to find out how large the image is.

        // BitmapFactory is used to create bitmaps from pixels in a file.
        // Many options and settings, so use a BitmapFactory.Options object to store our desired settings.
        // Set the inJustDecodeBounds flag to true,
        // which means just the *information about* the picture is decoded and stored in bOptions
        // Not all of the pixels have to be read and stored in this process.
        // When we've done this, we can query bOptions to find out the original picture's height and width.

        BitmapFactory.Options bOptions = new BitmapFactory.Options(); //Dealing with pictures alot of different things
        bOptions.inJustDecodeBounds = true; //Go find out meta data about the file, How big is it?

        File file = new File(Environment.getExternalStorageDirectory(), mImageFilename); //know where we saved the file
        Uri imageFileUri = Uri.fromFile(file);
        String photoFilePath = imageFileUri.getPath();
        BitmapFactory.decodeFile(photoFilePath, bOptions);      //Get information about the image, How big is it?
        int pictureHeight = bOptions.outHeight;
        int pictureWidth = bOptions.outWidth;

        //Step 3. Can use the original size and target size to calculate scale factor
        int scaleFactor = Math.min(pictureHeight / imageViewHeight, pictureWidth / imageViewWidth); //To fit it into the screen

        //Step 4. Decode the image file into a new bitmap, scaled to fit the ImageView
        bOptions.inJustDecodeBounds = false;   //now we want to get a bitmap
        bOptions.inSampleSize = scaleFactor; //Turn it into something we can display.
        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath, bOptions);
        return bitmap;
    }
}

*/