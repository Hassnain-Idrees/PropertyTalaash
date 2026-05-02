package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOC_PERM = 201;
    private GoogleMap mMap;
    private double selectedLat = 33.6844;
    private double selectedLng = 73.0479;
    private String selectedAddress = "";
    private TextView tvSelectedAddress;
    private FusedLocationProviderClient fusedClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        tvSelectedAddress = findViewById(R.id.tv_selected_address);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        selectedLat = getIntent().getDoubleExtra("lat", 33.6844);
        selectedLng = getIntent().getDoubleExtra("lng", 73.0479);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("lat", selectedLat);
            result.putExtra("lng", selectedLng);
            result.putExtra("address", selectedAddress);
            setResult(RESULT_OK, result);
            finish();
        });

        findViewById(R.id.fab_my_location).setOnClickListener(v -> goToMyLocation());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        LatLng initial = new LatLng(selectedLat, selectedLng);

        if (selectedLat == 33.6844 && selectedLng == 73.0479) {
            goToMyLocation();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initial, 14f));
        mMap.addMarker(new MarkerOptions().position(initial).title("Selected"));

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;
            reverseGeocode(selectedLat, selectedLng);
        });
    }

    private void goToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERM);
            return;
        }
        tvSelectedAddress.setText("Getting your location...");
        fusedClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
        ).addOnSuccessListener(location -> {
            if (location != null && mMap != null) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(ll).title("My Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 16f));
                selectedLat = ll.latitude;
                selectedLng = ll.longitude;
                reverseGeocode(selectedLat, selectedLng);
            } else {
                tvSelectedAddress.setText("Could not get location");
            }
        });
    }

    private void reverseGeocode(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                selectedAddress = addresses.get(0).getAddressLine(0);
                tvSelectedAddress.setText(selectedAddress);
            } else {
                selectedAddress = "Lat: " + lat + ", Lng: " + lng;
                tvSelectedAddress.setText(selectedAddress);
            }
        } catch (IOException e) {
            selectedAddress = "Lat: " + lat + ", Lng: " + lng;
            tvSelectedAddress.setText(selectedAddress);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOC_PERM && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                try { mMap.setMyLocationEnabled(true); } catch (SecurityException ignored) {}
            }
            goToMyLocation();
        }
    }
}
