package com.hayes.patrick.foodfinder;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Patrick on 12/10/2016.
 */

public interface YelpBusinessService {
    @GET("business/{latitude}/{longitude}/{mileRadius}/{searchTerm}")
    Call<List<YelpBusiness>> get(@Path("latitude") double latitude, @Path("longitude") double longitude,
                                 @Path("mileRadius") double mileRadius, @Path("searchTerm") String searchTerm);
}