package org.expertprogramming.taskplaceremainder.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;

import org.expertprogramming.taskplaceremainder.Activities.SignupActivity;
import org.expertprogramming.taskplaceremainder.Database.DatabaseHelper;
import org.expertprogramming.taskplaceremainder.Helper.DialogHelper;
import org.expertprogramming.taskplaceremainder.LocationService.LocationRequestHelper;
import org.expertprogramming.taskplaceremainder.R;
import org.expertprogramming.taskplaceremainder.Model.TaskDetails;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

/**
 * Created by pramod on 10/2/18.
 */

public class NavMapFragment extends Fragment implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {
    View view;
    LatLng destLatLng;
    double currlat, currlng;
    ArrayList<Double> destlat, destlng;
    ArrayList<String> taskTitle;
    LatLng latLng;
    GoogleMap mMap;
    Snackbar snackbar = null;
    DialogHelper dialogHelper;
    ArrayList<String> Places;
    Button start, stop;
    boolean flag = true;

    @SuppressLint("MissingPermission")
    public View onCreateView(LayoutInflater inflater, ViewGroup conatainer, Bundle b) {
        view = inflater.inflate(R.layout.activity_navmapfragment, conatainer, false);
        destlat = new ArrayList<>();
        destlng = new ArrayList<>();
        Places = new ArrayList<>();
        taskTitle = new ArrayList<>();
        dialogHelper=new DialogHelper(getActivity());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SupportMapFragment mapFragment = (SupportMapFragment) (NavMapFragment.this).getChildFragmentManager().findFragmentById(R.id.navmapfragment);
                mapFragment.getMapAsync(NavMapFragment.this);
            }
        }, 300);
        start = view.findViewById(R.id.startdirectionButton);
        stop = view.findViewById(R.id.stopdirectionButton);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        start.setEnabled(false);
        stop.setEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Map");
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
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(21.7679, 78.8718), 4));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        addMarkers(true);
    }

    public void addMarkers(boolean flag) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        DatabaseHelper db = new DatabaseHelper(getActivity());
        //if requesting trigger is false then only add marker on map
        if (LocationRequestHelper.getRequestingTrigger(getActivity()) == false) {
            for (TaskDetails details : db.fetchData()) {
                latLng = new LatLng(Double.valueOf(details.getLat()), Double.valueOf(details.getLng()));
                mMap.addMarker(new MarkerOptions().position(latLng).title(details.getTaskTitle()).snippet(details.getPlace()));
                mMap.addCircle(new CircleOptions().center(latLng).radius(Integer.parseInt(sharedPreferences.getString("radius", "100"))).fillColor(0x220000FF).strokeColor(Color.BLUE).strokeWidth(1));
                if(flag) {
                    destlat.add(Double.valueOf(details.getLat()));
                    destlng.add(Double.valueOf(details.getLng()));
                    Places.add(details.getPlace());
                    taskTitle.add(details.getTaskTitle());
                }
            }
            if(flag) {
                dialogHelper.showProgressDialog("Getting your current location...may take some time");
                snackbar = Snackbar.make(getActivity().findViewById(R.id.linearnavmap), "Get the direction.....", Snackbar.LENGTH_INDEFINITE).setAction("Get Direction"
                        , new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDialog();
                            }
                        });
            }
            //if task is enable show pg for getting curr location

        } else {
            snackbar = Snackbar.make(getActivity().findViewById(R.id.linearnavmap), "Please first enable your task", Snackbar.LENGTH_INDEFINITE).setAction("Activate"
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Fragment fragment = new ViewTaskFragment();
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();
                        }
                    });
            snackbar.show();

        }
    }

    public void getDirectionLib(final int listPos) {
        //clear all the poly made on map
        mMap.clear();
        destLatLng = new LatLng(destlat.get(listPos), destlng.get(listPos));
        final LatLng currLatlng = new LatLng(currlat, currlng);
        GoogleDirection.withServerKey(getString(R.string.directionapikey))
                .from(currLatlng)
                .to(destLatLng)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            Route route = direction.getRouteList().get(0);
                            //mMap.addMarker(new MarkerOptions().position(currLatlng));
                            mMap.addMarker(new MarkerOptions().position(destLatLng).snippet(Places.get(listPos)).title(taskTitle.get(listPos)));
                            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                            mMap.addPolyline(DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.RED));

                        }
                        mMap.addMarker(new MarkerOptions().position(currLatlng).title("Your Location"));
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Toasty.error(getActivity(), "Can't show direction", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void onDestroy() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();

    }

    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //if (key.equals(LocationRequestHelper.LOCATION_UPDATES)){
        //getting curr location
        String currlatlng = LocationRequestHelper.getLocationRequesting(getActivity());
        String[] curr = currlatlng.split(":");
        //end
        currlat = Double.parseDouble(curr[0]);
        currlng = Double.parseDouble(curr[1]);
        dialogHelper.dismissProgressDialog();

        if(flag==true){
            start.setEnabled(true);
            stop.setEnabled(false);
        }
        else{
            start.setEnabled(false);
            stop.setEnabled(true);
        }
        //}
    }
    public void showDialog() {
        ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, Places);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setTitle("Get Direction")
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDirectionLib(which);
                    }
                });
        builder.create();
        builder.show();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startdirectionButton:
                flag=false;
                showDialog();
                start.setEnabled(false);
                stop.setEnabled(true);
                break;
            case R.id.stopdirectionButton:
                flag=true;
                mMap.clear();
                addMarkers(false);
                start.setEnabled(true);
                stop.setEnabled(false);
                break;
        }
    }
}