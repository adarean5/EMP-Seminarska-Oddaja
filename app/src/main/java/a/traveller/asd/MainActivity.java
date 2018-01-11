package a.traveller.asd;

import android.*;
import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.location.LocationRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public final int EDIT_CARD = 3001;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM, YYYY");

    EditText mKam;
    Button buttonAddSlike;
    Button buttonSelectCamera;
    EditText editDescription;
    ImageView previewImage;

    RecyclerView recyclerViewMain;
    LinearLayoutManager llm;
    FloatingActionButton fab;
    RVAdapter rvAdapter;

    ArrayList<MainJourneyCard> mainJourneyCards;
    ArrayList<String> imageFilePaths;

    BitmapDecoder decoder;
    LocationManager locationManager;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        Lokacija.createLocationRequest(locationRequest);

        //startService(in);

        if (savedInstanceState == null || !savedInstanceState.containsKey("cardlist")){
            mainJourneyCards = new ArrayList<>();
            Log.v("t", "NEW");
            mainJourneyCards = readFromInternalStorage();
        }

        else{
            mainJourneyCards = savedInstanceState.getParcelableArrayList("cardlist");
            Log.v("r", "RESTORED");
        }

        recyclerViewMain = (RecyclerView)findViewById(R.id.recyclerViewMain);
        llm = new LinearLayoutManager(this);
        rvAdapter = new RVAdapter(mainJourneyCards);
        recyclerViewMain.setLayoutManager(llm);
        recyclerViewMain.setAdapter(rvAdapter);
        decoder = new BitmapDecoder();
        fab = findViewById(R.id.fab);

        recyclerViewMain.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < -10 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, EditJourneyActivity.class);
                startActivityForResult(intent, EDIT_CARD);
                Globals g = Globals.getInstance();
                g.setTest(1);
                //Location l = g.getData();
                //Log.e("LOC", String.valueOf(l.getLatitude()));
            }
        });
    }

    public ArrayList<MainJourneyCard> readFromInternalStorage() {
        ArrayList<MainJourneyCard> toReturn = new ArrayList<MainJourneyCard>();
        FileInputStream fis;
        try {
            fis = this.openFileInput("MainJourneyCards");
            ObjectInputStream oi = new ObjectInputStream(fis);
            toReturn = (ArrayList<MainJourneyCard>) oi.readObject();
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
            FileOutputStream fos = this.openFileOutput("MainJourneyCards", Context.MODE_PRIVATE);
            ObjectOutputStream of = new ObjectOutputStream(fos);
            of.writeObject(mainJourneyCards);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("cardlist", mainJourneyCards);
        super.onSaveInstanceState(outState);
        Log.v("s", "SAVED");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("cardlist")){
            mainJourneyCards = new ArrayList<>();
            Log.v("t", "NEW");
        }

        else{
            mainJourneyCards = savedInstanceState.getParcelableArrayList("cardlist");
            Log.v("r", "RESTORED");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("p", "PAUSED");
        saveToInternalStorage();
        //stopService(new Intent(MainActivity.this, MyLocationService.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == ImagePickHandler.PICK_IMAGE) {
            imageFilePaths = ImagePickHandler.onPickFromGallery(data, this, "path");
            previewImage.setImageBitmap(BitmapFactory.decodeFile(imageFilePaths.get(0)));
        }

        if (resultCode == RESULT_OK && requestCode == ImagePickHandler.REQUEST_IMAGE_CAPTURE) {
            //imageFilePaths = ImagePickHandler.onPickFromCamera(data, this);
            previewImage.setImageBitmap(BitmapFactory.decodeFile(imageFilePaths.get(0)));
        }

        if (resultCode == RESULT_OK  && requestCode == EDIT_CARD){
            Bundle bundle = data.getExtras();
            int position = bundle.getInt("Edit.position");
            imageFilePaths = bundle.getStringArrayList("Edit.imageFilePaths");
            String title = bundle.getString("Edit.editTitle");
            String description = bundle.getString("Edit.editDesc");
            String currentDate = bundle.getString("Edit.dateText");

            String parentImageFolder = title + "-"+ currentDate.toString();

            mainJourneyCards.add(0, new MainJourneyCard(imageFilePaths.get(0), title, description, currentDate, parentImageFolder) );
            (recyclerViewMain.getAdapter()).notifyDataSetChanged();

            Bitmap coverImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imageFilePaths.get(0)), 600, 400);
            String coverPath =  new File(imageFilePaths.get(0)).getName() + "_cover.jpg";
            ImagePickHandler.saveToInternalStorage(MainActivity.this, coverImage, coverPath);

            try {
                OutputStream fos = openFileOutput(parentImageFolder, MODE_PRIVATE);
                ObjectOutputStream of = new ObjectOutputStream(fos);
                of.writeObject(imageFilePaths);
                of.flush();
                of.close();
                fos.close();
                saveToInternalStorage();
            }catch (IOException e){
                Log.d("IO Error", "Pisanje neuspesno");
                e.printStackTrace();
            }
            Log.e("w", "EDIT_CARD RESULT_OK ");
        }

        if (resultCode == RESULT_OK  && requestCode == 8001){
            Bundle bundle = data.getExtras();
            //imageFilePaths = bundle.getStringArrayList("Edit.imageFilePaths");
            int position = bundle.getInt("Edit.position");
            String title = bundle.getString("Edit.editTitle");
            String description = bundle.getString("Edit.editDesc");

            mainJourneyCards.get(position).setTopText(title);
            mainJourneyCards.get(position).setBotText(description);

            (recyclerViewMain.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ImagePickHandler.REQUEST_CAMERA_PERMISSION){
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED){
                ImagePickHandler.pickFromCamera(MainActivity.this);
            }
        }

        if (requestCode == ImagePickHandler.REQUEST_GALLERY_PERMISSION){
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                ImagePickHandler.pickFromGallery(this);
            }
        }

    }
}


