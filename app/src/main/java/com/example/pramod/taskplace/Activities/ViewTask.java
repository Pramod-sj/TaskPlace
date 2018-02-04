package com.example.pramod.taskplace.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.pramod.taskplace.Adapters.TaskViewAdapter;
import com.example.pramod.taskplace.Database.FirebaseDatabaseHelper;
import com.example.pramod.taskplace.LocationService.LocationRequestHelper;
import com.example.pramod.taskplace.LocationService.LocationUpdatesBroadcastReceiver;
import com.example.pramod.taskplace.CurrentUserData;
import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.Geofence.GeofenceMethods;
import com.example.pramod.taskplace.Geofence.GeofenceRequestHelper;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.TaskDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class ViewTask extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    ArrayList<Double> distMeter;
    View view;
    CurrentUserData currentUserData;
    DatabaseReference taskDetailsCloudEndPoint;
    GoogleApiClient mGoogleApiClient;
    //array for viewing purpose
    ArrayList<String> contents=new ArrayList<String>();
    ArrayList<String> dates=new ArrayList<String>();
    ArrayList<String> places=new ArrayList<String>();
    HashMap<String,LatLng> LANDMARKS;
    ProgressBar pg1;
    ListView listView;
    Switch aSwitch;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    LinearLayout container;
    ArrayList<TaskDetails> data;
    ArrayList<String> ids=new ArrayList<String>();
    //ArrayAdapter<String> arrayAdapter;
    TaskViewAdapter adapter;
    DatabaseHelper db;
    SQLiteDatabase sql;
    LocationRequest locationRequest;
    PlaceAutocompleteFragment placeAutocompleteFragment;
    @SuppressLint("MissingPermission")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_view_task, container, false);
        db=new DatabaseHelper(getActivity());
        sql=db.getReadableDatabase();
        distMeter=new ArrayList<Double>();
        adapter=new TaskViewAdapter(getActivity().getBaseContext(),contents,dates,places,distMeter);
        adapter.notifyDataSetChanged();
        LANDMARKS=new HashMap<String, LatLng>();
        pg1=view.findViewById(R.id.loading);
        pg1.setVisibility(View.VISIBLE);
        pg1.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF8C00"), PorterDuff.Mode.MULTIPLY);
        currentUserData=new CurrentUserData(getActivity().getBaseContext());
        taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        listView=view.findViewById(R.id.listView);
        aSwitch=view.findViewById(R.id.taskActivator);
        aSwitch.setVisibility(View.VISIBLE);
        switchState();
        if(fetchOfflineData()==0){
            aSwitch.setClickable(false);
            Toast.makeText(getActivity().getApplicationContext(),"No Task",Toast.LENGTH_SHORT).show();
        }
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //disable touch when no data exists
                if(fetchOfflineData()==0){
                    aSwitch.setClickable(false);
                    Toast.makeText(getActivity().getApplicationContext(),"No Task",Toast.LENGTH_SHORT).show();
                }
                GeofenceMethods geofenceMethods=null;
                if (!LANDMARKS.equals(null)) {
                    geofenceMethods = new GeofenceMethods(getActivity(), mGoogleApiClient, LANDMARKS);
                }
                if(isChecked) {
                    if (!LANDMARKS.equals(null)) {
                        Log.i("LANDMARKS",String.valueOf(LANDMARKS));
                        Log.i("IDS",String.valueOf(ids));
                        Log.i("Adding","Wait we are adding");
                        geofenceMethods.populateGeofences();
                        requestLocationUpdates();
                    }
                }
                else {
                    if(!ids.equals(null)) {
                        Log.i("Removing","Wait we are removing");
                        geofenceMethods.removeGeofence(ids);
                        removeLocationUpdates();
                    }
                }
            }
        });

        listView=view.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int index=position;
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setTitle("Task")
                        .setMessage("Do you want to remove this task")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //                        try {
                                //                              Toast.makeText(getApplicationContext(), String.valueOf(data.get(which).taskid), Toast.LENGTH_SHORT).show();
                                //                            }
                                //                              catch (Exception e){
