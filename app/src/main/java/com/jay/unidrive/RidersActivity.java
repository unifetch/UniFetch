package com.jay.unidrive;

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.SphericalUtil;
import com.jay.unidrive.model.Direction.FetchUrl;
import com.jay.unidrive.model.Direction.TaskLoadedCallback;
import com.jay.unidrive.model.RouteInfo;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.Objects;


public class RidersActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, TaskLoadedCallback {

    int AUTOCOMPLETE_REQUEST_CODE = 1;

    private static final String TAG = "RidersActivity";
    private GoogleMap mMap;
    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private Location lastKnownLocation;
    private Marker locationMarker;
    private Marker originMarker;
    private Place destination;
    private Place origin;
    private Marker destinationMarker;
    private ParseUser user;
    private boolean navigating = false;
    private LinearLayout container;
    private TextView distanceTextView;
    private TextView destinationNameTextView;
    private TextView originNameTextView;
    private String destinationName;
    private Button fetchButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    Polyline currentPolyline;
    LatLng currentLocation;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riders);

        drawerLayout = findViewById(R.id.drawer_layout);
        fetchButton = findViewById(R.id.fetchButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        setupMenu();
        setupTransition();

        user = ParseUser.getCurrentUser();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initializeSearchBar();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateMap(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(RidersActivity.this, latLng.latitude +","+latLng.longitude, Toast.LENGTH_SHORT).show();
            }
        });
        getLocation();
    }

    private void initializeSearchBar() {
        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyA9QNjBgd0mIabGWyboE_ZrTPfNdn1RJi4");

        // Origin Search Bar
        AutocompleteSupportFragment autocompleteFragmentOrigin = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_origin);

        // Specify the types of place data to return.
        autocompleteFragmentOrigin.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragmentOrigin.setLocationBias(RectangularBounds.newInstance(
                new LatLng(3.215299, 101.748198),
                new LatLng(3.218896, 101.751801)));
        autocompleteFragmentOrigin.setCountry("MY");

        // Origin Place Listener
        autocompleteFragmentOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                origin = place;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        //Destination Search Bar
        AutocompleteSupportFragment autocompleteFragmentDest = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_dest);

        // Specify the types of place data to return.
        autocompleteFragmentDest.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragmentDest.setLocationBias(RectangularBounds.newInstance(
                new LatLng(3.215299, 101.748198),
                new LatLng(3.218896, 101.751801)));
        autocompleteFragmentDest.setCountry("MY");


