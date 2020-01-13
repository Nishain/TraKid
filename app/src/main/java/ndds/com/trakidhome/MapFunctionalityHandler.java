package ndds.com.trakidhome;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapFunctionalityHandler {
    private final SQLIteDataHandler data;
    private GoogleMap map;
    private int lightColor, normalColor, darkColor, errorColor;
    private int maximumTimeInterval;

    public MapFunctionalityHandler(GoogleMap map, Context context) {
        this.map = map;
        data = new SQLIteDataHandler(context);
    }

    public MapFunctionalityHandler setColors(int light, int normal, int dark, int error) {
        lightColor = light;
        normalColor = normal;
        darkColor = dark;
        errorColor = error;
        return this;
    }


    public void showStopsInMap(ArrayList<LatLng> stopPositions){
        for (LatLng position:stopPositions)
            map.addCircle(new CircleOptions()
                    .center(position)
                    .fillColor(darkColor)
                    .radius(3)
                    .strokeColor(normalColor));
    }
    public void showHighSpeedInMap(ArrayList<Integer> highSpeedTimeStamps){
        for (int i = 0; i < highSpeedTimeStamps.size(); i+=2) {
            map.addPolyline(new PolylineOptions().
                    addAll(data.getCoordinateSubList(
                            highSpeedTimeStamps.get(i), highSpeedTimeStamps.get(i + 1)
                    )).color(lightColor));
        }
    }
    public void addMapRoute() {

        boolean isError = false;
        PolylineOptions p = new PolylineOptions();
        p.color(normalColor);
        for (int i = 0; i < data.getSize(); i++) {
            p.add(data.getCoordinates(i));
            if (i == (data.getSize() - 1)) {
                map.addPolyline(p);
                continue;
            }
            if (data.getTimeStamps(i + 1) - data.getTimeStamps(i) > maximumTimeInterval) {
                if (!isError) {
                    isError = true;
                    if(i!=0)
                        map.addPolyline(p);
                    p=new PolylineOptions();
                    p.color(errorColor);
                    p.add(data.getCoordinates(i));
                }
            } else if (isError) {
                isError = false;
                map.addPolyline(p);
                p=new PolylineOptions();
                p.color(normalColor);
                p.add(data.getCoordinates(i));
            }
            /*map.addPolyline(new PolylineOptions()
                    .color(timeStamps.get(i + 1) - timeStamps.get(i) > 1 ? Color.RED : Color.BLUE)
                    .add(coordinates.get(i))
                    .add(coordinates.get(i + 1)));*/
        }
    }

    public MapFunctionalityHandler setMaximumTimeInterval(int maximumTimeInterval) {
        this.maximumTimeInterval = maximumTimeInterval;
        return this;
    }
}
