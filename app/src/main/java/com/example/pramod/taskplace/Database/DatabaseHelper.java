package com.example.pramod.taskplace.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.tasks.Task;

/**
 * Created by ashish on 1/6/18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    static String db_name="TaskPlace.db";
    static String t_name="PlaceDatabase";
    static String PLACE="place";
    static String LAT="latitude";
    static String LONG="longitude";
    static String TASK_TITLE="task_title";
    static String TASK_DESC="task_desc";
    static String DATE_="taskdate";
    public DatabaseHelper(Context context) {
        super(context, db_name, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+t_name+"(sr_no INTEGER primary key,task_id varchar(30),"+PLACE+" varchar(50),"+TASK_TITLE+" varchar(20),"+TASK_DESC+" varchar(60),"+DATE_+" varchar(20),"+LAT+" varchar(20),"+LONG+" varchar(20));");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists "+db_name);
        onCreate(db);
    }
    public void removeAlldata(){
        SQLiteDatabase sqLiteDatabase=this.getWritableDatabase();
        sqLiteDatabase.delete(t_name,null,null);
    }
}
