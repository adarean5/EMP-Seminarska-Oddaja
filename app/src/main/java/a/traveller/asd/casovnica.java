package a.traveller.asd;

/**
 * Created by smrki on 27. 12. 2017.
 */
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;


public class casovnica extends Fragment {
    private static final int REQUEST_READ_PERMISSION = 2001;
    private int clickPosition;

    RecyclerView recyclerViewImage;
    TimelineRecyclerAdapter timelineRecyclerAdapter;
    LinearLayoutManager llm;

    ArrayList<String> imageFilePaths;
    String parentImageFolder;

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
            imageFilePaths = savedInstanceState.getStringArrayList(parentImageFolder);
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

        View rootView = inflater.inflate(R.layout.casovnica, container, false);
        recyclerViewImage = (RecyclerView) rootView.findViewById(R.id.recyclerViewImages);
        llm = new LinearLayoutManager(getActivity());
        timelineRecyclerAdapter = new TimelineRecyclerAdapter(imageFilePaths);
        recyclerViewImage.setLayoutManager(llm);
        recyclerViewImage.setAdapter(timelineRecyclerAdapter);

        final FloatingActionButton slikaj = getActivity().findViewById(R.id.fab);

        recyclerViewImage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && slikaj.getVisibility() == View.VISIBLE) {
                    slikaj.hide();
                } else if (dy < -10 && slikaj.getVisibility() != View.VISIBLE) {
                    slikaj.show();
                }
            }
        });
        return rootView;
    }

    public void SetImage(ArrayList<String> imagePaths){
        imageFilePaths = imagePaths;
        //timelineRecyclerAdapter.refreshAdapter(imageFilePaths);
        timelineRecyclerAdapter.refreshAdapter(imageFilePaths);
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
