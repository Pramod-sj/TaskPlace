package org.expertprogramming.taskplace;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by pramod on 7/2/18.
 */

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    public GoogleApiHelper(Context context){
        this.context=context;
        buildGoogleApiClient();
        connect();
    }
    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }
    public boolean isConnected(){
        return mGoogleApiClient!=null && mGoogleApiClient.isConnected();
    }
    public void connect(){
        if(mGoogleApiClient!=null){
            mGoogleApiClient.connect();
        }
    }

    public void disconnect(){
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

}
