package com.example.pramod.taskplace;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by pramod on 11/1/18.
 */
public class TaskDetails {
    public String taskid;
    public String content;
    public LatLng latlng;
    public String place;
    public String taskdate;
    public void setTaskid(String taskid){
        this.taskid=taskid;
    }
    public void setContent(String content){
        this.content=content;
    }
    public void setLatlng(LatLng latlng){
        this.latlng=latlng;
    }
    public void setPlace(String place){
        this.place=place;
    }
    public String getTaskid(){
        return taskid;
    }
    public String getContent(){
        return content;
    }
    public LatLng getLatlng(){
        return latlng;
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

}


