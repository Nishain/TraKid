package ndds.com.trakidhome;

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.core.graphics.ColorUtils;

public class MapFunctionalityHandler {
    private SQLIteLocationDataHandler data;
    private GoogleMap map;
    private int lightColor, normalColor, darkColor;
    private int maximumWaitTime, maxDistanceInHighSpeed;
    private float maximumSpeedTolerance;
    private Context context;
    private float negletableDistanceThreshold = 3;
    private String paircode;
    private Marker addedMarker;

    public MapFunctionalityHandler(GoogleMap map, Context context, String paircode) {
        this.map = map;
        data = new SQLIteLocationDataHandler(context);
        this.context = context;
        this.paircode = paircode;
        setColors();
        //(Integer) sharedPrefernceManager.getValue(R.string.minFrequencyInHighSpeed);
    }

    private void setConstantParams() {
        SharedPrefernceManager sharedPrefernceManager = new SharedPrefernceManager(context);
        maximumSpeedTolerance = (float) sharedPrefernceManager.getValue(R.string.maxSpeedThreshold);
        maximumWaitTime = (int) sharedPrefernceManager.getValue(R.string.maxWaitingTime);
        maxDistanceInHighSpeed = (int) sharedPrefernceManager.getValue(R.string.maxDistanceInHighSpeed);
    }

    public MapFunctionalityHandler setColors() {
        lightColor = context.getResources().getColor(R.color.appThemeLight);
        normalColor = context.getResources().getColor(R.color.appThemeNormal);
        darkColor = context.getResources().getColor(R.color.appThemeDark);
        return this;
    }

    public void clearPreviousCoordinate() {
        data.deletePaircode(paircode);
        map.clear();
    }
    public void addSingleCoordinate() {
        Cursor LI = data.getCursor(paircode);
        if (LI.getCount() > 1) {
            LI.moveToLast();
            map.addPolyline(new PolylineOptions().add(
                    data.getPreviousCoordinate(),
                    data.getCurrentCoordinate()
            ).color(darkColor));
            if (addedMarker != null)
                addedMarker.setVisible(true);
            addedMarker = drawNormalMarker(data.getCurrentCoordinate(), data.getCurrentTimeStamp());
            addedMarker.setVisible(false);
        }


    }

