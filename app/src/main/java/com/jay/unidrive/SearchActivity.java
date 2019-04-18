package com.jay.unidrive;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private Place origin;
    private Place destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setUpSearchBar();
    }

    private void setUpSearchBar(){
        Places.initialize(getApplicationContext(), "AIzaSyA9QNjBgd0mIabGWyboE_ZrTPfNdn1RJi4");

        // Origin Search Bar
        AutocompleteSupportFragment autocompleteFragmentOrigin = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_origin);

        // Specify the types of place data to return.
        autocompleteFragmentOrigin.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragmentOrigin.setLocationBias(RectangularBounds.newInstance(
                new LatLng(3.215299, 101.748198),
                new LatLng(3.218896, 101.751801)));
        autocompleteFragmentOrigin.setCountry("MY");

        // Origin Place Listener
        autocompleteFragmentOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                origin = place;
                if (destination != null){
                    Intent data = new Intent();
                    data.putExtra("origin", origin.getId());
                    data.putExtra("destination", destination.getId());
                    setResult(RESULT_OK, data);
                    finish();
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        //Destination Search Bar
        AutocompleteSupportFragment autocompleteFragmentDest = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_dest);

        // Specify the types of place data to return.
        autocompleteFragmentDest.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragmentDest.setLocationBias(RectangularBounds.newInstance(
                new LatLng(3.215299, 101.748198),
                new LatLng(3.218896, 101.751801)));
        autocompleteFragmentDest.setCountry("MY");


// Set up a PlaceSelectionListener to handle the response.
        autocompleteFragmentDest.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.

                destination = place;
                if (origin != null){
                    Intent data = new Intent();
                    data.putExtra("origin", origin.getId());
                    data.putExtra("destination", destination.getId());
                    setResult(RESULT_OK, data);
                    finish();
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }
}
