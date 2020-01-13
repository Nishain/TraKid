package ndds.com.trakidhome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeedAnalysisReport {

    private final SQLIteDataHandler data;
    private float highSpeedThereshold;
    private int maxWaitingTime;
    ArrayList<LatLng> stopCoordinates;
    ArrayList<Integer> highSpeedTimes;

    public SpeedAnalysisReport(Context context, float highSpeedThereshold, int maxWaitingTime) {
        data = new SQLIteDataHandler(context);
        this.highSpeedThereshold = highSpeedThereshold;
        this.maxWaitingTime = maxWaitingTime;
    }
    public static float distanceMoved(LatLng a, LatLng b){
        float[] result = new float[1];
        Location.distanceBetween(a.latitude,a.longitude,b.latitude,b.longitude,result);
        return result[0]; //distance in meters
    }
    @SuppressLint("DefaultLocale")

    public String makeReport(Context context) {
        highSpeedTimes = new ArrayList<>();
        stopCoordinates = new ArrayList<>();
        Geocoder geocoder=new Geocoder(context,Locale.getDefault());
        //fields for speed report
        float speed;
        int timesInHighSpeed = 0;
        int startTime = 0;
        LatLng startPosition = null;
        boolean a;
        //fields for stop report
        boolean isWaiting=false;
        int startWaitTime = 0;
        int timestampIndex = 0;
        LatLng stopCoordinate = null;
        String report = "";
        boolean b;
        for (int i = 0; i < data.getSize(); i++) {
            if (i != data.getSize() - 1) {
                speed = distanceMoved(data.getCoordinates(i), data.getCoordinates(i + 1)) / (data.getTimeStamps(i + 1) - data.getTimeStamps(i));
                if (a = speed > highSpeedThereshold) {
                    if (timesInHighSpeed == 0) {
                        startPosition = data.getCoordinates(i);
                        startTime = data.getTimeStamps(i);
                        timestampIndex = i;
                    }
                    timesInHighSpeed++;
                }
                if(!isWaiting) {
                    stopCoordinate = data.getCoordinates(i);
                    startWaitTime = data.getTimeStamps(i);
                }
                if (b = distanceMoved(stopCoordinate, data.getCoordinates(i + 1)) < 10) {
                    isWaiting=true;
                }
            }else
                a=b=false;
            if (!a && timesInHighSpeed > 1) {
                highSpeedTimes.add(timestampIndex);
                highSpeedTimes.add(i);
                String placeName = getLocationAddress(data.getCoordinates(i), geocoder);
                report += String.format("\u2022 At time %d user move away%s by %.2f m until time %d\n",
                        startTime, placeName != null ? " to " + placeName : "", distanceMoved(startPosition, data.getCoordinates(i)), data.getTimeStamps(i));
            }
            if (!a)
                timesInHighSpeed = 0;

            if (!b && isWaiting && (data.getTimeStamps(i) - startWaitTime) >= maxWaitingTime) {
                String placeName = getLocationAddress(data.getCoordinates(i), geocoder);
                report += String.format("\u2022 at time %d user waited%s %d min\n", startWaitTime, placeName != null ? " on " + placeName + " for" : "", data.getTimeStamps(i) - startWaitTime);
                stopCoordinates.add(stopCoordinate);
            }
            if (!b)
                isWaiting = false;
        }
        return report;
    }
    private String getLocationAddress(LatLng position, Geocoder geocoder){
        String placeName;
        try {
            List<Address> addresses=geocoder.getFromLocation(position.latitude,position.longitude,1);
            if(addresses.size()>0) {
                Address ad =addresses.get(0);
                ad.setCountryName("idk");
                placeName="";
                for (int j = 0; j < ad.getMaxAddressLineIndex()+1; j++) {
                    placeName=TextUtils.join(" ",new String[]{placeName,ad.getAddressLine(j)});
                }
                }else
                placeName = null;
        } catch (IOException e) {
            placeName = null;
        }
        return placeName;
    }
}
