package ndds.com.trakidhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SharedPrefernceManager {
    private Context context;
    private SharedPreferences sharedPreferences;

    public SharedPrefernceManager(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    }
    public Object getValue(int field){
        String fieldName=context.getString(field).split(",")[0];
        String type=context.getString(field).split(",")[1];
        String defaultValue=context.getString(field).split(",")[2];
        Log.i("info","type - "+type);
        if(type.equals("s"))
            return sharedPreferences.getString(fieldName,defaultValue);
        else if(type.equals("i"))
            return sharedPreferences.getInt(fieldName, Integer.parseInt(defaultValue));
        else if(type.equals("f"))
            return sharedPreferences.getFloat(fieldName, Float.parseFloat(defaultValue));
        return null;
    }
    public void setValue(int field,Object value){
        String valueInString=null;
        boolean isValueString;
        if(isValueString=value instanceof String)
            valueInString=(String)value;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String type=context.getString(field).split(",")[1];
        String fieldName=context.getString(field).split(",")[0];
        if(type.equals("s"))
            editor.putString(fieldName, String.valueOf(value));
        else if(type.equals("i"))
            editor.putInt(fieldName, isValueString?Integer.parseInt(valueInString):(Integer) value);
        else if(type.equals("f"))
            editor.putFloat(fieldName,isValueString?Float.parseFloat(valueInString):(Float) value);
        editor.commit();
    }
}
