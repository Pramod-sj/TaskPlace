package org.expertprogramming.taskplace.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import android.widget.TextView;
import android.widget.Toast;

import org.expertprogramming.taskplace.Fragments.HistoryFragment;
import org.expertprogramming.taskplace.LocationService.FusedLocationService;
import org.expertprogramming.taskplace.Model.CurrentUserData;
import org.expertprogramming.taskplace.Fragments.NavMapFragment;
import org.expertprogramming.taskplace.Fragments.SetTaskFragment;
import org.expertprogramming.taskplace.Fragments.ViewTaskFragment;
import org.expertprogramming.taskplace.R;
import org.expertprogramming.taskplace.TaskPlace;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private CircleImageView circleImageView;
    private View header;
    private Uri ImagePATH;
    private final int SELECT_PHOTO=100;
    private AlertDialog alertDialog;
    private GoogleApiClient mGoogleApiClient;
    private TextView username,useremail;
    private static int backpressedCount=0;
    //Firebase
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setVerticalScrollBarEnabled(false);
        header = navigationView.getHeaderView(0);
        circleImageView = header.findViewById(R.id.imageView);
        if(!isUserLoginWithGmail()) {
            circleImageView.setClickable(true);
            circleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "Click", Toast.LENGTH_SHORT).show();
                    alertDialog.show();
                    }
            });
        }
        username = header.findViewById(R.id.username);
        useremail = header.findViewById(R.id.useremail);
        setUserProfile();
        //for notification
        mGoogleApiClient = TaskPlace.getGoogleApiHelper().getGoogleApiClient();
        mGoogleApiClient.connect();
        //end
        showFragment();
        //shortcut fragment selection
       // shourtcutSelection();
        //AlertDialog for image selection and upload
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        alertDialog = builder.create();
        if (!isUserLoginWithGmail()) {
            if (!storage.getReference().child("images/" + new CurrentUserData(this).getCurrentUID()).equals(null)) {
                storage.getReference().child("images/" + new CurrentUserData(this).getCurrentUID()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(MainActivity.this).load(uri).placeholder(R.drawable.placeholder).into(circleImageView);
                    }
                });
            }
        }

    }
    public boolean isUserLoginWithGmail(){
        for(UserInfo userInfo:FirebaseAuth.getInstance().getCurrentUser().getProviderData()){
            if(userInfo.getProviderId().equals("google.com")){
                return true;
            }
        }
        return false;
    }
    public void showFragment(){
        String ScrollViewActivity = getIntent().getStringExtra("scrollView");
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(prefs.getBoolean("firstlogin",true)){
            prefs.edit().putBoolean("firstlogin",false).commit();
            navItem(R.id.set_navtask);
        }
        else if(ScrollViewActivity != null) {
            if (prefs.getBoolean("firstlogin",false)==false) {
                navItem(R.id.view_navTask);
            }
            else {
                navItem(R.id.set_navtask);
            }
        }
        else{
            shourtcutSelection();
        }

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
        if(!checkPermissions(this)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }
    }

    public static boolean checkPermissions(Context context) {
        int permissionState = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }
    protected void onResume(){
        super.onResume();
        if(checkPermissions(this)) {
            TaskPlace.getGoogleApiHelper().connect();
            if(mGoogleApiClient.isConnected()) {
                locationPermissionChecker(mGoogleApiClient, MainActivity.this);
            }
        }
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
                switch (resultCode){
                    case Activity.RESULT_OK:
                        TaskPlace.getGoogleApiHelper().connect();
                        if(mGoogleApiClient.isConnected()) {
                            locationPermissionChecker(mGoogleApiClient, MainActivity.this);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Toasty.error(getApplicationContext(),"This application requires prompted permission",Toast.LENGTH_LONG).show();
                        finish();

                }
                break;
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
            exitDialog();
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
        else if(id==R.id.view_navHistoryTask){
            fragment=new HistoryFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent, fragment).commit();

        }
        else if(id==R.id.nav_logout){
            logOutDialog();
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
    public void locationPermissionChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(FusedLocationService.DEFAULT_INTERVAL);
        locationRequest.setFastestInterval(FusedLocationService.FASTEST_INTERVAL);
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
                        //Toasty.success(MainActivity.this,"Successfully granted required permission",Toast.LENGTH_SHORT).show();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(activity, REQUEST_PERMISSIONS_REQUEST_CODE);
                        } catch (Exception e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
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
    public void logOutDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Logout")
                .setMessage("Do you want to logout")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                ;
        builder.create();
        builder.show();

    }

    @Override
    public void onBackPressed(){
        exitDialog();
    }
    public void exitDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setMessage("Do you want to exit")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
    public void shourtcutSelection(){
        Fragment f;
        if("com.expertprogramming.taskplace.settask".equals(getIntent().getAction())){
            f=new SetTaskFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent,f).commit();
            Log.i("shortcut","settask");
        }
        else if("com.expertprogramming.taskplace.history".equals(getIntent().getAction())){
            f=new HistoryFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent,f).commit();
            Log.i("shortcut","history");
        }
        else {
            //this will execute when user click shortcut or for default launching
            f=new ViewTaskFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.flContent,f).commit();
        }
    }
}
