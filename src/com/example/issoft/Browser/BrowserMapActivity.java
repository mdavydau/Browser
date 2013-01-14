package com.example.issoft.Browser;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

        initMapFragment();
        initAutoComplete();
        initButton();

    }

    private void initButton() {
        saveButton = (Button) findViewById(R.id.saveLocationButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng currentLatLng = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            }
        });
    }


    private void initMapFragment() {
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        Marker minsk = mMap.addMarker(new MarkerOptions()
                .position(MINSK)
                .title("Minsk")
                .snippet("You are here"));
    }

    private void initAutoComplete() {
        // Get a reference to the AutoCompleteTextView in the layout
        textView = (AutoCompleteTextView) findViewById(R.id.cityAutoCompleteLocation);
        // Get the string array
        String[] countries = getResources().getStringArray(R.array.city_array);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries);
        textView.setAdapter(adapter);

        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String cityName = (String) adapterView.getItemAtPosition(i);
                if (cityName.equals("MINSK")) mMap.animateCamera(CameraUpdateFactory.newLatLng(MINSK));
                else if (cityName.equals("NOVOPOLOTSK")) mMap.animateCamera(CameraUpdateFactory.newLatLng(NOVOPOLOTSK));
            }
        });
    }
}