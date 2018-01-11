package a.traveller.asd;

/**
 * Created by smrki on 27. 12. 2017.
 */

// TODO: Navodila za uporabo
/**
 * Ce ces dobit thumbnail: aja pa mogoc bos mogu u ksn try catch zapret to
 String thumbPath =  new File(this.imageFilePaths.get(i)).getName() + "_thumb.jpg";
 InputStream input = getContext().openFileInput(thumbPath);
 smrkiImageView.setImageBitmap(BitmapFactory.decodeStream(input));

 Ce ces dobit opis, coordinates ma dva elementa pol s koordinatami
 ExifInterface exifInterface = new ExifInterface(imageFilePaths.get(i));
 String[] coordinates = exifInterface.getAttribute("UserComment").split(" ");
 */

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import a.traveller.asd.DirectionFinder;
import a.traveller.asd.DirectionFinderListener;
import a.traveller.asd.Route;


public class zemljevid extends Fragment implements OnMapReadyCallback, DirectionFinderListener {
    private static final int REQUEST_READ_PERMISSION = 2001;

    String thumbPath;
    private GoogleMap mMap;
    Context mContext;
    String etOrigin1;
    String etDestination1;
    Random rnd;
    int stevilo1;
    int stevilo2;
    ImageView smrkiImageVie;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    FloatingActionButton pot;
    AppBarLayout appBarLayout;

    Toolbar toolbar;
    AppBarLayout.LayoutParams params;

    private ArrayList<String> imageFilePaths;
    String parentImageFolder;

    public zemljevid newInstance(Context mContext) {
        zemljevid fragment = new zemljevid();
        this.mContext = mContext;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            parentImageFolder = getArguments().getString("parentImageFolder");
        }

        if(savedInstanceState == null){
            imageFilePaths = new ArrayList<String>();
            imageFilePaths = readFromInternalStorage();
        }
        else{
            imageFilePaths=savedInstanceState.getStringArrayList(parentImageFolder);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState == null){
            imageFilePaths = new ArrayList<String>();
            imageFilePaths = readFromInternalStorage();
        }
        else{
            imageFilePaths = savedInstanceState.getStringArrayList("imageFilePaths");
        }

        View rootView = inflater.inflate(R.layout.zemljevid, null, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar3);
        params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        pot = (FloatingActionButton) rootView.findViewById(R.id.pot);
        pot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                View mView = getLayoutInflater().inflate(R.layout.navigacija, null);
                final EditText zacetek = (EditText) mView.findViewById(R.id.zacetek);
                final EditText konec = (EditText) mView.findViewById(R.id.konec);


                mBuilder.setPositiveButton("Potrdi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                mBuilder.setNegativeButton("Preklici", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!zacetek.getText().toString().isEmpty() && !konec.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), "Successful", Toast.LENGTH_SHORT).show();
                            etOrigin1 = zacetek.getText().toString();
                            etDestination1 = konec.getText().toString();
                            sendRequest();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Without empty fields", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        return rootView;
    }

    private void sendRequest() {
        String origin = etOrigin1;
        String destination = etDestination1;
        if (origin.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void SetImage(ArrayList<String> imagePaths){
        imageFilePaths = imagePaths;
    }

    public ArrayList<String> readFromInternalStorage() {
        ArrayList<String> toReturn = new ArrayList<>();
        FileInputStream fis;
        try {
            fis = getContext().openFileInput(parentImageFolder);
            ObjectInputStream oi = new ObjectInputStream(fis);
            toReturn = ( ArrayList<String>) oi.readObject();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ExifInterface exifInterface = null;
        String[] coordinates;
        LatLng hcmus;


        try {
            for(int i=0;i<imageFilePaths.size();i++){
                stevilo1 = ThreadLocalRandom.current().nextInt(1,  20+ 1);
                stevilo2 = ThreadLocalRandom.current().nextInt(1,  20+ 1);
                thumbPath =  new File(this.imageFilePaths.get(i)).getName() + "_thumb.jpg";
                InputStream input = getContext().openFileInput(thumbPath);
                Bitmap slika= BitmapFactory.decodeStream(input);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(slika, 150, 150, false);
                exifInterface = new ExifInterface(imageFilePaths.get(i));
                coordinates = exifInterface.getAttribute("UserComment").split(" ");
                //hcmus = new LatLng(Double.parseDouble(coordinates[0]),Double.parseDouble(coordinates[1]));
                hcmus = new LatLng(stevilo1, stevilo2);
                mMap.addMarker(new MarkerOptions()
                        .position(hcmus)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
                        );


            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(getActivity(), "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));


            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("imageFilePaths", imageFilePaths);
        outState.putString("parentImageFolder", parentImageFolder);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey(parentImageFolder)){
            imageFilePaths = new ArrayList<>();
            Log.v("t", "NEW FILEPATHS MREZA");
            imageFilePaths = readFromInternalStorage();
        }

        else{
            imageFilePaths = savedInstanceState.getStringArrayList(parentImageFolder);
            Log.v("r", "RESTORED FILEPATSH MREZA");
        }
    }
    @Override
    public void onResume(){
        super.onResume();

        if(mMap != null){ //prevent crashing if the map doesn't exist yet (eg. on starting activity)
            mMap.clear();

            // add markers from database to the map
        }
    }
}