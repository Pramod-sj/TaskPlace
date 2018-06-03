package org.expertprogramming.taskplaceremainder.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.expertprogramming.taskplaceremainder.Adapters.HistoryAdapter;
import org.expertprogramming.taskplaceremainder.Database.FirebaseDatabaseHelper;
import org.expertprogramming.taskplaceremainder.Model.CurrentUserData;
import org.expertprogramming.taskplaceremainder.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

/**
 * Created by pramod on 24/3/18.
 */

public class HistoryFragment extends Fragment {
    View view;
    ListView listView;
    HistoryAdapter adapter;
    private ArrayList<String> firebaseIDS=new ArrayList<>();
    private ArrayList<String> taskTitle=new ArrayList<String>();
    private ArrayList<String> places=new ArrayList<String>();
    private ArrayList<String> dates=new ArrayList<String>();
    private ArrayList<String> taskDesc=new ArrayList<String>();
    FirebaseDatabaseHelper helper;
    ProgressBar pg;
    TextView historyTextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.activity_history, container, false);
        helper=new FirebaseDatabaseHelper(getActivity());
        listView=view.findViewById(R.id.history_listView);
        pg=view.findViewById(R.id.loading);
        pg.setVisibility(View.VISIBLE);
        historyTextView=view.findViewById(R.id.historyStatus);
        adapter=new HistoryAdapter(getActivity().getBaseContext(),taskTitle,taskDesc,dates,places,firebaseIDS);
        listView.setAdapter(adapter);
        setUpAdaper();
        onItemClick();
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("History");
    }
    private void setUpAdaper(){
        if(isConnected_custom()) {
            firebaseIDS.clear();
            taskDesc.clear();
            taskTitle.clear();
            places.clear();
            dates.clear();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
            ref.child(new CurrentUserData(getActivity()).getCurrentUID()).child("History").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Log.i("NODATA", "NODATA");
                        pg.setVisibility(View.GONE);
                        listView.setVisibility(View.GONE);
                        historyTextView.setVisibility(View.VISIBLE);
                        return;
                    }
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        firebaseIDS.add(ds.getKey());
                        taskTitle.add(ds.child("taskTitle").getValue(String.class));
                        taskDesc.add(ds.child("taskDesc").getValue(String.class));
                        places.add(ds.child("place").getValue(String.class));
                        dates.add(ds.child("taskdate").getValue(String.class));
                    }

                    adapter.notifyDataSetChanged();
                    pg.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else{
            pg.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            historyTextView.setVisibility(View.VISIBLE);
            Toasty.warning(getActivity().getApplicationContext(),"Internet required..!",Toast.LENGTH_SHORT).show();
        }

    }
    public void onItemClick(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textview=view.findViewById(R.id.hist_firebaseId);
                String firebase_ID=textview.getText().toString();
                restoreTask(firebase_ID,position);
            }
        });
    }
    public void restoreTask(final String FirebaseId,final int pos){
        if(FirebaseId!=null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle("Restore Task")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(isConnected_custom()) {
                                firebaseIDS.remove(pos);
                                taskDesc.remove(pos);
                                taskTitle.remove(pos);
                                places.remove(pos);
                                dates.remove(pos);
                                helper.moveHistoryToTask(FirebaseId);
                                adapter.notifyDataSetChanged();
                            }else{
                                Toasty.warning(getActivity().getApplicationContext(),"Internet required..!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Completely Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            firebaseIDS.remove(pos);
                            taskDesc.remove(pos);
                            taskTitle.remove(pos);
                            places.remove(pos);
                            dates.remove(pos);
                            helper.removeDatafromFirebaseHistory(FirebaseId);
                            adapter.notifyDataSetChanged();
                        }
                    });
            builder.show();
        }
        else{
            Log.i("restoreTask()","Firebase Id is null");
        }
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
}
