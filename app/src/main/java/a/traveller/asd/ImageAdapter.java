package a.traveller.asd;

import android.app.Activity;
import android.media.ExifInterface;
import android.util.Log;
import android.widget.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import android.graphics.*;
import android.view.*;
import android.content.*;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> imageFilePaths;

    public ImageAdapter(Context context, ArrayList<String> imageFilePaths) {
        this.context = context;
        this.imageFilePaths = imageFilePaths;
    }

    public int getCount() {
        return this.imageFilePaths.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater .inflate(R.layout.grid_item_left, null);

        if (convertView == null) {
            imageView = (ImageView) view.findViewById(R.id.item_image);
        } else {
            imageView = (ImageView) convertView;
        }
        try {
            String thumbPath =  new File(this.imageFilePaths.get(position)).getName() + "_thumb.jpg";
            InputStream input = context.openFileInput(thumbPath);
            imageView.setImageBitmap(BitmapFactory.decodeStream(input));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageView;
    }

    public void refreshAdapter(List<String> newImageFilePaths) {
        this.imageFilePaths.clear();
        this.imageFilePaths.addAll(newImageFilePaths);
        notifyDataSetChanged();
    }
}

