package com.example.issoft.Browser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import com.example.issoft.Browser.Route.GoogleParser;
import com.example.issoft.Browser.Route.Parser;
import com.example.issoft.Browser.Route.Route;
import com.example.issoft.Browser.Util.CustomAddress;
import com.example.issoft.Browser.Util.CustomAddressArrayAdapter;
import com.example.issoft.Browser.db.Country;
import com.example.issoft.Browser.db.DatabaseHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

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

    private static final String BROWSER_MAP_ACTIVITY = BrowserMapActivity.class.getName();
    private static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    private static final String MODE = "driving";
    private final int CHOOSE_POINT_DIALOG = 0;
    private final int LOAD_ALL_SAVED_COUNTRIES = 1;

    private GoogleMap mMap;
    private Polyline polyline;
    private Marker markerStart;
    private Marker markerFinish;

    private AutoCompleteTextView textView;
    private Button saveButton;
    private CustomAddress currentCustomAddress;
    private AlertDialog.Builder builder;

    private LatLng startLatlng;
    private LatLng finishLatlng;
    private LatLng currentLatlng;

    private DatabaseHandler db;

    private CustomAddressArrayAdapter<CustomAddress> customAddressArrayAdapter;

    private List<Country> countries;
    //    private List<CustomAddress> menuSavedCustomAddress = new ArrayList<CustomAddress>();
    private ArrayList<CustomAddress> customAddressArrayList = new ArrayList<CustomAddress>();

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_widget);

        db = new DatabaseHandler(this);
        initViews();
        initAutoComplete();
        makeMarker(MINSK, "Minsk", "");
    }

    protected void onResume() {
        super.onResume();
        /**
         * bad realization, but worked
         * @param currentLocation
         * take from zero-search place
         * */
        SharedPreferences settings = getSharedPreferences(BROWSER_MAP_ACTIVITY, 0);
        String[] currentLocation;
        try {
            currentLocation = settings.getString(CURRENT_LOCATION, "").split(",");
            findCurrentLocationByFeatureNameAndCountryName(currentLocation[0], currentLocation[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(BROWSER_MAP_ACTIVITY, e.toString());
        }
    }

    protected void onPause() {
        super.onPause();
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
                currentLatlng = latLng;
                showDialog(CHOOSE_POINT_DIALOG);
                makeToast("lat:" + latLng.latitude + " lng: " + latLng.longitude);
            }
        });

        saveButton = (Button) findViewById(R.id.saveLocationButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCurrentLocationToMenu();

                SharedPreferences settings = getSharedPreferences(BROWSER_MAP_ACTIVITY, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(CURRENT_LOCATION, currentCustomAddress.getAddress().getFeatureName() + ", " + currentCustomAddress.getAddress().getCountryName());
                editor.commit();
            }
        });
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CHOOSE_POINT_DIALOG:
                final String[] dialogItems = {"Start point", "End point"};
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Add route points");
                builder.setItems(dialogItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (dialogItems[item].equals("Start point")) {
                            startLatlng = currentLatlng;
                            if (polyline != null) clearAllRoutes();
                            markerStart = makeMarker(startLatlng, "Start", "");
                        } else if (dialogItems[item].equals("End point")) {
                            finishLatlng = currentLatlng;
                            if (startLatlng != null && finishLatlng != null) route();
                            markerFinish = makeMarker(finishLatlng, "Finish", "");
                        }
                    }
                });
                return builder.create();
            case LOAD_ALL_SAVED_COUNTRIES:
                final String[] countryItems = getAllSavedCountries();
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Saved locations");
                builder.setItems(countryItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        String[] splitCountries = countryItems[item].split(",");
                        Country country = db.getCountry(Integer.parseInt(splitCountries[2].trim()));
                        goToCurrentPosition(country);
                        makeToast(country.get_latitude() + ", " + country.get_longitude());
                    }
                });
                return builder.create();
            default:
                return null;
        }
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
                showDialog(LOAD_ALL_SAVED_COUNTRIES);
                return true;
            case R.id.clearAllRoutes:
                clearAllRoutes();
                return true;
            case R.id.saveCurrentLocation:
                saveCurrentLocationToMenu();
        }
        return false;
    }

    private String[] getAllSavedCountries() {
        countries = db.getAllCountries();
        String[] countriesArray = new String[countries.size()];
        int i = 0;

        for (Country cn : countries) {
            countriesArray[i] = cn.get_name() + ", " + cn.get_id();
            i++;
        }
        return countriesArray;
    }

    private void clearAllRoutes() {
        if (polyline != null) polyline.remove();
        if (markerStart != null) markerStart.remove();
        if (markerFinish != null) markerFinish.remove();
    }

    private void saveCurrentLocationToMenu() {
        db.addCountry(new Country(
                currentCustomAddress.getAddress().getFeatureName() + ", " + currentCustomAddress.getAddress().getCountryName(),
                currentCustomAddress.getAddress().getLatitude(),
                currentCustomAddress.getAddress().getLongitude(),
                ""));
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
                currentCustomAddress = (CustomAddress) adapterView.getItemAtPosition(position);
                goToCurrentPosition(currentCustomAddress.getAddress());
                textView.setText(currentCustomAddress.getAddress().getFeatureName() + ", " + currentCustomAddress.getAddress().getCountryName());
                hideKeyboardAndRequestFocus();
            }
        });
    }

    private Marker makeMarker(LatLng position, String title, String snippet) {
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
        return marker;
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

        makeMarker(currentLatLng, address.getFeatureName(), address.getCountryName() + " (" + address.getLatitude() + ", " + address.getLongitude() + ")");
    }

    private void goToCurrentPosition(Country country) {
        LatLng currentLatLng = new LatLng(country.get_latitude(), country.get_longitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));

        makeMarker(currentLatLng, country.get_name(), " (" + country.get_latitude() + ", " + country.get_longitude() + ")");
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

    private void findCurrentLocationByFeatureNameAndCountryName(final String feature, final String country) {
        final Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                List<Address> addresses;
                Address currentAddress;
                try {
                    addresses = geoCoder.getFromLocationName(feature, 5);
                    if (addresses.size() > 1) {
                        for (Address address : addresses) {
                            currentAddress = new CustomAddress(address);
                            if (currentAddress.getCountryName().equals(country))
                                goToCurrentPosition(currentAddress);
                        }
                    } else if (addresses.size() == 1) {
                        goToCurrentPosition(addresses.get(0));
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

    private void route() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                polyline = mMap.addPolyline(new PolylineOptions()
                        .addAll(directions(startLatlng, finishLatlng).getPoints())
                        .width(5)
                        .color(Color.BLUE));
            }
        };
        handler.postDelayed(runnable, 1000);

    }

    private Route directions(final LatLng start, final LatLng dest) {
        Parser parser;
        //https://developers.google.com/maps/documentation/directions/#JSON <- get api
        String jsonURL = "http://maps.googleapis.com/maps/api/directions/json?";
        final StringBuffer sBuf = new StringBuffer(jsonURL);
        sBuf.append("origin=");
        sBuf.append(start.latitude);
        sBuf.append(',');
        sBuf.append(start.longitude);
        sBuf.append("&destination=");
        sBuf.append(dest.latitude);
        sBuf.append(',');
        sBuf.append(dest.longitude);
        sBuf.append("&sensor=true&mode=");
        sBuf.append(MODE);
        parser = new GoogleParser(sBuf.toString());
        return parser.parse();
    }
}