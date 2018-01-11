package a.traveller.asd;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;



public class List_slik_adapter extends ArrayAdapter<String> {
    Context context;
    private ArrayList<String> imagePaths;
    public List_slik_adapter(Context c, ArrayList<String> imagePaths){
        super(c,R.layout.casovnica_item_layout);
        this.context=c;
        this.imagePaths=imagePaths;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View r=convertView;


        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row=inflater.inflate(R.layout.casovnica_item_layout,parent,false);
        ImageView myImage=row.findViewById(R.id.imageView10);



        File imgFile= new File(this.imagePaths.get(position));
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        myImage.setImageResource(R.drawable.ic_launcher_foreground);

        return row;
    }

}
