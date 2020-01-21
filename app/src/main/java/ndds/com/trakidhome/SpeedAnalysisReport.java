package ndds.com.trakidhome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeedAnalysisReport {

    private final SQLIteLocationDataHandler data;
    private float highSpeedThreshold;
    private int maxWaitingTime;
    ArrayList<LatLng> stopCoordinates;
    ArrayList<Integer> highSpeedTimes;
    private int maximumTimesInHighSpeed;
    private Context context;

    public SpeedAnalysisReport(Context context, float highSpeedThreshold, int maxWaitingTime, int maximumTimesInHighSpeed) {
        data = new SQLIteLocationDataHandler(context);
        this.highSpeedThreshold = highSpeedThreshold;
        this.maxWaitingTime = maxWaitingTime;
        this.maximumTimesInHighSpeed = maximumTimesInHighSpeed;
        this.context = context;
    }
    public static float distanceMoved(LatLng a, LatLng b){
        float[] result = new float[1];
        Location.distanceBetween(a.latitude,a.longitude,b.latitude,b.longitude,result);
        return result[0]; //distance in meters
    }


    @SuppressLint("DefaultLocale")
    public String makeReport() {
        NetworkInfo ni = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Toast.makeText(context, "No internet connection to display about places", Toast.LENGTH_LONG).show();
        }
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

        Cursor LI = data.getCursor();
        LI.moveToPosition(-1);
        LatLng currentCoordinate, nextCoordinate;
        int currentTimestamp, nextTimestamp;
        while (LI.moveToNext()) {
            currentCoordinate = data.getCurrentCoordinate();
            currentTimestamp = data.getCurrentTimeStamp();
            if (!LI.isLast()) {
                nextCoordinate = data.getNextCoordinate();
                nextTimestamp = data.getNextTimestamp();
                speed = distanceMoved(currentCoordinate, nextCoordinate) / (nextTimestamp - currentTimestamp);
                if (a = speed >= highSpeedThreshold) {
                    if (timesInHighSpeed == 0) {
                        startPosition = currentCoordinate;
                        startTime = currentTimestamp;
                        timestampIndex = LI.getPosition();
                    }
                    timesInHighSpeed++;
                }
                if(!isWaiting) {
                    stopCoordinate = currentCoordinate;
                    startWaitTime = currentTimestamp;
                }
                if (b = distanceMoved(stopCoordinate, nextCoordinate) < 10) {
                    isWaiting=true;
                }
            }else
                a=b=false;
            if (!a && timesInHighSpeed >= maximumTimesInHighSpeed) {
                highSpeedTimes.add(timestampIndex);
                highSpeedTimes.add(LI.getPosition());
                String placeName = getLocationAddress(currentCoordinate, geocoder);
                report += String.format("\u2022 At time %d user move away%s by %.2f m until time %d\n",
                        startTime, placeName != null ? " to " + placeName : "", distanceMoved(startPosition, currentCoordinate), currentTimestamp);
            }
            if (!a)
                timesInHighSpeed = 0;

            if (!b && isWaiting && (currentTimestamp - startWaitTime) >= maxWaitingTime) {
                String placeName = getLocationAddress(currentCoordinate, geocoder);
                report += String.format("\u2022 at time %d user waited%s %d min\n", startWaitTime, placeName != null ? " on " + placeName + " for" : "", currentTimestamp - startWaitTime);
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
                ad.setCountryName(null);
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
