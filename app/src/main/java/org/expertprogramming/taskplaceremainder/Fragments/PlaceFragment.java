package org.expertprogramming.taskplaceremainder.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.expertprogramming.taskplaceremainder.Adapters.PlaceAdapter;
import org.expertprogramming.taskplaceremainder.Model.Places;
import org.expertprogramming.taskplaceremainder.R;

import org.expertprogramming.taskplaceremainder.TaskPlace;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by pramod on 14/2/18.
 */

public class PlaceFragment extends Fragment {
    View view;
    ArrayList<String> places=new ArrayList<>();
    ArrayList<String> placesAddress=new ArrayList<>();
    PlaceAdapter adapter;
    ArrayList<LatLng> latLng=new ArrayList<>();
    ListView listView;
    TextView noSavedPlace;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle b){
        view=inflater.inflate(R.layout.activity_placefragment,container,false);
        listView=view.findViewById(R.id.placefragmentView);
        adapter=new PlaceAdapter(getActivity(),places,placesAddress);
        listView.setAdapter(adapter);
        noSavedPlace=view.findViewById(R.id.noSavedPlace);
        getPlaces();
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
                selectDialog(places.get(position),placesAddress.get(position),latLng.get(position),position);
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
            placesAddress.add(placesObj.get(i).getPlaceAddress());
            latLng.add(new LatLng(placesObj.get(i).getLat(),placesObj.get(i).getLng()));
        }
        if(flag){
            listView.setVisibility(View.VISIBLE);
            noSavedPlace.setVisibility(View.GONE);
        }
        else{
            listView.setVisibility(View.GONE);
            noSavedPlace.setVisibility(View.VISIBLE);
        }
    }
    public void selectDialog(final String name,final String addr,final LatLng latLngObj,final int position){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity())
                .setTitle("What to do..?")
                .setPositiveButton("Select as my place", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Fragment fragment=new SetTaskFragment();
                        Bundle bundle=new Bundle();
                        bundle.putString("place_name",name);
                        bundle.putString("place_address",addr);
                        bundle.putDouble("lat",latLngObj.latitude);
                        bundle.putDouble("lng",latLngObj.longitude);
                        fragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContent,fragment).commit();

                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TaskPlace.getDatabaseHelper().deletePlaceByName(addr);
                        places.remove(position);
                        placesAddress.remove(position);
                        latLng.remove(position);
                        adapter.notifyDataSetChanged();
                        if(places.size()==0){
                            listView.setVisibility(View.GONE);
                            noSavedPlace.setVisibility(View.VISIBLE);
                        }
                    }
                });
        builder.create();
        builder.show();
    }
    public void onDestroy(){
        super.onDestroy();

    }
}
