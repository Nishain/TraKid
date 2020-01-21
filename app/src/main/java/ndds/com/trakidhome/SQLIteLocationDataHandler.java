package ndds.com.trakidhome;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class SQLIteLocationDataHandler implements SQLiteDbStructure {
    private final SQLiteDatabase db;
    private Cursor locationInfoCursor;

    SQLIteLocationDataHandler(Context context) {
        db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        String createTableQry = String.format("create table if not EXISTS %s (%s);", trackingInfoTableName, TextUtils.join(",", TrackingTableColumns));
        db.execSQL(createTableQry);
    }

    public LatLng getCurrentCoordinate() {
        return new LatLng(locationInfoCursor.getDouble(0), locationInfoCursor.getDouble(1));
    }

    public int getCurrentTimeStamp() {
        return locationInfoCursor.getInt(2);
    }

    public LatLng getNextCoordinate() {
        locationInfoCursor.moveToNext();
        LatLng nextCoorinate = new LatLng(locationInfoCursor.getDouble(0), locationInfoCursor.getDouble(1));
        locationInfoCursor.moveToPrevious();
        return nextCoorinate;
    }

    public int getNextTimestamp() {
        locationInfoCursor.moveToNext();
        int nextTimestamp = locationInfoCursor.getInt(2);
        locationInfoCursor.moveToPrevious();
        return nextTimestamp;
    }

    public Cursor getCursor() {
        locationInfoCursor = db.rawQuery(String.format("select %s,%s,%s from %s",
                LatitudeColumn.split(" ")[0],
                LongitudeColumn.split(" ")[0],
                TimestampColumn.split(" ")[0],
                trackingInfoTableName), null);
        return locationInfoCursor;
    }


    public ArrayList<LatLng> getCoordinateSubList(int start, int end) {
        ArrayList<LatLng> a = new ArrayList<>();
        locationInfoCursor.moveToPosition(start);
        int count = start;
        while (count <= end) {
            a.add(new LatLng(locationInfoCursor.getDouble(0), locationInfoCursor.getDouble(1)));
            locationInfoCursor.moveToNext();
            ++count;
        }
        return a;
    }

    public void truncateTable() {
        db.execSQL("DELETE FROM " + trackingInfoTableName + ";");
    }

    public void insertNewCoordinateInfo(double Lat, double Long, int timestamp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LatitudeColumn.split(" ")[0], Lat);
        contentValues.put(LongitudeColumn.split(" ")[0], Long);
        contentValues.put(TimestampColumn.split(" ")[0], timestamp);
        db.insert(trackingInfoTableName, null, contentValues);
    }

    @Override
    protected void finalize() throws Throwable {
        db.close();
    }

    public int getPreviousTimestamp() {
        locationInfoCursor.moveToPrevious();
        int previousTimestamp = locationInfoCursor.getInt(2);
        locationInfoCursor.moveToNext();
        return previousTimestamp;
    }
}
