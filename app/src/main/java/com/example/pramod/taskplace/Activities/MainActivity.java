package com.example.pramod.taskplace.Activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pramod.taskplace.Model.CurrentUserData;
import com.example.pramod.taskplace.Fragments.NavMapFragment;
import com.example.pramod.taskplace.Fragments.SetTaskFragment;
import com.example.pramod.taskplace.Fragments.ViewTaskFragment;
import com.example.pramod.taskplace.R;
import com.example.pramod.taskplace.TaskPlace;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private CircleImageView circleImageView;
    private View header;
    private Uri ImagePATH;
    private final int ALARM_ID=10;
    private final int SELECT_PHOTO=100;
    private AlertDialog alertDialog;
    private GoogleApiClient mGoogleApiClient;
    private AlarmManager alarmManager=null;
    private PendingIntent pendingIntent=null;
    private TextView username,useremail;
    //Firebase
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage=FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
            }

            public void onDrawerClosed(View drawerView){
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setVerticalScrollBarEnabled(false);
        header=navigationView.getHeaderView(0);
        circleImageView= header.findViewById(R.id.imageView);
        circleImageView.setClickable(true);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Click",Toast.LENGTH_SHORT).show();
                alertDialog.show();
            }
        });
        username=header.findViewById(R.id.username);
        useremail=header.findViewById(R.id.useremail);
        setUserProfile();
        //for notification
        mGoogleApiClient=TaskPlace.getGoogleApiHelper().getGoogleApiClient();
        mGoogleApiClient.connect();
        String ScrollViewActivity=getIntent().getStringExtra("scrollView");
        if(ScrollViewActivity!=null){
            if(ScrollViewActivity.equals("scrollView")){
                Fragment f = new ViewTaskFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent, f).commit();
            }
        }
        else {
            navItem(R.id.set_navtask);
        }
        //end
        //AlertDialog for image selection and upload
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Upload an Image");
        builder.setMessage("Please choose image from your gallery..:)");
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
        builder.setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog=builder.create();
        if(!storage.getReference().child("images/" + new CurrentUserData(this).getCurrentUID()).equals(null)) {
            storage.getReference().child("images/" + new CurrentUserData(this).getCurrentUID()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(MainActivity.this).load(uri).placeholder(R.drawable.placeholder).into(circleImageView);
                }
            });
        }

        if(!checkPermissions()){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_PERMISSIONS_REQUEST_CODE);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                coverImage();
            }
        },200);
    }
    public void setUserProfile(){
        Uri url=FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        String email_id,user_name;
        email_id=FirebaseAuth.getInstance().getCurrentUser().getEmail();
        user_name=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if(url!=null&&email_id!=null&&user_name!=null){
            Picasso.with(MainActivity.this).load(url).placeholder(R.drawable.placeholder).into(circleImageView);
            useremail.setText(email_id);
            username.setText(user_name);
        }
        else{
            CurrentUserData currentUserData=new CurrentUserData(MainActivity.this);
            useremail.setText(currentUserData.getCurrentUserEmail());
            String[] name=currentUserData.getCurrentUserEmail().split("@");
            username.setText(name[0]);
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
    protected void onResume(){
        super.onResume();
    }
    @Override
    public void onStart() {
        super.onStart();
        TaskPlace.getGoogleApiHelper().connect();
        if(mGoogleApiClient.isConnected()) {
            locationPermissionChecker(mGoogleApiClient, MainActivity.this);
        }
    }

    @Override
    public void onDestroy() {

        TaskPlace.getGoogleApiHelper().disconnect();
        if(alarmManager!=null) {
            alarmManager.cancel(pendingIntent);
        }
        super.onDestroy();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    ImagePATH = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(ImagePATH);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    circleImageView.setImageBitmap(yourSelectedImage);
                }
                uploadImage();
                break;
            case REQUEST_PERMISSIONS_REQUEST_CODE:
                switch (requestCode){
                    case Activity.RESULT_OK:
                        Toasty.success(getApplicationContext(),"Successfully granted required permission",Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toasty.error(getApplicationContext(),"This application requires prompted permission",Toast.LENGTH_SHORT).show();

                }
                break;
            /*case ALARM_ID:
                checkTime();
                break;*/
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finish();
            System.exit(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.settingOption) {
            Intent i=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(i);
            return true;
        }
        else if (id == R.id.devOption) {
            Intent i=new Intent(MainActivity.this,DevPageActivity.class);
            startActivity(i);
            return true;
        }
        else if(id == R.id.aboutapp){
            Intent i=new Intent(MainActivity.this,AboutActivity.class);
            startActivity(i);
            return true;
        }
        else if(id==R.id.LibOption){
            Intent i=new Intent(MainActivity.this,OpenLibraries_Activity.class);
            startActivity(i);
            return true;
        }
        else if(id==R.id.exitOption){
            System.exit(0);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        final int id = item.getItemId();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    navItem(id);
                }
        }, 400);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void navItem(int id){
        Fragment fragment = null;
        if (id == R.id.set_navtask) {
            fragment=new SetTaskFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();
        }else if (id == R.id.view_navTask) {
            fragment=new ViewTaskFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();
        }
        else if(id==R.id.map_nav){
            fragment=new NavMapFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();
        }
        else if(id==R.id.nav_logout){
            final GoogleApiClient mAuthGoogleApiClient=buildGoogleApiClientAuth();
            mAuthGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    FirebaseAuth.getInstance().signOut();
                    if(mAuthGoogleApiClient.isConnected()){
                        Auth.GoogleSignInApi.signOut(mAuthGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if(status.isSuccess()){
                                    TaskPlace.getDatabaseHelper().removeAllPlacedata();
                                    TaskPlace.getDatabaseHelper().removeAlldata();
                                    Intent i=new Intent(MainActivity.this,LoginActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });
        }
    }
    private void uploadImage() {
        if(ImagePATH != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference ref =storageReference.child("images/"+new CurrentUserData(this).getCurrentUID());
            ref.putFile(ImagePATH)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
    public static void locationPermissionChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(activity, REQUEST_PERMISSIONS_REQUEST_CODE);
                        } catch (Exception e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    protected synchronized GoogleApiClient buildGoogleApiClientAuth() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        mGoogleApiClient.connect();
        return mGoogleApiClient;
    }
    /*public void coverImage(){
        alarmManager= (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent=createPendingResult(ALARM_ID,new Intent(),0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+10000,10000,pendingIntent);
    }*/
    public void coverImage(){
        ImageView img=findViewById(R.id.cover_image);
        Calendar calendar=Calendar.getInstance();
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        if(hour>=0 && hour<12){
            img.setImageDrawable(getResources().getDrawable(R.drawable.morn_min));
            Log.i("test","GOOD MORNING");
        }else if(hour>=12 && hour<16){
            img.setImageDrawable(getResources().getDrawable(R.drawable.after_min));
            Log.i("test","GOOD Afternoon");
        }else if(hour>=16 && hour<21){
            img.setImageDrawable(getResources().getDrawable(R.drawable.eve_min));
            Log.i("test","GOOD evening");
            username.setTextColor(Color.WHITE);
            useremail.setTextColor(Color.WHITE);
        }else if(hour>=21 && hour<24){
            img.setImageDrawable(getResources().getDrawable(R.drawable.night_min));
            username.setTextColor(Color.WHITE);
            useremail.setTextColor(Color.WHITE);
            Log.i("test","GOOD night");
        }
    }
}
