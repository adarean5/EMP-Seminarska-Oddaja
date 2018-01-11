package a.traveller.asd;

import android.location.Location;
import android.util.Log;

/**
 * Created by Jernej on 7. 01. 2018.
 */

public class Globals {
    private static Globals instance;

    // Global variable
    private Location data;
    private int test;

    // Restrict the constructor from being instantiated
    private Globals(){}

    public void setData(Location d){
        Log.e("Data SET", "DATA SET");
        this.data=d;
    }
    public Location getData(){
        return this.data;
    }

    public void setTest(int test) {
        this.test = test;
    }

    public int getTest() {
        return test;
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }
}
