package com.example.pramod.taskplace;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.pramod.taskplace.Activities.SetTask;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
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

public class MapsActivity extends Fragment implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {
    SupportPlaceAutocompleteFragment supportPlaceAutocompleteFragment;
    private GoogleMap mMap;
    View view;
    String placeSelected;
    String Latlng;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        view=inflater.inflate(activity_maps,container,false);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        supportPlaceAutocompleteFragment= (SupportPlaceAutocompleteFragment)this.getChildFragmentManager().findFragmentById(R.id.place);
        supportPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).snippet("Click me").title("select me as your location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15));
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
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
                Toast.makeText(getActivity().getApplicationContext(),placeSelected,Toast.LENGTH_SHORT).show();
                Log.i("lat", String.valueOf(marker.getPosition().latitude));
                Fragment f=new SetTask();
                Bundle b=new Bundle();
                b.putString("Address",placeSelected);
                b.putDouble("lat",marker.getPosition().latitude);
                b.putDouble("lng",marker.getPosition().longitude);
                f.setArguments(b);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,f).commit();
            }
        });

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).snippet("Click me").title("select me as your location"));

    }
}