// Set up a PlaceSelectionListener to handle the response.
        autocompleteFragmentDest.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.

                destination = place;

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupMenu() {
        navigationView = findViewById(R.id.nv);
//        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.openDrawer, R.string.closeDrawer);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Toast.makeText(RidersActivity.this, "Camera", Toast.LENGTH_SHORT).show();
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        Toast.makeText(RidersActivity.this, "Camera", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.nav_settings:
                        Intent intent = new Intent(RidersActivity.this, SettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_logout:
                        ParseUser.logOut();
                        finish();
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void setupTransition() {
        container = findViewById(R.id.container);
        LayoutTransition transition = new LayoutTransition();
        container.setLayoutTransition(transition);
    }

    public void expandDetails(View view) {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View addView = layoutInflater.inflate(R.layout.navigating_details_layout, null);
        container.addView(addView);
        view.setVisibility(View.INVISIBLE);
        distanceTextView = findViewById(R.id.distanceTextView);
        destinationNameTextView = findViewById(R.id.destPlace_name_textView);
        originNameTextView = findViewById(R.id.originPlace_name_textView);

        ImageView cancelButton = findViewById(R.id.cancelImageView);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private Bitmap getMarkerBitmap(int id) {

        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(id);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 120, 120, false);
        return smallMarker;
    }

    private void updateMap(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if (locationMarker != null) locationMarker.remove();
        locationMarker = addMarker(new MarkerOptions().position(userLocation).title("Your Location").icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(R.drawable.human_icon))));
        currentLocation = userLocation;

        ParseGeoPoint currentLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        user.put("location", currentLocation);
        user.saveInBackground();
    }

    private void recenterCamera(){
        if (lastKnownLocation != null) {
            LatLng userLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18));
        }
    }

    public void moveCameraWhenPressed(View view) {
        getLocation();
    }

    private void moveCamera(Marker marker1, Marker marker2){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            //the include method will calculate the min and max bound.
            builder.include(marker1.getPosition());
            builder.include(marker2.getPosition());

            LatLngBounds bounds = builder.build();

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels - 300;
            int padding = (int) (width * 0.15); // offset from edges of the map 15% of screen

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            mMap.animateCamera(cu);
    }

    public final Marker addMarker (MarkerOptions options) {
        return mMap.addMarker(options);
    }

    private Location getLocation() {
        lastKnownLocation = null;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 50, locationListener);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null){
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation != null){
                updateMap(lastKnownLocation);
            }
        }
        recenterCamera();
        return lastKnownLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 50, locationListener);

                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastKnownLocation != null) {

                        updateMap(lastKnownLocation);
                        getLocation();
                    }
                }

            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private String getUrl(LatLng origin, Place dest, String directionMode){
        String str_origin = "origin=" +origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.getLatLng().latitude + "," + dest.getLatLng().longitude;
        String mode = "mode=" + directionMode;
        String parameter = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameter + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null){
            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
        if (originMarker != null) originMarker.remove();
        originMarker = addMarker(new MarkerOptions().position(Objects.requireNonNull(origin.getLatLng())).title("Your Location").icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmap(R.drawable.map_marker))));

        if (destinationMarker != null) destinationMarker.remove();
        destinationMarker = addMarker(new MarkerOptions().position(Objects.requireNonNull(destination.getLatLng())).title("Your Destination"));
        moveCamera(originMarker, destinationMarker);
        navigating = true;
        populate((RouteInfo) values[1]);
        String originString = origin.getLatLng().latitude + "," + origin.getLatLng().longitude;
        String destinationString = destination.getLatLng().latitude + "," + destination.getLatLng().longitude;
        user.put("origin", originString);
        user.put("destination", destinationString);
        user.saveInBackground();
       }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (navigating){
            currentPolyline.remove();
            originMarker.remove();
            destinationMarker.remove();
            navigating = false;
            container.removeAllViews();
            fetchButton.setVisibility(View.VISIBLE);
            fetchButton.setTop(R.id.place_autocomplete_fragment_dest);
        } else {
            finishAffinity();
        }
    }

    public void fetch(View view){
        if (origin != null && destination != null) {
            Log.i(TAG, "Place: " + destination.getName() + ", " + destination.getId());
            String url = getUrl(Objects.requireNonNull(origin.getLatLng()), destination, "driving");
            Log.i(TAG, "Place direction: " + url);
            expandDetails(view);
            new FetchUrl(RidersActivity.this).execute(url, "driving");
        } else {
            Toast.makeText(this, "Please select origin and destination", Toast.LENGTH_SHORT).show();
        }

    }

    public void searchLocation(View view){
        Intent intent = new Intent(RidersActivity.this, SearchActivity.class);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    public void switchActivity(View view){
        Intent intent = new Intent(RidersActivity.this, DriverActivity.class);
        startActivity(intent);
        finish();
    }

    public void openDrawer(View view){
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    private void populate(RouteInfo routeInfo){
        distanceTextView.setText(routeInfo.getDistance()/1000 + " km");
        destinationNameTextView.setText("to : " + destination.getName());
        originNameTextView.setText("from : " + origin.getName());
    }

    //    private ResultCallback<PlaceBuffer> updatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
//        @Override
//        public void onResult(@NonNull PlaceBuffer places) {
//            if (!places.getStatus().isSuccess()){
//                Log.d(TAG, "onResult: Place query did not complete successfully." + places.getStatus().toString());
//                places.release();
//            }
//            final Place place = places.get(0);
//
//            Log.d(TAG, "onResult: place details : " + place.getName());
//            final String placeId = place.getId();
//            Toast.makeText(RidersActivity.this, placeId, Toast.LENGTH_SHORT).show();
//            places.release();
//        }
//    };
}
