package ndds.com.trakidhome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class HomeSettings extends AppCompatActivity {

    private SharedPrefernceManager sharedPrefernceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_settings);
        sharedPrefernceManager=new SharedPrefernceManager(this);
        setInitialValues();
    }

    private void setInitialValues() {
        ((EditText) findViewById(R.id.setting_speed_threshold)).setText(String.valueOf(sharedPrefernceManager.getValue(R.string.maxSpeedThreshold)));
        int time = (int) sharedPrefernceManager.getValue(R.string.maxWaitingTime);
        ((EditText) findViewById(R.id.setting_waiting_time_threshold_min)).setText(String.valueOf(time / 60));
        ((EditText) findViewById(R.id.setting_waiting_time_threshold_sec)).setText(String.valueOf(time % 60));
        ((EditText) findViewById(R.id.setting_max_distanceInHighSpeed)).setText(String.valueOf(sharedPrefernceManager.getValue(R.string.maxDistanceInHighSpeed)));
    }

    public void cancelSettings(View v) {
        onBackPressed();
        Toast.makeText(this, "Setting changes discarded", Toast.LENGTH_SHORT).show();
    }
    public void saveSettings(View view){
        String mins = ((EditText) findViewById(R.id.setting_waiting_time_threshold_min)).getText().toString(),
                secs = ((EditText) findViewById(R.id.setting_waiting_time_threshold_sec)).getText().toString(),
                speedThreshold = ((EditText) findViewById(R.id.setting_speed_threshold)).getText().toString(),
                maxDistanceInHighSpeed = ((EditText) findViewById(R.id.setting_max_distanceInHighSpeed)).getText().toString();
        if (mins.length() == 0 ||
                secs.length() == 0 ||
                speedThreshold.length() == 0) {
            Toast.makeText(this, "Some fields are empty", Toast.LENGTH_SHORT).show();
            return;
        }
        int min, sec;
        min = Integer.parseInt(mins);
        sec = Integer.parseInt(secs);
        if (sec > 59) {
            Toast.makeText(this, "Enter seconds between 0 and 59", Toast.LENGTH_SHORT).show();
            return;
        } else if ((sec == 0 && min == 0)
                || Float.parseFloat(speedThreshold) == 0
                || Integer.parseInt(maxDistanceInHighSpeed) == 0) {
            Toast.makeText(this, "Numeric fields cannot have 0 value", Toast.LENGTH_SHORT).show();
            return;
        }
        sharedPrefernceManager.setValue(R.string.maxWaitingTime, (min * 60) + sec);
        sharedPrefernceManager.setValue(R.string.maxSpeedThreshold, speedThreshold);
        sharedPrefernceManager.setValue(R.string.maxDistanceInHighSpeed, maxDistanceInHighSpeed);
        Toast.makeText(this, "saved settings", Toast.LENGTH_SHORT).show();
    }

}
