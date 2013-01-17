package com.example.issoft.Browser.Util;

import android.content.Context;
import android.location.Address;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CustomAddressArrayAdapter<T> extends ArrayAdapter<CustomAddress> {

    public CustomAddressArrayAdapter(Context context, int textViewResourceId, List<CustomAddress> objects) {
        super(context, textViewResourceId, objects);
    }
}
