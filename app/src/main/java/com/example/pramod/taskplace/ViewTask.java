package com.example.pramod.taskplace;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewTask extends Fragment {
    View view;
    CurrentUserData currentUserData;
    DatabaseReference taskDetailsCloudEndPoint;
    //array for viewing purpose
    ArrayList<String> contents=new ArrayList<String>();
    ArrayList<String> dates=new ArrayList<String>();
    ArrayList<String> places=new ArrayList<String>();
    ProgressBar pg1;
    ListView listView;
    LinearLayout container;
    ArrayList<TaskDetails> data;
    //ArrayAdapter<String> arrayAdapter;
    CustomDataAdapter adapter;
    PlaceAutocompleteFragment placeAutocompleteFragment;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_view_task, container, false);
        adapter=new CustomDataAdapter(getActivity().getBaseContext(),contents,dates,places);
        pg1=view.findViewById(R.id.loading);
        pg1.setVisibility(View.VISIBLE);
        pg1.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF8C00"), PorterDuff.Mode.MULTIPLY);
        currentUserData=new CurrentUserData(getActivity().getBaseContext());
        taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        fetchData();
        container = view.findViewById(R.id.ll);
        listView=view.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int index=position;
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setTitle("Task")
                        .setMessage("Have You complete your task")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //                        try {
                                //                              Toast.makeText(getApplicationContext(), String.valueOf(data.get(which).taskid), Toast.LENGTH_SHORT).show();
                                //                            }
                                //                              catch (Exception e){
//                                    Toast.makeText(getApplicationContext(), String.valueOf(data.get(which).taskid), Toast.LENGTH_SHORT).show();

                                //}
                                removeTask(data.get(index).taskid);
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



        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("View Task");
    }
    public void fetchData(){
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data=new ArrayList<TaskDetails>();
                if(!dataSnapshot.exists()){
                    Snackbar.make(getActivity().findViewById(R.id.ll),"No Task",Toast.LENGTH_SHORT).setAction("Set task", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Fragment fragment = new SetTask();
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();
                        }
                    }).show();
                }
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String task_id=ds.getKey();
                    String content = ds.child("content").getValue(String.class);
                    float longi = ds.child("latlng").child("longitude").getValue(Float.class);
                    float lati = ds.child("latlng").child("latitude").getValue(Float.class);
                    String date=ds.child("taskdate").getValue(String.class);
                    String place_n=ds.child("place").getValue(String.class);
                    TaskDetails d=new TaskDetails();
                    d.setTaskid(task_id);
                    d.setPlace(place_n);
                    d.setLatlng(new LatLng(lati,longi));
                    d.setContent(content);
                    d.setTaskdate(date);
                    data.add(d);
                }

                if(!contents.isEmpty()||!dates.isEmpty()||!places.isEmpty()){
                    contents.clear();
                    dates.clear();
                    places.clear();
                }
                for(TaskDetails d:data){
                    dates.add(d.getTaskdate());
                    contents.add(d.getContent());
                    places.add(d.getPlace());
                }
                //ra=new CustomDataAdapter(a);
                pg1.setVisibility(View.GONE);
                //recyclerView.setVisibility(View.VISIBLE);
                //recyclerView.setAdapter(ra);
                listView.setAdapter(adapter);
                listView.setVisibility(View.VISIBLE);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void removeTask(final String taskid){
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child(taskid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Snackbar.make(getActivity().findViewById(R.id.ll),"Successfully removed",Snackbar.LENGTH_SHORT).show();
                }
                else {
                    Snackbar.make(getActivity().findViewById(R.id.ll), "Unable to remove", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        fetchData();
    }

}
