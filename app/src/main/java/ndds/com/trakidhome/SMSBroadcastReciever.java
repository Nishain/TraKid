package ndds.com.trakidhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

public class SMSBroadcastReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus=(Object[])intent.getExtras().get("pdus");
        SmsMessage smsMessage=SmsMessage.createFromPdu((byte[]) pdus[pdus.length-1]);
        Intent i=new Intent(context,SOSPage.class).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK).
                putExtra("message",smsMessage.getMessageBody());
        context.startActivity(i);
    }
}
