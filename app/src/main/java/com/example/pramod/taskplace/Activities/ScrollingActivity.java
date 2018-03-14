package com.example.pramod.taskplace.Activities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pramod.taskplace.Database.FirebaseDatabaseHelper;
import com.example.pramod.taskplace.LocationService.LocationResultHelper;
import com.example.pramod.taskplace.Model.TaskDetails;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.TaskPlace;
import com.google.android.gms.location.LocationResult;

import es.dmoral.toasty.Toasty;

public class ScrollingActivity extends AppCompatActivity {
    TaskDetails details;
    TextView taskDESC,taskDATE,taskPLACE;
    EditText edt1,edt2;
    AlertDialog alertUpdate,alertDelete;
    String firebaseDataId;
    Button b1;
    int sr_no;
    FloatingActionButton fab;
    Vibrator vibrator;
    MediaPlayer mediaPlayer;
    boolean flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        vibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);
        fab = findViewById(R.id.fab);
        String page=getIntent().getStringExtra("NotifyPage");
        b1=findViewById(R.id.deleteButton);
        if(page!=null) {
            if (page.equals("fromNotif")) {
                if(Integer.parseInt(getIntent().getExtras().getString("not_type"))==1) {
                    flag = true;
                }
                firebaseDataId = getIntent().getStringExtra("task_id");
                b1.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);
            }
        }
        else{
            firebaseDataId=getIntent().getExtras().getString("id");
            fab.setVisibility(View.VISIBLE);
            b1.setVisibility(View.GONE);
        }
        details= TaskPlace.getDatabaseHelper().getDetailsById(firebaseDataId);
        setTitle(details.getTaskTitle());
        firebaseDataId=details.getTaskid();
        taskDESC=findViewById(R.id.TaskDescTextView);
        taskDATE=findViewById(R.id.TaskDateTextView);
        taskPLACE=findViewById(R.id.TaskPlaceTextView);
        taskDESC.setText(details.getTaskDesc());
        taskPLACE.setText(details.getPlace());
        taskDATE.setText(details.getTaskdate());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteOrEdit();

            }
        });
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneTask();
            }
        });
    }
    public void editDialog(){
        View view=getLayoutInflater().inflate(R.layout.acitvity_editdetails,null);
        edt1=view.findViewById(R.id.editTaskTitle);
        edt2=view.findViewById(R.id.editTaskDesc);
        edt1.setText(details.getTaskTitle());
        edt2.setText(details.getTaskDesc());
        AlertDialog.Builder builder=new AlertDialog.Builder(ScrollingActivity.this);
        builder.setTitle("Update Task")
                .setMessage("Do you really want to update")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isConnected_custom()) {
                            TaskPlace.getDatabaseHelper().updateTaskDetails(firebaseDataId,edt1.getText().toString(),edt2.getText().toString());
                            FirebaseDatabaseHelper helper=new FirebaseDatabaseHelper(ScrollingActivity.this);
                            helper.updateDataInFirebase(firebaseDataId,edt1.getText().toString(),edt2.getText().toString());
                            taskDESC.setText(edt2.getText().toString());
                            getSupportActionBar().setTitle(edt1.getText().toString());
                        }
                        else{
                            Toasty.warning(getApplicationContext(),"We need Internet", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false);
        alertUpdate=builder.create();
        alertUpdate.setView(view);
        alertUpdate.show();

    }
    public boolean isConnected_custom(){
        boolean isInternetAvailable = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo != null && (networkInfo.isConnected())){
                isInternetAvailable  = true;
            }
        }
        catch(Exception exception) {}
        return isInternetAvailable;
    }
    public void removeTask(){
        Log.i("firebase_id",firebaseDataId);
        FirebaseDatabaseHelper firebaseDatabase=new FirebaseDatabaseHelper(ScrollingActivity.this);
        firebaseDatabase.removeDatafromFirebase(firebaseDataId);
        TaskPlace.getDatabaseHelper().deteleData(firebaseDataId);
        //Snackbar.make(ll,"Successfully removed",Snackbar.LENGTH_SHORT).show();
    }
    public void deleteOrEdit(){
        String[] list=new String[]{"Update Task","Delete Task"};
        ArrayAdapter adapter=new ArrayAdapter(ScrollingActivity.this,android.R.layout.simple_list_item_1,list);
        AlertDialog.Builder builderDelete=new AlertDialog.Builder(ScrollingActivity.this);
        builderDelete.setTitle("Select Operation");
        builderDelete.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    editDialog();
                }else if(which==1){
                    removeTask();
                    Intent i=new Intent(ScrollingActivity.this, MainActivity.class);
                    i.putExtra("scrollView","scrollView");
                    startActivity(i);
                    finish();
                }
            }
        });
        alertDelete=builderDelete.create();
        alertDelete.show();
    }
    public void doneTask(){
        AlertDialog.Builder builder=new AlertDialog.Builder(ScrollingActivity.this)
                .setTitle("Remove Task")
                .setMessage("do you want to remove this task")
                .setPositiveButton("remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isConnected_custom()) {
                            if(flag) {
                                if (vibrator.hasVibrator()) {
                                    vibrator.cancel();
                                }
                                mediaPlayer.stop();
                            }
                            TaskPlace.getDatabaseHelper().deteleData(firebaseDataId);
                            FirebaseDatabaseHelper helper = new FirebaseDatabaseHelper(ScrollingActivity.this);
                            helper.removeDatafromFirebase(firebaseDataId);
                            Toasty.success(ScrollingActivity.this,"Successfully deleted",Toast.LENGTH_SHORT).show();
                            Intent i=new Intent(ScrollingActivity.this, MainActivity.class);
                            i.putExtra("scrollView","scrollView");
                            startActivity(i);
                            finish();
                        }else{
                            Toasty.warning(getApplicationContext(),"We need Internet", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        builder.create();
        builder.show();
    }
    public void vibratePhone(){
        long [] pattern={0,1000,900,1000,900,1000,900,1000,1000,900,1000,900,1000,900,1000,0};
        vibrator.vibrate(pattern,1);
    }
    public void playRingTone(){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url=preferences.getString("notifications_new_message_ringtone","content://settings/system/alarm_alert");
        mediaPlayer=MediaPlayer.create(getApplicationContext(), Uri.parse(url));
        mediaPlayer.start();
    }
    public void onStart() {
        super.onStart();
        if(flag) {
            vibratePhone();
            playRingTone();
        }
    }
    public void onDestroy() {
        if(flag) {
            if (vibrator.hasVibrator()) {
                vibrator.cancel();
            }
            mediaPlayer.stop();
        }
        super.onDestroy();
    }

}
