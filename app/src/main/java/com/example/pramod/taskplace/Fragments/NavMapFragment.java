package com.example.pramod.taskplace.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.LocationService.LocationRequestHelper;
import com.example.pramod.taskplace.Model.CurrentUserData;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.Model.TaskDetails;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by pramod on 10/2/18.
 */

public class NavMapFragment extends Fragment implements OnMapReadyCallback,LocationListener {
    View view;
    LatLng currlatlng;
    double currlat,currlng;
    ArrayList<Double> destlat,destlng;
    LatLng latLng;
    LocationManager locationManager;
    GoogleMap mMap;
    Button b;
    Snackbar snackbar=null;
    @SuppressLint("MissingPermission")
    public View onCreateView(LayoutInflater inflater, ViewGroup conatainer, Bundle b){
        view=inflater.inflate(R.layout.activity_navmapfragment,conatainer,false);
        destlat=new ArrayList<>();
        destlng=new ArrayList<>();
        locationManager= (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,10,10,this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SupportMapFragment mapFragment = (SupportMapFragment)(NavMapFragment.this).getChildFragmentManager().findFragmentById(R.id.navmapfragment);
                mapFragment.getMapAsync(NavMapFragment.this);
            }
        },300);
        //b=getActivity().findViewById(R.id.directionButton);
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Map");
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getMyLocation();
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(21.7679,78.8718),4));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        addMarkers();
    }
    public void addMarkers(){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        DatabaseHelper db=new DatabaseHelper(getActivity());
        //if requesting trigger is false then only add marker on map
        if (LocationRequestHelper.getRequestingTrigger(getActivity()) == false) {
            for (TaskDetails details : db.fetchData()) {
                latLng = new LatLng(Double.valueOf(details.getLat()), Double.valueOf(details.getLng()));
                mMap.addMarker(new MarkerOptions().position(latLng).title(details.getPlace()).snippet(details.getTaskTitle()));
                mMap.addCircle(new CircleOptions().center(latLng).radius(Integer.parseInt(sharedPreferences.getString("radius", "100"))).fillColor(0x220000FF).strokeColor(Color.BLUE).strokeWidth(1));
                destlat.add(Double.valueOf(details.getLat()));
                destlng.add(Double.valueOf(details.getLng()));
            }
        }else{
            snackbar=Snackbar.make(getActivity().findViewById(R.id.linearnavmap),"first enable your task..",Snackbar.LENGTH_INDEFINITE).setAction("Activate"
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Fragment fragment=new ViewTaskFragment();
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,fragment).commit();
                        }
                    });
            snackbar.show();

        }
    }
    public GeoApiContext getGeoApiContext(){
        GeoApiContext geoApiContext=new GeoApiContext();
        geoApiContext.setQueryRateLimit(3);
        geoApiContext.setApiKey("AIzaSyAg4vdxuQp5yUmNrMhQtrUhdtAOr9b7KIU");
        geoApiContext.setConnectTimeout(1, TimeUnit.SECONDS);
        geoApiContext.setReadTimeout(1,TimeUnit.SECONDS);
        geoApiContext.setWriteTimeout(1,TimeUnit.SECONDS);
        return geoApiContext;
    }
    public void getDirection(){
        DateTime now=new DateTime();
        try {
            DirectionsResult result= DirectionsApi.newRequest(getGeoApiContext())
                    .mode(TravelMode.DRIVING)
                    .origin(new com.google.maps.model.LatLng(currlat,currlng))
                    .destination(new com.google.maps.model.LatLng(destlat.get(0),destlng.get(0)))
                    .waypoints(new com.google.maps.model.LatLng(currlat,currlng),new com.google.maps.model.LatLng(destlat.get(0),destlng.get(0)))
                    .departureTime(now)
                    .await();
            addPolygon(result,mMap);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addPolygon(DirectionsResult result,GoogleMap map){
        ArrayList<LatLng> decodedPath= (ArrayList<LatLng>) PolyUtil.decode(result.routes[0].overviewPolyline.getEncodedPath());
        map.addPolygon(new PolygonOptions().addAll(decodedPath).strokeColor(Color.GREEN));

    }

    @Override
    public void onLocationChanged(Location location) {
        currlat=location.getLatitude();
        currlng=location.getLongitude();
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
    public void onDestroy(){
        if(snackbar!=null){
            snackbar.dismiss();
        }
        super.onDestroy();

    }
}
