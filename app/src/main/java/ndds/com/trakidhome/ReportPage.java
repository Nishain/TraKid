package ndds.com.trakidhome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ReportPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_page);
        TextView reportText = findViewById(R.id.report_text);
        String report = getIntent().getStringExtra("report");
        findViewById(R.id.report_loading_spinner).setVisibility(View.GONE);
        if(report.equals(""))
            getLayoutInflater().inflate(R.layout.no_report_poster, (ViewGroup) getWindow().getDecorView().getRootView());
        else
            reportText.setText(report);
    }
    public void showOnMap(View v){
        setResult(RESULT_OK);
        finish();
    }
}
