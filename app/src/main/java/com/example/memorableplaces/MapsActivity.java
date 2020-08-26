package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Location lastKnownLocation;
    String encodedString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Intent intent = getIntent();
        int itemClicked = intent.getIntExtra("itemNumber",-1);
        mMap = googleMap;
        LatLng userLocation;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,1,locationListener);
            mMap.setMyLocationEnabled(true);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastKnownLocation == null){
                Log.i("Info:","GPS provider fail");
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(lastKnownLocation == null){
                    Log.i("Info:","Network provider fail");
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    if(lastKnownLocation == null){
                        Log.i("Info:","Passive provider fail, setting default");
                        lastKnownLocation = new Location("DEFAULT");
                        lastKnownLocation.setLatitude(25.033964);
                        lastKnownLocation.setLongitude(121.564468);
                    }
                }
            }
            Log.i("Last Known Location", lastKnownLocation.toString());
            userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
            Log.i("Item clicked",String.valueOf(itemClicked));
            LatLng pos;
            String address;
            String[] latlong;
            if(itemClicked != -1){ //If a list item is clicked
                latlong = MainActivity.myPlaces.get(itemClicked).split(",");
                pos = new LatLng(Double.parseDouble(latlong[0]), Double.parseDouble(latlong[1]));
                address = MainActivity.myAddresses.get(itemClicked);
                Log.i("pos",pos.toString());
                Log.i("address",address);
                mMap.addMarker(new MarkerOptions().position(pos).title("Saved Location").snippet(address));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));

            }
            else{ //If the floating '+' button is clicked
                Toast.makeText(getApplicationContext(), "Long press on map to save location", Toast.LENGTH_LONG).show();
                if(MainActivity.myAddresses.size()>0){
                    for(int i=0;i<MainActivity.myAddresses.size();i++){
                        latlong = MainActivity.myPlaces.get(i).split(",");
                        pos = new LatLng(Double.parseDouble(latlong[0]), Double.parseDouble(latlong[1]));
                        address = MainActivity.myAddresses.get(i);
                        mMap.addMarker(new MarkerOptions().position(pos).title("Saved Location").snippet(address));
                    }
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
            }
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
        }

        //Long click on location to add place
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String address = "";
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    //gets all addresses associated with the location and store them in a list
                    List<Address> listOfAddresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    if(listOfAddresses != null && listOfAddresses.size() > 0){
                        //if at least one address exists, use the first one
                        Log.i("Address",listOfAddresses.get(0).toString());
                        if(listOfAddresses.get(0).getAddressLine(0) != null){
                            address = listOfAddresses.get(0).getAddressLine(0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    address = "Unknown address";
                }
                googleMap.addMarker(new MarkerOptions().position(latLng).title("Saved Location").snippet(address)); //add a marker on map
                MainActivity.myPlaces.add(latLng.latitude + "," +latLng.longitude);//add the location data to the array list
                MainActivity.myAddresses.add(address);//add the address to the array list

                try {
                    //encode all data and save to shared preferences
                    encodedString = ObjectSerializer.serialize(MainActivity.myAddresses);
                    MainActivity.savedData.edit().putString("addresses", encodedString).apply();
                    encodedString = ObjectSerializer.serialize(MainActivity.myPlaces);
                    MainActivity.savedData.edit().putString("places", encodedString).apply();
                }catch(Exception e){
                    e.printStackTrace();
                    Log.i("Warning","Serialization failed!");
                }

                MainActivity.arrayAdapter.notifyDataSetChanged(); //update the address arrayAdapter
                MainActivity.textView.setVisibility(View.GONE); //hides the 'nothing to show' text view
                Toast.makeText(getApplicationContext(), "Location saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 0 ){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,1,locationListener);
                    mMap.setMyLocationEnabled(true);
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(lastKnownLocation == null){
                        Log.i("Info:","GPS provider fail");
                        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(lastKnownLocation == null){
                            Log.i("Info:","Network provider fail");
                            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                            if(lastKnownLocation == null){
                                Log.i("Info:","Passive provider fail, setting default");
                                lastKnownLocation = new Location("DEFAULT");
                                lastKnownLocation.setLatitude(25.033964);
                                lastKnownLocation.setLongitude(121.564468);
                            }
                        }
                    }
                    Log.i("Last Known Location", lastKnownLocation.toString());
                    if(lastKnownLocation != null) {
                        LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
                        Log.i("Last Known Location", userLocation.toString());
                    }
                }
            }
        }
    }
}