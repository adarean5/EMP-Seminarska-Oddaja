package a.traveller.asd;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * Created by Jernej on 31. 12. 2017.
 */

public class BitmapDecoder {

    /*
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(getResources(), R.id.myimage, options);
    int imageHeight = options.outHeight;
    int imageWidth = options.outWidth;
    String imageType = options.outMimeType;
    */

    BitmapFactory.Options options;

    public BitmapDecoder(){
        this.options = new BitmapFactory.Options();
    }

    public void sampleBitmap(Bitmap originalBitmap, int reqWidth, int reqHeight){

    }

    public Bitmap decodeSampledBitmapFromFile
            (String pathName, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        //final BitmapFactory.Options options = new BitmapFactory.Options();
        this.options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, this.options);

        // Calculate inSampleSize
        this.options.inSampleSize = calculateInSampleSize(reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        this.options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, this.options);
    }

    private int calculateInSampleSize
            (int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = this.options.outHeight;
        final int width = this.options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
