package a.traveller.asd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jernej on 4. 01. 2018.
 */

public class TimelineRecyclerAdapter extends RecyclerView.Adapter<TimelineRecyclerAdapter.TimelineCardViewHolder>{

    public static class TimelineCardViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        CardView imageCardView;
        TextView journeyDate;
        ImageView journeyThumbImage;
        TextView journeyDesc;
        TextView journeyLocation;

        ImageButton deleteCardButton;
        ImageButton editCardButton;

        TimelineCardViewHolder (final View itemView){
            super(itemView);

            imageCardView = (CardView) itemView.findViewById(R.id.imageCardView);
            journeyDate = (TextView) itemView.findViewById(R.id.imageCardDate);
            journeyThumbImage = (ImageView) itemView.findViewById(R.id.includeImageGridItem);
            journeyDesc = (TextView) itemView.findViewById(R.id.imageCardDesc);
            journeyLocation = (TextView) itemView.findViewById(R.id.imageCardLocation);

            deleteCardButton = (ImageButton) itemView.findViewById(R.id.imageCardDelete);
            editCardButton = (ImageButton) itemView.findViewById(R.id.imageCardEdit);
        }

        @Override
        public void onClick(View view) {
            if (view == journeyDesc){

            }
        }
    }

    List<String> imageFilePaths;
    android.support.media.ExifInterface exifInterface;
    private String date;
    private String description;
    private int pos;
    //BitmapDecoder decoder;
    //private boolean locationDiv = true;

    TimelineRecyclerAdapter(List<String> imageFilePaths){
        this.imageFilePaths = imageFilePaths;
        //Collections.reverse(imageFilePaths);
        //imageFilePaths.add(0, "locationDiv");
        this.pos = 0;
        //this.decoder = new BitmapDecoder();
    }

    public void removeCard (int position){
        imageFilePaths.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, imageFilePaths.size());
    }

    @Override
    public int getItemCount() {
        return imageFilePaths.size();
    }

    @Override
    public TimelineRecyclerAdapter.TimelineCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View locationDivider = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.location_card, viewGroup, false);
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_card, viewGroup, false);
        TimelineCardViewHolder tlcvh;
        if (imageFilePaths.get(pos) == "locationDiv"){
            tlcvh = new TimelineCardViewHolder(locationDivider);
        }else
            tlcvh = new TimelineCardViewHolder(v);
        return tlcvh;
    }

    @Override
    public void onBindViewHolder(final TimelineRecyclerAdapter.TimelineCardViewHolder holder, final int position) {
        String date = "Not available";
        String desc = "Click here to edit description";
        String loc = "Not available";

        if (imageFilePaths.get(pos) != "locationDiv"){
            //pos = position+1;
            try {
                exifInterface = new android.support.media.ExifInterface(this.imageFilePaths.get(position));
                date = exifInterface.getAttribute(android.support.media.ExifInterface.TAG_DATETIME);
                //Log.e("asd", exifInterface.getAttribute("UserComment"));
                desc = exifInterface.getAttribute(android.support.media.ExifInterface.TAG_IMAGE_DESCRIPTION);
                loc = exifInterface.getAttribute(android.support.media.ExifInterface.TAG_GPS_AREA_INFORMATION);
            } catch (IOException e) {
                e.printStackTrace();
            }
            holder.journeyDesc.setText(desc);
            holder.journeyDate.setText(date);
            holder.journeyLocation.setText(loc);

            try {
                String thumbPath =  new File(this.imageFilePaths.get(position)).getName() + "_thumb.jpg";
                InputStream input = holder.itemView.getContext().openFileInput(thumbPath);
                holder.journeyThumbImage.setImageBitmap(BitmapFactory.decodeStream(input));
            } catch (IOException e) {
                e.printStackTrace();
            }

            holder.journeyDesc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(view.getContext());
                    LayoutInflater inflater = (LayoutInflater) view.getContext()
                            .getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    View mView = inflater.inflate(R.layout.edit_description_popup,null);
                    final EditText editText = mView.findViewById(R.id.editAddNote);

                    mBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                ExifInterface exifInterface = new ExifInterface(imageFilePaths.get(position));
                                exifInterface.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION,
                                        editText.getText().toString());
                                exifInterface.saveAttributes();
                                holder.journeyDesc.setText(editText.getText().toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dialogInterface.dismiss();
                        }
                    });

                    mBuilder.setView(mView);
                    final AlertDialog dialog = mBuilder.create();
                    dialog.show();
                }
            });

            holder.journeyThumbImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Log.v("c", "ON ITEM CLICK " + position);
                        String filePath = imageFilePaths.get(position);
                        ExifInterface exifInterface = new ExifInterface(filePath);
                        Intent intent = new Intent();
                        intent.setClass(view.getContext(), FullScreenImage.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("imageFilePath", filePath);
                        bundle.putString("imageDesc", exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
                        bundle.putString("imageDate", exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
                        bundle.putInt("postition", position);
                        intent.putExtras(bundle);
                        view.getContext().startActivity(intent);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void refreshAdapter(List<String> newImageFilePaths) {
        this.imageFilePaths.clear();
        this.imageFilePaths.addAll(newImageFilePaths);
        notifyDataSetChanged();
    }

    private void displayFullScreenImage(int i) throws IOException {

    }
}
