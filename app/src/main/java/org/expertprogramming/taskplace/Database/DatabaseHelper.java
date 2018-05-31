package org.expertprogramming.taskplace.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.expertprogramming.taskplace.Model.Places;
import org.expertprogramming.taskplace.Model.TaskDetails;

import java.util.ArrayList;

/**
 * Created by ashish on 1/6/18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    static String db_name="TaskPlace.db";
    static String t_name="Task";
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
        db.execSQL("create table "+t_name+"(sr_no INTEGER primary key,task_id varchar(30),"+PLACE+" varchar(50),"+TASK_TITLE+" varchar(20),"+TASK_DESC+" varchar(60),"+DATE_+" varchar(20),"+LAT+" varchar(20),"+LONG+" varchar(20),place_address varchar(100));");
        db.execSQL("create table Places(id INTEGER primary key,place_name varchar(40),place varchar(100),lat varchar(30),lng varchar(30))");
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
    public boolean isDataExist(){
        Cursor cursor=this.getReadableDatabase().rawQuery("select * from Task",null);
        if(cursor.getCount()==0){
            return false;
        }
        else {
            return true;
        }
    }
    public void insertData(TaskDetails details,String task_id){
        SQLiteDatabase sql=this.getWritableDatabase();
        //adding data to offline db
        ContentValues values = new ContentValues();
        values.put("task_id", task_id);
        values.put("place", details.getPlace());
        values.put("place_address",details.getPlaceAddress());
        values.put("task_title", details.getTaskTitle());
        values.put("task_desc", details.getTaskDesc());
        values.put("taskdate", details.getTaskdate());
        values.put("latitude", details.getLat());
        values.put("longitude", details.getLng());
        // Inserting Row
        sql.insert(t_name, null, values);
        sql.close(); //closing sql connection
        this.close(); // Closing database connection
    }
    public void deteleData(String ids){
        SQLiteDatabase sql=this.getWritableDatabase();
        sql.delete(t_name,"task_id=?",new String[]{ids});

    }
    public ArrayList<TaskDetails> fetchData(){
        Log.i("INSIDE fetchData()","fetching data from offline database");
        ArrayList<TaskDetails> details=new ArrayList<>();
        Cursor cursor=null;
        cursor = this.getReadableDatabase().rawQuery("select * from " + t_name , null);
        while(cursor.moveToNext()){
            TaskDetails d=new TaskDetails();
            d.setTaskid(cursor.getString(1));
            d.setPlace(cursor.getString(2));
            d.setLat(cursor.getString(6));
            d.setLng(cursor.getString(7));
            d.setPlaceAddress(cursor.getString(8));
            d.setTaskTitle(cursor.getString(3));
            d.setTaskDesc(cursor.getString(4));
            d.setTaskdate(cursor.getString(5));
            details.add(d);
        }
        return details;
    }
    public TaskDetails getDetailsById(String id){
        //adding by 1 because we are getting their position from list view..ie.start from 0 and so on
        String query="select * from "+t_name+" where task_id='"+id+"'";
        Cursor cursor=this.getReadableDatabase().rawQuery(query,null);
        cursor.moveToNext();
        TaskDetails details=new TaskDetails(cursor.getString(3)
                ,cursor.getString(4),cursor.getString(2),cursor.getString(8)
                ,cursor.getString(5)
                ,cursor.getString(1));
        return details;
    }
    public void updateTaskDetails(String id,String taskTitle,String taskDesc){
        ContentValues values=new ContentValues();
        values.put(TASK_TITLE,taskTitle);
        values.put(TASK_DESC,taskDesc);
        this.getWritableDatabase().update(t_name,values,"task_id='"+id+"'",null);
    }
    public void insertPlace(String placeName,String placeAddress,double lat,double lng){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("place_name",placeName);
            contentValues.put("place", placeAddress);
            contentValues.put("lat",String.valueOf(lat));
            contentValues.put("lng",String.valueOf(lng));
            this.getWritableDatabase().insertOrThrow("Places", null, contentValues);
        }catch(Exception e){
            Log.i("insertPlace()","unable to insert place data");
        }
    }
    public void deletePlaceByName(String name){
        this.getWritableDatabase().delete("Places","place='"+name+"'",null);
    }
    public void removeAllPlacedata(){
        this.getWritableDatabase().delete("Places",null,null);
    }
    public ArrayList<Places> getPlaceData(){
        ArrayList<Places> place=new ArrayList<>();
        Cursor cursor=this.getReadableDatabase().rawQuery("select * from Places",null);
        while(cursor.moveToNext()){
            Places places=new Places(cursor.getInt(0),cursor.getString(1),cursor.getString(2),Double.valueOf(cursor.getString(3)),Double.valueOf(cursor.getString(4)));
            place.add(places);
        }
        return place;
    }
    public boolean matchPlaces(String place){
        Cursor cursor=this.getReadableDatabase().rawQuery("select * from Places",null);
        while(cursor.moveToNext()){
            if(cursor.getString(1).equals(place)){
                return true;
            }
        }
        return false;
    }

}
