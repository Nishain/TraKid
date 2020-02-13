package ndds.com.trakidhome;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fenchtose.tooltip.AnimationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_UID = "uid";
    public static final int SettingResultRequestCode = 704;
    TextView userNameText;
    TextView userMailText;
    NavigationView navigationView;
    ImageView imageView;
    SignInActivity logoutObject;
    private GoogleMap mMap;
    View mMapView;
    LatLng currentLocation;
    FusedLocationProviderClient mFusedLocationProviderClient;
    Location mLastKnownLocation;
    double lat, lon, myLat = 0, myLon = 0;

    boolean mLocationPermissionGranted;
    private ValueEventListener locationChangeListener;
    private DatabaseReference currentReference;

    private ChildHandler childHandler;
    private int PERMISSION_CODE = 123;
    private float preferedZoom = 23;
    private SQLIteLocationDataHandler locationDatabase;
    private Calendar midnightCalendar;
    private MapFunctionalityHandler mapFunctionHandler;
    private ArrayList<String> report = new ArrayList<>();
    private MarkerOptions currentMarker;
    private InternetChangeListener internetChangeListener;
    private boolean isShowingPlainCoordinate = false;
    private Marker previousMarker;
    private StateObject fullyLoadedState = new StateObject() {
        @Override
        void onBothConditionStatisfied() {
            setMarkerTitle();
        }
    };
    private boolean isLegendVisible = false;

    private void setMarkerTitle() {
        String currentChildName = new SQLiteChildDataHandler(this).getTitleFromPairCode((String) fullyLoadedState.getData());
        if (currentChildName == null)
            currentChildName = "Unknown";
        previousMarker.setTitle(currentChildName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(internetChangeListener = new InternetChangeListener(this), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(internetChangeListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String username = (String) getIntent().getExtras().get(EXTRA_USERNAME);
        String email = (String) getIntent().getExtras().get(EXTRA_EMAIL);
        String url = (String) getIntent().getExtras().get(EXTRA_URL);
        String UID = getIntent().getStringExtra(EXTRA_UID);
        logoutObject = new SignInActivity();
        locationDatabase = new SQLIteLocationDataHandler(this);
        SharedPrefernceManager sharedPrefernce = new SharedPrefernceManager(this);
        isShowingPlainCoordinate = ((int) sharedPrefernce.getValue(R.string.isShowingOpenAppButton)) == 0;
        if (!sharedPrefernce.isDataRecordedOnSameDay()) {
            locationDatabase.truncateTable();
            Toast.makeText(this, "local history has reset", Toast.LENGTH_SHORT).show();
        }
        if (sharedPrefernce.isPreviousUserLogged(UID) != 1)
            sharedPrefernce.setUserEmail(email);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final LinearLayout holder = findViewById(R.id.holder);
        FloatingActionButton mylocation = findViewById(R.id.find_child_location);
        mylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //move the camera to tracking device location
                if (mMap == null) {
                    Toast.makeText(MainActivity.this, "wait till map loads", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentLocation == null)
                    Toast.makeText(MainActivity.this, "No coordinates received yet", Toast.LENGTH_SHORT).show();
                else
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
            }
        });
        navigationView = findViewById(R.id.nav_view);

        View hview = navigationView.inflateHeaderView(R.layout.nav_header_main);

        userNameText = hview.findViewById(R.id.txt_username);
        userMailText = hview.findViewById(R.id.txt_email);
        userNameText.setText(username);
        userMailText.setText(email);

        imageView = hview.findViewById(R.id.profile_image);
        if (url != null)
            Glide.with(this).load(url).into(imageView);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {


            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                float scaleFactor = 7f;
                float slideX = drawerView.getWidth() * slideOffset;

                holder.setTranslationX(slideX);
                holder.setScaleX(1 - (slideOffset / scaleFactor));
                holder.setScaleY(1 - (slideOffset / scaleFactor));

                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);// will remove all possible our aactivity's window bounds
        }

        drawer.addDrawerListener(toggle);

        drawer.setScrimColor(Color.TRANSPARENT);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapView = mapFragment.getView();
        childHandler = new ChildHandler(UID, this) {
            @Override
            void onChildrenLoaded() {
                initializeChildSelectorWindow();
                findViewById(R.id.children_loading_window).setVisibility(View.GONE);
                fullyLoadedState.setA(true);
            }

            @Override
            void onDefaultPaircodeChanged(String defaultPairCode) {
                if (defaultPairCode != null)
                    addLocationChangeListener(defaultPairCode);
                else
                    childHandler.showNoDefaultChildAlert();
            }
        };
        intializeBottomNavigationBar();
        midnightCalendar = Calendar.getInstance();
        midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
        midnightCalendar.set(Calendar.MINUTE, 0);
        midnightCalendar.set(Calendar.SECOND, 0);
        mapFragment.getMapAsync(this);
        findViewById(R.id.coordinate_aid).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openAssociatedApp();
                return true;
            }
        });
        checkAndRequestPermissions();
    }

    private void RefreshMapWithNewReport() {
        if (mMap != null && mapFunctionHandler != null) {
            mMap.clear();
            report = mapFunctionHandler.displayCoordinatesInMap();
            if (currentMarker != null)
                previousMarker = mMap.addMarker(currentMarker);
        }
    }

    private boolean checkAndRequestPermissions() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String[] permissions = info.requestedPermissions;
        ArrayList<String> permissionRequired = new ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
                permissionRequired.add(p);
        }
        if (permissionRequired.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionRequired.toArray(new String[0]), PERMISSION_CODE);
            return false;
        } else
            return true;
    }

    public void loadChildren() {
        childHandler.getChildrenFromFirebase();
    }


    private void initializeChildSelectorWindow() {
        final ArrayList<String> children = new ArrayList<>();
        children.add(" +child ");
        SQLiteChildDataHandler data = new SQLiteChildDataHandler(this);
        Cursor childrenCursor = data.getCursor();
        while (childrenCursor.moveToNext()) {
            children.add(childrenCursor.getString(1));
        }
        RecyclerView recyclerView = findViewById(R.id.childSelector);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new ChildListAdapter(children, getResources()) {
            @Override
            public void onItemClicked(int position) {
                if (position > 0)
                    childHandler.changeChild(position - 1);
                else
                    childHandler.showAddChildWindow();
            }

            @Override
            void onItemLongClicked(int position) {
                if (position > 0)
                    childHandler.showChildInfo(position - 1);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        String disabledFunctionalities = "";
        mLocationPermissionGranted = true;
        if (requestCode == PERMISSION_CODE) {
            boolean minorPerssionMissing = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        disabledFunctionalities += "cannot get current location of this device.";
                        mLocationPermissionGranted = false;
                    } else if (permissions[i].equals(Manifest.permission.RECEIVE_SMS))
                        disabledFunctionalities += "cannot receive SOS through SMS.";
                    else
                        minorPerssionMissing = true;
                }
            }
            if (minorPerssionMissing)
                disabledFunctionalities += "some other minor permissions missing.";
            if (disabledFunctionalities.length() > 0)
                new AlertDialog.Builder(this).setTitle("Permissions missing!")
                        .setMessage(disabledFunctionalities
                                + "\nyou can re-assign the permission in navigation menu")
                        .show();
            if (mLocationPermissionGranted)
                updateLocationUI();
        }

        /*switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }*/
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

            } else {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            myLat = mLastKnownLocation != null ? mLastKnownLocation.getLatitude() : Double.NaN;
                            myLon = mLastKnownLocation != null ? mLastKnownLocation.getLongitude() : Double.NaN;
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), 300));
                        } else {
//                            Log.e("Current location is null. Using defaults.");
//                            Log.e(TAG, "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }


    private void intializeBottomNavigationBar() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_options);

        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) bottomNavigationView.getLayoutParams();
            layoutParams.bottomMargin = getResources().getDimensionPixelSize(resourceId);
            bottomNavigationView.setLayoutParams(layoutParams);
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.show_report_btn)
                    showReport();
                else if (id == R.id.refresh_btn) {
                    if (mapFunctionHandler != null)
                        RefreshMapWithNewReport();
                } else if (id == R.id.help_btn)
                    showHelp();
                return false;
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.notification) {
            // Handle the camera action
        } else if (id == R.id.help) {

        } else if (id == R.id.settings) {
            showSettings();
        } else if (id == R.id.truncateTable) {
            locationDatabase.truncateTable();
        } else if (id == R.id.logout) {
            logoutObject.signOut();
            if (locationChangeListener != null)
                currentReference.removeEventListener(locationChangeListener);
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else if (id == R.id.checkPermission) {
            if (checkAndRequestPermissions())
                Toast.makeText(MainActivity.this, "All permissions are granted thank you!", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    public void showSettings() {
        startActivityForResult(new Intent(this, HomeSettings.class), SettingResultRequestCode);
    }

    public void showHelp() {
        if (!HelpGuider.isInStanceRunning)
            new HelpGuider(this).popHelp();
    }

    private void setMapListeners() {
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Animation animation = android.view.animation.AnimationUtils.loadAnimation(MainActivity.this, R.anim.shift_open);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        isLegendVisible = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                findViewById(R.id.mapLegendContainer).startAnimation(animation);
            }
        });
        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (isLegendVisible) {
                    findViewById(R.id.mapLegendContainer).startAnimation(android.view.animation.AnimationUtils.loadAnimation(MainActivity.this, R.anim.shift_close));
                    isLegendVisible = false;
                }

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(preferedZoom);
        setMapListeners();
        repositionLocationButton();
        //move the camera to device current location.
        loadChildren();
        getDeviceLocation();
        updateLocationUI();
    }

    private void repositionLocationButton() {
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        rlp.setMargins(30, 0, 0, 0);
    }

    private void showReport() {
        ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_report_page, null);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isConectedToNetwork = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        viewGroup.findViewById(R.id.no_internetAccess_text).setVisibility(isConectedToNetwork ? View.GONE : View.VISIBLE);
        if (report.size() == 0)
            viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.no_report_poster, null);
        else {
            Toast.makeText(this, "size " + report.size(), Toast.LENGTH_SHORT).show();
            ViewPager viewPager = viewGroup.findViewById(R.id.report_view_pager);
            viewPager.setAdapter(new ReportPageAdapter(report, this));
            //viewPager.setCurrentItem(0);
        }
        new AlertDialog.Builder(this).setView(viewGroup).show();
    }

    private void addLocationChangeListener(final String paircode) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (currentReference != null && locationChangeListener != null)
            currentReference.removeEventListener(locationChangeListener);
        currentReference = database.getReference("location/" + paircode);
        mapFunctionHandler = new MapFunctionalityHandler(mMap, this, paircode);
        //mapFunctionHandler.showLegend((LinearLayout) findViewById(R.id.mapLegendContainer));

        mMap.clear();
        report = mapFunctionHandler.displayCoordinatesInMap();
        fullyLoadedState.reset();
        currentReference.addValueEventListener(locationChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    Toast.makeText(MainActivity.this, "unknown paircode " + paircode, Toast.LENGTH_SHORT).show();
                    return;
                }
                setmMarkers(dataSnapshot, paircode);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setmMarkers(DataSnapshot dataSnapshot, String paircode) {

        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();

        String latitude = (String) value.get("lat");
        String[] spliterLat = latitude.split("=", 2);

        String longtiude = (String) (value.containsKey("lng") ? value.get("lng") : value.get("lon"));
        String[] spliterlong = longtiude.split("=", 2);

        /*String speeds= (String) value.get("speed");
        String [] spliterspeed=speeds.split("=",2);*/

        /*String device= (String) value.get("devid");
        String [] spliterdevice=device.split("=",2);*/


        lat = Double.parseDouble(spliterLat[1]);
        lon = Double.parseDouble(spliterlong[1]);

        if (locationDatabase.insertNewCoordinateInfo(lat, lon, (int) ((Calendar.getInstance().getTimeInMillis() - midnightCalendar.getTimeInMillis()) / 1000), paircode))
            mapFunctionHandler.addSingleCoordinate();
        currentLocation = new LatLng(lat, lon);
        if (isShowingPlainCoordinate)
            ((TextView) findViewById(R.id.coordinate_aid)).setText("Lat:" + lat + " Lon:" + lon);
        if (currentMarker != null)
            if (previousMarker != null)
                previousMarker.remove();
        previousMarker = mMap.addMarker(currentMarker = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_emoji)).position(currentLocation));
        fullyLoadedState.setData(paircode);
        fullyLoadedState.setB(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, preferedZoom));
    }

    public void openAssociatedApp() {
        if (lat == 0 && lon == 0)
            return;
        String text = "Lat:" + lat + " Lon:" + lon;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(this, "Coordination coppied to clipboard", Toast.LENGTH_SHORT).show();
        if (!isShowingPlainCoordinate)
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    String.format("geo:%f,%f?q=%f,%f", lat, lon, lat, lon)
            )));
    }
    /*public void getDistanceFromUser(){
        if(myLat==0 && myLon ==0)
            return;
        Location locationA = new Location("point A");
        locationA.setLatitude(lat);
        locationA.setLongitude(lon);

        Location locationB = new Location("point B");
        locationB.setLatitude(myLat);
        locationB.setLongitude(myLon);

        float distance = locationA.distanceTo(locationB)/100000;

        TextView textDistance=findViewById(R.id.get_distance);
        textDistance.setText("Air Distance : "+String.format("%.2f",distance)+" km");


    }*/

    public void onNetworkAvailabilityChange(boolean isAvailable) {
        findViewById(R.id.noInternetBanner).setVisibility(isAvailable ? View.GONE : View.VISIBLE);
        if (!isAvailable)
            findViewById(R.id.children_loading_window).setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data == null)
            return;
        if (requestCode == SettingResultRequestCode)
            isShowingPlainCoordinate = !data.getBooleanExtra("isShowingOpenAppButton", true);
        if (!isShowingPlainCoordinate)
            ((TextView) findViewById(R.id.coordinate_aid)).setText("Open in Associated App");
        else if (lat != 0 && lon != 0)
            ((TextView) findViewById(R.id.coordinate_aid)).setText("Lat:" + lat + " Lon:" + lon);
    }

}



