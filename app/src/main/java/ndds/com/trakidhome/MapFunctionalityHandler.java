package ndds.com.trakidhome;

import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapFunctionalityHandler {
    private GoogleMap map;
    private ArrayList<Integer> timeStamps;
    private ArrayList<LatLng> coordinates;
    private int lightColor,normalColor,darkColor;
    public MapFunctionalityHandler(GoogleMap map, ArrayList<LatLng> coordinates, ArrayList<Integer> timeStamps) {
        this.map = map;
        this.coordinates=coordinates;
        this.timeStamps=timeStamps;

    }

    /*public void addSingleCoordinate(double lat, double lng) {
        route.add(new LatLng(lat, lng));
    }*/

    public void showStopsInMap(ArrayList<LatLng> stopPositions){
        for (LatLng position:stopPositions)
            map.addCircle(new CircleOptions().center(position).fillColor(Color.RED).radius(3));
    }
    public void showHighSpeedInMap(ArrayList<Integer> highSpeedTimeStamps){
        for (int i = 0; i < highSpeedTimeStamps.size(); i+=2) {
            map.addPolyline(new PolylineOptions().
            addAll(coordinates.subList(
                    highSpeedTimeStamps.get(i),highSpeedTimeStamps.get(i+1)+1
            )).color(Color.GREEN));
        }
    }
    public void addMapRoute() {
        //6.838786, 79.964810
        //6.838634, 79.964306
        //6.839135, 79.964813

        boolean isRed=false;
        PolylineOptions p = new PolylineOptions();
        p.color(Color.BLUE);
        for (int i = 0; i < timeStamps.size(); i++) {
            /*map.addMarker(new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)).
                    position(coordinates.get(i)).
                    flat(true).
                    title(timeStamps.get(i).toString())
            );*/
            p.add(coordinates.get(i));
            if(i==(timeStamps.size()-1)){
                map.addPolyline(p);
                continue;
            }
            if(timeStamps.get(i + 1) - timeStamps.get(i) > 1){
                if(!isRed){
                    isRed = true;
                    if(i!=0)
                        map.addPolyline(p);
                    p=new PolylineOptions();
                    p.color(Color.RED);
                    p.add(coordinates.get(i));
                }
            }else if(isRed){
                isRed = false;
                map.addPolyline(p);
                p=new PolylineOptions();
                p.color(Color.BLUE);
                p.add(coordinates.get(i));
            }
            /*map.addPolyline(new PolylineOptions()
                    .color(timeStamps.get(i + 1) - timeStamps.get(i) > 1 ? Color.RED : Color.BLUE)
                    .add(coordinates.get(i))
                    .add(coordinates.get(i + 1)));*/
        }
    }
}
