package ndds.com.trakidhome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ReportPage extends AppCompatActivity {

    ArrayList<LatLng> coordinates;
    ArrayList<Integer> timestamps;
    private SpeedAnalysisReport speedAnalysisReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_page);
        TextView reportText = findViewById(R.id.report_text);
        SharedPrefernceManager sharedPrefernceManager=new SharedPrefernceManager(this);
        speedAnalysisReport=new SpeedAnalysisReport(
                this,
                (float) sharedPrefernceManager.getValue(R.string.maxSpeedThreshold),
                Math.round((int) sharedPrefernceManager.getValue(R.string.minWaitingTime))
                );
        String report=speedAnalysisReport.makeReport(this);
        findViewById(R.id.report_loading_spinner).setVisibility(View.GONE);
        if(report.equals(""))
            getLayoutInflater().inflate(R.layout.no_report_poster, (ViewGroup) getWindow().getDecorView().getRootView());
        else
            reportText.setText(report);
    }
    public void showOnMap(View v){
        startActivity(new Intent(this,MapsActivity.class).
                putExtra("showReport",true).
                putExtra("stopCoordinates",speedAnalysisReport.stopCoordinates).
                putExtra("highSpeedTerminals",speedAnalysisReport.highSpeedTimes));
        finish();
    }
}
