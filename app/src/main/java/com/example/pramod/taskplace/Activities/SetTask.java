package com.example.pramod.taskplace.Activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pramod.taskplace.CurrentUserData;
import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.Database.FirebaseDatabaseHelper;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.TaskDetails;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;

public class SetTask extends Fragment {
    CurrentUserData currentUserData;
    View view;
    String taskdesc;
    //Button and SupportPlaceAutocompleteFragment declaration
    EditText taskEditText,taskDetails;
    Button setB;
    //declaring data variables for storing value temporary purpose
    LatLng latlng;
    String placeSelected="";
    String taskData="";
    //end
    //
    TextView txt;
    double lat;
    double lng;
    DatabaseHelper db;
    ProgressDialog progressDialog;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_set_task, container, false);
        setB=view.findViewById(R.id.setTask);
        db=new DatabaseHelper(getActivity());
        txt=view.findViewById(R.id.placeTextView);
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f=new MapsActivity();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,f).commit();
            }
        });
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage("Saving new Task...");
        taskDetails=view.findViewById(R.id.taskDetails);
        taskEditText=view.findViewById(R.id.taskTitle);
        setB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskdesc=taskDetails.getText().toString();
                taskData=taskEditText.getText().toString();
                if(taskData.equals("")||placeSelected.equals("")||taskdesc.equals("")){
                    Snackbar.make(v,"Please set all the stuffs....:P",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                addTaskDetailstoArrayList();
            }
        });
        DatabaseHelper databaseHelper=new DatabaseHelper(getActivity());
        return view;
    }
    @Override
    public void onStart(){
        super.onStart();
        Bundle b=this.getArguments();
        if(b!=null){
            placeSelected=b.getString("Address");
            lat=b.getDouble("lat");
            lng=b.getDouble("lng");
            txt.setText(placeSelected);
            txt.setTextColor(Color.BLACK);
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Set Task");
        currentUserData=new CurrentUserData(getActivity().getBaseContext());
    }
    public void addTaskDetailstoArrayList() {
        //ArrayList<TaskDetails> Entries = new ArrayList<>();
        TaskDetails details = new TaskDetails();
        details.setContent(taskData);
        details.setLat(String.valueOf(lat));
        details.setLng(String.valueOf(lng));
        details.setTaskdate(String.valueOf(Calendar.getInstance().getTime()));
        details.setPlace(placeSelected);
        details.setTaskDesc(taskdesc);
        //Entries.add(details);
        //online addition of data
        FirebaseDatabaseHelper firebaseDatabaseHelper=new FirebaseDatabaseHelper(getActivity());
        String task_id=firebaseDatabaseHelper.addDataToFirebase(details);
        db.insertData(details,task_id);

        Snackbar.make(getActivity().findViewById(R.id.ll1),"Successfully added",Snackbar.LENGTH_SHORT).show();
        progressDialog.dismiss();

    }

}
