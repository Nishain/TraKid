package ndds.com.trakidhome;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.skyfishjy.library.RippleBackground;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SOSPage extends AppCompatActivity {

    private Handler handler;
    private Runnable mRunnable;

    @Override
    protected void onDestroy() {
        if (handler != null)
            handler.removeCallbacks(mRunnable);
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sospage);
        //sosView.setText(getIntent'().getStringExtra("message"));
        ((RippleBackground)findViewById(R.id.sos_ripple_effect)).startRippleAnimation();
        TextView header = findViewById(R.id.SOSHeaderTitle);
        if (getIntent().hasExtra("name"))
            header.setText(header.getText() + " from " + getIntent().getStringExtra("name"));
        final TextView timeGapIndicator = findViewById(R.id.sos_time_gap_lbl);
        timeGapIndicator.setText("Just now");
        final long occurenceTime = System.currentTimeMillis();
        handler = new Handler();
        handler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                long timeDifferance = System.currentTimeMillis() - occurenceTime;
                timeDifferance = TimeUnit.MILLISECONDS.toMinutes(timeDifferance);
                int hours = (int) (timeDifferance / 60);
                int mins = (int) (timeDifferance % 60);
                handler.postDelayed(this, 60000);
                timeGapIndicator.setText((hours > 0 ? hours + " hours and " : "") + mins + " minutes ago");
            }
        }, 60000);
    }

    public void openApp(View v) {
        startActivity(new Intent(this, SignInActivity.class));
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardLocked()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyguardManager.requestDismissKeyguard(this, null);
            } else
                Toast.makeText(this, "unlock the screen to view the app", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onAttachedToWindow() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        );
    }

}
