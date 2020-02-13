package ndds.com.trakidhome;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class SQLiteChildDataHandler implements SQLiteDbStructure {
    private final SQLiteDatabase db;

    SQLiteChildDataHandler(Context context) {
        db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
        String createTableQry = String.format("create table if not EXISTS %s (%s);", childInfoTableName, TextUtils.join(",", ChildInfoTableColumns));
        db.execSQL(createTableQry);
    }

    public String getTitleFromPairCode(String paircode) {
        Cursor c = getCursor();
        while (c.moveToNext()) {
            if (c.getString(0).equals(paircode))
                return c.getString(1);
        }
        return null;
    }
    public Cursor getCursor() {
        return db.rawQuery(String.format("select %s,%s from %s",
                ChildIDColumn.split(" ")[0],
                ChildFirstNameColumn.split(" ")[0],
                childInfoTableName), null);
    }

    public void deletePaircode(String pairCode) {
        db.execSQL(String.format("DELETE FROM %s where %s='%s';",
                childInfoTableName, ChildIDColumn.split(" ")[0], pairCode));
    }

    public int getPositionOfPairCode(String pairCode) {
        int position = 0;
        Cursor c = getCursor();
        while (c.moveToNext()) {
            if (c.getString(0).equals(pairCode)) {
                return position;
            }
            ++position;
        }
        return -1;
    }

    public void insertNewChild(String ID, String firstName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChildIDColumn.split(" ")[0], ID);
        contentValues.put(ChildFirstNameColumn.split(" ")[0], firstName);
        db.insert(childInfoTableName, null, contentValues);
    }

    public void truncateTable() {
        db.execSQL("DELETE FROM " + childInfoTableName + ";");
    }
}
