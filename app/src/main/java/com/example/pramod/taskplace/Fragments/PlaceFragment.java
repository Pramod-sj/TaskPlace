package com.example.pramod.taskplace.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.pramod.taskplace.Adapters.PlaceAdapter;
import com.example.pramod.taskplace.Model.Places;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.TaskPlace;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by pramod on 14/2/18.
 */

public class PlaceFragment extends Fragment {
    View view;
    Snackbar snackbar;
    Context context;
    ArrayList<String> places=new ArrayList<>();
    PlaceAdapter adapter;
    ArrayList<LatLng> latLng=new ArrayList<>();
    ListView listView;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle b){
        view=inflater.inflate(R.layout.activity_placefragment,container,false);
        getPlaces();
        listView=view.findViewById(R.id.placefragmentView);
        adapter=new PlaceAdapter(getActivity(),places);
        listView.setAdapter(adapter);
        //going back to previous page i.e. setTask.class
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN){
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new SetTaskFragment()).commit();
                    Log.i("BACK","going back to setTask()");
                    return true;
                }
                return false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectDialog(places.get(position),latLng.get(position));
            }
        });
        return view;
    }
    public void getPlaces(){
        boolean flag=false;
        ArrayList<Places> placesObj=TaskPlace.getDatabaseHelper().getPlaceData();
        for(int i=0;i<placesObj.size();i++){
            flag=true;
            places.add(placesObj.get(i).getPlace());
            latLng.add(new LatLng(placesObj.get(i).getLat(),placesObj.get(i).getLng()));
        }
        if(!flag){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    snackbar=Snackbar.make(getActivity().findViewById(R.id.linearSavedPlace),"No Saved place",Snackbar.LENGTH_INDEFINITE)
                            .setAction("Select place", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new MapsFragment()).commit();
                                }
                            });
                    snackbar.show();
                }
            },200);
        }
    }
    public void selectDialog(final String name,final LatLng latLng){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                .setTitle("What to do..?")
                .setPositiveButton("Select as my place", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Fragment fragment=new SetTaskFragment();
                        Bundle bundle=new Bundle();
                        bundle.putString("Address",name);
                        bundle.putDouble("lat",latLng.latitude);
                        bundle.putDouble("lng",latLng.longitude);
                        fragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,fragment).commit();

                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TaskPlace.getDatabaseHelper().deletePlaceByName(name);
                        adapter.notifyDataSetChanged();
                    }
                });
        builder.create();
        builder.show();
    }
    public void onDestroy(){
        if(snackbar!=null){
            snackbar.dismiss();
        }
        super.onDestroy();

    }
}
