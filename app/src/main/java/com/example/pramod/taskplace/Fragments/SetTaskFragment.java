package com.example.pramod.taskplace.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pramod.taskplace.Model.CurrentUserData;
import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.Database.FirebaseDatabaseHelper;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.Model.TaskDetails;
import com.example.pramod.taskplace.TaskPlace;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;

import es.dmoral.toasty.Toasty;

public class SetTaskFragment extends Fragment {
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
    AlertDialog alertDialog;
    TextView txt;
    double lat;
    double lng;
    DatabaseHelper db;
    ProgressDialog progressDialog;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_set_task, container, false);
        setB=view.findViewById(R.id.setTask);
        db= TaskPlace.getDatabaseHelper();
        txt=view.findViewById(R.id.placeTextView);
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog for selection
                selectPlaceDialog();
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
                if(taskData.equals("")||placeSelected==null||taskdesc.equals("")){
                    Snackbar.make(v,"Please set all the stuffs....:P",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                if(isConnected_custom()) {
                    addTaskDetailstoArrayList();
                }else{
                    Toasty.warning(getActivity().getApplicationContext(),"We need Internet",Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        details.setLat(String.valueOf(lat));
        details.setLng(String.valueOf(lng));
        details.setTaskdate(String.valueOf(Calendar.getInstance().getTime()));
        details.setPlace(placeSelected);
        details.setTaskTitle(taskData);
        details.setTaskDesc(taskdesc);
        //Entries.add(details);
        //online addition of data
        FirebaseDatabaseHelper firebaseDatabaseHelper=new FirebaseDatabaseHelper(getActivity());
        String task_id=firebaseDatabaseHelper.addDataToFirebase(details);
        db.insertData(details,task_id);
        Snackbar.make(getActivity().findViewById(R.id.ll1),"Successfully added",Snackbar.LENGTH_SHORT).show();
        taskDetails.setText("");
        taskEditText.setText("");
        txt.setText("click me to select place..");
        progressDialog.dismiss();


    }

    public boolean isConnected_custom(){
        boolean isInternetAvailable = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && (networkInfo.isConnected())){
                isInternetAvailable  = true;
            }
        }
        catch(Exception exception) {}
        return isInternetAvailable;
    }
    public void selectPlaceDialog(){
        String list[]={"Saved Place","Select new Place"};
        ArrayAdapter adapter=new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,list);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new PlaceFragment()).commit();
                        }else {
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new MapsFragment()).commit();
                        }
                    }
                })
                .setCancelable(true)
                .setTitle("Place");
        alertDialog=builder.create();
        alertDialog.show();
    }
}
