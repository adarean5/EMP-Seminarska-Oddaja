package a.traveller.asd;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Jernej on 27. 12. 2017.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {


    public static class CardViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView journeyTitle;
        TextView journeyDate;
        ImageView journeyMainImage;
        TextView journeyDesc;

        ImageButton deleteCardButton;
        ImageButton editCardButton;

        CardViewHolder (final View itemView){
            super(itemView);

            cv = (CardView) itemView.findViewById(R.id.imageCardView);
            journeyTitle = (TextView) itemView.findViewById(R.id.journeyCardTitle);
            journeyDate = (TextView) itemView.findViewById(R.id.imageCardDate);
            journeyMainImage = (ImageView) itemView.findViewById(R.id.editCardCover);
            journeyDesc = (TextView) itemView.findViewById(R.id.imageCardDesc);

            deleteCardButton = (ImageButton) itemView.findViewById(R.id.imageCardDelete);
            editCardButton = (ImageButton) itemView.findViewById(R.id.imageCardEdit);

            /*cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(itemView.getContext(), SecondActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("imageFilePath", imageFilePaths.get(i));
                    bundle.putInt("postition", i);
                    intent.putExtras(bundle);
                    itemView.getContext().startActivity(intent);
                }
            });*/
    }
    }

    List<MainJourneyCard> mainJourneyCards;
    //BitmapDecoder decoder;

    RVAdapter(List<MainJourneyCard> mainJourneyCards){
        this.mainJourneyCards = mainJourneyCards;
        //this.decoder = new BitmapDecoder();
    }

    public void removeCard (int position){
        mainJourneyCards.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mainJourneyCards.size());
    }

    @Override
    public int getItemCount() {
        return mainJourneyCards.size();
    }

    @Override
    public RVAdapter.CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.journey_card_front, viewGroup, false);
        CardViewHolder cvh = new CardViewHolder(v);
        return cvh;
    }

    @Override
    public void onBindViewHolder(RVAdapter.CardViewHolder holder, final int position) {
        holder.journeyTitle.setText(mainJourneyCards.get(position).getTopText());

        //holder.journeyMainImage.setImageBitmap(BitmapFactory.decodeFile(mainJourneyCards.get(position).getImagePath()));
        /*
        Bitmap decodedBitmap = decoder.decodeSampledBitmapFromFile(mainJourneyCards.get(
                position).getImagePath(), 100, 100);
        holder.journeyMainImage.setImageBitmap(decodedBitmap);
        */
        /*
        Bitmap bitmap = BitmapFactory.decodeFile(mainJourneyCards.get(position).getImagePath());
        holder.journeyMainImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 120, 120, false));
        */
        //Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mainJourneyCards.get(position).getImagePath()), 500, 300);
        //holder.journeyMainImage.setImageBitmap(thumbImage);

        try {
            //Bitmap myBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
            String thumbPath =  new File(mainJourneyCards.get(position).getImagePath()).getName() + "_cover.jpg";
            InputStream input = holder.itemView.getContext().openFileInput(thumbPath);
            holder.journeyMainImage.setImageBitmap(BitmapFactory.decodeStream(input));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        holder.journeyDesc.setText(mainJourneyCards.get(position).getBotText());
        holder.journeyDate.setText(mainJourneyCards.get(position).getDateText());

        holder.deleteCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("press", "DELETE PRESSED" + mainJourneyCards.get(position));
                mainJourneyCards.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mainJourneyCards.size());
            }
        });

        holder.editCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("press", "EDIT PRESSED");
                Intent intent = new Intent();
                intent.setClass(view.getContext(), EditJourneyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("journeyTitle", mainJourneyCards.get(position).getTopText());
                bundle.putString("journeyCoverImagePath", mainJourneyCards.get(position).getImagePath());
                bundle.putString("journeyDescription", mainJourneyCards.get(position).getBotText());
                bundle.putString("dateText", mainJourneyCards.get(position).getDateText());
                bundle.putInt("position", position);
                //String parent = mainJourneyCards.get(position).getParentImageFolder();
                //bundle.putString("parentImageFolder", mainJourneyCards.get(position).getParentImageFolder());
                intent.putExtras(bundle);
                view.getContext().startActivity(intent);
            }
        });

         holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(view.getContext(), SecondActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("journeyTitle", mainJourneyCards.get(position).getTopText());
                    bundle.putString("journeyCoverImagePath", mainJourneyCards.get(position).getImagePath());
                    String parent = mainJourneyCards.get(position).getParentImageFolder();
                    bundle.putString("parentImageFolder", mainJourneyCards.get(position).getParentImageFolder());
                    intent.putExtras(bundle);
                    view.getContext().startActivity(intent);
                }
            });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
