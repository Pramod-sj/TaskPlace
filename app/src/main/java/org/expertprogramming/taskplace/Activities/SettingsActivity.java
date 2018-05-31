package org.expertprogramming.taskplace.Activities;
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
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.widget.Toast;

import org.expertprogramming.taskplace.LocationService.FusedLocationService;
import org.expertprogramming.taskplace.LocationService.LocationRequestHelper;
import org.expertprogramming.taskplace.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import es.dmoral.toasty.Toasty;

public class SettingsActivity extends PreferenceActivity {
    GoogleApiClient mGoogleApiClient;
    private AppCompatDelegate delegate;
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
                Toasty.warning(getApplicationContext(),"We are working on this feature",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor=preferences.edit();
        final SwitchPreference vibration= (SwitchPreference) findPreference("notifications_new_message_vibrate");
        if(vibration.isChecked()){
            vibration.setSummary("Vibration is enable");
        }else{
            vibration.setSummary("Vibration is disable");
        }
        vibration.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(vibration.isChecked()){
                    vibration.setSummary("Vibration is enable");
                }else{
                    vibration.setSummary("Vibration is disable");
                }
                return true;
            }
        });
        final ListPreference listPreference= (ListPreference) findPreference("radius");
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                listPreference.setValue((String) newValue);
                return true;
            }
        });
        final ListPreference NotiTypePreference= (ListPreference) findPreference("not_type");
        NotiTypePreference.setSummary(NotiTypePreference.getEntry());
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                NotiTypePreference.setValue((String) newValue);
                NotiTypePreference.setSummary(NotiTypePreference.getEntry());
                return true;
            }
        });
        final SwitchPreference switchPreference= (SwitchPreference) findPreference("mode");
        switchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(switchPreference.isChecked()){
                    editor.putBoolean("mode",true);
                    if(LocationRequestHelper.getRequestingTrigger(getApplicationContext())==false) {
                        getApplicationContext().stopService(new Intent(getApplicationContext(), FusedLocationService.class));

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getApplicationContext().startForegroundService(new Intent(getApplicationContext(), FusedLocationService.class));
                        }else {
                            getApplicationContext().startService(new Intent(getApplicationContext(), FusedLocationService.class));
                        }
                    }
                }
                else{
                    if(LocationRequestHelper.getRequestingTrigger(getApplicationContext())==false) {
                        getApplicationContext().stopService(new Intent(getApplicationContext(), FusedLocationService.class));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getApplicationContext().startForegroundService(new Intent(getApplicationContext(), FusedLocationService.class));
                        }else {
                            getApplicationContext().startService(new Intent(getApplicationContext(), FusedLocationService.class));
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
    private AppCompatDelegate getDelegate(){
        if(delegate==null){
            delegate=AppCompatDelegate.create(this,null);
        }
        return delegate;
    }
    private void setupActionBar() {
        ActionBar actionBar =  getDelegate().getSupportActionBar();
        if (actionBar != null) {
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

    protected void onStop(){
        super.onStop();
    }

    protected void onDestroy(){
        finish();
        super.onDestroy();
    }


}
