package com.example.yanl1393.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.util.CrashUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xml.sax.helpers.ParserAdapter;

import java.nio.file.SecureDirectoryStream;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;

    private final double SEARCH_DISTANCE = 5.0/60.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(32.885939, -117.221751);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Born Here (ish)"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed FINE permission check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed COARSE permission check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        locationSearch = (EditText) findViewById(R.id.editText);
    }

    public void changeView(View view) {
        if (mMap.getMapType()==GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

    }

    public void onSearch(View view) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        //use LocationManager  for uesr location info
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMapsApp:", "onSearch: location = " + location);
        Log.d("MyMapsApp:", "onSearch: provider = " + provider);

        LatLng userLocation = null;

        try {
            //check last known location, need to specifically list the provider network or gps
            if (locationManager != null) {
                if ((myLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER user location is : " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else if ((myLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER user location is : " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MyMapsApp", "onSearch: myLocation is null");
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMapsApp", "onSearch: Exception on getLastKnownLocation");
        }

        if (!location.matches("")) {
            //create Geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {
                addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - SEARCH_DISTANCE,
                        userLocation.longitude - SEARCH_DISTANCE,
                        userLocation.latitude + SEARCH_DISTANCE,
                        userLocation.longitude + SEARCH_DISTANCE);

                Log.d("MyMapsApp", "created addressList");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!addressList.isEmpty()) {
                Log.d("MyMapsApp", "Address list size: " + addressList.size());

                for (int i = 0; i<addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLatitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }
    }
}
