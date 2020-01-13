package ndds.com.trakidhome;

public interface SQLiteDbStructure {
    String dbName = "TraKid";
    String trackingInfoTableName = "childInfo";
    String LatitudeColumn = "latitude REAL";
    String LongitudeColumn = "longitude REAL";
    String TimestampColumn = "timestamp INTEGER";
    String[] coloumns = {
            LatitudeColumn,
            LongitudeColumn,
            TimestampColumn
    };
}
