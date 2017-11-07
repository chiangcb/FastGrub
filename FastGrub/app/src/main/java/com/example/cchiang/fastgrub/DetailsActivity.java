package com.example.cchiang.fastgrub;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {
    String name;
    float lat;
    float lng;
    float rating;
    String phone;
    String street;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            name = bundle.getString("name");
            lat = bundle.getFloat("lat");
            lng = bundle.getFloat("lng");
            rating = bundle.getFloat("rating");
            phone = bundle.getString("phone");
            street = bundle.getString("street");
        }

        TextView text = (TextView) findViewById(R.id.name);
        text.setText(name);
        text = (TextView) findViewById(R.id.rating);
        text.setText("" + rating);
        text = (TextView) findViewById(R.id.address);
        text.setText("" + street);
        text = (TextView) findViewById(R.id.phone);
        text.setText("" + phone);
    }
}
