package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
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
        
        // Use full screen / transparent status bar for modern look
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        
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

        // Clean up UI for a more modern experience
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        LatLng initial = new LatLng(selectedLat, selectedLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initial, 16f));

        // Modern UX: Address updates as the user drags the map
        mMap.setOnCameraIdleListener(() -> {
            LatLng center = mMap.getCameraPosition().target;
            selectedLat = center.latitude;
            selectedLng = center.longitude;
            reverseGeocode(selectedLat, selectedLng);
        });
        
        // Move to current location if it's the default
        if (selectedLat == 33.6844 && selectedLng == 73.0479) {
            goToMyLocation();
        }
    }

    private void goToMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERM);
            return;
        }
        
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && mMap != null) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ll, 16f));
            }
        });
    }

    private void reverseGeocode(double lat, double lng) {
        tvSelectedAddress.setText("Locating...");
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        selectedAddress = addresses.get(0).getAddressLine(0);
                        // Clean up address (remove country/code for brevity if needed)
                        tvSelectedAddress.setText(selectedAddress);
                    } else {
                        selectedAddress = String.format(Locale.US, "%.5f, %.5f", lat, lng);
                        tvSelectedAddress.setText(selectedAddress);
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    selectedAddress = String.format(Locale.US, "%.5f, %.5f", lat, lng);
                    tvSelectedAddress.setText(selectedAddress);
                });
            }
        }).start();
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
