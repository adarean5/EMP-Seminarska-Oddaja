package a.traveller.asd;

/**
 * Created by smrki on 4. 01. 2018.
 */

import java.util.List;
import a.traveller.asd.Route;
/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
