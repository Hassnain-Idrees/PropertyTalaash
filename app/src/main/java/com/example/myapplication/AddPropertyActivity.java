package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.model.Property;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddPropertyActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 200;

    private EditText etTitle, etDescription, etAddress, etPrice, etArea,
            etBedrooms, etBathrooms, etPhone;
    private AutoCompleteTextView spinnerType, spinnerCity, spinnerAreaUnit;
    private Chip cbGarage, cbGarden, cbPool;
    private MaterialButton btnGetLocation, btnSubmit, btnPickMap;
    private TextView tvLocationStatus;
    private LinearLayout imagesContainer;
    private View pickImageOverlay;

    private FirebaseAuth mAuth;
    private DatabaseReference propertiesRef;
    private FusedLocationProviderClient fusedLocationClient;

    private double currentLat = 0, currentLng = 0;
    private final List<Uri> selectedImageUris = new ArrayList<>();
    private String editPropertyId = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUris.add(uri);
                    refreshImagePreviews();
                }
            });

    private final ActivityResultLauncher<Intent> mapPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    currentLat = result.getData().getDoubleExtra("lat", 0);
                    currentLng = result.getData().getDoubleExtra("lng", 0);
                    String addr = result.getData().getStringExtra("address");
                    tvLocationStatus.setText(addr != null ? addr : "Lat: " + currentLat + ", Lng: " + currentLng);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        mAuth = FirebaseAuth.getInstance();
        propertiesRef = FirebaseDatabase.getInstance().getReference("Properties");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        editPropertyId = getIntent().getStringExtra("propertyId");

        init();

        if (editPropertyId != null) {
            btnSubmit.setText("Save Changes");
            loadPropertyForEdit();
        }
    }

    private void init() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        imagesContainer = findViewById(R.id.images_container);
        pickImageOverlay = findViewById(R.id.pick_image_overlay);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etAddress = findViewById(R.id.et_address);
        etPrice = findViewById(R.id.et_price);
        etArea = findViewById(R.id.et_area);
        etBedrooms = findViewById(R.id.et_bedrooms);
        etBathrooms = findViewById(R.id.et_bathrooms);
        etPhone = findViewById(R.id.et_phone);
        
        spinnerType = findViewById(R.id.spinner_type_new);
        spinnerCity = findViewById(R.id.spinner_city_new);
        spinnerAreaUnit = findViewById(R.id.spinner_area_unit_new);
        
        cbGarage = findViewById(R.id.cb_garage);
        cbGarden = findViewById(R.id.cb_garden);
        cbPool = findViewById(R.id.cb_pool);
        
        btnGetLocation = findViewById(R.id.btn_get_location);
        btnPickMap = findViewById(R.id.btn_pick_on_map);
        btnSubmit = findViewById(R.id.btn_submit);
        tvLocationStatus = findViewById(R.id.tv_location_status);

        // Setup Dropdowns
        String[] types = {"House", "Apartment", "Villa", "Plot", "Commercial"};
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, types));

        String[] cities = {"Lahore", "Karachi", "Islamabad", "Rawalpindi", "Faisalabad",
                "Multan", "Peshawar", "Quetta", "Sialkot", "Gujranwala"};
        spinnerCity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cities));

        String[] areaUnits = {"Marla", "Kanal", "Sq Ft", "Sq Yard"};
        spinnerAreaUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, areaUnits));

        pickImageOverlay.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnGetLocation.setOnClickListener(v -> fetchCurrentLocation());
        btnPickMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            mapPickerLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> submitProperty());
    }

    private void refreshImagePreviews() {
        imagesContainer.removeAllViews();
        pickImageOverlay.setVisibility(selectedImageUris.isEmpty() ? View.VISIBLE : View.GONE);

        for (int i = 0; i < selectedImageUris.size(); i++) {
            ImageView iv = new ImageView(this);
            int size = (int) (120 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, (int) (160 * getResources().getDisplayMetrics().density));
            lp.setMargins(0, 0, (int) (12 * getResources().getDisplayMetrics().density), 0);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // In a real app, use Glide here for better performance
            iv.setImageURI(selectedImageUris.get(i));
            
            final int index = i;
            iv.setOnLongClickListener(v -> {
                selectedImageUris.remove(index);
                refreshImagePreviews();
                return true;
            });
            imagesContainer.addView(iv);
        }

        if (!selectedImageUris.isEmpty()) {
            MaterialButton addMore = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            int size = (int) (100 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.gravity = android.view.Gravity.CENTER_VERTICAL;
            addMore.setLayoutParams(lp);
            addMore.setText("+");
            addMore.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
            imagesContainer.addView(addMore);
        }
    }

    private void loadPropertyForEdit() {
        propertiesRef.child(editPropertyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Property p = snapshot.getValue(Property.class);
                if (p == null) return;
                etTitle.setText(p.getTitle());
                etDescription.setText(p.getDescription());
                etAddress.setText(p.getAddress());
                etPrice.setText(String.valueOf((long) p.getPrice()));
                etArea.setText(String.valueOf(p.getArea()));
                etBedrooms.setText(String.valueOf(p.getBedrooms()));
                etBathrooms.setText(String.valueOf(p.getBathrooms()));
                etPhone.setText(p.getOwnerPhone());
                
                cbGarage.setChecked(p.isHasGarage());
                cbGarden.setChecked(p.isHasGarden());
                
                currentLat = p.getLatitude();
                currentLng = p.getLongitude();

                if (currentLat != 0 || currentLng != 0) {
                    Geocoder geocoder = new Geocoder(AddPropertyActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(currentLat, currentLng, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            tvLocationStatus.setText(addresses.get(0).getAddressLine(0));
                        } else {
                            tvLocationStatus.setText("Lat: " + currentLat + ", Lng: " + currentLng);
                        }
                    } catch (IOException e) {
                        tvLocationStatus.setText("Lat: " + currentLat + ", Lng: " + currentLng);
                    }
                }

                spinnerType.setText(p.getType(), false);
                spinnerCity.setText(p.getCity(), false);
                spinnerAreaUnit.setText(p.getAreaUnit(), false);

                if (p.getImagePaths() != null) {
                    for (String path : p.getImagePaths()) {
                        File f = new File(path);
                        if (f.exists()) selectedImageUris.add(Uri.fromFile(f));
                    }
                }
                refreshImagePreviews();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(currentLat, currentLng, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        tvLocationStatus.setText(addresses.get(0).getAddressLine(0));
                    } else {
                        tvLocationStatus.setText("Lat: " + currentLat + ", Lng: " + currentLng);
                    }
                } catch (IOException e) {
                    tvLocationStatus.setText("Lat: " + currentLat + ", Lng: " + currentLng);
                }
            } else {
                Toast.makeText(this, "Could not get location. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> saveAllImagesLocally(String propertyId) {
        List<String> savedPaths = new ArrayList<>();
        File imageDir = new File(getFilesDir(), "property_images");
        if (!imageDir.exists()) imageDir.mkdirs();

        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri uri = selectedImageUris.get(i);
            if ("file".equals(uri.getScheme())) {
                File f = new File(uri.getPath());
                if (f.exists() && f.getAbsolutePath().startsWith(imageDir.getAbsolutePath())) {
                    savedPaths.add(f.getAbsolutePath());
                    continue;
                }
            }
            try {
                File destFile = new File(imageDir, propertyId + "_" + i + ".jpg");
                InputStream in = getContentResolver().openInputStream(uri);
                OutputStream out = new FileOutputStream(destFile);
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                out.close();
                in.close();
                savedPaths.add(destFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return savedPaths;
    }

    private void submitProperty() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String areaStr = etArea.getText().toString().trim();
        String bedroomsStr = etBedrooms.getText().toString().trim();
        String bathroomsStr = etBathrooms.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String type = spinnerType.getText().toString();
        String city = spinnerCity.getText().toString();
        String areaUnit = spinnerAreaUnit.getText().toString();

        if (title.isEmpty()) { etTitle.setError("Required"); return; }
        if (address.isEmpty()) { etAddress.setError("Required"); return; }
        if (priceStr.isEmpty()) { etPrice.setError("Required"); return; }
        if (phone.isEmpty()) { etPhone.setError("Required"); return; }

        double price = Double.parseDouble(priceStr);
        double area = areaStr.isEmpty() ? 0 : Double.parseDouble(areaStr);
        int bedrooms = bedroomsStr.isEmpty() ? 0 : Integer.parseInt(bedroomsStr);
        int bathrooms = bathroomsStr.isEmpty() ? 0 : Integer.parseInt(bathroomsStr);

        String ownerId = mAuth.getCurrentUser().getUid();
        String propertyId = editPropertyId != null ? editPropertyId : propertiesRef.push().getKey();

        List<String> localImagePaths = saveAllImagesLocally(propertyId);
        String firstImage = localImagePaths.isEmpty() ? "" : localImagePaths.get(0);

        FirebaseDatabase.getInstance().getReference("Users").child(ownerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String ownerName = snapshot.child("name").getValue(String.class);
                        if (ownerName == null) ownerName = "Anonymous";

                        Property property = new Property();
                        property.setPropertyId(propertyId);
                        property.setOwnerId(ownerId);
                        property.setOwnerName(ownerName);
                        property.setOwnerPhone(phone);
                        property.setTitle(title);
                        property.setDescription(description);
                        property.setType(type);
                        property.setCity(city);
                        property.setAddress(address);
                        property.setPrice(price);
                        property.setArea(area);
                        property.setAreaUnit(areaUnit);
                        property.setBedrooms(bedrooms);
                        property.setBathrooms(bathrooms);
                        property.setHasGarage(cbGarage.isChecked());
                        property.setHasGarden(cbGarden.isChecked());
                        property.setLatitude(currentLat);
                        property.setLongitude(currentLng);
                        property.setImageUrl(firstImage);
                        property.setImagePaths(localImagePaths);
                        property.setTimestamp(System.currentTimeMillis());

                        propertiesRef.child(propertyId).setValue(property)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(AddPropertyActivity.this, "Property Saved!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(AddPropertyActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        }
    }
}
