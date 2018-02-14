package com.example.pramod.taskplace.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.pramod.taskplace.Adapters.TaskViewAdapter;
import com.example.pramod.taskplace.Database.FirebaseDatabaseHelper;
import com.example.pramod.taskplace.LocationService.LocationRequestHelper;
import com.example.pramod.taskplace.LocationService.LocationServiceMethods;
import com.example.pramod.taskplace.Model.CurrentUserData;
import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.Model.TaskDetails;
import com.example.pramod.taskplace.TaskPlace;
import com.expertprogramming.taskplace.ScrollingActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class ViewTaskFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    ArrayList<String> distMeter;
    View view;
    CurrentUserData currentUserData;
    DatabaseReference taskDetailsCloudEndPoint;
    GoogleApiClient mGoogleApiClient;
    ProgressDialog progressDialog;
    //array for viewing purpose
    ArrayList<String> firebaseIDS=new ArrayList<>();
    ArrayList<String> contents=new ArrayList<String>();
    ArrayList<String> dates=new ArrayList<String>();
    ArrayList<String> places=new ArrayList<String>();
    ProgressBar pg1;
    ListView listView;
    Switch aSwitch;
    //ArrayList<TaskDetails> data=new ArrayList<>();
    Snackbar snackbar;
    //ArrayAdapter<String> arrayAdapter;
    TaskViewAdapter adapter;
    DatabaseHelper db;
    LinearLayout ll;
    LocationServiceMethods methods;
    @SuppressLint("MissingPermission")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("oncreateview","inside it");
        view=inflater.inflate(R.layout.activity_view_task, container, false);
        //getting GoogleApiClient from Application using singleton;
        getGoogleApiClient();
        db=TaskPlace.getDatabaseHelper();
        pg1=view.findViewById(R.id.loading);
        distMeter=new ArrayList<>();
        listView=view.findViewById(R.id.listView);
        adapter=new TaskViewAdapter(getActivity().getBaseContext(),contents,dates,places,distMeter,firebaseIDS);
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
                TextView firebase_id=view.findViewById(R.id.firebaseId);
                Intent i=new Intent(getActivity(), ScrollingActivity.class);
                i.putExtra("id",firebase_id.getText().toString());
                startActivity(i);
            }
        });
    }
    private void switchState(){

        snackbar=Snackbar.make(ll,"No Task",Snackbar.LENGTH_INDEFINITE)
                .setAction("Set some tasks", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment f=new SetTaskFragment();
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,f).commit();
                    }
                });

        fetchOfflineData();
        if(!db.isDataExist()) {
            methods.removeLocationUpdates();
            aSwitch.setClickable(false);
            aSwitch.setChecked(false);
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
            contents.add(details.getTaskTitle());
            places.add(details.getPlace());
            firebaseIDS.add(details.getTaskid());
        }
        pg1.setVisibility(View.GONE);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setVisibility(View.VISIBLE);
        aSwitch.setVisibility(View.VISIBLE);
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
            String query="select * from Task";
            Cursor cursor = db.getReadableDatabase().rawQuery(query, null);
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
    @Override
    public void onPause(){
        super.onPause();
        if(snackbar.isShown()){
            snackbar.dismiss();
        }
    }


}
