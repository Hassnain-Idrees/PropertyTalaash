package com.example.myapplication.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private SearchResultsAdapter adapter;
    private final List<HomeProperty> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorites = view.findViewById(R.id.rv_favorites);
        tvEmpty = view.findViewById(R.id.tv_empty);

        rvFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SearchResultsAdapter(items, new SearchResultsAdapter.Listener() {
            @Override
            public void onPropertyClicked(HomeProperty item) {
                FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, PropertyPreviewFragment.newInstance(item.getPropertyId()));
                ft.addToBackStack("preview");
                ft.commit();
            }

            @Override
            public void onLikeClicked(HomeProperty item, int position) {

                SharedPreferences prefs = com.example.myapplication.FavHelper.getPrefs(requireContext());
                Set<String> favs = new HashSet<>(prefs.getStringSet("fav_ids", new HashSet<>()));
                favs.remove(item.getPropertyId());
                prefs.edit().putStringSet("fav_ids", favs).apply();
                items.remove(position);
                adapter.notifyItemRemoved(position);
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                rvFavorites.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
        rvFavorites.setAdapter(adapter);

        loadFavorites();

        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_favorites);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment target = null;
            if (id == R.id.nav_home) target = new HomeFragment();
            else if (id == R.id.nav_favorites) return true;
            else if (id == R.id.nav_profile) target = new ProfileFragment();
            if (target != null) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, target).commit();
            }
            return true;
        });
    }

    private void loadFavorites() {
        SharedPreferences prefs = com.example.myapplication.FavHelper.getPrefs(requireContext());
        Set<String> favIds = prefs.getStringSet("fav_ids", new HashSet<>());

        if (favIds.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
            return;
        }

        FirebaseDatabase.getInstance().getReference("Properties")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        items.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Property p = ds.getValue(Property.class);
                            if (p != null && favIds.contains(p.getPropertyId())) {
                                String priceText = "PKR " + String.format(Locale.getDefault(), "%,.0f", p.getPrice());
                                items.add(new HomeProperty(p.getPropertyId(), p.getType(), p.getTitle(),
                                        p.getCity() + ", " + p.getAddress(), priceText,
                                        p.getFirstImage(), p.getOwnerPhone(), true));
                            }
                        }
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                        rvFavorites.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
}

