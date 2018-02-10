package com.example.pramod.taskplace.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.example.pramod.taskplace.TaskDetails;
import com.example.pramod.taskplace.CurrentUserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 * Created by pramod on 4/2/18.
 */

public class FirebaseDatabaseHelper {
    //database reference variable
    DatabaseReference taskDetailsCloudEndPoint;//end
    CurrentUserData currentUserData;
    private Context context;
    public FirebaseDatabaseHelper(Context context){
        taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserData=new CurrentUserData(context);
        this.context=context;
    }

    public String addDataToFirebase(TaskDetails details) {
        String taskid = taskDetailsCloudEndPoint.push().getKey();
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child(taskid).setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.i("DATA ADDED","SUCCESSFULLY ADDED DATA TO FIREBASE");
                    } else {
                        Log.i("DATA NOT ADDED","FAILED WHILE ADDING DATA TO FIREBASE");
                    }
                }
            });
        return taskid;

    }
    public void removeDatafromFirebase(String taskid){
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child(taskid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i("DATA REMOVED","SUCCESSFULLY REMOVED DATA FROM FIREBASE");
                }
                else {
                    Log.i("DATA CANNOT BE REMOVED","FAILED TO REMOVE DATA FROM FIREBASE");
                }
            }
        });

    }

    public void insertDataToOffline() {
        DatabaseHelper db=new DatabaseHelper(context);
        SQLiteDatabase sql=db.getWritableDatabase();
        String query="insert into PlaceDatabase values(null,?,?,?,?,?,?,?)";
        final SQLiteStatement sqLiteStatement=sql.compileStatement(query);
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //checking if data exist in database
                if (!dataSnapshot.exists()) {
                    Toast.makeText(context,"Set some task",Toast.LENGTH_SHORT).show();
                    return;
                }
                //if data exist fetch all data from firebase and store it to local database...
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String task_id = ds.getKey();
                    String content = ds.child("content").getValue(String.class);
                    String desc=ds.child("taskDesc").getValue(String.class);
                    String longi = ds.child("lng").getValue(String.class);
                    String lati = ds.child("lat").getValue(String.class);
                    String date = ds.child("taskdate").getValue(String.class);
                    String place_n = ds.child("place").getValue(String.class);
                    sqLiteStatement.bindString(1,task_id);
                    sqLiteStatement.bindString(2,place_n);
                    sqLiteStatement.bindString(3,content);
                    sqLiteStatement.bindString(4,desc);
                    sqLiteStatement.bindString(5,date);
                    sqLiteStatement.bindString(6,String.valueOf(lati));
                    sqLiteStatement.bindString(7,String.valueOf(longi));
                    sqLiteStatement.execute();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