    public static float distanceMoved(LatLng a, LatLng b) {
        float[] result = new float[1];
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result);
        return result[0]; //distance in meters
    }

    private String getLocationAddress(LatLng position, Geocoder geocoder) {
        String placeName = null;
        try {
            Address address;
            List<Address> addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1);
            if (addresses.size() > 0 && addresses.get(0).getMaxAddressLineIndex() > -1) {
                address = addresses.get(0);
                placeName = address.getAddressLine(0);
            }

        } catch (IOException e) {
            placeName = null;
        }
        return placeName;
    }


    public ArrayList<String> displayCoordinatesInMap() {
        ArrayList<String> report = new ArrayList<>();
        LatLng currentCoordinate, nextCoordinate = null;
        int currentTimestamp, nextTimestamp;
        boolean a, b, isBeforeLast, isWaiting = false;
        float speed = 0;
        LatLng firstWaitingCoordinate = null, firstCoordinateInHighSpeed = null;
        int firstTimestampMovingHighSpeed = 0, firstTimestampOfWaiting = 0;
        int positionIndexOfHighSpeed = 0;
        int highSpeedFrequency = 0;
        int distance = 0;
        Marker waitedMarker = null;
        ArrayList<MarkerOptions> pendingCoordinates = new ArrayList<>();
        Geocoder geocoder = new Geocoder(context);

        setConstantParams();
        Cursor LI = data.getCursor(paircode);
        PolylineOptions normalRoute = new PolylineOptions();
        normalRoute.color(darkColor);
        while (LI.moveToNext())
            normalRoute.add(data.getCurrentCoordinate());
        map.addPolyline(normalRoute);

        LI.moveToPosition(-1);
        while (LI.moveToNext()) {
            isBeforeLast = !LI.isLast();
            currentCoordinate = data.getCurrentCoordinate();
            currentTimestamp = data.getCurrentTimeStamp();

            if (isBeforeLast) {
                nextCoordinate = data.getNextCoordinate();
                nextTimestamp = data.getNextTimestamp();
                speed = distanceMoved(currentCoordinate, nextCoordinate) / (nextTimestamp - currentTimestamp);
            }
            a = isBeforeLast && (speed >= maximumSpeedTolerance);
            if (a) {
                if (highSpeedFrequency == 0) {
                    firstCoordinateInHighSpeed = currentCoordinate;
                    firstTimestampMovingHighSpeed = currentTimestamp;
                    positionIndexOfHighSpeed = LI.getPosition();
                }
                highSpeedFrequency++;
                distance += distanceMoved(currentCoordinate, nextCoordinate);
            }

            if (!isWaiting) {
                firstWaitingCoordinate = currentCoordinate;
                firstTimestampOfWaiting = currentTimestamp;
                if (isBeforeLast)
                    waitedMarker = drawNormalMarker(currentCoordinate, currentTimestamp);
                if (pendingCoordinates.size() > 0) {
                    for (MarkerOptions pendingCoordinate : pendingCoordinates) {
                        map.addMarker(pendingCoordinate);
                    }
                }
            } else {
                pendingCoordinates.add(
                        createMarker(currentCoordinate, currentTimestamp)
                );
            }
            b = isBeforeLast && distanceMoved(firstWaitingCoordinate, nextCoordinate) < negletableDistanceThreshold;
            if (b)
                isWaiting = true;
            if (!a && highSpeedFrequency > 0 && distance > maxDistanceInHighSpeed) {
                map.addPolyline(new PolylineOptions().color(lightColor)
                        .addAll(data.getCoordinateSubList(positionIndexOfHighSpeed, LI.getPosition())));
                String location = getLocationAddress(currentCoordinate, geocoder);
                report.add(String.format(
                        Locale.getDefault(),
                        "At %s user moves away by air distance of %.1f m for %s until %s%s",
                        timestampInReadableText(firstTimestampMovingHighSpeed, false),
                        distanceMoved(firstCoordinateInHighSpeed, currentCoordinate),
                        timestampInReadableText(currentTimestamp - firstTimestampMovingHighSpeed, true),
                        timestampInReadableText(currentTimestamp, false),
                        location == null ? "" : "to " + location + " "
                ));
            }
            if (!a) {
                highSpeedFrequency = 0;
                distance = 0;
            }
            if (!b && isWaiting && (currentTimestamp - firstTimestampOfWaiting) >= maximumWaitTime) {
                pendingCoordinates.clear();
                String location = getLocationAddress(firstWaitingCoordinate, geocoder);
                report.add(String.format(
                        "At %s user waited for %s until %s %s",
                        timestampInReadableText(firstTimestampOfWaiting, false),
                        timestampInReadableText(currentTimestamp - firstTimestampOfWaiting, true),
                        timestampInReadableText(currentTimestamp, false),
                        location == null ? "" : ("at " + location + " ")
                ));
                drawWaitCircle(firstWaitingCoordinate, waitedMarker, currentTimestamp - firstTimestampOfWaiting);
                firstWaitingCoordinate = null;
            }
            if (!b)
                isWaiting = false;
        }
        return report;
    }

    public void drawWaitCircle(LatLng position, Marker marker, int waitedTime) {
        map.addCircle(new CircleOptions()
                .fillColor(ColorUtils.setAlphaComponent(normalColor, 100))
                .radius(negletableDistanceThreshold)
                .center(position)
                .strokeWidth(1)
                .strokeColor(normalColor)
        );
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        marker.setTitle(marker.getTitle());
        marker.setSnippet("waited for " + timestampInReadableText(waitedTime, true));
    }

    private Marker drawNormalMarker(LatLng position, int timestamp) {
        return map.addMarker(new MarkerOptions()
                .title("Time: " + timestampInReadableText(timestamp, false))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .position(position));
    }

    private MarkerOptions createMarker(LatLng position, int timestamp) {
        return new MarkerOptions()
                .title(timestampInReadableText(timestamp, false))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .position(position);
    }
    private String timestampInReadableText(int timestamp, boolean isShowingTimeGap) {
        int mins = (timestamp % (60 * 60)) / 60;
        int hours = timestamp / (60 * 60);
        int seconds = timestamp % 60;
        String timeGap = "";
        if (isShowingTimeGap) {
            if (hours > 0)
                timeGap += hours + " h ";
            if (mins > 0)
                timeGap += mins + " min ";
            if (seconds > 0)
                timeGap += seconds + " sec";
            return timeGap.trim();
        }
        else {

            if (hours > 12)
                hours -= 12;
            String sessionPrefix = (timestamp / (60 * 60)) > 12 ? "PM" : "AM";
            return hours + ":" + mins + " " + sessionPrefix;
        }

    }


}
