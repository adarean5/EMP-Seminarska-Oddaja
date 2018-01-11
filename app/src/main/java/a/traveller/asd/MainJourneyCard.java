package a.traveller.asd;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainJourneyCard implements Parcelable, Serializable{
    //private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM, YYYY");

    private String imagePath;
    private String topText;
    private String botText;
    private String dateText;
    private String parentImageFolder;

    public MainJourneyCard(String imagePath, String topText, String botText, String dateText, String parentImageFolder){
        //this.imageBitmap = imageBitmap;
        this.imagePath = imagePath;
        this.topText = topText;
        this.botText = botText;
        /*String currentDate = DATE_FORMAT.format(new Date());
        this.dateText = currentDate.toString();
        this.parentImageFolder = this.topText + "_"; //+ this.dateText;*/
        this.dateText = dateText;
        this.parentImageFolder = parentImageFolder;
    }

    private MainJourneyCard(Parcel in){
        imagePath = in.readString();
        topText = in.readString();
        botText = in.readString();
        dateText = in.readString();
        parentImageFolder = in.readString();
    }

    public String getBotText() {
        return botText;
    }

    public String getTopText() {
        return topText;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDateText() {
        return dateText;
    }

    public String getParentImageFolder() {
        return parentImageFolder;
    }

    public void setTopText(String topText) {
        this.topText = topText;
    }

    public void setBotText(String botText) {
        this.botText = botText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imagePath);
        parcel.writeString(topText);
        parcel.writeString(botText);
    }

    public static final Parcelable.Creator<MainJourneyCard> CREATOR = new Parcelable.Creator<MainJourneyCard>() {
        public MainJourneyCard createFromParcel(Parcel in) {
            return new MainJourneyCard(in);
        }

        public MainJourneyCard[] newArray(int size) {
            return new MainJourneyCard[size];
        }
    };
}
