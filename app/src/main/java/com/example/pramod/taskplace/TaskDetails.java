package com.example.pramod.taskplace;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by pramod on 11/1/18.
 */
public class TaskDetails {
    public String task_id;
    public String content;
    public String lng;
    public String lat;
    public String place;
    public String taskdate;
    public String taskDesc;
    public void setTaskid(String taskid){
        this.task_id=taskid;
    }
    public void setContent(String content){
        this.content=content;
    }
    public void setLat(String lat){
        this.lat=lat;
    }
    public void setLng(String lng){
        this.lng=lng;
    }
    public void setPlace(String place){
        this.place=place;
    }
    public String getTaskid(){
        return task_id;
    }
    public String getContent(){
        return content;
    }
    public String getPlace(){
        return place;
    }
    public void setTaskdate(String date){
        this.taskdate=date;
    }
    public String getTaskdate(){
        return taskdate;
    }
    public void setTaskDesc(String taskDesc){
        this.taskDesc=taskDesc;
    }
    public String getTaskDesc(){
        return taskDesc;
    }
    public String getLat(){return lat;}
    public String getLng(){return lng;}


}


