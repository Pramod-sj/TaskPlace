package org.expertprogramming.taskplaceremainder.Model;

/**
 * Created by pramod on 14/2/18.
 */

public class Places {
    private int id;
    private String place;

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    private String placeAddress;
    private double lat;
    private double lng;
    public Places(){}

    public Places(int id, String place,String placeAddress, double lat, double lng) {
        this.id = id;
        this.place = place;
        this.lat = lat;
        this.placeAddress=placeAddress;
        this.lng = lng;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

}
