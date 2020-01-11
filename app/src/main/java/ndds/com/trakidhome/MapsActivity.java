package ndds.com.trakidhome;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

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

    ArrayList<Integer> timeStamps = new ArrayList<>();

    ArrayList<LatLng> coordinates = new ArrayList<>();



    public void setDummyValues(){
        timeStamps.add(12);
        timeStamps.add(13);
        timeStamps.add(14);
        timeStamps.add(15);
        timeStamps.add(19);
        timeStamps.add(20);
        timeStamps.add(21);

        coordinates.add(new LatLng(6.839241, 79.964737));
        coordinates.add(new LatLng(6.839100, 79.964715));
        coordinates.add(new LatLng(6.839105, 79.964720));
        coordinates.add(new LatLng(6.839096, 79.964725));
        coordinates.add(new LatLng(6.838786, 79.964810));
        coordinates.add(new LatLng(6.838634, 79.964306));
        coordinates.add(new LatLng(6.839135, 79.964813));


    }
    private void initializeChildSelectorWindow(){
        final ArrayList<String> children =new ArrayList<>();
        children.add("Nishain");
        children.add("Ashain");
        children.add("Kasum");
        children.add("pasan");
        children.add("suri");
        children.add("lasan");
        children.add("garuka");
        children.add("navith");
        children.add("hero");
        RecyclerView recyclerView=findViewById(R.id.childSelector);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(),RecyclerView.HORIZONTAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new ChildListAdapter(children,getResources().getColor(R.color.appThemeSelection), getResources().getColor(R.color.appTheme)) {
            @Override
            public void onItemClicked(int position) {
                Toast.makeText(MapsActivity.this, "item "+children.get(position)+" clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setDummyValues();
        initializeChildSelectorWindow();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        checkAndRequestPermissions();
        //startService(new Intent(this,SOSSMSListener.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        internetChangeListener = new InternetChangeListener(this);
        registerReceiver(internetChangeListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    public void showReport(View v){
        startActivity(new Intent(this,ReportPage.class).
                putExtra("coordinates",coordinates).
                putExtra("timestamps",timeStamps));
    }

    public void showSOS(View v){
        startActivity(new Intent(this,SOSPage.class));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(internetChangeListener);
    }
    public void showSettings(View v){
        startActivity(new Intent(this,HomeSettings.class));
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

    public void showHelp(){

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
        mapFunctionalityHandler=new MapFunctionalityHandler(mMap,coordinates,timeStamps);
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(6.839241, 79.964737);

        mapFunctionalityHandler.addMapRoute();
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,19));

        if(getIntent().hasExtra("showReport")){
            ArrayList<LatLng> positions = getIntent().getParcelableArrayListExtra("stopCoordinates");
            mapFunctionalityHandler.showStopsInMap(positions);//highSpeedTerminals
            ArrayList<Integer> highSpeedTerminals = getIntent().getIntegerArrayListExtra("highSpeedTerminals");
            mapFunctionalityHandler.showHighSpeedInMap(highSpeedTerminals);
        }
    }
}
