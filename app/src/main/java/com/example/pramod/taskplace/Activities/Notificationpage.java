package com.example.pramod.taskplace.Activities;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.pramod.taskplace.Database.DatabaseHelper;
import com.example.pramod.taskplace.R;

/**
 * Created by pramod on 30/1/18.
 */

public class Notificationpage extends AppCompatActivity {
    TextView t1,t2,t3;
    Vibrator vibrator;
    Ringtone ringtone;
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_notificationpage);
        Bundle bundle = getIntent().getExtras();
        final String task_id = bundle.getString("task_id");
        t1 = findViewById(R.id.n_taskTitle);
        t2 = findViewById(R.id.n_taskDesc);
        t3 = findViewById(R.id.n_taskPlace);
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(this, uri);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        DatabaseHelper db = new DatabaseHelper(this);
        SQLiteDatabase sql = db.getWritableDatabase();
        String query = "select * from PlaceDatabase";
        Cursor cursor = sql.rawQuery(query, null);
        while (cursor.moveToNext()) {
            if (cursor.getString(1).equals(task_id)) {
                t1.setText(cursor.getString(3));
                t2.setText(cursor.getString(4));
                t3.setText(cursor.getString(2));
            }
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        vibrator.cancel();
        ringtone.stop();
    }

    @Override
    public void onResume(){
        super.onResume();
        long l[]={60,120,180,240,300,360,420,480};
        vibrator.vibrate(l,1);
        ringtone.play();
    }

}
