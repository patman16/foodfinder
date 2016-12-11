package com.hayes.patrick.foodfinder;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Patrick on 3/20/2016.
 */
public class YelpBusiness {
    @SerializedName("name")
    String name;

    @SerializedName("address")
    String address;

    @SerializedName("starRating")
    double starRating;

    @SerializedName("latitude")
    double latitude;

    @SerializedName("longitude")
    double longitude;

    public YelpBusiness(String name, String address, double starRating, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.starRating = starRating;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
