package a.traveller.asd;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by Jernej on 27. 12. 2017.
 */

public class ImagePickHandler {

    public static final int PICK_IMAGE = 0001;
    public static final int REQUEST_IMAGE_CAPTURE = 0002;

    public static final int REQUEST_CAMERA_PERMISSION = 1001;
    public static final int REQUEST_GALLERY_PERMISSION = 1002;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("H:mm, d. MMM");

    private static Uri imageUri;
    private static String mCurrentPhotoPath = null;

    private static android.support.media.ExifInterface exifInterface;
    private static BitmapDecoder bitmapDecoder = new BitmapDecoder();

    private static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static int screenHeight;
    public static int screenWidth;

    //private static Lokacija lokacija = new Lokacija();

    public static void pickFromGallery(Context context){
        Activity activity = (Activity) context;

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        activity.startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public static void pickFromCamera(Context context){
        //checkCameraPermissions(context);

        Activity activity = (Activity) context;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String imageName = "Image-"+ n;
                photoFile = createImageFile(imageName, context);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        "a.traveller.asd.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // onActivityResult

    public static ArrayList<String> onPickFromGallery(Intent data, Context context, String thumbnailPath){
        ArrayList<String> imageFilePaths = new ArrayList<String>();

        if (data.getClipData() != null) {
            ClipData mClipData = data.getClipData();
            ArrayList<Uri> mArrayUri = new ArrayList<Uri>();

            for (int i = 0; i < mClipData.getItemCount(); i++) {
                ClipData.Item item = mClipData.getItemAt(i);
                Uri uri = item.getUri();

                String picturePath = getRealPathFromURI(uri, context);
                //imageFilePaths.add(picturePath);

                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 200);

                try {
                    exifInterface = new android.support.media.ExifInterface(picturePath);
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    switch(orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            bitmap = rotateImage(bitmap, 90);
                            thumbBitmap = rotateImage(thumbBitmap, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            bitmap = rotateImage(bitmap, 180);
                            thumbBitmap = rotateImage(thumbBitmap, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            bitmap = rotateImage(bitmap, 270);
                            thumbBitmap = rotateImage(thumbBitmap, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                SaveImage(bitmap, picturePath);
                imageFilePaths.add(picturePath);

                String thumbPath =  new File(picturePath).getName() + "_thumb.jpg";
                saveToInternalStorage(context, thumbBitmap, thumbPath);
            }
            Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
        }
        return imageFilePaths;
    }

    public static ArrayList<String> onPickFromCamera(Intent data, Context context, Address address){
        //SecondActivity.lokacija.getLocation();
        ArrayList<String> imageFilePaths = new ArrayList<String>();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        Bitmap bitmap = bitmapDecoder.decodeSampledBitmapFromFile(mCurrentPhotoPath, screenWidth, screenHeight);
        Bitmap thumbBitmap = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);

        try {
            exifInterface = new android.support.media.ExifInterface(mCurrentPhotoPath);
            int orientation = exifInterface.getAttributeInt(android.support.media.ExifInterface.TAG_ORIENTATION,
                    android.support.media.ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    thumbBitmap = rotateImage(thumbBitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    thumbBitmap = rotateImage(thumbBitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    thumbBitmap = rotateImage(thumbBitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SaveImage(bitmap, mCurrentPhotoPath);

        try {
            exifInterface = new android.support.media.ExifInterface(mCurrentPhotoPath);
            String currentDate = DATE_FORMAT.format(new Date());
            exifInterface.setAttribute(android.support.media.ExifInterface.TAG_DATETIME, currentDate);
            exifInterface.setAttribute(android.support.media.ExifInterface.TAG_IMAGE_DESCRIPTION, "Click to add a note");
            String a = address.getLocality() + ", " + address.getAdminArea();
            exifInterface.setAttribute(android.support.media.ExifInterface.TAG_GPS_AREA_INFORMATION, a);
            exifInterface.setAttribute("UserComment", String.valueOf(address.getLatitude()) + " " + String.valueOf(address.getLongitude()));
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageFilePaths.add(mCurrentPhotoPath);

        String thumbPath =  new File(mCurrentPhotoPath).getName() + "_thumb.jpg";
        saveToInternalStorage(context, thumbBitmap, thumbPath);

        return imageFilePaths;
    }

    public static File createImageFile(String imageFileName, Context context) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpg");
        image.createNewFile();
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static String getRealPathFromURI(Uri contentUri, Context context) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public static void SaveImage(Bitmap finalBitmap, String myDir) {
        File file = new File(myDir);
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveToInternalStorage(Context context, Bitmap bitmap, String path) {
        try {
            FileOutputStream out = context.openFileOutput(path, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e("InternalStorage", e.getMessage());
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static void checkCameraPermissions(Context context){
        int permCamera = context.checkSelfPermission(Manifest.permission.CAMERA);
        int permWrite = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permRead = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permCamera != PackageManager.PERMISSION_GRANTED || permWrite != PackageManager.PERMISSION_GRANTED ||
                permRead != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                    },
                    REQUEST_CAMERA_PERMISSION);
            return;
        } else
            pickFromCamera(context);
    }

    public static void checkGalleryPermissions(Context context){
        int permWrite = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permRead = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permWrite != PackageManager.PERMISSION_GRANTED || permRead != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    REQUEST_GALLERY_PERMISSION);
        } else
            pickFromGallery(context);
    }
}
