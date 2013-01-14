package com.example.issoft.Browser;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.maps.GeoPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * User: nikitadavydov
 * Date: 1/9/13
 */
public class BrowserMapActivity extends FragmentActivity {

    private GoogleMap mMap;
    private AutoCompleteTextView textView;
    private Button saveButton;
    static final LatLng MINSK = new LatLng(53.9, 27.566667);
    static final LatLng NOVOPOLOTSK = new LatLng(55.5333, 28.6500);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_widget);

        initViews();
        initAutoComplete();

        setMarker(MINSK, "Minsk", "");
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        saveButton = (Button) findViewById(R.id.saveLocationButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng currentLatLng = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            }
        });
    }

    private void setMarker(LatLng position, String title, String snippet) {
        Marker marker;
        if (snippet != null) {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet));
        } else {
            marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title));
        }
    }

    private void initAutoComplete() {
        // Get a reference to the AutoCompleteTextView in the layout
        textView = (AutoCompleteTextView) findViewById(R.id.cityAutoCompleteLocation);
        // Get the string array
        String[] countries = getResources().getStringArray(R.array.city_array);

        // Create the adapter and set it to the AutoCompleteTextView
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, countries);
        adapter.setNotifyOnChange(true);
        textView.setAdapter(adapter);

        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                geoCoder(editable.toString());
            }
        });

        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String str = (String) adapterView.getItemAtPosition(position);
                makeToast(str);
            }
        });
    }

    private List<Address> geoCoder(String locationName) {
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geoCoder.getFromLocationName(locationName, 5);
            String add = "";
            if (addresses.size() > 0) {
                return addresses;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}