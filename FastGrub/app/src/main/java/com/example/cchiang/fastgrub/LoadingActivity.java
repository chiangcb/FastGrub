package com.example.cchiang.fastgrub;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.Coordinate;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by cchiang on 5/9/17.
 */

public class LoadingActivity extends AppCompatActivity {
    private SQLiteDatabase sqLiteDatabase;
    private YelpAPIFactory JSONApiFactory;
    private YelpAPI jsonYelpAPI;
    Map<String,String> paramsJSON;
    Call<SearchResponse> call;
    Response<SearchResponse> response;
    ArrayList<Business> businessJSONList;
    int numOfResponse;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

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
        new DownloadJSON(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class DownloadJSON extends AsyncTask<String, String, String> {
        Context context;
        private ProgressDialog pdia;
        Bundle bundle;
        String category;
        String my_var;
        double lat1, lng1;

        DownloadJSON(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bundle = LoadingActivity.this.getIntent().getExtras();
            if (bundle != null) {
                category = bundle.getString("category");
                my_var = bundle.getString("my_var");
                lat1 = bundle.getDouble("lat1");
                lng1 = bundle.getDouble("lng1");
            }
            pdia = new ProgressDialog(context);
            pdia.setMessage("Please wait...");
            pdia.show();
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
                        float lat;
                        float lng;
                        if (businessJSONList.get(i).location().coordinate() != null) {
                            lat = (float) (double) businessJSONList.get(i).location().coordinate().latitude();
                            lng = (float) (double) businessJSONList.get(i).location().coordinate().longitude();
                        }
                        else {
                            continue;
                        }
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


                /*
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
                        "city VARCHAR, state VARCHAR, postal_code VARCHAR, website VARCHAR, category VARCHAR)");*/
            }
            Cursor c = sqLiteDatabase.rawQuery("SELECT " +
                    "id from " +
                    "restaurants where category = '" + category + "' " +
                    "AND locationInput = '" + my_var + "' order by random() limit 1", null);
            c.moveToFirst();
            int sendId = c.getInt(c.getColumnIndex("id"));

            Intent i = new Intent(getApplication(), MapsActivity.class);
            i.putExtra("sendId", sendId);
            startActivity(i);
            return null;

        }
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            finish();
            pdia.dismiss();
        }

    }
}
