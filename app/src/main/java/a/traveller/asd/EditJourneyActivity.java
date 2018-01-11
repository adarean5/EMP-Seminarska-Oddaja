package a.traveller.asd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EditJourneyActivity extends AppCompatActivity {
    //private Toolbar toolbar;
    private android.support.v7.widget.Toolbar toolbar;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM, YYYY");

    private boolean imagePicked = false;
    private int editCardPosition;
    private boolean editMode =false;

    EditText editTitle;
    EditText editDesc;
    TextView dateText;

    ImageView coverImage;

    ImageButton pickFromCamera;
    ImageButton pickFromGallery;

    Geocoder geocoder;
    Address address;

    ArrayList<String> imageFilePaths = new ArrayList<String>();
    LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journey);

        geocoder = new Geocoder(this, Locale.getDefault());
        Lokacija.createLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarSave);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Add Journey");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imageFilePaths = new ArrayList<String>();

        editTitle = (EditText) findViewById(R.id.journeyCardTitle);
        editDesc = (EditText) findViewById(R.id.imageCardDesc);
        dateText = (TextView) findViewById(R.id.imageCardDate);
        coverImage = (ImageView) findViewById(R.id.editCardCover);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout2);

        pickFromCamera = (ImageButton) findViewById(R.id.buttonEditCamera);
        pickFromGallery = (ImageButton) findViewById(R.id.buttonEditGallery);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null){
                editMode = true;
                getSupportActionBar().setTitle("Edit Journey");
                editTitle.setText(bundle.getString("journeyTitle"));
                editDesc.setText(bundle.getString("journeyDescription"));
                dateText.setText(bundle.getString("dateText"));
                coverImage.setImageBitmap(BitmapFactory.decodeFile(bundle.getString("journeyCoverImagePath")));
                coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imagePicked = true;
                editCardPosition = bundle.getInt("position");
                linearLayout.setVisibility(View.GONE);
            }
            else {
                String currentDate = DATE_FORMAT.format(new Date());
                dateText.setText(currentDate);

                pickFromCamera.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View view) {
                        if (Lokacija.checkLocationPermissions(EditJourneyActivity.this)){
                            mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(EditJourneyActivity.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        try {
                                            address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        ImagePickHandler.checkCameraPermissions(EditJourneyActivity.this);
                                    }
                                    else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(EditJourneyActivity.this);
                                        builder.setTitle("Location unavailable")
                                                .setMessage("Please enable location or wait for a location update")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // continue with delete
                                                    }
                                                })
                                                .show();
                                    }
                                    }
                                });
                        }
                    }
                });

                pickFromGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ImagePickHandler.checkGalleryPermissions(EditJourneyActivity.this);
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save){
            if (!editMode){
                if (editTitle.getText().toString().equals("")){
                    Toast.makeText(getBaseContext(),"Please enter a title", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (!imagePicked){
                    Toast.makeText(getBaseContext(),"Please select a cover image", Toast.LENGTH_SHORT).show();
                    return false;
                }

                Log.v("d", "CLICKED SAVE ON MENU!!!!");
                Intent returnIntent = new Intent();

                Bundle bundle = new Bundle();
                bundle.putString("Edit.editTitle", editTitle.getText().toString());
                bundle.putString("Edit.editDesc", editDesc.getText().toString());
                bundle.putString("Edit.dateText", dateText.getText().toString());
                bundle.putStringArrayList("Edit.imageFilePaths", imageFilePaths);
                bundle.putInt("Edit.position", editCardPosition);

                returnIntent.putExtras(bundle);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
            else {
                if (editTitle.getText().toString().equals("")){
                    Toast.makeText(getBaseContext(),"Please enter a title", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (!imagePicked){
                    Toast.makeText(getBaseContext(),"Please select a cover image", Toast.LENGTH_SHORT).show();
                    return false;
                }

                Log.v("d", "CLICKED SAVE ON MENU!!!!");
                Intent returnIntent = new Intent();

                Bundle bundle = new Bundle();
                bundle.putString("Edit.editTitle", editTitle.getText().toString());
                bundle.putString("Edit.editDesc", editDesc.getText().toString());
                bundle.putInt("Edit.position", editCardPosition);

                returnIntent.putExtras(bundle);
                setResult(RESULT_OK, returnIntent);
                finish();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == ImagePickHandler.PICK_IMAGE) {
            imageFilePaths = ImagePickHandler.onPickFromGallery(data, this, "path");
            coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            coverImage.setImageBitmap(BitmapFactory.decodeFile(imageFilePaths.get(0)));
            imagePicked = true;

        }

        if (requestCode == ImagePickHandler.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageFilePaths = ImagePickHandler.onPickFromCamera(data, this, address);
            coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            coverImage.setImageBitmap(BitmapFactory.decodeFile(imageFilePaths.get(0)));
            imagePicked = true;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ImagePickHandler.REQUEST_CAMERA_PERMISSION){
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED){
                ImagePickHandler.pickFromCamera(this);
            }
        }

        if (requestCode == ImagePickHandler.REQUEST_GALLERY_PERMISSION){
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                ImagePickHandler.pickFromGallery(this);
            }
        }

        if (requestCode == Lokacija.REQUEST_LOCATION_PERMISSIONS){
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(EditJourneyActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    try {
                                        address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    ImagePickHandler.checkCameraPermissions(EditJourneyActivity.this);
                                }
                                else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(EditJourneyActivity.this);
                                    builder.setTitle("Location unavailable")
                                            .setMessage("Please enable location or wait for a location update")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // continue with delete
                                                }
                                            })
                                            .show();
                                }
                            }
                        });
            }
        }
    }
}
