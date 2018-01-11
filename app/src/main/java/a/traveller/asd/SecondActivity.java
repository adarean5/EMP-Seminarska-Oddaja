package a.traveller.asd;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SecondActivity extends AppCompatActivity {
    mreza tab1;
    casovnica tab2;
    zemljevid tab3;
    ArrayList<Bitmap> imageBitmaps;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    String journeyTitle;
    String journeyCoverImagePath;
    String parentImageFolder;

    ArrayList<String> imageFilePaths;

    //public static Lokacija lokacija;
    //public static String kraj;

    Geocoder geocoder;
    Address address;
    LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        geocoder = new Geocoder(this, Locale.getDefault());
        Lokacija.createLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        final Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null){
                journeyTitle = bundle.getString("journeyTitle");
                journeyCoverImagePath = bundle.getString("journeyCoverImagePath");
                parentImageFolder = bundle.getString("parentImageFolder");
            }
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey(parentImageFolder)){
            imageFilePaths=new ArrayList<String>();
            Log.v("t", "NEW");
            imageFilePaths = readFromInternalStorage();
        }

        else{
            imageFilePaths = savedInstanceState.getStringArrayList(parentImageFolder);
            Log.v("r", "RESTORED");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(journeyTitle);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            /*AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
            params.setScrollFlags(0);*/
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.containerFrag);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        Bundle bundle =getIntent().getExtras();

        FloatingActionButton slikaj = findViewById(R.id.fab);
        slikaj.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (Lokacija.checkLocationPermissions(SecondActivity.this)){
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(SecondActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        try {
                                            address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        ImagePickHandler.checkCameraPermissions(SecondActivity.this);
                                    }
                                    else {
                                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SecondActivity.this);
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

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ImagePickHandler.REQUEST_CAMERA_PERMISSION){
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                ImagePickHandler.pickFromCamera(SecondActivity.this);
            }
        }
        if (requestCode == Lokacija.REQUEST_LOCATION_PERMISSIONS){
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(SecondActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    try {
                                        address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    ImagePickHandler.checkCameraPermissions(SecondActivity.this);
                                }
                                else {
                                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(SecondActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode, data);
        if (requestCode == ImagePickHandler.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageFilePaths.addAll(ImagePickHandler.onPickFromCamera(data, this, address));
            saveToInternalStorage();
            tab1.SetImage(imageFilePaths);
            tab2.SetImage(imageFilePaths);
        }
    }

    public ArrayList<String> readFromInternalStorage() {
        ArrayList<String> toReturn = new ArrayList<>();
        FileInputStream fis;
        try {
            fis = this.openFileInput(parentImageFolder);
            ObjectInputStream oi = new ObjectInputStream(fis);
            toReturn = (ArrayList<String>) oi.readObject();
            oi.close();
            Log.v("ir", "Read from internal storage");
        } catch (FileNotFoundException e) {
            Log.e("InternalStorage", e.getMessage());
        } catch (IOException e) {
            Log.e("InternalStorage", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    public void saveToInternalStorage() {
        try {
            FileOutputStream fos = this.openFileOutput(parentImageFolder, Context.MODE_PRIVATE);
            ObjectOutputStream of = new ObjectOutputStream(fos);
            of.writeObject(imageFilePaths);
            of.flush();
            of.close();
            fos.close();
            Log.v("iw", "Written to internal storage");
        }
        catch (Exception e) {
            Log.e("InternalStorage", e.getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("imagePaths", imageFilePaths);
        super.onSaveInstanceState(outState);
        Log.v("s", "SAVED");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("imagePaths")){
            imageFilePaths = new ArrayList<String>();
            Log.v("t", "NEW");
        }

        else{
            imageFilePaths = savedInstanceState.getStringArrayList("imagePaths");
            Log.v("r", "RESTORED");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("p", "PAUSED");
        saveToInternalStorage();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_second, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter  {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.v("lll", "SECOND SECTIONS PAGE ADAPTER");
            Bundle bundle = new Bundle();
            bundle.putString("parentImageFolder", parentImageFolder);
            switch (position){
                case 0:
                    tab1 = new mreza();
                    tab1.setArguments(bundle);
                    return tab1;
                case 1:
                    tab2 = new casovnica();
                    tab2.setArguments(bundle);
                    return tab2;
                case 2:
                    tab3 = new zemljevid();
                    tab3.setArguments(bundle);
                    return tab3;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        public CharSequence getPageTitle(int position){
            switch (position){
                case 0:

                    return "GALLERY";
                case 1:

                    return "TIMLINE";
                case 2:

                    return "MAP";
            }
            return null;
        }
    }


}
