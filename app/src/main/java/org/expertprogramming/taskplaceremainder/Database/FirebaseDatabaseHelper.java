package org.expertprogramming.taskplaceremainder.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.util.Log;

import org.expertprogramming.taskplaceremainder.Model.TaskDetails;
import org.expertprogramming.taskplaceremainder.Model.CurrentUserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by pramod on 4/2/18.
 */

public class FirebaseDatabaseHelper {
    //database reference variable
    TaskDetails details;
    DatabaseHelper db;
    SQLiteDatabase sql;
    DatabaseReference taskDetailsCloudEndPoint;//end
    CurrentUserData currentUserData;
    private Context context;
    public FirebaseDatabaseHelper(Context context){
        taskDetailsCloudEndPoint= FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserData=new CurrentUserData(context);
        this.context=context;
        db=new DatabaseHelper(context);
        sql=db.getWritableDatabase();
    }

    public String addDataToFirebase(TaskDetails details) {
        String taskid = taskDetailsCloudEndPoint.push().getKey();
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child("Active Task").child(taskid).setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    public void moveDataToHistoryChild(final String taskid){
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child("Active Task").child(taskid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String content = dataSnapshot.child("taskTitle").getValue(String.class);
                String desc=dataSnapshot.child("taskDesc").getValue(String.class);
                String longi = dataSnapshot.child("lng").getValue(String.class);
                String lati = dataSnapshot.child("lat").getValue(String.class);
                String date = dataSnapshot.child("taskdate").getValue(String.class);
                String place_n = dataSnapshot.child("place").getValue(String.class);
                String placeAddress = dataSnapshot.child("placeAddress").getValue(String.class);
                details=new TaskDetails();
                details.setLat(lati);
                details.setLng(longi);
                details.setTaskTitle(content);
                details.setPlace(place_n);
                details.setTaskdate(date);
                details.setTaskDesc(desc);
                details.setPlaceAddress(placeAddress);
                if(details!=null) {
                    taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child("History").child(taskid).setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i("MOVED DATA", "SUCCESSFULLY ADDED DATA TO HISTORY");
                                removeDatafromFirebase(taskid);

                            } else {
                                Log.i("CAN'T MOVE DATA", "FAILED WHILE ADDING DATA TO HISTORY");
                            }
                        }
                    });

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    public void moveHistoryToTask(final String taskid){
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child("History").child(taskid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String content = dataSnapshot.child("taskTitle").getValue(String.class);
                final String desc=dataSnapshot.child("taskDesc").getValue(String.class);
                final String longi = dataSnapshot.child("lng").getValue(String.class);
                final String lati = dataSnapshot.child("lat").getValue(String.class);
                final String date = dataSnapshot.child("taskdate").getValue(String.class);
                final String place_n = dataSnapshot.child("place").getValue(String.class);
                final String placeAddress = dataSnapshot.child("placeAddress").getValue(String.class);
                details=new TaskDetails();
                details.setLat(lati);
                details.setLng(longi);
                details.setTaskTitle(content);
                details.setPlace(place_n);
                details.setTaskdate(date);
                details.setTaskDesc(desc);
                details.setPlaceAddress(placeAddress);
                taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child("Active Task").child(taskid).setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i("DATA RETORE","SUCCESSFULLY RESTORE DATA IN FIREBASE");
                            //now storing data to offline DB
                            String query="insert into Task values(null,?,?,?,?,?,?,?,?)";
                            final SQLiteStatement sqLiteStatement=sql.compileStatement(query);
                            sqLiteStatement.bindString(1,taskid);
                            sqLiteStatement.bindString(2,place_n);
                            sqLiteStatement.bindString(3,content);
                            sqLiteStatement.bindString(4,desc);
                            sqLiteStatement.bindString(5,date);
                            sqLiteStatement.bindString(6,String.valueOf(lati));
                            sqLiteStatement.bindString(7,String.valueOf(longi));
                            sqLiteStatement.bindString(8,String.valueOf(placeAddress));
                            sqLiteStatement.execute();
                            //end

                            removeDatafromFirebaseHistory(taskid);
                        }
                        else {
                            Log.i("DATA CANNOT BE RESTORED","FAILED TO RESTORE DATA IN FIREBASE");
                        }
                    }
                });
             }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void removeDatafromFirebase(String taskid){
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child("Active Task").child(taskid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public void removeDatafromFirebaseHistory(String taskid){
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child("History").child(taskid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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
        String query="insert into Task values(null,?,?,?,?,?,?,?,?)";
        final SQLiteStatement sqLiteStatement=sql.compileStatement(query);
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child("Active Task").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //checking if data exist in database
                if (!dataSnapshot.exists()) {
                    return;
                }
                //if data exist fetch all data from firebase and store it to local database...
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Check the existance of History tree
                    //if(ds.getKey().equals("History")){
                    //return;
                    //}
                    String task_id = ds.getKey();
                    String content = ds.child("taskTitle").getValue(String.class);
                    String desc=ds.child("taskDesc").getValue(String.class);
                    String longi = ds.child("lng").getValue(String.class);
                    String lati = ds.child("lat").getValue(String.class);
                    String date = ds.child("taskdate").getValue(String.class);
                    String place_n = ds.child("place").getValue(String.class);
                    String placeAddress= ds.child("placeAddress").getValue(String.class);
                    sqLiteStatement.bindString(1,task_id);
                    sqLiteStatement.bindString(2,place_n);
                    sqLiteStatement.bindString(3,content);
                    sqLiteStatement.bindString(4,desc);
                    sqLiteStatement.bindString(5,date);
                    sqLiteStatement.bindString(6,String.valueOf(lati));
                    sqLiteStatement.bindString(7,String.valueOf(longi));
                    sqLiteStatement.bindString(8,placeAddress);
                    sqLiteStatement.execute();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void updateDataInFirebase(final String task_id, final String tasktitle, final String taskDesc){
        taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child("Active Task").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //checking if data exist in database
                if (!dataSnapshot.exists()) {
                    return;
                }
                //if data exist fetch all data from firebase and store it to local database...
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child(task_id).child("taskTitle").setValue(tasktitle);
                    taskDetailsCloudEndPoint.child(currentUserData.getCurrentUID()).child(task_id).child("taskDesc").setValue(taskDesc);
                }
                Log.i("FIREBASE","UPDATED VALUES");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
