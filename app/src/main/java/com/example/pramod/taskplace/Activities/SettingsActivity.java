package com.example.pramod.taskplace.Activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.pramod.taskplace.Activities.MainActivity;
import com.example.pramod.taskplace.AppCompatPreferenceActivity;
import com.example.pramod.taskplace.LocationService.LocationRequestHelper;
import com.example.pramod.taskplace.LocationService.LocationServiceMethods;
import com.example.pramod.taskplace.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_setting);
        buildGoogleApiClient();
        Preference clearcache= findPreference("cache");
        clearcache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                clearCache();
                return true;
            }
        });
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor=preferences.edit();
        if(preferences.getBoolean("notification",false)==true){
            Toast.makeText(getApplicationContext(),"Notification enable",Toast.LENGTH_SHORT).show();
        }
        final ListPreference listPreference= (ListPreference) findPreference("radius");
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                listPreference.setValue((String) newValue);
                return true;
            }
        });
        final SwitchPreference switchPreference= (SwitchPreference) findPreference("mode");
        switchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(switchPreference.isChecked()){
                    editor.putBoolean("mode",true);
                    if(mGoogleApiClient.isConnected()){
                        LocationServiceMethods methods=new LocationServiceMethods(getApplicationContext(),mGoogleApiClient);
                        if(LocationRequestHelper.getRequestingTrigger(getApplicationContext())==false) {
                            methods.removeLocationUpdates();
                            methods.createLocationRequest();
                            methods.requestLocationUpdates();
                        }
                    }
                }
                else{
                    editor.putBoolean("mode",false);
                    if(mGoogleApiClient.isConnected()){
                        LocationServiceMethods methods=new LocationServiceMethods(getApplicationContext(),mGoogleApiClient);
                        if(LocationRequestHelper.getRequestingTrigger(getApplicationContext())==false) {
                            methods.removeLocationUpdates();
                            methods.createLocationRequest();
                            methods.requestLocationUpdates();
                        }
                    }
                }
                return true;
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    private synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
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
        mGoogleApiClient.connect();

    }
    private void clearCache(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ((ActivityManager)getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
        }
       /* try{
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
        */
    }

    protected void onStop(){
        super.onStop();
        finish();
    }

}
