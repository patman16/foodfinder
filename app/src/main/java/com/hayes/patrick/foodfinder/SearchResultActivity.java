package com.hayes.patrick.foodfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Patrick on 12/25/2016.
 */

public class SearchResultActivity extends AppCompatActivity {
    private int resultIndex = 0;
    private int resultCount = 0;
    protected TextView resultName;
    protected TextView resultAddress;
    protected TextView resultStars;
    protected SupportMapFragment mapFragment;
    private String searchText;
    private List<YelpBusiness> queriedBusinesses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_result_activity_food_finder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager mapManager = getSupportFragmentManager();
        mapFragment = (SupportMapFragment)mapManager.findFragmentById(R.id.resultMap);
        FragmentTransaction ft = mapManager.beginTransaction();
        ft.hide(mapFragment);
        ft.commit();

        final ImageButton prevResultButton = (ImageButton)findViewById(R.id.prevResultButton);
        final ImageButton nextResultButton = (ImageButton)findViewById(R.id.nextResultButton);

        prevResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultIndex == 0) {
                    return;
                }

                resultIndex--;
                YelpBusiness currentBusiness = queriedBusinesses.get(resultIndex);
                resultName.setText(currentBusiness.name);
                resultAddress.setText(currentBusiness.address);
                String starText = String.format(getString(R.string.star_text), currentBusiness.starRating);
                resultStars.setText(starText);
                CheckButtonVisibility(prevResultButton, nextResultButton);
                UpdateResultCountText();
                UpdateMap(currentBusiness.name, currentBusiness.latitude, currentBusiness.longitude);
            }
        });

        nextResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int finalResultIndex = resultCount - 1;
                if (resultIndex == finalResultIndex) {
                    return;
                }

                resultIndex++;
                YelpBusiness currentBusiness = queriedBusinesses.get(resultIndex);
                resultName.setText(currentBusiness.name);
                resultAddress.setText(currentBusiness.address);
                String starText = String.format(getString(R.string.star_text), currentBusiness.starRating);
                resultStars.setText(starText);
                CheckButtonVisibility(prevResultButton, nextResultButton);
                UpdateResultCountText();
                UpdateMap(currentBusiness.name, currentBusiness.latitude, currentBusiness.longitude);
            }
        });

        RetrieveBusinesses();
    }

    private void RetrieveBusinesses() {
        Intent intent = getIntent();
        searchText = intent.getStringExtra(HomeActivity.SEARCH_TERM_MESSAGE);
        resultName = (TextView)findViewById(R.id.resultName);
        resultAddress = (TextView)findViewById(R.id.resultAddress);
        resultStars = (TextView)findViewById(R.id.resultStars);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://patman16foodfinder.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        YelpBusinessService service = retrofit.create(YelpBusinessService.class);
        Call<List<YelpBusiness>> createCall = service.get(searchText);
        final List<YelpBusiness> businesses = new ArrayList<YelpBusiness>();
        createCall.enqueue(new Callback<List<YelpBusiness>>() {
            @Override
            public void onResponse(Call<List<YelpBusiness>> _, Response<List<YelpBusiness>> resp) {
                businesses.addAll(resp.body());
                queriedBusinesses = businesses;
                resultCount = businesses.size();
                YelpBusiness business = businesses.get(0);
                resultName.setText(business.name);
                resultAddress.setText(business.address);
                String starText = String.format(getString(R.string.star_text), business.starRating);
                resultStars.setText(starText);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.show(mapFragment);
                ft.commit();
                UpdateMap(business.name, business.latitude, business.longitude);
                resultIndex = 0;

                TextView resultsCountView = (TextView) findViewById(R.id.resultsCountView);
                resultsCountView.setVisibility(View.VISIBLE);
                String resultCountText = String.format(getString(R.string.results_count), resultIndex + 1, resultCount);
                resultsCountView.setText(resultCountText);

                if (resultCount > 1) {
                    ImageButton nextResultButton = (ImageButton) findViewById(R.id.nextResultButton);
                    nextResultButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<YelpBusiness>> _, Throwable t) {
                t.printStackTrace();
                resultName.setText(t.getMessage());
                resultAddress.setText("");
                resultStars.setText("");
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.hide(mapFragment);
                ft.commit();
            }
        });
    }

    private void CheckButtonVisibility(ImageButton prevButton, ImageButton nextButton) {
        if (resultIndex == 0) {
            prevButton.setVisibility(View.GONE);
        }
        else {
            prevButton.setVisibility(View.VISIBLE);
        }

        int finalResultIndex = resultCount - 1;
        if (resultIndex == finalResultIndex) {
            nextButton.setVisibility(View.GONE);
        }
        else {
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    private void UpdateResultCountText() {
        TextView resultsCountView = (TextView)findViewById(R.id.resultsCountView);
        String resultCountText = String.format(getString(R.string.results_count), resultIndex+1, resultCount);
        resultsCountView.setText(resultCountText);
    }

    private void UpdateMap(final String businessName, final double lat, final double lon) {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.clear();
                LatLng markerPosition = new LatLng(lat, lon);
                googleMap.addMarker(new MarkerOptions().position(markerPosition).title(businessName));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition,15));
                googleMap.animateCamera(CameraUpdateFactory.zoomIn());
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_food_finder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}