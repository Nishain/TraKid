package ndds.com.trakidhome;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
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
    private float preferedZoom = 20;
    private SQLIteLocationDataHandler locationDatabase;
    private Calendar midnightCalendar;
    private MapFunctionalityHandler mapFunctionHandler;
    private ArrayList<String> report = new ArrayList<String>();

    private MarkerOptions currentMarker;
    private InternetChangeListener internetChangeListener;
    private Marker previousMarker;

    /*the purpose of variable 'fullyLoadedState' is to listen if both condition
     * A and B is set to true,
     * flag A - is children/pair code loaded into SQLite database
     * flag B - is marker denoting the current location of the tracking
     *          is added to Google map.
     * if(A && B) then a abstract callback is triggered indicating child's name can
     * can be added to marker's tille which is exist in the database*/
    private StateObject fullyLoadedState = new StateObject() {
        @Override
        void onBothConditionStatisfied() {
            setMarkerTitle();
        }
    };
    private boolean isLegendVisible = false;
    private LatLng myDevicelocation;

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
        mLocationPermissionGranted = ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        /*Warning this activity should be start directly as this activity require details from authenticated
         * user.The details are provided by the SignIn activity.So this activity should be
         * require details filled in intent object associated with this activity*/
        String username = (String) getIntent().getExtras().get(EXTRA_USERNAME);
        String email = (String) getIntent().getExtras().get(EXTRA_EMAIL);
        String url = (String) getIntent().getExtras().get(EXTRA_URL);
        String UID = getIntent().getStringExtra(EXTRA_UID);
        logoutObject = new SignInActivity();
        locationDatabase = new SQLIteLocationDataHandler(this);
        SharedPrefernceManager sharedPrefernce = new SharedPrefernceManager(this);
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
                /*when children/paircode(s) is finished fetching from the database
                 * the flag is A is set to true indicating now SQLite database contains details
                 * of the pair code(s) for this user*/
                fullyLoadedState.setA(true);
            }

            @Override
            void onDefaultPaircodeChanged(String defaultPairCode) {
                /*whenever the active pair code updates this callback is called.
                 * And in a rare case where if the default pair code is not assigned in the
                 * firebase database then user is prompt to select a pair code.
                 * This happens when user doesn't have any assigned paircode paired to the user*/
                if (defaultPairCode != null)
                    addLocationChangeListener(defaultPairCode);
                else
                    childHandler.showNoDefaultChildAlert();
            }
        };
        //adding click events for the quick action bottom navigation
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
        findViewById(R.id.coordinate_aid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Please long press button", Toast.LENGTH_SHORT).show();
            }
        });
        checkAndRequestPermissions();
    }

    private void RefreshMapWithNewReport() {
        /*make sure Google map and variable 'mapFunctionHandler' is initialized
         * before continue.The map is cleared to erase the drawings by the previous report
         * and re-add the marker which was erased by mMap.clear() function */
        if (mMap != null && mapFunctionHandler != null) {
            mMap.clear();
            report = mapFunctionHandler.displayCoordinatesInMap();
            if (currentMarker != null)
                previousMarker = mMap.addMarker(currentMarker);
        }
    }

    private void eraseMap() {
        new AlertDialog.Builder(this).setMessage(getString(R.string.clearHistoryConfirmationMsg))
                .setTitle("Confirmation")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.clear();
                        if (mapFunctionHandler != null && currentMarker != null) {
                            report.clear();
                            mapFunctionHandler.clearPreviousCoordinate();
                            previousMarker = mMap.addMarker(currentMarker);
                        }
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
    private boolean checkAndRequestPermissions() {
        /*this function automatically read the manifest file get the demanded permissions
         * and check each of those permission are granted by the user.
         * The permissions which stored on a buffer variable and request permissions in
         * the buffer from the user*/
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
        children.add(" +child "); //adding the add child button to child selection menu
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
            /*the item with position index of 0 is the child add button which is
             * used to pair a new device hence the index of the corresponding in SQLite
             * database should be position - 1*/
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
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    /*update the variable 'disabledFunctionalities' about the description
                     * about function disabilities when the relevant permission is denied*/
                    if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        disabledFunctionalities += "cannot get current location of this device.";
                        mLocationPermissionGranted = false;
                    } else if (permissions[i].equals(Manifest.permission.RECEIVE_SMS))
                        disabledFunctionalities += "cannot receive SOS through SMS.";

                }
            }
            /*showing the user the functions which will not be operated in the
             * application when the respective demanded permission is not granted*/
            if (disabledFunctionalities.length() > 0)
                new AlertDialog.Builder(this).setTitle("Permissions missing!")
                        .setMessage(disabledFunctionalities
                                + "\nyou can re-assign the permission in navigation menu")
                        .show();
            if (mLocationPermissionGranted)
                addMyLocationButton();
        }
    }

    private void addMyLocationButton() {
        if (mMap == null) {
            return;
        }
        try {
            //enable/add the My location button only if ACCESS_FINE_LOCATION permission is granted
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
            repositionLocationButton();
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
                            myLat = mLastKnownLocation != null ? mLastKnownLocation.getLatitude() : 0;
                            myLon = mLastKnownLocation != null ? mLastKnownLocation.getLongitude() : 0;
                            if (myLon == 0 && myLat == 0)
                                return;
                            myDevicelocation = new LatLng(myLat, myLon);
                            updateDistanceBetweenAParentAndChild();
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

        /*placing the bottom Quick action navigation just above the default system bottom
         *navigation menu so that the widget will not overlap the default bottom naviagtion bar*/
        //get dimension of height of the default navigation bar
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
        } else if (id == R.id.settings) {
            showSettings();
        } else if (id == R.id.clearHistory) {
            eraseMap();
        } else if (id == R.id.logout) {
            logoutObject.signOut();
            /*remove the currently attached listener in the firebase and
             * stop listen for location updates and signing out the user*/
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
            //help tour will only start when only no tour is show in current moment
            new HelpGuider(this).popHelp();
    }

    private void setMapListeners() {

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                /*update the global variable 'myDevicelocation' everytime My Location button
                 * clicked in the Google map*/
                getDeviceLocation();
                //checking if GPS is turned on when my Location button is clicked
                LocationManager lm = ((LocationManager) getSystemService(Context.LOCATION_SERVICE));
                if (!(lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))
                    new AlertDialog.Builder(MainActivity.this).setTitle("Location disabled")
                            .setMessage("Please turn on location information to get your current location")
                            .show();
                return false;
            }
        });
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                //show the map legend when the user not interacting with the map
                Animation animation = android.view.animation.AnimationUtils.loadAnimation(MainActivity.this, R.anim.shift_open);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        /*Only when this flag is true the legend will
                         * will collapsed.This is to avoid collapsing the legend when it is
                         * not already opened*/
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
                /*when user is interacting with the map hide or collapse
                 * the map legend so that user can clearly see the map
                 * without an obstruction*/
                if (isLegendVisible) {
                    findViewById(R.id.mapLegendContainer).startAnimation(android.view.animation.AnimationUtils.loadAnimation(MainActivity.this, R.anim.shift_close));
                    isLegendVisible = false;
                }

            }
        });
    }

    public void updateDistanceBetweenAParentAndChild() {
        /*This function is used to estimate the distance between the user device
         * and child's tracking device*/
        if (myDevicelocation != null && currentLocation != null) {
            /*getting the distance in km*/
            double distance = MapFunctionalityHandler.distanceMoved(myDevicelocation, currentLocation) / 1000;
            findViewById(R.id.distance_user_txt).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.distance_user_txt)).setText(String.format("you are away by %.2f Km", distance));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMapListeners();
        //load the paircodes for the respective user from firebase
        loadChildren();
        getDeviceLocation();
        /*add built-in my location button to the Google map
         *when permission are available*/
        addMyLocationButton();

    }

    private void repositionLocationButton() {
        /*Since My location button is google map built in button
         * it is not possible correctly position the button in xml layut
         * so needs to be placed programmatically*/
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        rlp.setMargins(30, 0, 7, 0);
    }

    private void showReport() {
        ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_report_page, null);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        /*check if internet connection is available Geocoder will fails to get the address
         * from Latitude/Longitude coordination*/
        boolean isConectedToNetwork = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        viewGroup.findViewById(R.id.no_internetAccess_text).setVisibility(isConectedToNetwork ? View.GONE : View.VISIBLE);
        if (report.size() == 0)
            /*if no report detail is generated then view the user
             * with a nice poster indicating no report generated*/
            viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.no_report_poster, null);
        else {
            /*if reports available then inflate viewPager to view details of the map*/
            ViewPager viewPager = viewGroup.findViewById(R.id.report_view_pager);
            viewPager.setAdapter(new ReportPageAdapter(report, this));
        }
        new AlertDialog.Builder(this).setView(viewGroup).show();
    }

    private void addLocationChangeListener(final String paircode) {
        /*this function is called whenever a the active pair code changes.This method add a
         * new listener to firebase database to listen to location changes for the new active pair code*/

        /*we have to first remove the listener attached to previous pair code or else
         * listener will continue to provide locations of the previous active pair ode*/
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (currentReference != null && locationChangeListener != null)
            currentReference.removeEventListener(locationChangeListener);

        currentReference = database.getReference("location/" + paircode);
        mapFunctionHandler = new MapFunctionalityHandler(mMap, this, paircode);

        //we have to clear the drawings of maps drawn by previous pair code
        mMap.clear();
        /*every time is function is triggered a new report need to be generated to the user*/
        report = mapFunctionHandler.displayCoordinatesInMap();
        fullyLoadedState.reset();
        currentReference.addValueEventListener(locationChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /*this happens rarely, for the safety precautions in case if the listener listen for
                 * a pair code which is not existing in the firebase database then function
                 * quit without doing anything*/
                if (dataSnapshot.getValue() == null) {
                    Toast.makeText(MainActivity.this, "unknown paircode " + paircode, Toast.LENGTH_SHORT).show();
                    return;
                }

                addMarkerOnMap(dataSnapshot, paircode);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addMarkerOnMap(DataSnapshot dataSnapshot, String paircode) {

        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();

        String latitude = (String) value.get("lat");
        String[] spliterLat = latitude.split("=", 2);

        String longtiude = (String) (value.containsKey("lng") ? value.get("lng") : value.get("lon"));
        String[] spliterlong = longtiude.split("=", 2);

        lat = Double.parseDouble(spliterLat[1]);
        lon = Double.parseDouble(spliterlong[1]);

        if (locationDatabase.insertNewCoordinateInfo(lat, lon, (int) ((Calendar.getInstance().getTimeInMillis() - midnightCalendar.getTimeInMillis()) / 1000), paircode))
            mapFunctionHandler.addSingleCoordinate();
        currentLocation = new LatLng(lat, lon);
        /*update the distance difference between the parent and child's tracking
         * device every time the tracking device's location changes*/
        updateDistanceBetweenAParentAndChild();
        if (currentMarker != null) {
            /*remove the previous marker before adding a new marker to the map*/
            if (previousMarker != null)
                previousMarker.remove();
        }
        previousMarker = mMap.addMarker(currentMarker = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_emoji)).position(currentLocation));

        /*update the address of the new current location of the tracking device*/
        Geocoder geocoder = new Geocoder(this);
        TextView addressText = findViewById(R.id.current_location_adress_txt);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses.size() > 0 && addresses.get(0).getMaxAddressLineIndex() > -1)
                addressText.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            /*if the address is somehow not successfully retrieved then set the
             * text as 'Awaiting'.This is mostly likely if internet connection is not available*/
            addressText.setText("Awaiting");
        }
        addressText.append("\n" + lat + "," + lon);

        /*raised the flag B to true indicating that marker is drawn
         * and passing the data as pair code of the currently drawn marker*/
        fullyLoadedState.setData(paircode);
        fullyLoadedState.setB(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, preferedZoom));
    }

    public void openAssociatedApp() {
        /*if no coordinate is computed this function will ignore and exits.
         * this function coped the current coordination to clipboard as text
         * and and open an associated application*/
        if (lat == 0 && lon == 0)
            return;
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                String.format("geo:%f,%f?q=%f,%f", lat, lon, lat, lon)
        )));
    }

    public void copyCoordinateToCliboard(View v) {
        if (lat == 0 && lon == 0)
            return;
        String text = "Lat:" + lat + " Lon:" + lon;
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Coordination coppied to clipboard", Toast.LENGTH_SHORT).show();
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
        /*indicate no internet connection as a banner in dashboard*/
        findViewById(R.id.noInternetBanner).setVisibility(isAvailable ? View.GONE : View.VISIBLE);
        if (!isAvailable)
            findViewById(R.id.children_loading_window).setVisibility(View.GONE);
    }
}



