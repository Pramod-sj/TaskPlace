package com.example.pramod.taskplace.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pramod.taskplace.CurrentUserData;
import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.Geofence.GeofenceMethods;
import com.example.pramod.taskplace.R;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    CircleImageView circleImageView;
    View header;
    Uri ImagePATH;
    final int SELECT_PHOTO=100;
    AlertDialog alertDialog;
    GoogleApiClient mGoogleApiClient;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage=FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        buildGoogleApiClient();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView username,useremail;
        CurrentUserData currentUserData=new CurrentUserData(MainActivity.this);
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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setVerticalScrollBarEnabled(false);
        header=navigationView.getHeaderView(0);
        circleImageView=(CircleImageView) header.findViewById(R.id.imageView);
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
        useremail.setText(currentUserData.getCurrentUserEmail());
        String[] name=currentUserData.getCurrentUserEmail().split("@");
        username.setText(name[0]);
        //for notification
        String notifydata=getIntent().getStringExtra("NotifyPage");
        if(notifydata!=null){
            if(notifydata.equals("ViewTask")) {
                Fragment f = new ViewTask();
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
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
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
                .addApi(LocationServices.API)
                .build();
    }
    protected void onResume(){
        super.onResume();
        locationPermissionChecker(mGoogleApiClient,MainActivity.this);
    }
    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
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
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            moveTaskToBack(true);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
            // Handle the camera action
            fragment=new SetTask();
        }else if (id == R.id.view_navTask) {
            fragment=new ViewTask();
        }else if(id==R.id.nav_logout){
            FirebaseAuth.getInstance().signOut();
            DatabaseHelper db=new DatabaseHelper(MainActivity.this);
            db.removeAlldata();
            GeofenceMethods geofenceMethods=new GeofenceMethods(MainActivity.this,mGoogleApiClient);
            geofenceMethods.removeallgeofences();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i=new Intent(MainActivity.this,Login.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            },100);
        }
        try {
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), String.valueOf(e), Toast.LENGTH_SHORT).show();
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
    private void clearCache(){
        try{
            File cacheDir= new File(getCacheDir().getParent());
            if(cacheDir.isDirectory()&&cacheDir!=null){
                cacheDir.delete();
                Toast.makeText(getApplicationContext(),"Cache removed",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"Cache doesnt exist",Toast.LENGTH_SHORT).show();
            }
            File cacheExternalDir=getApplicationContext().getExternalCacheDir();
            if(cacheExternalDir.isDirectory() && cacheExternalDir!=null){
                cacheExternalDir.delete();
                Toast.makeText(getApplicationContext(),"E Cache exist",Toast.LENGTH_SHORT).show();

            }
            else {
                Toast.makeText(getApplicationContext(),"E Cache doesnt exist",Toast.LENGTH_SHORT).show();

            }
        }
        catch(Exception e){Toast.makeText(getApplicationContext(),"Error while deleting cache\n"+e,Toast.LENGTH_SHORT).show();}
    }

    public static void locationPermissionChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
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
                            status.startResolutionForResult(
                                    activity, 1000);
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





}
