package com.example.cchiang.fastgrub;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private SQLiteDatabase sqLiteDatabase;


    Spinner staticSpinner;
    ArrayAdapter<CharSequence> staticAdapter;
    String category;
    EditText destination;
    AutoCompleteTextView destination2;
    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;
    private LatLng latLng;
    String my_var;
    private TextView mPlaceDetailsText;
    private TextView mPlaceDetailsAttribution;
    private ImageView imgview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();
        setContentView(R.layout.activity_main);
        final int[] imageArray = {
                R.drawable.all, R.drawable.american, R.drawable.asian, R.drawable.mexican, R.drawable.mediterranean,
                R.drawable.pizza, R.drawable.noodles, R.drawable.middle_eastern, R.drawable.sushi, R.drawable.italian,
                R.drawable.seafood, R.drawable.vegetarian};
        imgview = (ImageView) findViewById(R.id.imageView4);
        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_places);

        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, null,
                null);
        mAutocompleteView.setAdapter(mAdapter);
        TextWatcher watcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                my_var = null;
                Log.v("lol", "Yes it's null!");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //YOUR CODE
            }

            @Override
            public void afterTextChanged(Editable s) {

                //YOUR CODE
            }
        };
        mAutocompleteView.addTextChangedListener(watcher);

        staticSpinner = (Spinner) findViewById(R.id.static_spinner);
        staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.yelpcategories,
                        android.R.layout.simple_spinner_item);
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        staticSpinner.setAdapter(staticAdapter);

        Button clearButton = (Button) findViewById(R.id.button_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutocompleteView.setText("");
                my_var = null;
            }
        });

        // destination = (EditText) findViewById(R.id.searchEditText);

        staticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                if (position == 0) {
                    category = "food";
                    Log.v("lol", String.valueOf(position));
                }
                else {
                    category = (String) parent.getItemAtPosition(position);
                }
                imgview.setBackgroundResource(imageArray[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        try {
            sqLiteDatabase = openOrCreateDatabase("sqLiteDatabase",MODE_PRIVATE,null);
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS restaurants " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR," +
                    " lat REAL, lng REAL, " +
                    "rating REAL, img_url VARCHAR, phone VARCHAR, street VARCHAR, " +
                    "city VARCHAR, state VARCHAR, postal_code VARCHAR, website VARCHAR, category VARCHAR," +
                    "locationInput VARCHAR)");

        }
        catch(SQLiteException e) {
            Log.e(getClass().getSimpleName(),
                    "Could not create or Open the database");
        }

        Log.i("info", "before create if");
        if (isJSONDataInstantianted()) {
            // populateQuakeData(" ");
        }
        else {
            // new DownloadJSON().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    public void fetchYelpJSON(View v) {
        if (my_var != null) {
            Intent intent = new Intent(this, LoadingActivity.class);
            intent.putExtra("category", category);
            intent.putExtra("my_var", my_var);
            intent.putExtra("lat1", latLng.latitude);
            intent.putExtra("lng1", latLng.longitude);
            // new DownloadJSON().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            startActivity(intent);
        }
    }

    private boolean isJSONDataInstantianted() {
        long cnt  = DatabaseUtils.queryNumEntries(sqLiteDatabase, "restaurants");
        Log.i("info", "checked initiated with" + Long.toString(cnt));
        return cnt > 0;
    }
    /*
    class DownloadJSON extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {

        }

        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        protected String doInBackground(String... strings) {
            long entries = DatabaseUtils.queryNumEntries(sqLiteDatabase, "restaurants",
                    "category=? AND locationInput=?", new String[] {category, my_var});
            Log.v("lol", String.valueOf(entries));
            if ((!category.equals("") || category != null ||
                    !my_var.equals("") || my_var != null)
                    && entries == 0) {
                JSONApiFactory = new YelpAPIFactory(getString(R.string.consumerKey), getString(R.string.consumerSecret), getString(R.string.token), getString(R.string.tokenSecret));
                double lat1 = latLng.latitude;
                double lng1 = latLng.longitude;
                CoordinateOptions coordinate = CoordinateOptions.builder().latitude(lat1).longitude(lng1).build();
                jsonYelpAPI = JSONApiFactory.createAPI();
                paramsJSON = new HashMap<>();
                paramsJSON.put("term", category);
                paramsJSON.put("radius_filter", "1000");
                call = jsonYelpAPI.search(coordinate, paramsJSON);
                response = null;
                try {
                    response = call.execute();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                if (response != null) {
                    businessJSONList = response.body().businesses();
                    numOfResponse = businessJSONList.size();

                    for (int i = 0; i < numOfResponse; i++) {
                        String name = businessJSONList.get(i).name() == null ? "No Name" : businessJSONList.get(i).name();
                        name = name.replace("'", "''");
                        Log.v("asd", name);
                        float lat = (float) (double) businessJSONList.get(i).location().coordinate().latitude();
                        float lng = (float)(double) businessJSONList.get(i).location().coordinate().longitude();
                        float rating = businessJSONList.get(i).rating() == null ? 0 :
                                (float)(double) businessJSONList.get(i).rating();
                        String img_url =  businessJSONList.get(i).imageUrl() == null ? "" :
                                businessJSONList.get(i).imageUrl().replace("'", "''");
                        String phone = businessJSONList.get(i).phone() == null ? "" :
                                businessJSONList.get(i).phone().replace("'", "''");
                        String website = businessJSONList.get(i).url() == null ? "" :
                                businessJSONList.get(i).url().replace("'", "''");
                        String street = "";
                        String city = businessJSONList.get(i).location().city() == null ? "" :
                                businessJSONList.get(i).location().city().replace("'", "''");
                        String state = businessJSONList.get(i).location().stateCode() == null ? "" :
                                businessJSONList.get(i).location().stateCode().replace("'", "''");
                        String postal_code = businessJSONList.get(i).location().postalCode() == null ? "" :
                                businessJSONList.get(i).location().postalCode().replace("'", "''");

                        ArrayList<String> streetArray = businessJSONList.get(i).location().displayAddress();
                        for (String item : streetArray) {
                            street += item.replace("'", "''") + " ";
                        }
                        String category_yelp = category;

                        sqLiteDatabase.execSQL("INSERT INTO restaurants (name, lat, lng, rating, img_url, " +
                                "phone, street, city, state, postal_code, website, category, locationInput) " +
                                "VALUES('" + name + "','" + lat + "','" + lng + "','" + rating + "','" +
                                img_url + "','" + phone + "','" + street + "','" + city + "','" + state + "','"
                                + postal_code + "','" + website + "','" + category_yelp +  "','" + my_var + "');");

                    }
                }

                sqLiteDatabase.execSQL("INSERT INTO earthquake " +
                "(datetime, lat, lng, depth, src, eqid, magnitude, isfav) " +
                        "VALUES('" +
                        datetime + "','" +
                        lat + "','" + lng  + "'," +
                        depth  + ",'" +
                        src  + "','" + eqid + "','" + magnitude + "'," + isfav + ");");
                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS restaurants " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, type VARCHAR," +
                    " lat REAL, lng REAL, " +
                    "rating REAL, img_url VARCHAR, phone VARCHAR, street VARCHAR, " +
                    "city VARCHAR, state VARCHAR, postal_code VARCHAR, website VARCHAR, category VARCHAR)");
            }
            return null;

        }

    }
    */
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e("lol", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);
            my_var = item.getFullText(null).toString();
            Log.i("lol", "Autocomplete item selected: " + my_var);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess()) {
                            final Place myPlace = places.get(0);
                            LatLng queriedLocation = myPlace.getLatLng();
                            Log.v("Latitude is", "" + queriedLocation.latitude);
                            Log.v("Longitude is", "" + queriedLocation.longitude);
                            latLng = myPlace.getLatLng();
                        }
                        places.release();
                    }});


            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i("lol", "Called getPlaceById to get Place details for " + placeId);
        }
    };
}

