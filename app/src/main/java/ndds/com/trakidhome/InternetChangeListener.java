package ndds.com.trakidhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetChangeListener extends BroadcastReceiver {
    private MainActivity mainActivity;

    public InternetChangeListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public  InternetChangeListener(){
        super();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=cm.getActiveNetworkInfo();
        mainActivity.onNetworkAvailabilityChange(networkInfo != null && networkInfo.isConnected());

    }
}