//                                    Toast.makeText(getApplicationContext(), String.valueOf(data.get(which).taskid), Toast.LENGTH_SHORT).show();

                                //}
                                if(!ids.isEmpty()){
                                    ids.clear();
                                }
                                ids.add(data.get(index).getPlace());
                                removeTask(ids);
                                //disable if no data exists
                                if(fetchOfflineData()==0){
                                    aSwitch.setChecked(false);
                                    aSwitch.setClickable(false);
                                    Toast.makeText(getActivity().getApplicationContext(),"No Task",Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setCancelable(false);
                AlertDialog alert=builder.create();
                alert.show();


            }
        });
        Log.i("geoKey",String.valueOf(GeofenceRequestHelper.getGeoReuesting(getActivity())));
        Log.i("locationkey",String.valueOf(LocationRequestHelper.getRequesting(getActivity())));

        buildGoogleApiClient();

        return view;
    }
    private void switchState(){
        if(GeofenceRequestHelper.getGeoReuesting(getActivity())==false && LocationRequestHelper.getRequesting(getActivity())==false) {
            aSwitch.setChecked(true);
        }else{

            aSwitch.setChecked(false);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
        switchState();
        if(LocationRequestHelper.getLocationRequesting(getActivity()).equals(false)){
            requestLocationUpdates();
        }

    }
    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();

    }
    private void createLocationRequest(){
        locationRequest=new LocationRequest();
        locationRequest.setInterval(7000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setMaxWaitTime(7000*3);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    protected synchronized void buildGoogleApiClient() {
        if(mGoogleApiClient!=null){
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(getActivity(), LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("View Task");
    }
    public int fetchOfflineData(){
        data=new ArrayList<TaskDetails>();
        String query="select * from PlaceDatabase";
        Cursor cursor = sql.rawQuery(query, null);
        while(cursor.moveToNext()){
            Log.i("lat",cursor.getString(7));
            TaskDetails d=new TaskDetails();
            d.setPlace(cursor.getString(2));
            d.setLat(cursor.getString(6));
            d.setLng(cursor.getString(7));
            d.setContent(cursor.getString(3));
            d.setTaskDesc(cursor.getString(4));
            d.setTaskdate(cursor.getString(5));
            data.add(d);
            LANDMARKS.put(cursor.getString(2), new LatLng(Float.parseFloat(cursor.getString(6)), Float.parseFloat(cursor.getString(7))));
            ids.add(cursor.getString(2));
        }
        if(!contents.isEmpty()||!dates.isEmpty()||!places.isEmpty()){
            contents.clear();
            dates.clear();
            places.clear();
        }
        for(TaskDetails d1:data){
            dates.add(d1.getTaskdate());
            contents.add(d1.getContent());
            places.add(d1.getPlace());
        }

        //ra=new TaskViewAdapter(a);
        pg1.setVisibility(View.GONE);
        //recyclerView.setVisibility(View.VISIBLE);
        //recyclerView.setAdapter(ra);
        listView.setVisibility(View.VISIBLE);

        listView.setAdapter(adapter);


        return contents.size();
    }

    public void removeTask(ArrayList<String> ids){
        GeofenceMethods geofenceMethods=new GeofenceMethods(getActivity(),mGoogleApiClient);
        geofenceMethods.removeGeofence(ids);
        LANDMARKS.remove(ids.get(0));
        String task_id=null;
        Cursor cursor=sql.rawQuery("select * from PlaceDatabase where place='"+ids.get(0)+"';",null);
        if(cursor.getCount()==1){
            task_id=cursor.getString(1);
        }
        sql.delete("PlaceDatabase","place=?",new String[]{ids.get(0)});
        FirebaseDatabaseHelper firebaseDatabase=new FirebaseDatabaseHelper(getActivity());
        firebaseDatabase.removeDatafromFirebase(task_id);
        Snackbar.make(getActivity().findViewById(R.id.ll),"Successfully removed",Snackbar.LENGTH_SHORT).show();
        fetchOfflineData();
    }




    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, getPendingIntent());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                getPendingIntent());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(LocationRequestHelper.LOCATION_UPDATES)){
            //getting curr location
            String currlatlng=LocationRequestHelper.getLocationRequesting(getActivity());
            String []curr=currlatlng.split(":");
            //end
            Location currloc=new Location("");
            Location destloc=new Location("");
            currloc.setLatitude(Double.parseDouble(curr[0]));
            currloc.setLongitude(Double.parseDouble(curr[1]));
            String query="select * from PlaceDatabase";
            Cursor cursor = sql.rawQuery(query, null);
            distMeter.clear();
            while(cursor.moveToNext()){
                destloc.setLatitude(Double.valueOf(cursor.getString(6)));
                destloc.setLongitude(Double.valueOf(cursor.getString(7)));
                Log.i("distance",String.valueOf(currloc.distanceTo(destloc)));
                distMeter.add(Double.valueOf(currloc.distanceTo(destloc)));
            }

            adapter.notifyDataSetChanged();

        }
    }
}
