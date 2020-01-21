package ndds.com.trakidhome;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapFunctionalityHandler mapFunctionalityHandler;
    private InternetChangeListener internetChangeListener;
    private boolean isDisplayRoute;
    private int reportRequestCode = 456;

    public void setDummyChildren() {
        SQLiteChildDataHandler data = new SQLiteChildDataHandler(this);
        data.insertNewChild(423, "Nishain", "0770665281");
    }
    public void setDummyValues(){
        SQLIteLocationDataHandler data = new SQLIteLocationDataHandler(this);
        data.insertNewCoordinateInfo(6.839241, 79.964737, 12);
        data.insertNewCoordinateInfo(6.839100, 79.964715, 13);
        data.insertNewCoordinateInfo(6.839105, 79.964720, 14);
        data.insertNewCoordinateInfo(6.839096, 79.964725, 15);
        data.insertNewCoordinateInfo(6.838786, 79.964810, 19);
        data.insertNewCoordinateInfo(6.838634, 79.964306, 20);
        data.insertNewCoordinateInfo(6.839135, 79.964813, 21);
    }
    private void initializeChildSelectorWindow(){
        final ArrayList<String> children =new ArrayList<>();
        children.add("+");
        SQLiteChildDataHandler data = new SQLiteChildDataHandler(this);
        Cursor childrenCursor = data.getCursor();
        while (childrenCursor.moveToNext()) {
            children.add(childrenCursor.getString(1));
        }
        RecyclerView recyclerView=findViewById(R.id.childSelector);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(),RecyclerView.HORIZONTAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new ChildListAdapter(children,getResources().getColor(R.color.appThemeSelection), getResources().getColor(R.color.appTheme)) {
            @Override
            public void onItemClicked(int position) {
                if (position > 0)
                    onChildChange(position, -1);
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        checkAndRequestPermissions();
        Switch s = findViewById(R.id.routeDotToggleSwitch);
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setText(isChecked ? "show Dots" : "show Route");
                onRouteModeToggle();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        internetChangeListener = new InternetChangeListener(this);
        registerReceiver(internetChangeListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    public void showReport(View v){
        if (mapFunctionalityHandler == null)
            return;
        Toast.makeText(this, "hold on..", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(this, ReportPage.class)
                .putExtra("report", mapFunctionalityHandler.getSpeedAnalysisReport().makeReport()), reportRequestCode);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(internetChangeListener);
    }
    public void showSettings(View v){
        startActivity(new Intent(this,HomeSettings.class));
    }

    public void onRefreshClicked(View v) {
        refreshMap();
    }

    public void onToggleViewClicked(View v) {
        onRouteModeToggle();
    }
    private void checkAndRequestPermissions(){
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String[] permissions = info.requestedPermissions;
        ArrayList<String> permissionRequired= new ArrayList<>();
        for(String p:permissions){
            if(ContextCompat.checkSelfPermission(this,p)!=PackageManager.PERMISSION_GRANTED)
                permissionRequired.add(p);
        }
        if(permissionRequired.size()>0)
            ActivityCompat.requestPermissions(this, permissionRequired.toArray(new String[0]),123);
    }
    public void onNetworkAvailabilityChange(boolean isAvailable){
        if(!isAvailable)
        new AlertDialog.Builder(this).setTitle("No internet Connection!").
                setMessage(getString(R.string.no_internet_message))
                .show();
    }

    public void onCoordinatesAdded() {
        SQLIteLocationDataHandler dataHandler = new SQLIteLocationDataHandler(this);
        dataHandler.truncateTable();
        setDummyValues();//for now :)
        refreshMap();
    }

    public void onChildrenAdded() {
        SQLiteChildDataHandler dataHandler = new SQLiteChildDataHandler(this);
        dataHandler.truncateTable();
        setDummyChildren();//for now :>
        initializeChildSelectorWindow();
    }

    public void onSingleCoordinateAdded(double latitude, double longitude, int timestamp) {
        SQLIteLocationDataHandler dataHandler = new SQLIteLocationDataHandler(this);
        dataHandler.insertNewCoordinateInfo(latitude, longitude, timestamp);
        mapFunctionalityHandler.addSingleCoordinate(isDisplayRoute);
    }

    public void onRouteModeToggle() {
        isDisplayRoute = !isDisplayRoute;
        refreshMap();
    }

    public void onChildAdded(int childID, String firstName, String phonenumber) {
        SQLiteChildDataHandler dataHandler = new SQLiteChildDataHandler(this);
        dataHandler.insertNewChild(childID, firstName, phonenumber);
        //add code for updating location infoTable..
        refreshMap();
    }

    public void onChildChange(int position, int positionOffset) {
        int rawPosition = position + positionOffset;
        SQLiteChildDataHandler dataHandler = new SQLiteChildDataHandler(this);
        Cursor c = dataHandler.getCursor();
        c.moveToPosition(rawPosition);
        String childID = c.getString(0);
        //update table according to the childID
    }

    public void onInitFirstTime() {
        onChildrenAdded();
        onCoordinatesAdded();
        refreshMap();
        mapFunctionalityHandler.adjustCamera();
    }

    public void refreshMap() {
        if (mapFunctionalityHandler != null)
            mapFunctionalityHandler.refreshMap(isDisplayRoute);
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapFunctionalityHandler = new MapFunctionalityHandler(mMap, this)
                .setMaximumTimeInterval(1)//if time gap greater than this interval if will be reported to report
                .setColors(
                        getResources().getColor(R.color.appThemeLight),
                        getResources().getColor(R.color.appThemeNormal),
                        getResources().getColor(R.color.appThemeDark),
                        getResources().getColor(R.color.appThemeError)
                );
        mapFunctionalityHandler.showLegend((LinearLayout) findViewById(R.id.mapLegendContainer));
        mapFunctionalityHandler.refreshMap(isDisplayRoute);
        onInitFirstTime();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == reportRequestCode && resultCode == RESULT_OK) {
            if (!isDisplayRoute)
                onRouteModeToggle();
            mapFunctionalityHandler.showStopsInMap();//highSpeedTerminals
            mapFunctionalityHandler.showHighSpeedInMap();
        }
    }
}
