package ndds.com.trakidhome;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

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

    public void deletePaircode(String pairCode) {
        db.execSQL(String.format("DELETE FROM %s where %s='%s';",
                trackingInfoTableName, PairCodeColunm.split(" ")[0], pairCode));
    }

    public Cursor getCursor(String pairCode) {
        locationInfoCursor = db.rawQuery(String.format("select %s,%s,%s from %s where %s='%s'",
                LatitudeColumn.split(" ")[0],
                LongitudeColumn.split(" ")[0],
                TimestampColumn.split(" ")[0],
                trackingInfoTableName,
                PairCodeColunm.split(" ")[0],
                pairCode
        ), null);
        return locationInfoCursor;
    }


    public ArrayList<LatLng> getCoordinateSubList(int start, int end) {
        ArrayList<LatLng> a = new ArrayList<>();
        int initialPosition = locationInfoCursor.getPosition();
        locationInfoCursor.moveToPosition(start);
        int count = start;
        while (count <= end) {
            a.add(new LatLng(locationInfoCursor.getDouble(0), locationInfoCursor.getDouble(1)));
            locationInfoCursor.moveToNext();
            ++count;
        }
        locationInfoCursor.moveToPosition(initialPosition);
        return a;
    }

    public void truncateTable() {
        db.execSQL("DELETE FROM " + trackingInfoTableName + ";");
    }

    public boolean insertNewCoordinateInfo(double Lat, double Long, int timestamp, String paircode) {
        getCursor(paircode);
        if (locationInfoCursor.getCount() > 0) {
            int position = locationInfoCursor.getPosition();
            locationInfoCursor.moveToLast();
            if (locationInfoCursor.getDouble(0) == Lat
                    && locationInfoCursor.getDouble(1) == Long) {
                locationInfoCursor.moveToPosition(position);
                return false;//avoid duplicate coordinate inserting to database.
            }
            locationInfoCursor.moveToPosition(position);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(LatitudeColumn.split(" ")[0], Lat);
        contentValues.put(LongitudeColumn.split(" ")[0], Long);
        contentValues.put(TimestampColumn.split(" ")[0], timestamp);
        contentValues.put(PairCodeColunm.split(" ")[0], paircode);
        db.insert(trackingInfoTableName, null, contentValues);
        return true;
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

    public LatLng getPreviousCoordinate() {
        locationInfoCursor.moveToPrevious();
        LatLng previousLocation = new LatLng(locationInfoCursor.getDouble(0), locationInfoCursor.getDouble(1));
        locationInfoCursor.moveToNext();
        return previousLocation;
    }
}
