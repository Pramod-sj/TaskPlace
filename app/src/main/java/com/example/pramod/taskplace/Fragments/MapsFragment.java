package com.example.pramod.taskplace.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pramod.taskplace.Activities.MainActivity;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.TaskPlace;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.example.pramod.taskplace.R.layout.activity_maps;

public class MapsFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnMapClickListener{
    SupportPlaceAutocompleteFragment supportPlaceAutocompleteFragment;
    private GoogleMap mMap;
    View view;
    String placeSelected;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        view=inflater.inflate(activity_maps,container,false);
        //going back to previous page i.e. setTask.class
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN){
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new SetTaskFragment()).commit();
                    Log.i("BACK","going back to setTask()");
                    return true;
                }
                return false;
            }
        });




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        supportPlaceAutocompleteFragment= (SupportPlaceAutocompleteFragment)this.getChildFragmentManager().findFragmentById(R.id.place);
        supportPlaceAutocompleteFragment.setHint("Search place");
        supportPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                Marker marker=mMap.addMarker(new MarkerOptions().position(place.getLatLng()).snippet("Click me").title("select me as your location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15));
                marker.showInfoWindow();
            }

            @Override
            public void onError(Status status) {

            }
        });

        return view;
    }
    public void onResume(){
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    public void onStop(){
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        super.onStop();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},10);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(21.7679,78.8718),3));
        mMap.setOnMapClickListener(this);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Geocoder geocoder=new Geocoder(getActivity(), Locale.getDefault());
                try {
                    ArrayList<Address> addresses= (ArrayList<Address>) geocoder.getFromLocation(marker.getPosition().latitude,marker.getPosition().longitude,1);
                    placeSelected= String.valueOf(addresses.get(0).getAddressLine(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(!placeSelected.equals(null)){
                    //if matchPlaces(place) returns false then only insert new place...
                    if(TaskPlace.getDatabaseHelper().matchPlaces(placeSelected)==false) {
                        TaskPlace.getDatabaseHelper().insertPlace(placeSelected,marker.getPosition().latitude,marker.getPosition().longitude);
                    }
                }
                Log.i("lat", String.valueOf(marker.getPosition().latitude));
                Fragment f=new SetTaskFragment();
                Bundle b=new Bundle();
                b.putString("Address",placeSelected);
                b.putDouble("lat",marker.getPosition().latitude);
                b.putDouble("lng",marker.getPosition().longitude);
                f.setArguments(b);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,f).commit();

            }

        });

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }
    @Override
    public void onMapClick(LatLng latLng) {
        mMap.clear();
        Marker marker=mMap.addMarker(new MarkerOptions().position(latLng).snippet("Click me").title("select me as your location"));
        marker.showInfoWindow();
        Log.i("MAP","YOU JUST CLICK ON MAP");
    }
}
