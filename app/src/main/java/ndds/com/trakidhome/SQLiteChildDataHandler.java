package ndds.com.trakidhome;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class SQLiteChildDataHandler implements SQLiteDbStructure {
    private final SQLiteDatabase db;

    SQLiteChildDataHandler(Context context) {
        db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        String createTableQry = String.format("create table if not EXISTS %s (%s);", childInfoTableName, TextUtils.join(",", ChildInfoTableColumns));
        db.execSQL(createTableQry);
    }

    public Cursor getCursor() {
        return db.rawQuery(String.format("select %s,%s,%s from %s",
                ChildIDColumn.split(" ")[0],
                ChildFirstNameColumn.split(" ")[0],
                TrackingDevicePhoneNumber.split(" ")[0],
                childInfoTableName), null);
    }

    public void insertNewChild(int ID, String firstName, String phonenumber) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChildIDColumn.split(" ")[0], ID);
        contentValues.put(ChildFirstNameColumn.split(" ")[0], firstName);
        contentValues.put(TrackingDevicePhoneNumber.split(" ")[0], phonenumber);
        db.insert(childInfoTableName, null, contentValues);
    }

    public void truncateTable() {
        db.execSQL("DELETE FROM " + childInfoTableName + ";");
    }
}
