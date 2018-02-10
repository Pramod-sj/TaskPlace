package com.example.pramod.taskplace.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
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
import com.example.pramod.taskplace.LocationService.LocationServiceMethods;
import com.example.pramod.taskplace.CurrentUserData;
import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.TaskDetails;
import com.example.pramod.taskplace.TaskPlace;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewTask extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    ArrayList<String> distMeter;
    View view;
    CurrentUserData currentUserData;
    DatabaseReference taskDetailsCloudEndPoint;
    GoogleApiClient mGoogleApiClient;
    ProgressDialog progressDialog;
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
    Snackbar snackbar;
    //ArrayAdapter<String> arrayAdapter;
    TaskViewAdapter adapter;
    DatabaseHelper db;
    LinearLayout ll;
    SQLiteDatabase sql;
    LocationServiceMethods methods;
    public void onAttached(Activity activity){
        super.onAttach(activity);

    }
    @SuppressLint("MissingPermission")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("oncreateview","inside it");
        view=inflater.inflate(R.layout.activity_view_task, container, false);
        //getting GoogleApiClient from Application using singleton;
        getGoogleApiClient();

        db=new DatabaseHelper(getActivity());
        sql=db.getWritableDatabase();
        pg1=view.findViewById(R.id.loading);
        distMeter=new ArrayList<>();
        LANDMARKS=new HashMap<>();
        listView=view.findViewById(R.id.listView);
        adapter=new TaskViewAdapter(getActivity().getBaseContext(),contents,dates,places,distMeter);
        listView.setAdapter(adapter);
        ll=view.findViewById(R.id.ll);
        pg1.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF8C00"), PorterDuff.Mode.MULTIPLY);
        aSwitch=view.findViewById(R.id.taskActivator);
        //firebase
        currentUserData=new CurrentUserData(getActivity().getBaseContext());
        taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        switchListener();
        listViewListener();
        //inistantiating LocationServiceMethods
        methods=new LocationServiceMethods(getActivity(),mGoogleApiClient);
        return view;
    }
    public void getGoogleApiClient(){
        if(TaskPlace.getGoogleApiHelper().isConnected()){
            mGoogleApiClient=TaskPlace.getGoogleApiHelper().getGoogleApiClient();
        }
    }
    public void listViewListener(){
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
                                removeTask(data.get(index).getTaskid());
                                //disable if no data exists
                                switchState();
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
    }
    private void switchState(){
        snackbar=Snackbar.make(ll,"No Task",Snackbar.LENGTH_INDEFINITE)
                .setAction("Set some tasks", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment f=new SetTask();
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,f).commit();
                    }
                });
        fetchOfflineData();
        if(!db.isDataExist()) {
            methods.removeLocationUpdates();
            aSwitch.setClickable(false);
            snackbar.show();

        }
        else {
            snackbar.dismiss();
            if (LocationRequestHelper.getRequestingTrigger(getActivity()) == false) {
                aSwitch.setChecked(true);
                methods.requestLocationUpdates();
            } else {
                aSwitch.setChecked(false);
                methods.removeLocationUpdates();
            }
        }
    }


    public void switchListener(){
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(aSwitch.isChecked()){
                    methods.createLocationRequest();
                    methods.requestLocationUpdates();
                }
                else {
                    methods.removeLocationUpdates();
                }
            }
        });
    }



    @Override
    public void onStart(){
        Log.i("onstart","inside it");
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onResume(){
        Log.i("onresume","inside it");
        super.onResume();
        switchState();
    }
    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("View Task");
    }
    public void fetchOfflineData(){
        if(!contents.isEmpty()||!dates.isEmpty()||!places.isEmpty()){
            contents.clear();
            dates.clear();
            places.clear();
        }
        for(TaskDetails details:db.fetchData()){
            dates.add(details.getTaskdate());
            contents.add(details.getContent());
            places.add(details.getPlace());
        }
        pg1.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        listView.setVisibility(View.VISIBLE);
        aSwitch.setVisibility(View.VISIBLE);
    }

    public void removeTask(String ids){
        LANDMARKS.remove(ids);
        db.deteleData(ids);
        FirebaseDatabaseHelper firebaseDatabase=new FirebaseDatabaseHelper(getActivity());
        firebaseDatabase.removeDatafromFirebase(ids);
        Snackbar.make(ll,"Successfully removed",Snackbar.LENGTH_SHORT).show();
        fetchOfflineData();
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
                distMeter.add((int)currloc.distanceTo(destloc)+" m");
            }
            adapter.notifyDataSetChanged();
        }
    }


}
