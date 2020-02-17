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
        String message = smsMessage.getMessageBody();
        SQLiteChildDataHandler DB = new SQLiteChildDataHandler(context);

        String childName;
        if (message.contains("Please find me. I am in danger-")) {
            String paircode = message.substring(message.indexOf("-") + 1);
            childName = DB.getTitleFromPairCode(paircode);
            if (childName == null)
                return;
            Intent i = new Intent(context, SOSPage.class).
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK).
                    putExtra("name", childName);
            context.startActivity(i);
        }
    }
}
