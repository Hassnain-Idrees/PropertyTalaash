package com.example.myapplication.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.SearchResultsAdapter;
import com.example.myapplication.model.HomeProperty;
import com.example.myapplication.model.Property;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchResultsFragment extends Fragment {

    private ImageView ivBack;
    private RecyclerView rvResults;
    private SearchResultsAdapter adapter;
    private final List<HomeProperty> allItems = new ArrayList<>();
    private final List<HomeProperty> filteredItems = new ArrayList<>();
    private View btnFilter;
    private TextView tvResultCount;
    private DatabaseReference propertiesRef;
    private String currentSearchQuery = "";

    private String filterType = "All";
    private String filterSort = "";
    private String filterCity = "";
    private int filterBedrooms = 0;
    private int filterBathrooms = 0;
    private float filterPriceMin = 0, filterPriceMax = Float.MAX_VALUE;
    private float filterAreaMin = 0, filterAreaMax = Float.MAX_VALUE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        getParentFragmentManager().setFragmentResultListener("filter_result", this, (requestKey, result) -> {
            filterType = result.getString("type", "All");
            filterSort = result.getString("sort", "");
            filterBedrooms = result.getInt("bedrooms", 0);
            filterBathrooms = result.getInt("bathrooms", 0);
            filterPriceMin = result.getFloat("priceMin", 0) * 100000;
            filterPriceMax = result.getFloat("priceMax", Float.MAX_VALUE) * 100000;
            filterAreaMin = result.getFloat("areaMin", 0);
            filterAreaMax = result.getFloat("areaMax", Float.MAX_VALUE);
            applyFilters(currentSearchQuery);
        });
    }

    private void init(View view) {
        ivBack = view.findViewById(R.id.iv_back);
        rvResults = view.findViewById(R.id.rv_results);
        btnFilter = view.findViewById(R.id.btn_filter);

        View chipPrice = view.findViewById(R.id.chip_price);
        View chipType = view.findViewById(R.id.chip_type);
        View chipRoom = view.findViewById(R.id.chip_room);
        View chipCity = view.findViewById(R.id.chip_city);
        tvResultCount = view.findViewById(R.id.tv_result_count);

        propertiesRef = FirebaseDatabase.getInstance().getReference("Properties");

        rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvResults.setHasFixedSize(true);

        adapter = new SearchResultsAdapter(filteredItems, new SearchResultsAdapter.Listener() {
            @Override
            public void onPropertyClicked(HomeProperty item) {
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
                ft.add(R.id.fragment_container,
                        PropertyPreviewFragment.newInstance(item.getPropertyId()));
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
        rvResults.setAdapter(adapter);

        loadProperties();

        EditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters(currentSearchQuery);
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnFilter.setOnClickListener(v -> {
            FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, new FilterSortFragment());
            ft.addToBackStack("filter_sort");
            ft.commit();
        });

        chipPrice.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 0, 0, "Any Price");
            popup.getMenu().add(0, 1, 1, "Under 50 Lac");
            popup.getMenu().add(0, 2, 2, "50 - 100 Lac");
            popup.getMenu().add(0, 3, 3, "100 - 300 Lac");
            popup.getMenu().add(0, 4, 4, "300+ Lac");
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0: filterPriceMin = 0; filterPriceMax = Float.MAX_VALUE; break;
                    case 1: filterPriceMin = 0; filterPriceMax = 5000000; break;
                    case 2: filterPriceMin = 5000000; filterPriceMax = 10000000; break;
                    case 3: filterPriceMin = 10000000; filterPriceMax = 30000000; break;
                    case 4: filterPriceMin = 30000000; filterPriceMax = Float.MAX_VALUE; break;
                }
                applyFilters(currentSearchQuery);
                return true;
            });
            popup.show();
        });

        chipType.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 0, 0, "All Types");
            popup.getMenu().add(0, 1, 1, "House");
            popup.getMenu().add(0, 2, 2, "Apartment");
            popup.getMenu().add(0, 3, 3, "Villa");
            popup.getMenu().add(0, 4, 4, "Plot");
            popup.getMenu().add(0, 5, 5, "Commercial");
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0: filterType = "All"; break;
                    case 1: filterType = "House"; break;
                    case 2: filterType = "Apartment"; break;
                    case 3: filterType = "Villa"; break;
                    case 4: filterType = "Plot"; break;
                    case 5: filterType = "Commercial"; break;
                }
                applyFilters(currentSearchQuery);
                return true;
            });
            popup.show();
        });

        chipRoom.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 0, 0, "Any Rooms");
            popup.getMenu().add(0, 1, 1, "1+ Bedroom");
            popup.getMenu().add(0, 2, 2, "2+ Bedrooms");
            popup.getMenu().add(0, 3, 3, "3+ Bedrooms");
            popup.getMenu().add(0, 4, 4, "4+ Bedrooms");
            popup.getMenu().add(0, 5, 5, "5+ Bedrooms");
            popup.setOnMenuItemClickListener(item -> {
                filterBedrooms = item.getItemId();
                applyFilters(currentSearchQuery);
                return true;
            });
            popup.show();
        });

        if (chipCity != null) {
            chipCity.setOnClickListener(v -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(requireContext(), v);
                popup.getMenu().add(0, 0, 0, "All Cities");
                popup.getMenu().add(0, 1, 1, "Lahore");
                popup.getMenu().add(0, 2, 2, "Karachi");
                popup.getMenu().add(0, 3, 3, "Islamabad");
                popup.getMenu().add(0, 4, 4, "Rawalpindi");
                popup.getMenu().add(0, 5, 5, "Faisalabad");
                popup.getMenu().add(0, 6, 6, "Multan");
                popup.getMenu().add(0, 7, 7, "Peshawar");
                popup.getMenu().add(0, 8, 8, "Quetta");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0) filterCity = "";
                    else filterCity = item.getTitle().toString();
                    applyFilters(currentSearchQuery);
                    return true;
                });
                popup.show();
            });
        }

        ivBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void loadProperties() {
        SharedPreferences prefs = com.example.myapplication.FavHelper.getPrefs(requireContext());
        Set<String> favIds = prefs.getStringSet("fav_ids", new HashSet<>());

        propertiesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allItems.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Property p = ds.getValue(Property.class);
                    if (p != null) {
                        String priceText = "PKR " + String.format(Locale.getDefault(), "%,.0f", p.getPrice());
                        boolean isFav = favIds.contains(p.getPropertyId());
                        allItems.add(new HomeProperty(p.getPropertyId(), p.getType(), p.getTitle(),
                                p.getCity() + ", " + p.getAddress(), priceText,
                                p.getFirstImage(), p.getOwnerPhone(), isFav,
                                p.getPrice(), p.getArea(), p.isHasGarage(), p.isHasGarden(),
                                p.getBedrooms(), p.getBathrooms(), p.getCity()));
                    }
                }
                applyFilters(currentSearchQuery);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Error loading", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters(String searchQuery) {
        filteredItems.clear();
        for (HomeProperty item : allItems) {

            if (!filterType.equals("All") && !item.getType().equalsIgnoreCase(filterType)) continue;

            if (!filterCity.isEmpty()) {
                if (item.getCity() == null || !item.getCity().equalsIgnoreCase(filterCity)) continue;
            }

            if (!searchQuery.isEmpty()) {
                String q = searchQuery.toLowerCase();
                if (!item.getTitle().toLowerCase().contains(q)
                        && !item.getLocation().toLowerCase().contains(q)) {
                    continue;
                }
            }

            if (item.getRawPrice() < filterPriceMin || item.getRawPrice() > filterPriceMax) continue;

            if (item.getRawArea() < filterAreaMin || item.getRawArea() > filterAreaMax) continue;

            if (filterBedrooms > 0 && item.getBedrooms() < filterBedrooms) continue;

            if (filterBathrooms > 0 && item.getBathrooms() < filterBathrooms) continue;

            filteredItems.add(item);
        }

        if ("highest".equals(filterSort)) {
            filteredItems.sort((a, b) -> Double.compare(b.getRawPrice(), a.getRawPrice()));
        } else if ("lowest".equals(filterSort)) {
            filteredItems.sort((a, b) -> Double.compare(a.getRawPrice(), b.getRawPrice()));
        }

        adapter.notifyDataSetChanged();
        if (tvResultCount != null) {
            tvResultCount.setText(filteredItems.size() + " properties found");
        }
    }
}
