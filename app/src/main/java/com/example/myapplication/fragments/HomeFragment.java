package com.example.myapplication.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.HomePropertyAdapter;
import com.example.myapplication.model.HomeProperty;
import com.example.myapplication.model.Property;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_CODE = 100;

    private RecyclerView rvProperties;
    private BottomNavigationView bottomNav;
    private TextView tvLocationValue;
    private MaterialCardView chipAll, chipVilla, chipApartment, chipHouse;
    private MaterialCardView chipShowAll, chipNearby;
    private HomePropertyAdapter adapter;
    private final List<HomeProperty> allItems = new ArrayList<>();
    private final List<HomeProperty> filteredItems = new ArrayList<>();
    private View ivMenu, searchContainer;
    private ImageView ivAvatar;
    private DatabaseReference propertiesRef;
    private FusedLocationProviderClient fusedLocationClient;

    private ValueEventListener propertiesListener;
    private ValueEventListener profilePicListener;
    private com.google.firebase.database.Query profilePicRef;

    private String currentCity = "";
    private String selectedType = "All";
    private boolean showNearbyOnly = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (propertiesRef != null && propertiesListener != null) {
            propertiesRef.removeEventListener(propertiesListener);
            propertiesListener = null;
        }
        if (profilePicRef != null && profilePicListener != null) {
            profilePicRef.removeEventListener(profilePicListener);
            profilePicListener = null;
        }
    }

    private void init(View view) {
        ivMenu = view.findViewById(R.id.iv_menu);
        searchContainer = view.findViewById(R.id.search_container);
        rvProperties = view.findViewById(R.id.rv_properties);
        bottomNav = view.findViewById(R.id.bottom_nav);
        tvLocationValue = view.findViewById(R.id.tv_location_value);
        ivAvatar = view.findViewById(R.id.iv_avatar);

        chipAll = view.findViewById(R.id.chip_all);
        chipVilla = view.findViewById(R.id.chip_villa);
        chipApartment = view.findViewById(R.id.chip_apartment);
        chipHouse = view.findViewById(R.id.chip_house);

        chipShowAll = view.findViewById(R.id.chip_show_all);
        chipNearby = view.findViewById(R.id.chip_nearby);

        propertiesRef = FirebaseDatabase.getInstance().getReference("Properties");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        rvProperties.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvProperties.setHasFixedSize(true);

        adapter = new HomePropertyAdapter(filteredItems, new HomePropertyAdapter.Listener() {
            @Override
            public void onPropertyClicked(HomeProperty item) {
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, PropertyPreviewFragment.newInstance(item.getPropertyId()));
                ft.addToBackStack("property_preview");
                ft.commit();
            }

            @Override
            public void onLikeClicked(HomeProperty item, int position) {
                item.setLiked(!item.isLiked());
                adapter.notifyItemChanged(position);
                SharedPreferences prefs = com.example.myapplication.FavHelper.getPrefs(requireContext());
                Set<String> favs = new HashSet<>(prefs.getStringSet("fav_ids", new HashSet<>()));
                if (item.isLiked()) favs.add(item.getPropertyId());
                else favs.remove(item.getPropertyId());
                prefs.edit().putStringSet("fav_ids", favs).apply();
            }
        });
        rvProperties.setAdapter(adapter);

        loadPropertiesFromFirebase();
        getCurrentLocation();
        loadProfilePic();

        chipAll.setOnClickListener(v -> { selectedType = "All"; applyFilters(); });
        chipVilla.setOnClickListener(v -> { selectedType = "Villa"; applyFilters(); });
        chipApartment.setOnClickListener(v -> { selectedType = "Apartment"; applyFilters(); });
        chipHouse.setOnClickListener(v -> { selectedType = "House"; applyFilters(); });

        if (chipShowAll != null) chipShowAll.setOnClickListener(v -> {
            showNearbyOnly = false;
            updateNearbyChips();
            applyFilters();
        });
        if (chipNearby != null) chipNearby.setOnClickListener(v -> {
            if (currentCity.isEmpty()) {
                Toast.makeText(getContext(), "Detecting your location...", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
                return;
            }
            showNearbyOnly = true;
            updateNearbyChips();
            applyFilters();
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment target = null;
            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_favorites) target = new FavoritesFragment();
            else if (id == R.id.nav_profile) target = new ProfileFragment();
            if (target != null) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, target).commit();
            }
            return true;
        });

        ivMenu.setOnClickListener(v -> {
            DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
            if (drawerLayout != null) drawerLayout.open();
        });

        searchContainer.setOnClickListener(v -> {
            FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, new SearchResultsFragment());
            ft.addToBackStack("search_results");
            ft.commit();
        });

        view.findViewById(R.id.card_location).setOnClickListener(v -> getCurrentLocation());

        setSelectedChip(0);
        updateNearbyChips();
    }

    private void loadProfilePic() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || getContext() == null) return;

        if (profilePicRef != null && profilePicListener != null) {
            profilePicRef.removeEventListener(profilePicListener);
        }

        profilePicRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid()).child("profilePic");

        profilePicListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null || ivAvatar == null) return;
                if (snapshot.exists()) {
                    String picPath = snapshot.getValue(String.class);
                    if (picPath != null) {
                        File f = new File(picPath);
                        if (f.exists()) {
                            Glide.with(requireContext()).load(f).circleCrop().into(ivAvatar);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        profilePicRef.addValueEventListener(profilePicListener);
    }

    private void loadPropertiesFromFirebase() {
        SharedPreferences prefs = com.example.myapplication.FavHelper.getPrefs(requireContext());
        Set<String> favIds = prefs.getStringSet("fav_ids", new HashSet<>());

        if (propertiesListener != null) {
            propertiesRef.removeEventListener(propertiesListener);
        }

        propertiesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!isAdded() || getContext() == null) return;

                allItems.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Property p = ds.getValue(Property.class);
                    if (p != null) {
                        String priceText = "PKR " + String.format(Locale.getDefault(), "%,.0f", p.getPrice());
                        boolean isFav = favIds.contains(p.getPropertyId());
                        allItems.add(new HomeProperty(
                                p.getPropertyId(), p.getType(), p.getTitle(),
                                p.getCity() + ", " + p.getAddress(), priceText,
                                p.getFirstImage(), p.getOwnerPhone(), isFav,
                                p.getPrice(), p.getArea(), p.isHasGarage(), p.isHasGarden(),
                                p.getBedrooms(), p.getBathrooms(), p.getCity()));
                    }
                }
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Failed to load properties", Toast.LENGTH_SHORT).show();
            }
        };

        propertiesRef.addValueEventListener(propertiesListener);
    }

    private void applyFilters() {
        filteredItems.clear();
        for (HomeProperty item : allItems) {
            if (!selectedType.equals("All") && !item.getType().equalsIgnoreCase(selectedType)) continue;
            if (showNearbyOnly && !currentCity.isEmpty()) {
                if (item.getCity() == null || !item.getCity().equalsIgnoreCase(currentCity)) continue;
            }
            filteredItems.add(item);
        }
        adapter.notifyDataSetChanged();
        setChipStyle(chipAll, selectedType.equals("All"));
        setChipStyle(chipVilla, selectedType.equals("Villa"));
        setChipStyle(chipApartment, selectedType.equals("Apartment"));
        setChipStyle(chipHouse, selectedType.equals("House"));
    }

    private void updateNearbyChips() {
        if (chipShowAll != null) setChipStyle(chipShowAll, !showNearbyOnly);
        if (chipNearby != null) setChipStyle(chipNearby, showNearbyOnly);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            return;
        }
        tvLocationValue.setText("Detecting location...");
        com.google.android.gms.location.CurrentLocationRequest req =
                new com.google.android.gms.location.CurrentLocationRequest.Builder()
                        .setPriority(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY)
                        .setMaxUpdateAgeMillis(10000)
                        .build();
        fusedLocationClient.getCurrentLocation(req, null).addOnSuccessListener(location -> {
            if (location != null && getContext() != null) {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String city = addresses.get(0).getLocality();
                        if (city != null) {
                            currentCity = city;
                            tvLocationValue.setText(city + ", Pakistan");
                            if (showNearbyOnly) applyFilters();
                        } else {
                            tvLocationValue.setText(addresses.get(0).getAddressLine(0));
                        }
                    } else {
                        tvLocationValue.setText("Location found");
                    }
                } catch (IOException e) {
                    tvLocationValue.setText("Location found");
                }
            } else if (getContext() != null) {
                tvLocationValue.setText("Could not detect location");
            }
        }).addOnFailureListener(e -> {
            if (getContext() != null) tvLocationValue.setText("Location error");
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    private void setSelectedChip(int index) {
        setChipStyle(chipAll, index == 0);
        setChipStyle(chipVilla, index == 1);
        setChipStyle(chipApartment, index == 2);
        setChipStyle(chipHouse, index == 3);
    }

    private void setChipStyle(MaterialCardView chip, boolean selected) {
        if (chip == null) return;
        TextView tv = null;
        if (chip.getChildCount() > 0 && chip.getChildAt(0) instanceof TextView) {
            tv = (TextView) chip.getChildAt(0);
        }
        if (selected) {
            chip.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_primary_dark));
            chip.setStrokeWidth(0);
            if (tv != null) {
                tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
                tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
            }
        } else {
            chip.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_surface));
            chip.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.ps_divider));
            chip.setStrokeWidth((int) (getResources().getDisplayMetrics().density));
            if (tv != null) {
                tv.setTextColor(0xFF97A1AA);
                tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.NORMAL);
            }
        }
    }
}
