package org.expertprogramming.taskplace;
import android.app.Application;
import org.expertprogramming.taskplace.Database.DatabaseHelper;
import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;
/**
 * Created by pramod on 28/1/18.
 */

public class TaskPlace extends Application {
    public GoogleApiHelper mgoogleApiHelper;
    public DatabaseHelper databaseHelper;
    public static TaskPlace mInstance;
    public void onCreate(){
        super.onCreate();
        mInstance=this;
        mgoogleApiHelper=new GoogleApiHelper(mInstance);
        databaseHelper=new DatabaseHelper(mInstance);
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(false) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(false) //default: true
                .trackActivities(false) //default: false
                .minTimeBetweenCrashesMs(1000) //default: 3000
                .errorDrawable(R.drawable.customactivityoncrash_error_image) //default: bug image
                .eventListener(null) //default: null
                .apply();
    }
    public static synchronized TaskPlace getmInstance(){
        return mInstance;
    }
    public GoogleApiHelper getGoogleApiHelperInstance() {
        return this.mgoogleApiHelper;
    }

    public DatabaseHelper getDatabaseHelperInstance() {
        return this.databaseHelper;
    }
    public static GoogleApiHelper getGoogleApiHelper() {
        return getmInstance().getGoogleApiHelperInstance();
    }
    public static DatabaseHelper getDatabaseHelper(){
        return getmInstance().getDatabaseHelperInstance();
    }

}
