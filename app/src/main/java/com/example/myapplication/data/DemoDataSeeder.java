package com.example.myapplication.data;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.model.Property;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public final class DemoDataSeeder {

    private static final String OWNER_ID = "demo";
    private static final String OWNER_NAME = "Property Talash";
    private static final String OWNER_PHONE = "03001234567";
    private static final int MIN_PER_TYPE = 2;

    private DemoDataSeeder() {
    }

    public static void seedIfNeeded(@NonNull Context context) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int houseCount = 0;
                int apartmentCount = 0;
                int villaCount = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String ownerId = child.child("ownerId").getValue(String.class);
                    String type = child.child("type").getValue(String.class);
                    if (!OWNER_ID.equals(ownerId) || type == null) continue;
                    if ("House".equalsIgnoreCase(type)) houseCount++;
                    if ("Apartment".equalsIgnoreCase(type)) apartmentCount++;
                    if ("Villa".equalsIgnoreCase(type)) villaCount++;
                }

                if (houseCount >= MIN_PER_TYPE
                        && apartmentCount >= MIN_PER_TYPE
                        && villaCount >= MIN_PER_TYPE) {
                    return;
                }

                List<Property> demo = buildDemoProperties(context);
                for (Property p : demo) {
                    String type = p.getType();
                    if ("House".equalsIgnoreCase(type) && houseCount >= MIN_PER_TYPE) continue;
                    if ("Apartment".equalsIgnoreCase(type) && apartmentCount >= MIN_PER_TYPE) continue;
                    if ("Villa".equalsIgnoreCase(type) && villaCount >= MIN_PER_TYPE) continue;

                    String id = ref.push().getKey();
                    if (id == null) continue;
                    p.setPropertyId(id);
                    ref.child(id).setValue(p);

                    if ("House".equalsIgnoreCase(type)) houseCount++;
                    if ("Apartment".equalsIgnoreCase(type)) apartmentCount++;
                    if ("Villa".equalsIgnoreCase(type)) villaCount++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private static List<Property> buildDemoProperties(Context context) {
        List<Property> items = new ArrayList<>();
        long now = System.currentTimeMillis();
        int i = 0;

        i = addProperty(items, context, i, now, "House", "Modern House in DHA",
                "DHA Phase 6, Lahore", 32500000, 10, "Marla", 4, 4, true, true, 2021);
        i = addProperty(items, context, i, now, "House", "Corner House in Model Town",
                "Model Town, Lahore", 28000000, 10, "Marla", 5, 4, true, true, 2019);

        i = addProperty(items, context, i, now, "Apartment", "Luxury Apartment in Gulberg",
                "Gulberg III, Lahore", 18000000, 8, "Marla", 3, 3, false, false, 2022);
        i = addProperty(items, context, i, now, "Apartment", "City View Apartment",
                "Askari 10, Lahore", 14500000, 6, "Marla", 2, 2, false, false, 2020);

        i = addProperty(items, context, i, now, "Villa", "Executive Villa in Bahria Town",
                "Bahria Town, Lahore", 55000000, 1, "Kanal", 6, 6, true, true, 2018);
        addProperty(items, context, i, now, "Villa", "Elegant Villa in Cantonment",
                "Cantt, Lahore", 62000000, 1, "Kanal", 7, 7, true, true, 2017);

        return items;
    }

    private static int addProperty(List<Property> items, Context context, int index, long baseTime, String type,
                                   String title, String address, double price, double area, String areaUnit,
                                   int bedrooms, int bathrooms, boolean hasGarage, boolean hasGarden, int yearBuilt) {
        Property p = new Property();
        p.setOwnerId(OWNER_ID);
        p.setOwnerName(OWNER_NAME);
        p.setOwnerPhone(OWNER_PHONE);
        p.setTitle(title);
        p.setDescription("Demo listing for " + type + " in Lahore.");
        p.setType(type);
        p.setCity("Lahore");
        p.setAddress(address);
        p.setPrice(price);
        p.setArea(area);
        p.setAreaUnit(areaUnit);
        p.setBedrooms(bedrooms);
        p.setBathrooms(bathrooms);
        p.setHasGarage(hasGarage);
        p.setHasGarden(hasGarden);
        p.setLatitude(0);
        p.setLongitude(0);

        String imageUri = getResourceUri(context, R.drawable.default_house);
        List<String> imagePaths = new ArrayList<>();
        imagePaths.add(imageUri);
        imagePaths.add(imageUri);
        p.setImagePaths(imagePaths);
        p.setImageUrl(imageUri);

        p.setYearBuilt(yearBuilt);
        p.setTimestamp(baseTime + (index * 60000L));
        items.add(p);
        return index + 1;
    }

    private static String getResourceUri(Context context, int resId) {
        return "android.resource://" + context.getPackageName() + "/" + resId;
    }
}
