package ndds.com.trakidhome;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class SQLIteDataHandler implements SQLiteDbStructure {
    private final SQLiteDatabase db;
    private Cursor locationInfoCursor;
    private int size;

    SQLIteDataHandler(Context context) {
        db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        String createTableQry = String.format("create table if not EXISTS %s (%s);", trackingInfoTableName, TextUtils.join(",", coloumns));
        db.execSQL(createTableQry);
        locationInfoCursor = db.rawQuery(String.format("select %s,%s,%s from %s",
                LatitudeColumn.split(" ")[0],
                LongitudeColumn.split(" ")[0],
                TimestampColumn.split(" ")[0],
                trackingInfoTableName), null);
        size = locationInfoCursor.getCount();
    }

    public LatLng getCoordinates(int index) {
        locationInfoCursor.moveToPosition(index);
        return new LatLng(locationInfoCursor.getDouble(0), locationInfoCursor.getDouble(1));
    }

    public int getTimeStamps(int index) {
        locationInfoCursor.moveToPosition(index);
        return locationInfoCursor.getInt(2);
    }

    public int getSize() {
        return size;
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
        size++;
    }

    @Override
    protected void finalize() throws Throwable {
        db.close();
    }
}
