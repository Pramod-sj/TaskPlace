package org.expertprogramming.taskplaceremainder.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.expertprogramming.taskplaceremainder.Model.CurrentUserData;
import org.expertprogramming.taskplaceremainder.Database.DatabaseHelper;
import org.expertprogramming.taskplaceremainder.Database.FirebaseDatabaseHelper;
import org.expertprogramming.taskplaceremainder.R;

import org.expertprogramming.taskplaceremainder.Model.TaskDetails;
import org.expertprogramming.taskplaceremainder.TaskPlace;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class SetTaskFragment extends Fragment {
    CurrentUserData currentUserData;
    View view;
    private final int PLACEPICKER_REQUEST_CODE=100;

    String taskdesc;
    //Button and SupportPlaceAutocompleteFragment declaration
    EditText taskEditText,taskDetails;
    Button setB;
    //declaring data variables for storing value temporary purpose
    LatLng latlng;
    String placeName="";
    String placeAddress="";
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
                if(taskData.equals("")||placeName.equals("")||taskdesc.equals("")){
                    Snackbar.make(v,"All fields are mandatory",Snackbar.LENGTH_SHORT).show();
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
        Bundle b=getArguments();
        if(b!=null){
            if(placeName==""){
                placeName=b.getString("place_name");
                placeAddress=b.getString("place_address");
                lat= b.getDouble("lat");
                lng= b.getDouble("lng");
                if(placeName!="") {
                    txt.setText(placeName);
                    txt.setTextColor(Color.BLACK);
                }
                else{
                    Toasty.warning(getActivity(),"Some issue while getting place",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Set Task");
        currentUserData=new CurrentUserData(getActivity().getBaseContext());
    }
    public void addTaskDetailstoArrayList() {
        Log.i("placeName",placeName);
        Log.i("placeAddress",placeAddress);
        Log.i("lat", String.valueOf(lat));
        Log.i("lng", String.valueOf(lng));
        Log.i("taskDAta",taskData);
        Log.i("taskdesc",taskdesc);
        TaskDetails details = new TaskDetails();
        details.setLat(String.valueOf(lat));
        details.setLng(String.valueOf(lng));
        details.setTaskdate(String.valueOf(Calendar.getInstance().getTime()));
        details.setPlace(placeName);
        details.setPlaceAddress(placeAddress);
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
        txt.setText("Click me to select place");
        txt.setTextColor(Color.parseColor("#727272"));
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
        final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new PlaceFragment()).commit();
                        }else {
                            PlacePicker .IntentBuilder placebuilder=new PlacePicker.IntentBuilder();
                            try {
                                startActivityForResult(placebuilder.build(getActivity()),PLACEPICKER_REQUEST_CODE);
                            } catch (GooglePlayServicesRepairableException e) {
                                e.printStackTrace();
                            } catch (GooglePlayServicesNotAvailableException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setCancelable(true)
                .setTitle("Place");
        alertDialog=builder.create();
        alertDialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case PLACEPICKER_REQUEST_CODE:
                    getPlaceData(data);

            }
        }
    }
    public void getPlaceData(Intent data){
        Place p= PlacePicker.getPlace(getActivity(),data);
        Log.i("Place Name",placeName);
        placeName=p.getName().toString();
        placeAddress=p.getAddress().toString();
        if(placeAddress!="" && placeName!=""){
            //if matchPlaces(place) returns false then only insert new place...
            if(TaskPlace.getDatabaseHelper().matchPlaces(placeAddress)==false) {
                lat=p.getLatLng().latitude;
                lng=p.getLatLng().longitude;
                if(placeName!="") {
                    txt.setText(placeName);
                    txt.setTextColor(Color.BLACK);
                }
                TaskPlace.getDatabaseHelper().insertPlace(placeName,placeAddress,p.getLatLng().latitude,p.getLatLng().longitude);
            }
        }
    }

}
