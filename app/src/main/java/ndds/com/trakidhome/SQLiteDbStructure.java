package ndds.com.trakidhome;

public interface SQLiteDbStructure {
    String dbName = "TraKid";
    String trackingInfoTableName = "coordinateInfo";
    String childInfoTableName = "childInfoTableName";

    String LatitudeColumn = "latitude REAL";
    String LongitudeColumn = "longitude REAL";
    String TimestampColumn = "timestamp INTEGER";
    String PairCodeColunm = "pairCode TEXT";
    String[] TrackingTableColumns = {
            LatitudeColumn,
            LongitudeColumn,
            TimestampColumn,
            PairCodeColunm
    };
    String ChildFirstNameColumn = "childFirstName TEXT";
    String ChildIDColumn = "childID TEXT";
    String[] ChildInfoTableColumns = {
            ChildIDColumn,
            ChildFirstNameColumn
    };
}
