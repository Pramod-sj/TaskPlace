package com.example.pramod.taskplace;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class SetTask extends Fragment {
    CurrentUserData currentUserData;
    View view;
    //Button and SupportPlaceAutocompleteFragment declaration
    EditText taskEditText;
    Button setB;
    SupportPlaceAutocompleteFragment supportPlaceAutocompleteFragment;
    //end
    //declaring data variables for storing value temporary purpose
    LatLng latlng;
    String placeSelected="";
    String taskData="";
    //end
    //database reference variable
    DatabaseReference taskDetailsCloudEndPoint;//end
    //
    ProgressDialog progressDialog;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_set_task, container, false);
        setB=view.findViewById(R.id.setTask);
        //user2= FirebaseAuth.getInstance().getCurrentUser();
        //UID=user2.getUid();
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage("Saving new Task...");
        taskDetailsCloudEndPoint=FirebaseDatabase.getInstance().getReference().child("Users");
        taskEditText=view.findViewById(R.id.taskDetails);
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
                taskData=taskEditText.getText().toString();
                if(taskData.equals("")||placeSelected.equals("")){
                    Snackbar.make(v,"Please set all the stuffs....:P",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                progressDialog.show();
                addDataToFirebase();

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
    public ArrayList<TaskDetails> addTaskDetailstoArrayList() {
        ArrayList<TaskDetails> Entries = new ArrayList<>();
        TaskDetails details = new TaskDetails();
        details.setContent(taskData);
        details.setLatlng(latlng);
        details.setTaskdate(String.valueOf(Calendar.getInstance().getTime()));
        details.setPlace(placeSelected);
        Entries.add(details);
        return Entries;
    }

    private void addDataToFirebase() {
        final ArrayList<TaskDetails> EntriestoAdd = addTaskDetailstoArrayList();
        for (TaskDetails Entry : EntriestoAdd) {
            String taskid = taskDetailsCloudEndPoint.push().getKey();
            taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child(taskid).setValue(Entry).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Snackbar.make(getActivity().findViewById(R.id.ll1),"Successfully added",Snackbar.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Snackbar.make(getActivity().findViewById(R.id.ll1),"Unable to added new task",Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
