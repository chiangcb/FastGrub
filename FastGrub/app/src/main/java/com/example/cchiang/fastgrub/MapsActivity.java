package com.example.cchiang.fastgrub;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.MapFragment;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {
    private SQLiteDatabase sqLiteDatabase;

    private GoogleMap map;
    private LatLng myLocation;
    int sendId;
    Restaurant restaurant;
    private float lat;
    private float lng;
    private LatLng latLng;
    TextView txtview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.the_map);
        mf.getMapAsync(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            sendId = bundle.getInt("sendId");
        }
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
        restaurant = new Restaurant();
        txtview = (TextView) findViewById(R.id.restaurantTextView);
        setRestaurantData();

    }

    public void detailsPressed(View view) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("name", restaurant.getName());
        intent.putExtra("phone", restaurant.getPhone());
        intent.putExtra("street", restaurant.getStreet());
        intent.putExtra("lat", restaurant.getLat());
        intent.putExtra("lng", restaurant.getLng());
        intent.putExtra("rating", restaurant.getRating());
        startActivity(intent);
    }

    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void setRestaurantData() {
        Cursor res = sqLiteDatabase.rawQuery("select * from restaurants where id = " + sendId + ";", null);
        res.moveToFirst();
        restaurant.setName(res.getString(res.getColumnIndex("name")));
        restaurant.setPhone(res.getString(res.getColumnIndex(("phone"))));
        restaurant.setImg_url(res.getString(res.getColumnIndex(("img_url"))));
        restaurant.setStreet(res.getString(res.getColumnIndex(("street"))));
        restaurant.setLat(res.getFloat(res.getColumnIndex("lat")));
        restaurant.setLng(res.getFloat(res.getColumnIndex("lng")));
        restaurant.setRating(res.getFloat(res.getColumnIndex("rating")));
        txtview.setText(restaurant.getName());
        res.close();
    }

    private void setMapView() {
        lat = restaurant.getLat();
        lng = restaurant.getLng();
        latLng = new LatLng(lat, lng);
        map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Name: " + restaurant.getName())
                    .snippet("Street: " + restaurant.getStreet()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16), 5000, null);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        // map.setOnMapLoadedCallback(this);      // calls onMapLoaded when layout done
        UiSettings mapSettings;
        mapSettings = map.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);
        lat = restaurant.getLat();
        lng = restaurant.getLng();
        latLng = new LatLng(lat, lng);
        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Name: " + restaurant.getName())
                .snippet("Street: " + restaurant.getStreet()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8), 3000, null);
        marker.showInfoWindow();

    }

    public void onMapLoaded() {
        setMapView();
    }
    private void setInfoView() {

    }

    public boolean onMarkerClick(Marker marker) {
        if (myLocation != null) {
            setInfoView();
            LatLng markerLatLng = marker.getPosition();
            map.addPolyline(new PolylineOptions()
                    .add(myLocation)
                    .add(markerLatLng)
            );
            return true;
        } else {
            return false;
        }
    }
}
