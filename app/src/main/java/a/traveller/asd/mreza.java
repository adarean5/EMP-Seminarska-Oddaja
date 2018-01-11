package a.traveller.asd;

/**
 * Created by smrki on 27. 12. 2017.
 */
import android.*;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;



public class mreza extends Fragment {
    private static final int REQUEST_READ_PERMISSION = 2001;

    private GridView imageGrid;
    private ArrayList<String> imageFilePaths;
    private ImageAdapter imageAdapter;
    String parentImageFolder;
    int clickPosition;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        Log.v("sad", "ONCREATE");
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
        //imageAdapter.refreshAdapter(imageFilePaths);
        /*imageFilePaths = new ArrayList<String>();
        imageFilePaths = readFromInternalStorage();*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("sad", "ONCREATEVIEW");

        if(savedInstanceState == null){
            imageFilePaths = new ArrayList<String>();
            imageFilePaths = readFromInternalStorage();
        }
        else{
            imageFilePaths = savedInstanceState.getStringArrayList("imageFilePaths");
        }

        View rootView = inflater.inflate(R.layout.mreza, container, false);
        imageGrid = (GridView) rootView.findViewById(R.id.grid_view);
        imageAdapter = new ImageAdapter(getActivity(), imageFilePaths);
        imageGrid.setAdapter(imageAdapter);
        final FloatingActionButton fab = getActivity().findViewById(R.id.fab);

        imageGrid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (firstVisibleItem > totalItemCount - visibleItemCount) {
                    fab.hide();
                }else {
                    fab.show();
                }
            }
        });



        imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int permRead = getContext().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permRead != PackageManager.PERMISSION_GRANTED){
                    clickPosition = i;
                    requestPermissions(
                            new String[]{
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                            },
                            REQUEST_READ_PERMISSION);
                } else{
                    try {
                        displayFullScreenImage(i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return rootView;
    }

    public void SetImage(ArrayList<String> imagePaths){
        imageFilePaths = imagePaths;
        imageAdapter.refreshAdapter(imageFilePaths);
    }

    private void displayFullScreenImage(int i) throws IOException {
        Log.v("c", "ON ITEM CLICK " + i);
        String filePath = imageFilePaths.get(i);
        ExifInterface exifInterface = new ExifInterface(filePath);
        Intent intent = new Intent();
        intent.setClass(getContext(), FullScreenImage.class);
        Bundle bundle = new Bundle();
        bundle.putString("imageFilePath", filePath);
        bundle.putString("imageDesc", exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
        bundle.putString("imageDate", exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
        bundle.putInt("postition", i);
        intent.putExtras(bundle);
        getContext().startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_PERMISSION){
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                try {
                    displayFullScreenImage(clickPosition);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
    public void setArguments(Bundle args) {
        super.setArguments(args);
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
}
