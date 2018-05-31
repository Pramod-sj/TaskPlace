package org.expertprogramming.taskplace.Fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
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

import org.expertprogramming.taskplace.Adapters.TaskViewAdapter;
import org.expertprogramming.taskplace.LocationService.FusedLocationService;
import org.expertprogramming.taskplace.LocationService.LocationRequestHelper;
import org.expertprogramming.taskplace.Model.CurrentUserData;
import org.expertprogramming.taskplace.Database.DatabaseHelper;
import org.expertprogramming.taskplace.R;
import org.expertprogramming.taskplace.Model.TaskDetails;
import org.expertprogramming.taskplace.TaskPlace;
import org.expertprogramming.taskplace.Activities.ScrollingActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

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
    //ArrayList<String> dates=new ArrayList<String>();
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
    //LocationServiceMethods methods;
    @SuppressLint("MissingPermission")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("oncreateview","inside it");
        view=inflater.inflate(R.layout.activity_view_task, container, false);
        //getting GoogleApiClient from Application using singleton;
        mGoogleApiClient=TaskPlace.getGoogleApiHelper().getGoogleApiClient();
        mGoogleApiClient.connect();
        db=TaskPlace.getDatabaseHelper();
        pg1=view.findViewById(R.id.loading);
        distMeter=new ArrayList<>();
        listView=view.findViewById(R.id.listView);
        adapter=new TaskViewAdapter(getActivity().getBaseContext(),contents,places,distMeter,firebaseIDS);
        ll=view.findViewById(R.id.ll);
        pg1.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF8C00"), PorterDuff.Mode.MULTIPLY);
        aSwitch=view.findViewById(R.id.taskActivator);
        //firebase
        currentUserData=new CurrentUserData(getActivity().getBaseContext());
        taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        switchListener();
        listViewListener();
        //inistantiating LocationServiceMethods
        //methods=new LocationServiceMethods(getActivity(),mGoogleApiClient);
        return view;
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
            //methods.removeLocationUpdates();
            getActivity().stopService(new Intent(getActivity(), FusedLocationService.class));
            aSwitch.setClickable(false);
            aSwitch.setChecked(false);
            snackbar.show();
        }
        else {
            snackbar.dismiss();
            if (LocationRequestHelper.getRequestingTrigger(getActivity()) == false) {
                aSwitch.setChecked(true);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    getActivity().startForegroundService(new Intent(getActivity(),FusedLocationService.class));
                }else{
                    getActivity().startService(new Intent(getActivity(),FusedLocationService.class));
                }
            } else {
                aSwitch.setChecked(false);
                getActivity().stopService(new Intent(getActivity(), FusedLocationService.class));
            }
        }
    }


    public void switchListener(){
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(aSwitch.isChecked()){
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        getActivity().startForegroundService(new Intent(getActivity(),FusedLocationService.class));
                    }else{
                        getActivity().startService(new Intent(getActivity(),FusedLocationService.class));
                    }

                }
                else {
                    LocationRequestHelper.setNotificationFlag(getActivity(),true);
                    getActivity().stopService(new Intent(getActivity(), FusedLocationService.class));
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
        if(!TaskPlace.getGoogleApiHelper().isConnected()) {
            TaskPlace.getGoogleApiHelper().connect();
        }
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
        if(!contents.isEmpty()||!places.isEmpty()){
            contents.clear();
            places.clear();
        }
        for(TaskDetails details:db.fetchData()){
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
