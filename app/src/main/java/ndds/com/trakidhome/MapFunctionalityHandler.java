package ndds.com.trakidhome;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.security.PKCS12Attribute;
import java.util.ArrayList;
import java.util.HashMap;

public class MapFunctionalityHandler {
    private SQLIteLocationDataHandler data;
    private GoogleMap map;
    private int lightColor, normalColor, darkColor, errorColor;
    private int maximumTimeInterval;
    private Context context;
    private SpeedAnalysisReport speedAnalysisReport;

    public MapFunctionalityHandler(GoogleMap map, Context context) {
        this.map = map;
        data = new SQLIteLocationDataHandler(context);
        this.context = context;
        SharedPrefernceManager sharedPrefernceManager = new SharedPrefernceManager(context);
        speedAnalysisReport = new SpeedAnalysisReport(
                context,
                (float) sharedPrefernceManager.getValue(R.string.maxSpeedThreshold),
                Math.round((int) sharedPrefernceManager.getValue(R.string.minWaitingTime)),
                (Integer) sharedPrefernceManager.getValue(R.string.minFrequencyInHighSpeed)
        );
    }

    public void refreshMap(boolean isDisplayRoute) {
        map.clear();
        if (isDisplayRoute)
            addMapRoute();
        else
            addDotsInRoute();
    }

    public void adjustCamera() {
        SQLIteLocationDataHandler dataHandler = new SQLIteLocationDataHandler(context);
        dataHandler.getCursor().moveToLast();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(dataHandler.getCurrentCoordinate(), 19));
    }
    public MapFunctionalityHandler setColors(int light, int normal, int dark, int error) {
        lightColor = light;
        normalColor = normal;
        darkColor = dark;
        errorColor = error;
        return this;
    }

    public void addSingleCoordinate(boolean isDisplayRoute) {
        Cursor LI = data.getCursor();
        LI.moveToLast();
        int color = normalColor;
        if (data.getCurrentTimeStamp() - data.getPreviousTimestamp() > maximumTimeInterval)
            color = errorColor;
        if (isDisplayRoute) {
            LI.moveToPrevious();
            map.addPolyline(new PolylineOptions()
                    .color(color)
                    .add(data.getCurrentCoordinate(), data.getNextCoordinate()));
            return;
        }
        drawDotInMap(data.getCurrentCoordinate(), color);
    }

    public void showLegend(LinearLayout containerLayout) {
        LinearLayout layout = containerLayout;
        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("Long distance travel", R.color.appThemeLight);
        hashMap.put("Normal", R.color.appThemeNormal);
        hashMap.put("route missed coordinates", R.color.appThemeError);
        hashMap.put("circle - waited stop", R.color.appThemeDark);
        LayoutInflater inflater = LayoutInflater.from(context);
        Resources resources = context.getResources();
        ViewGroup group;
        for (String label : hashMap.keySet()) {
            group = (ViewGroup) inflater.inflate(R.layout.map_legend_view, null);
            ((TextView) group.getChildAt(0)).setText("\u2023 " + label);
            group.getChildAt(1).setBackgroundColor(resources.getColor(hashMap.get(label)));
            layout.addView(group);
        }


    }

    public void showStopsInMap() {
        for (LatLng position : speedAnalysisReport.stopCoordinates)
            map.addCircle(new CircleOptions()
                    .center(position)
                    .fillColor(darkColor)
                    .radius(3)
                    .strokeColor(normalColor));
    }

    public void showHighSpeedInMap() {
        ArrayList<Integer> highSpeedTimeStamps = speedAnalysisReport.highSpeedTimes;
        for (int i = 0; i < highSpeedTimeStamps.size(); i+=2) {
            map.addPolyline(new PolylineOptions().
                    addAll(data.getCoordinateSubList(
                            highSpeedTimeStamps.get(i), highSpeedTimeStamps.get(i + 1)
                    )).color(lightColor));
        }
    }

    public void drawDotInMap(LatLng origin, int color) {
        map.addCircle(new CircleOptions().radius(1)
                .center(origin)
                .fillColor(color)
                .strokeWidth(1)
                .strokeColor(Color.BLACK));
    }

    public void addDotsInRoute() {
        Cursor LI = data.getCursor();
        LI.moveToPosition(-1);
        int markColor = normalColor;
        LI.moveToFirst();
        drawDotInMap(data.getCurrentCoordinate(), markColor);
        while (LI.moveToNext()) {//starting from second iteration
            if (data.getCurrentTimeStamp() - data.getPreviousTimestamp() > maximumTimeInterval)
                markColor = errorColor;
            else
                markColor = normalColor;
            drawDotInMap(data.getCurrentCoordinate(), markColor);
        }
    }
    public void addMapRoute() {
        Cursor LI = data.getCursor();
        LI.moveToPosition(-1);
        boolean isError = false;
        PolylineOptions p = new PolylineOptions();
        p.color(normalColor);
        while (LI.moveToNext()) {
            //add coordinate to PolylineOptions
            p.add(data.getCurrentCoordinate());
            if (LI.isLast()) {
                map.addPolyline(p);
                break;
            }
            /*capture if the time gap between current coordinate
            and next coordinate exceed minimum time gap.If so then it is
            considered some of the coordinates missing between those given
            coordinates and should be marked with a different color.*/
            if (data.getNextTimestamp() - data.getCurrentTimeStamp() > maximumTimeInterval) {
                /*Identifying whether situation like this
                 * happen before with help of boolean variable isError
                 * if isError=false then this is the first occurrence of this kind
                 * of situation*/
                if (!isError) {
                    isError = true;
                    /*add the current PolylineOption to Google map
                     * with existing coordinates.This is to make sure the current existing
                     * coordinates are not lost when re-assigning the variable 'P'.Ignore if
                     * variable i=0 since no coordinates are added*/
                    if (!LI.isFirst())
                        map.addPolyline(p);
                    /*A polyLineOption with different color initialized here while
                     * add its first coordinate to PolylineOption*/
                    p=new PolylineOptions();
                    p.color(errorColor);
                    p.add(data.getCurrentCoordinate());
                }
            }
            /*if time gap between two consecutive coordinates is not greater
             * than maximumTimeInterval then the situation is normal.However
             * if the variable isError is true then it means the current PolylineOption
             * is indicate abnormal route and should be shifted to normal route with
             * normal color*/
            else if (isError) {
                isError = false;
                map.addPolyline(p);
                /*add the current PolylineOption to Google map and re-assign variable p
                 * to a new PolylineOption instance*/
                p=new PolylineOptions();
                p.color(normalColor);
                p.add(data.getCurrentCoordinate());
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

    public SpeedAnalysisReport getSpeedAnalysisReport() {
        return speedAnalysisReport;
    }
}
