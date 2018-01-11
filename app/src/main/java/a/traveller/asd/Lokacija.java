package a.traveller.asd;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

/**
 * Created by smrki on 4. 01. 2018.
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

import java.util.List;
import java.util.Locale;

public class Lokacija extends AppCompatActivity implements LocationListener {
    public static final int REQUEST_LOCATION_PERMISSIONS = 4001;

    public static Double[] koordinati=new Double[2];
    public static String[] kraj=new String[10];
    Button getLocationBtn;
    TextView locationText;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);


        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();

            }
        });

    }

    public static String[] getKraj() {
        return kraj;
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationText.setText("Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());
        koordinati[0]=location.getLongitude();
        koordinati[1]=location.getLatitude();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            locationText.setText(locationText.getText() + "\n"+addresses.get(0).getAddressLine(0)+", "+
                    addresses.get(0).getAddressLine(1)+", "+addresses.get(0).getAddressLine(2));
            kraj[0]=addresses.get(0).getAddressLine(0);
            kraj[1]=addresses.get(0).getAddressLine(1);
            kraj[2]=addresses.get(0).getAddressLine(2);
        }catch(Exception e)
        {

        }

    }

    public static boolean checkLocationPermissions(Context context){
        int permLocation = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permLocationFine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permLocation != PackageManager.PERMISSION_GRANTED || permLocationFine != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_LOCATION_PERMISSIONS);
            return false;
        } else return true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(Lokacija.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public static void createLocationRequest(LocationRequest mLocationRequest) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }
}