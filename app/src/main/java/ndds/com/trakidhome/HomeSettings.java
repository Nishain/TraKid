package ndds.com.trakidhome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Map;

public class HomeSettings extends AppCompatActivity {

    private LinearLayout settingContainer;
    int[] settingKeys={
            R.string.maxSpeedThreshold,
            R.string.minWaitingTime
    };
    private SharedPrefernceManager sharedPrefernceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_settings);
        sharedPrefernceManager=new SharedPrefernceManager(this);
        settingContainer = findViewById(R.id.settings_container);
        addSettingTitle("Reporting");
        setInitialValues(new EditText[]{
            addSetting(R.string.reportSetting1, InputType.TYPE_CLASS_NUMBER),
            //addSetting(R.string.reportSetting2,InputType.TYPE_NUMBER_FLAG_DECIMAL);
            addSetting(R.string.reportSetting3, InputType.TYPE_CLASS_NUMBER)
        });

    }
    private void setInitialValues(EditText[] editTexts){
        for (int i = 0; i < editTexts.length; i++) {
            editTexts[i].setText(String.valueOf(sharedPrefernceManager.getValue(settingKeys[i])));
        }
    }
    public void saveSettings(View view){
        View child;
        EditText editText;
        int editControlIndex=0;
        for (int i = 0; i < settingContainer.getChildCount(); i++) {
            if((child=settingContainer.getChildAt(i)) instanceof LinearLayout) {
                editText = child.findViewById(R.id.settingTextField);
                sharedPrefernceManager.setValue(settingKeys[editControlIndex],editText.getText().toString());
                editControlIndex++;
            }
        }
        Toast.makeText(this, "saved settings", Toast.LENGTH_SHORT).show();
    }
    private void addSettingTitle(String label){
        TextView txt=(TextView) getLayoutInflater().inflate(R.layout.setting_group_title,null);
        txt.setText(label);
        settingContainer.addView(txt);
    }
    private EditText addSetting(int label,int filedType){
        ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.typical_setting_textfield,null);
        ((TextView)viewGroup.findViewById(R.id.settingLabel)).setText(getString(label));
        EditText editText= viewGroup.findViewById(R.id.settingTextField);
        editText.setInputType(filedType);
        settingContainer.addView(viewGroup);
        return editText;
    }
}
