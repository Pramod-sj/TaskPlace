/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.expertprogramming.taskplaceremainder.LocationService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import org.expertprogramming.taskplaceremainder.TaskPlace;
import com.google.android.gms.location.LocationResult;
import java.util.List;
import es.dmoral.toasty.Toasty;
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    Context context;
    private static final String TAG = "TaskPlace_LocationUpdate";
    public static final String ACTION_PROCESS_UPDATES = "com.example.pramod.taskplace.action" + ".PROCESS_UPDATES";
    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context=context;
        TaskPlace.getGoogleApiHelper().connect();
        Log.i("INTENT",intent.getAction());
        if(intent.getAction().equals("android.location.PROVIDERS_CHANGED")){
            if(LocationRequestHelper.getRequestingTrigger(context)==false) {
                int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, 0);
                Log.i("MODE", String.valueOf(mode));
                if (mode == 0) {
                    Toasty.error(context, "No way to get Location Updates", Toast.LENGTH_LONG).show();
                } else if (mode == 2) {
                    Toasty.warning(context, "App may not work properly", Toast.LENGTH_LONG).show();
                } else if (mode == 1) {
                    Toasty.warning(context, "wait we are working on this mode for now change to GPS provider", Toast.LENGTH_LONG).show();
                } else if (mode == 3) {
                    Toasty.success(context, "Yeah we got all the stuff we needed...now make your tasks active", Toast.LENGTH_LONG).show();
                }
            }

        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.i("Boot","After Boot");
            if(LocationRequestHelper.getRequestingTrigger(context)==false) {
                Log.i("after Boot","inside getRequesting Trigger");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, FusedLocationService.class));
                }
                else {
                    context.startService(new Intent(context, FusedLocationService.class));
                }
            }

        }
        if (intent.getAction().equals(ACTION_PROCESS_UPDATES)) {
            LocationResult result = LocationResult.extractResult(intent);
            if (result == null) {
                return;
            }
            else{
                List<Location> locations = result.getLocations();
                String latlng=locations.get(0).getLatitude()+":"+locations.get(0).getLongitude();
                LocationRequestHelper.setLocationRequesting(context,latlng);
                LocationResultHelper locationResultHelper = new LocationResultHelper(context, locations);
                locationResultHelper.checkDistanceBetween(locations.get(0));
                locationResultHelper.saveResults();
                Log.i(TAG, LocationResultHelper.getSavedLocationResult(context));
            }
        }

    }

}
