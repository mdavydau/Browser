package com.example.issoft.Browser;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.issoft.Browser.Util.CustomAddress;
import com.example.issoft.Browser.Util.CustomAddressArrayAdapter;
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
    private CustomAddress customAddress;

    private CustomAddressArrayAdapter<CustomAddress> customAddressArrayAdapter;

    private List<CustomAddress> menuSavedCustomAddress = new ArrayList<CustomAddress>();
    private ArrayList<CustomAddress> customAddressArrayList = new ArrayList<CustomAddress>();

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_widget);

        initViews();
        initAutoComplete();

        makeMarker(MINSK, "Minsk", "");
    }

    private void initViews() {
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideKeyboardAndRequestFocus();
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //todo: set new pointer
            }
        });

        saveButton = (Button) findViewById(R.id.saveLocationButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCurrentLocationToMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.openSavedLocations:
                //todo: alert dialog with locations
                return true;
        }
        return false;
    }

    private void saveCurrentLocationToMenu() {
        //todo: before close program - save this stuff to DB
        if (!menuSavedCustomAddress.contains(customAddress)) menuSavedCustomAddress.add(customAddress);
    }

    private void initAutoComplete() {
        textView = (AutoCompleteTextView) findViewById(R.id.cityAutoCompleteLocation);
        setAdapter();

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
                    setAdapter();
                }
            }
        });

        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                customAddress = (CustomAddress) adapterView.getItemAtPosition(position);
                goToCurrentPosition(customAddress.getAddress());
                textView.setText(customAddress.getAddress().getFeatureName() + ", " + customAddress.getAddress().getCountryName());
                hideKeyboardAndRequestFocus();
            }
        });
    }

    private void makeMarker(LatLng position, String title, String snippet) {
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

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void setAdapter() {
        customAddressArrayAdapter = new CustomAddressArrayAdapter<CustomAddress>(this, R.layout.list_item, customAddressArrayList);
        textView.setAdapter(customAddressArrayAdapter);
    }

    private void goToCurrentPosition(Address address) {
        LatLng currentLatLng = new LatLng(address.getLatitude(), address.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));

        makeMarker(currentLatLng, address.getFeatureName(), address.getCountryName() + "\n" + "(" + address.getLatitude() + address.getLongitude() + ")");
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
                        customAddressArrayList.clear();
                        for (Address address : addresses) {
                            CustomAddress customAddress = new CustomAddress(address);
                            customAddressArrayList.add(customAddress);
                        }
                        customAddressArrayAdapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void hideKeyboardAndRequestFocus() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
        saveButton.requestFocus();
    }
}