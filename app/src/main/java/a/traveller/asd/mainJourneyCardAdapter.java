package a.traveller.asd;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class mainJourneyCardAdapter extends ArrayAdapter<MainJourneyCard>{
    private Context context;
    private List<MainJourneyCard> journeyCards;

    public mainJourneyCardAdapter(Context context, int resource, ArrayList<MainJourneyCard> objects){
        super(context, resource, objects);

        this.context = context;
        this.journeyCards = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        MainJourneyCard mainJourneyCard = journeyCards.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater .inflate(R.layout.journey_card_main, null);

        ImageView journeyImage = (ImageView) view.findViewById(R.id.imageMainCard);
        TextView topText = (TextView) view.findViewById(R.id.mainFirstLine);
        TextView botText = (TextView) view.findViewById(R.id.mainSecondLine);

        journeyImage.setImageBitmap(BitmapFactory.decodeFile(mainJourneyCard.getImagePath()));

        topText.setText(mainJourneyCard.getTopText());
        botText.setText(mainJourneyCard.getBotText());

        return view;
    }

    public void refreshAdapter(List<MainJourneyCard> journeyCards) {
        this.journeyCards.clear();
        this.journeyCards.addAll(journeyCards);
        notifyDataSetChanged();
    }
}
