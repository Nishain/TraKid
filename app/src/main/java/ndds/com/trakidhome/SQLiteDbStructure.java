package ndds.com.trakidhome;

public interface SQLiteDbStructure {
    String dbName = "TraKid";
    String trackingInfoTableName = "coordinateInfo";
    String childInfoTableName = "childInfoTableName";

    String LatitudeColumn = "latitude REAL";
    String LongitudeColumn = "longitude REAL";
    String TimestampColumn = "timestamp INTEGER";
    String[] TrackingTableColumns = {
            LatitudeColumn,
            LongitudeColumn,
            TimestampColumn
    };
    String ChildFirstNameColumn = "childFirstName TEXT";
    String ChildIDColumn = "childID INTEGER PRIMARY KEY";
    String TrackingDevicePhoneNumber = "trackingPhoneNumber TEXT";
    String[] ChildInfoTableColumns = {
            ChildIDColumn,
            ChildFirstNameColumn,
            TrackingDevicePhoneNumber
    };
}
