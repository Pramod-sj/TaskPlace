package com.example.pramod.taskplace.Activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    SupportPlaceAutocompleteFragment supportPlaceAutocompleteFragment;
    //end
    //declaring data variables for storing value temporary purpose
    LatLng latlng;
    String placeSelected="";
    String taskData="";
    //end
    //
    DatabaseHelper db;
    ProgressDialog progressDialog;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_set_task, container, false);
        setB=view.findViewById(R.id.setTask);
        //user2= FirebaseAuth.getInstance().getCurrentUser();
        //UID=user2.getUid();
        //offline db
        db=new DatabaseHelper(getActivity());
        //
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage("Saving new Task...");
        taskDetails=view.findViewById(R.id.taskDetails);
        taskEditText=view.findViewById(R.id.taskTitle);
        supportPlaceAutocompleteFragment= (SupportPlaceAutocompleteFragment)getChildFragmentManager().findFragmentById(R.id.place);
        supportPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //storing data
                placeSelected=(String)place.getName();
                latlng=place.getLatLng();
            }

            @Override
            public void onError(Status status) {

            }
        });
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
        supportPlaceAutocompleteFragment.setHint("Select the place");
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Set Task");
        currentUserData=new CurrentUserData(getActivity().getBaseContext());
    }
    public void addTaskDetailstoArrayList() {
        ArrayList<TaskDetails> Entries = new ArrayList<>();
        TaskDetails details = new TaskDetails();
        details.setContent(taskData);
        details.setLat(String.valueOf(latlng.latitude));
        details.setLng(String.valueOf(latlng.longitude));
        details.setTaskdate(String.valueOf(Calendar.getInstance().getTime()));
        details.setPlace(placeSelected);
        details.setTaskDesc(taskdesc);
        Entries.add(details);
        SQLiteDatabase sql=db.getReadableDatabase();
        Cursor cursor=sql.rawQuery("select * from PlaceDatabase where place='"+placeSelected+"';",null);
        if(cursor.getCount()>1){
            Snackbar.make(getActivity().findViewById(R.id.ll1),"Caanot add existing place",Snackbar.LENGTH_SHORT).show();
            return;
        }


        //online addition of data
        FirebaseDatabaseHelper firebaseDatabaseHelper=new FirebaseDatabaseHelper(getActivity());
        String task_id=firebaseDatabaseHelper.addDataToFirebase(Entries);
        Log.i("TASKID",task_id);
        //adding data to offline db
        details.setTaskid(null);
        ContentValues values = new ContentValues();
        values.put("sr_no",details.getTaskid());
        values.put("task_id", task_id);
        values.put("place", details.getPlace());
        values.put("task_title", details.getContent());
        values.put("task_desc", details.getTaskDesc());
        values.put("taskdate", details.getTaskdate());
        values.put("latitude", details.getLat());
        values.put("longitude", details.getLng());
        // Contact Phone Number
        // Inserting Row
        sql.insert("PlaceDatabase", null, values);
        sql.close();
        db.close(); // Closing database connection
        Snackbar.make(getActivity().findViewById(R.id.ll1),"Successfully added",Snackbar.LENGTH_SHORT).show();
        progressDialog.dismiss();

    }

}
