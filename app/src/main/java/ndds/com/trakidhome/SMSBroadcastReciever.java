package ndds.com.trakidhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSBroadcastReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus=(Object[])intent.getExtras().get("pdus");
        SmsMessage smsMessage=SmsMessage.createFromPdu((byte[]) pdus[pdus.length-1]);
        String originatingAddress = smsMessage.getOriginatingAddress();
        Cursor cursor = new SQLiteChildDataHandler(context).getCursor();
        boolean isPhonenumberMatching = false;
        String childName = null;
        Log.i("sos", "child phonenumber : " + originatingAddress);
        while (cursor.moveToNext()) {
            if (originatingAddress.contains("+94" + cursor.getString(2).substring(1))) {
                isPhonenumberMatching = true;
                childName = cursor.getString(1);
            }
        }
        if (!isPhonenumberMatching)
            return;
        Intent i=new Intent(context,SOSPage.class).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK).
                putExtra("name", childName);
        context.startActivity(i);
    }
}
