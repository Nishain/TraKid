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
        boolean isShowingOpenAppButton = ((int) sharedPrefernceManager.getValue(R.string.isShowingOpenAppButton)) == 1;
        ((EditText) findViewById(R.id.setting_waiting_time_threshold_min)).setText(String.valueOf(time / 60));
        ((EditText) findViewById(R.id.setting_waiting_time_threshold_sec)).setText(String.valueOf(time % 60));
        ((Switch) findViewById(R.id.setting_enabilityOpenAppButton)).setChecked(isShowingOpenAppButton);
    }
    public void saveSettings(View view){
        String mins = ((EditText) findViewById(R.id.setting_waiting_time_threshold_min)).getText().toString(),
                secs = ((EditText) findViewById(R.id.setting_waiting_time_threshold_sec)).getText().toString(),
                speedThreshold = ((EditText) findViewById(R.id.setting_speed_threshold)).getText().toString();
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
        }
        boolean isShowingOpenAppButton = ((Switch) findViewById(R.id.setting_enabilityOpenAppButton)).isChecked();
        sharedPrefernceManager.setValue(R.string.maxWaitingTime, (min * 60) + sec);
        sharedPrefernceManager.setValue(R.string.maxSpeedThreshold, speedThreshold);
        sharedPrefernceManager.setValue(R.string.isShowingOpenAppButton, isShowingOpenAppButton ? 1 : 0);
        setResult(MainActivity.SettingResultRequestCode, new Intent()
                .putExtra("isShowingOpenAppButton", isShowingOpenAppButton));
        Toast.makeText(this, "saved settings", Toast.LENGTH_SHORT).show();
    }

}
