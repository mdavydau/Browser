package com.example.issoft.Browser.db;

public class Country {
    private int _id;
    private String _name;
    private double _latitude;
    private double _longitude;
    private String _additional;

    public Country() {
    }

    public Country(String _name, double _latitude, double _longitude, String _additional) {
        this._name = _name;
        this._latitude = _latitude;
        this._longitude = _longitude;
        this._additional = _additional;
    }

    public Country(int _id, String _name, double _latitude, double _longitude) {
        this._id = _id;
        this._name = _name;
        this._latitude = _latitude;
        this._longitude = _longitude;
    }

    public Country(int _id, String _name, double _latitude, double _longitude, String _additional) {
        this._id = _id;
        this._name = _name;
        this._latitude = _latitude;
        this._longitude = _longitude;
        this._additional = _additional;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public double get_latitude() {
        return _latitude;
    }

    public void set_latitude(double _latitude) {
        this._latitude = _latitude;
    }

    public double get_longitude() {
        return _longitude;
    }

    public void set_longitude(double _longitude) {
        this._longitude = _longitude;
    }

    public String get_additional() {
        return _additional;
    }

    public void set_additional(String _additional) {
        this._additional = _additional;
    }
}
