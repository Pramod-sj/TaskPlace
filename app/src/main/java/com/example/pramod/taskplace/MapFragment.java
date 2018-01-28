package com.example.pramod.taskplace;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pramod on 18/1/18.
 */

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
    View view;
    GoogleMap mMap;
    int dataCount=-1;
    int d=0;
    FloatingActionButton fab;
    DrawerLayout drawer;
    protected ArrayList<Geofence> mGeofenceList;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    public static final HashMap<String, LatLng> LANDMARKS = new HashMap<String, LatLng>();
    HashMap<String, Marker> markerList = new HashMap<String, Marker>();
    HashMap<String, Circle> circleList = new HashMap<String, Circle>();
    ArrayList<String> ids=new ArrayList<String>();
    GoogleApiClient mGoogleApiClient;
    DatabaseReference taskDetailsCloudEndPoint;
    SharedPreferences preferences;
    FloatingActionMenu menu;
    Context context;
    GeofenceMethods geofenceMethods;
    com.github.clans.fab.FloatingActionButton fab1,fab2;
    @SuppressLint("ValidFragment")
    public MapFragment(Context context,ArrayList<String> ids){
        this.context=context;
        this.ids=ids;
    }
    public MapFragment(){}
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_map_fragment, null, false);
        context=getActivity().getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SupportMapFragment supportMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        supportMapFragment.getMapAsync(this);
        taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        buildGoogleApiClient();
        menu = (FloatingActionMenu)view.findViewById(R.id.fabmenu);
        menu.setClosedOnTouchOutside(true);
        fab1=view.findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dataCount!=0) {
                    geofenceMethods=new GeofenceMethods(context,mGoogleApiClient,mMap,LANDMARKS,mGeofenceList);

                    //Toast.makeText(getActivity().getApplicationContext(), "started", Toast.LENGTH_SHORT).show();
                    geofenceMethods.startGeofences();
                }
                else{
                    Snackbar s=Snackbar.make(getActivity().findViewById(R.id.linearlayoutmap),"Please set some task",Snackbar.LENGTH_SHORT);
                    s.setAction("set task", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Fragment fragment = new SetTask();
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();
                        }
                    });
                    s.show();
                    menu.animate().translationYBy(s.getView().getHeight());
                }
            }
        });
        fab2=view.findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dataCount!=0) {
                    geofenceMethods=new GeofenceMethods(context,mGoogleApiClient,mMap,LANDMARKS,mGeofenceList);

                    //Toast.makeText(getActivity().getApplicationContext(), "stop", Toast.LENGTH_SHORT).show();
                    geofenceMethods.removeGeofence(ids);
                }
            }
        });
        return view;
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_PERMISSIONS_REQUEST_CODE);
            }
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if(preferences.getString("APPSTATUS",null).equals("enter")) {
            Snackbar snackbar;
            if (preferences.getString("FLAG", null).equals("notallowed")) {
                snackbar=Snackbar.make(getActivity().findViewById(R.id.linearlayoutmap), "Geofence services is already started", Snackbar.LENGTH_LONG);
                snackbar.show();}
            else{
                snackbar=Snackbar.make(getActivity().findViewById(R.id.linearlayoutmap), "Geofence services is not started", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationPermissionChecker(mGoogleApiClient,getActivity());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }
    @Override
    public void onResume(){
        super.onResume();
        getGeofencesFromDatabase();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private void addMarker(String key, LatLng latLng) {
        mMap.addMarker(new MarkerOptions()
                .title("G:" + key)
                .position(latLng));
        mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(100.0f)
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor(Color.argb(80, 150,150,150)));
    }

    //Geofence Coding
    public void getGeofencesFromDatabase(){
        taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        //Toast.makeText(getActivity().getApplicationContext(),"Getting data",Toast.LENGTH_SHORT).show();

        taskDetailsCloudEndPoint.child(new CurrentUserData(getActivity().getBaseContext()).getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    dataCount=0;
                    Log.i("not populating", "No data");
                    return;
                }
                dataCount= (int) dataSnapshot.getChildrenCount();
                if(!ids.isEmpty()){
                    ids.clear();
                }
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    float longi = ds.child("latlng").child("longitude").getValue(Float.class);
                    float lati = ds.child("latlng").child("latitude").getValue(Float.class);
                    String place_n = ds.child("place").getValue(String.class);
                    LANDMARKS.put(place_n, new LatLng(lati, longi));
                    //adding id i.e.place name for removing Geofences
                    ids.add(place_n);
                }
                //Toast.makeText(getActivity().getApplicationContext(),"Started populating", Toast.LENGTH_SHORT).show();
                populateGeofences();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
    public void populateGeofences() {
        mGeofenceList=new ArrayList<Geofence>();
        if(!mGeofenceList.isEmpty()){
            mGeofenceList.clear();
        }
        try {
            for (Map.Entry<String, LatLng> entry : LANDMARKS.entrySet()) {
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(entry.getKey())
                        .setCircularRegion(entry.getValue().latitude, entry.getValue().longitude, 100.0f)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());
            }
            Log.i("FLAG",preferences.getString("FLAG",null));
            if(preferences.getString("FLAG", null).equals("notallowed")){
                for (Map.Entry<String, LatLng> entry : LANDMARKS.entrySet()) {
                    addMarker(entry.getKey(),new LatLng(entry.getValue().latitude, entry.getValue().longitude));
                }
            }
        }
        catch (Exception e){}
        Toast.makeText(getActivity().getApplicationContext(),"Populated",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        mMap.clear();
    }
    @Override
    public void onPause(){
        super.onPause();
        mMap.clear();
    }
    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
    public static void locationPermissionChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity, 1000);
                        } catch (Exception e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


}
