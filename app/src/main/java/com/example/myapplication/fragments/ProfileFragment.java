package com.example.myapplication.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.AddPropertyActivity;
import com.example.myapplication.R;
import com.example.myapplication.adapters.SearchResultsAdapter;
import com.example.myapplication.model.HomeProperty;
import com.example.myapplication.model.Property;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvNoListings;
    private ImageView ivProfilePic;
    private RecyclerView rvMyProperties;
    private SearchResultsAdapter adapter;
    private final List<HomeProperty> myItems = new ArrayList<>();

    private String viewUserId = null;
    private boolean isOwnProfile = true;

    private com.google.firebase.database.Query propertiesQuery;
    private ValueEventListener propertiesListener;

    private final ActivityResultLauncher<String> picPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && getContext() != null) {
                    String savedPath = saveProfilePicLocally(uri);
                    if (savedPath != null) {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(viewUserId).child("profilePic").setValue(savedPath);
                        loadProfilePic(savedPath);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey("userId")) {
            viewUserId = args.getString("userId");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (viewUserId == null && currentUser != null) {
            viewUserId = currentUser.getUid();
        }
        isOwnProfile = currentUser != null && viewUserId != null
                && viewUserId.equals(currentUser.getUid());

        tvName        = view.findViewById(R.id.tv_profile_name);
        tvEmail       = view.findViewById(R.id.tv_profile_email);
        tvNoListings  = view.findViewById(R.id.tv_no_listings);
        ivProfilePic  = view.findViewById(R.id.iv_profile_pic);
        rvMyProperties = view.findViewById(R.id.rv_my_properties);

        View avatarContainer = view.findViewById(R.id.avatar_container);
        View badgeCamera     = view.findViewById(R.id.badge_camera);
        if (isOwnProfile) {
            avatarContainer.setOnClickListener(v -> picPickerLauncher.launch("image/*"));
        } else {
            if (badgeCamera != null) badgeCamera.setVisibility(View.GONE);
        }

        rvMyProperties.setLayoutManager(new LinearLayoutManager(requireContext()));

        rvMyProperties.setItemAnimator(null);
        adapter = new SearchResultsAdapter(myItems, new SearchResultsAdapter.Listener() {
            @Override
            public void onPropertyClicked(HomeProperty item) {
                if (isOwnProfile) {
                    Intent intent = new Intent(requireContext(), AddPropertyActivity.class);
                    intent.putExtra("propertyId", item.getPropertyId());
                    startActivity(intent);
                } else {
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,
                                    PropertyPreviewFragment.newInstance(item.getPropertyId()))
                            .addToBackStack("detail").commit();
                }
            }
            @Override
            public void onLikeClicked(HomeProperty item, int position) { }
        });
        rvMyProperties.setAdapter(adapter);

        if (isOwnProfile) {
            adapter.setDeleteListener((item, position) ->
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Listing")
                            .setMessage("Are you sure you want to delete \""
                                    + item.getTitle() + "\"?\nThis cannot be undone.")
                            .setPositiveButton("Delete", (dialog, which) -> deleteProperty(item))
                            .setNegativeButton("Cancel", null)
                            .show()
            );
        }

        loadProfile();
        loadMyProperties();

        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_nav);
        if (isOwnProfile) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
        bottomNav.setOnItemSelectedListener(navItem -> {
            int id = navItem.getItemId();
            Fragment target = null;
            if      (id == R.id.nav_home)      target = new HomeFragment();
            else if (id == R.id.nav_favorites) target = new FavoritesFragment();
            else if (id == R.id.nav_profile) {
                if (isOwnProfile) return true;
                target = new ProfileFragment();
            }
            if (target != null) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, target).commit();
            }
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isOwnProfile) loadProfile();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (propertiesQuery != null && propertiesListener != null) {
            propertiesQuery.removeEventListener(propertiesListener);
            propertiesListener = null;
            propertiesQuery = null;
        }
    }

    private void loadProfile() {
        if (viewUserId == null) return;
        FirebaseDatabase.getInstance().getReference("Users").child(viewUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (getContext() == null) return;
                        String name       = snapshot.child("name").getValue(String.class);
                        String email      = snapshot.child("email").getValue(String.class);
                        String profilePic = snapshot.child("profilePic").getValue(String.class);
                        if (name  != null) tvName.setText(name);
                        if (email != null) tvEmail.setText(email);
                        if (profilePic != null) loadProfilePic(profilePic);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void loadProfilePic(String path) {
        if (path == null || path.isEmpty() || getContext() == null) return;
        File f = new File(path);
        if (f.exists()) Glide.with(requireContext()).load(f).circleCrop().into(ivProfilePic);
    }

    private String saveProfilePicLocally(Uri uri) {
        try {
            File dir = new File(requireContext().getFilesDir(), "profile_pics");
            if (!dir.exists()) dir.mkdirs();
            File destFile = new File(dir, viewUserId + ".jpg");
            InputStream  in  = requireContext().getContentResolver().openInputStream(uri);
            OutputStream out = new FileOutputStream(destFile);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            out.close();
            in.close();
            loadProfilePic(destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    private void loadMyProperties() {
        if (viewUserId == null) return;

        if (propertiesQuery != null && propertiesListener != null) {
            propertiesQuery.removeEventListener(propertiesListener);
            propertiesListener = null;
        }

        propertiesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (getContext() == null || adapter == null) return;
                myItems.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Property p = ds.getValue(Property.class);
                    if (p != null) {
                        String priceText = "PKR " + String.format(Locale.getDefault(), "%,.0f", p.getPrice());
                        myItems.add(new HomeProperty(
                                p.getPropertyId(), p.getType(), p.getTitle(),
                                p.getCity() + ", " + p.getAddress(), priceText,
                                p.getFirstImage(), p.getOwnerPhone(), false));
                    }
                }
                adapter.notifyDataSetChanged();
                if (tvNoListings != null)
                    tvNoListings.setVisibility(myItems.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        propertiesQuery = FirebaseDatabase.getInstance().getReference("Properties")
                .orderByChild("ownerId").equalTo(viewUserId);
        propertiesQuery.addValueEventListener(propertiesListener);
    }

    private void deleteProperty(HomeProperty item) {
        if (item.getPropertyId() == null || item.getPropertyId().isEmpty()) return;
        FirebaseDatabase.getInstance().getReference("Properties")
                .child(item.getPropertyId())
                .removeValue()
                .addOnSuccessListener(unused -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Listing deleted.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Delete failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    public static ProfileFragment newInstance(String userId) {
        ProfileFragment f = new ProfileFragment();
        Bundle b = new Bundle();
        b.putString("userId", userId);
        f.setArguments(b);
        return f;
    }
}
