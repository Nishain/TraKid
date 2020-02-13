/*
package ndds.com.trakidhome;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapFunctionalityHandler mapFunctionalityHandler;
    private InternetChangeListener internetChangeListener;
    private String report;

    public void setDummyChildren() {
        SQLiteChildDataHandler data = new SQLiteChildDataHandler(this);
        data.insertNewChild("453", "Nishain", "0770665281");
        data.insertNewChild("765","Chanaka","0771159813");
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

        data.insertNewCoordinateInfo(6.839136,79.964816,22);
        data.insertNewCoordinateInfo(6.839166,79.964836,23);
        data.insertNewCoordinateInfo(6.839100,79.964367,24);
        data.insertNewCoordinateInfo(6.839104,79.964370,25);
        data.insertNewCoordinateInfo(6.839189,79.964390,26);

    }
    private void initializeChildSelectorWindow(){
        final ArrayList<String> children =new ArrayList<>();
        children.add(" +child ");
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
                else
                    showAddChildWindow();
            }

            @Override
            void onItemLongClicked(int position) {
                showChildInfo(position,-1);
            }
        });
    }
    private void showChildInfo(int position, int offset){
        int rawPosition = position + offset;
        SQLiteChildDataHandler dataHandler = new SQLiteChildDataHandler(this);
        Cursor c = dataHandler.getCursor();
        c.moveToPosition(rawPosition);
        String info =
                "Name: "+c.getString(1)+
                "\nPair Code"+c.getString(0)+
                "\nPhone number"+c.getString(2);
        new AlertDialog.Builder(this).
                setTitle("Child Information").
                setMessage(info).show();
    }
    private void showAddChildWindow(){
        final ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.add_child_alert,null);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(viewGroup).create();
        viewGroup.findViewById(R.id.add_new_child_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paircode=((EditText)viewGroup.findViewById(R.id.new_child_pairCode)).getText().toString();
                String firstName=((EditText)viewGroup.findViewById(R.id.new_child_firstName)).getText().toString();
                String phoneumber=((EditText)viewGroup.findViewById(R.id.new_child_phoneNumber)).getText().toString();
                if(paircode.length()==0 || firstName.length()==0 || phoneumber.length()==0)
                    Toast.makeText(MapsActivity.this, "Some of the fields are empty", Toast.LENGTH_SHORT).show();
                else if(phoneumber.length()!=10)
                    Toast.makeText(MapsActivity.this, "Incorrect phone number please check again!", Toast.LENGTH_SHORT).show();
                else {
                    onChildAdded(paircode, firstName, phoneumber);
                    Toast.makeText(MapsActivity.this, "child "+firstName+" added!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
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
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_options);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.help_btn:
                        showHelp();
                        break;
                    case R.id.refresh_btn:
                        refreshMap();
                        break;
                    case R.id.show_report_btn:
                        showReport();
                        break;
                }
                return true;
            }
        });
    }

    private void showHelp() {
        Toast.makeText(this, "show help", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        internetChangeListener = new InternetChangeListener(this);
        registerReceiver(internetChangeListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    public void showReport(){
        ViewGroup viewGroup= (ViewGroup) getLayoutInflater().inflate(R.layout.activity_report_page,null);
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isConectedToNetwork = connectivityManager.getActiveNetworkInfo()!=null && connectivityManager.getActiveNetworkInfo().isConnected();
        viewGroup.findViewById(R.id.no_internetAccess_text).setVisibility(isConectedToNetwork?View.GONE:View.VISIBLE);
        if(report.equals(""))
            viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.no_report_poster,null);
        else
            ((TextView)viewGroup.findViewById(R.id.report_text)).setText(report);
        new AlertDialog.Builder(this).setView(viewGroup).show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(internetChangeListener);
    }
    public void showSettings(){
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
        mapFunctionalityHandler.addSingleCoordinate();
    }
    public void onChildAdded(String childID, String firstName, String phonenumber) {
        SQLiteChildDataHandler dataHandler = new SQLiteChildDataHandler(this);
        dataHandler.insertNewChild(childID, firstName, phonenumber);
        ChildListAdapter listAdapter=(ChildListAdapter)((RecyclerView) findViewById(R.id.childSelector)).getAdapter();
        if(listAdapter!=null)
            listAdapter.addChildAndFocus(firstName);
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
        refreshMap();
    }

    public void onInitFirstTime() {
        onChildrenAdded();
        onCoordinatesAdded();
        refreshMap();
        mapFunctionalityHandler.adjustCamera();
    }

    public void refreshMap() {
        if (mapFunctionalityHandler != null)
            report = mapFunctionalityHandler.refreshMap();
    }
    */
/**
 * Manipulates the map once available.
 * This callback is triggered when the map is ready to be used.
 * This is where we can add markers or lines, add listeners or move the camera. In this case,
 * we just add a marker near Sydney, Australia.
 * If Google Play services is not installed on the device, the user will be prompted to install
 * it inside the SupportMapFragment. This method will only be triggered once the user has
 * installed Google Play services and returned to the app.
 *//*

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
        onInitFirstTime();
    }
    public void dummystart(){
        mapFunctionalityHandler = new MapFunctionalityHandler(mMap, this)
                .setMaximumTimeInterval(1)//if time gap greater than this interval if will be reported to report
                .setColors(
                        getResources().getColor(R.color.appThemeLight),
                        getResources().getColor(R.color.appThemeNormal),
                        getResources().getColor(R.color.appThemeDark),
                        getResources().getColor(R.color.appThemeError)
                );
        mapFunctionalityHandler.showLegend((LinearLayout) findViewById(R.id.mapLegendContainer));
        onInitFirstTime();
    }


}
*/
