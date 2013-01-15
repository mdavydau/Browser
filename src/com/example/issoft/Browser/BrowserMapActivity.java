package com.example.issoft.Browser;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.issoft.Browser.Util.Constants.MINSK;

/**
 * User: nikitadavydov
 * Date: 1/9/13
 */
public class BrowserMapActivity extends FragmentActivity {

    private GoogleMap mMap;
    private AutoCompleteTextView textView;
    private Button saveButton;
    private ArrayAdapter<String> adapter;

    private ArrayList<Address> addressList = new ArrayList<Address>();
    private final Handler handler = new Handler();


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
        if (snippet != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet));
        } else {
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title));
        }
    }

    private void initAutoComplete() {
        textView = (AutoCompleteTextView) findViewById(R.id.cityAutoCompleteLocation);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        textView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 2) {
                    geoCoder(editable.toString());
                }
            }
        });

        adapter.setNotifyOnChange(true);
        textView.setAdapter(adapter);

        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String address = (String) adapterView.getItemAtPosition(position);

                String[] splitEditText = address.split(",");

                LatLng currentLatLng = new LatLng(Double.parseDouble(splitEditText[2]), Double.parseDouble(splitEditText[3]));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                setMarker(currentLatLng, splitEditText[0], splitEditText[1]);

                makeToast(address);
            }
        });
    }


    private void geoCoder(final String locationName) {
        final Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<Address> addresses;
                try {
                    addresses = geoCoder.getFromLocationName(locationName, 5);
                    if (addresses.size() > 0) {
                        addressList.clear();
                        adapter.clear();
                        for (Address address : addresses) {
                            addressList.add(address);
                            adapter.add(address.getFeatureName() + ", " + address.getCountryName() + ", " + address.getLatitude() + ", " + address.getLongitude());
                        }
                        adapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }
}