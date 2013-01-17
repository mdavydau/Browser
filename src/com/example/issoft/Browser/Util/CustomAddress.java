package com.example.issoft.Browser.Util;

import android.location.Address;

public class CustomAddress extends Address {

    private Address address;

    public CustomAddress(Address address) {
        super(address.getLocale());
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public String toString() {
        return address.getFeatureName() + ", " + address.getCountryName();
    }
}
