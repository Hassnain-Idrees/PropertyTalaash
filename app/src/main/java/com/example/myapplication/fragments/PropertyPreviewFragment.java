package com.example.myapplication.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.Property;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PropertyPreviewFragment extends Fragment {

    private TextView tvTitle, tvAddress, tvPrice, tvOwnerName, tvOwnerMeta, tvDescription, tvReadMore;
    private TextView tvDetailType, tvDetailYear, tvDetailArea, tvDetailBeds, tvDetailBaths;
    private TextView tvPageIndicator;
    private ImageView ivFav, ivOwnerPic;
    private ImageView ivFeatGarage, ivFeatGarden;
    private TextView tvFeatGarage, tvFeatGarden;
    private ViewPager2 vpImages;
    private View btnCall, btnChat, btnViewProfile, btnBack, btnFav, btnViewOnMap;

    private String propertyId;
    private String ownerPhone = "";
    private String ownerId = "";
    private double propertyLat = 0, propertyLng = 0;
    private boolean isLiked = false;
    private boolean descExpanded = false;
    private final List<String> allImagePaths = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_property_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        btnFav = view.findViewById(R.id.btn_fav);
        ivFav = view.findViewById(R.id.iv_fav);
        vpImages = view.findViewById(R.id.vp_images);
        tvPageIndicator = view.findViewById(R.id.tv_page_indicator);
        tvTitle = view.findViewById(R.id.tv_title);
        tvAddress = view.findViewById(R.id.tv_address);
        tvPrice = view.findViewById(R.id.tv_price);
        tvOwnerName = view.findViewById(R.id.tv_owner_name);
        tvOwnerMeta = view.findViewById(R.id.tv_owner_meta);
        tvDescription = view.findViewById(R.id.tv_description);
        tvReadMore = view.findViewById(R.id.tv_read_more);
        tvDetailType = view.findViewById(R.id.tv_detail_type);
        tvDetailYear = view.findViewById(R.id.tv_detail_year);
        tvDetailArea = view.findViewById(R.id.tv_detail_area);
        tvDetailBeds = view.findViewById(R.id.tv_detail_beds);
        tvDetailBaths = view.findViewById(R.id.tv_detail_baths);
        ivFeatGarage = view.findViewById(R.id.iv_feat_garage);
        tvFeatGarage = view.findViewById(R.id.tv_feat_garage);
        ivFeatGarden = view.findViewById(R.id.iv_feat_garden);
        tvFeatGarden = view.findViewById(R.id.tv_feat_garden);
        btnCall = view.findViewById(R.id.btn_call);
        btnChat = view.findViewById(R.id.btn_chat);
        btnViewProfile = view.findViewById(R.id.btn_view_profile);
        ivOwnerPic = view.findViewById(R.id.iv_owner_pic);
        btnViewOnMap = view.findViewById(R.id.btn_view_on_map);

        Bundle args = getArguments();
        if (args != null) propertyId = args.getString("propertyId");

        SharedPreferences prefs = com.example.myapplication.FavHelper.getPrefs(requireContext());
        Set<String> favs = prefs.getStringSet("fav_ids", new HashSet<>());
        isLiked = favs.contains(propertyId);
        updateHeartIcon();

        if (propertyId != null) loadPropertyFromFirebase();

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnFav.setOnClickListener(v -> {
            isLiked = !isLiked;
            updateHeartIcon();
            SharedPreferences p = com.example.myapplication.FavHelper.getPrefs(requireContext());
            Set<String> f = new HashSet<>(p.getStringSet("fav_ids", new HashSet<>()));
            if (isLiked) f.add(propertyId); else f.remove(propertyId);
            p.edit().putStringSet("fav_ids", f).apply();
        });

        btnCall.setOnClickListener(v -> {
            if (ownerPhone != null && !ownerPhone.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ownerPhone)));
            } else {
                Toast.makeText(getContext(), "No phone number available", Toast.LENGTH_SHORT).show();
            }
        });

        btnChat.setOnClickListener(v -> {
            if (ownerPhone != null && !ownerPhone.isEmpty()) {
                Intent sms = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + ownerPhone));
                sms.putExtra("sms_body", "Hi, I'm interested in your property on PropertyTalaash.");
                startActivity(sms);
            } else {
                Toast.makeText(getContext(), "No phone number available", Toast.LENGTH_SHORT).show();
            }
        });

        btnViewProfile.setOnClickListener(v -> {
            if (ownerId != null && !ownerId.isEmpty()) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, ProfileFragment.newInstance(ownerId))
                        .addToBackStack("owner_profile").commit();
            }
        });

        tvReadMore.setOnClickListener(v -> {
            descExpanded = !descExpanded;
            tvDescription.setMaxLines(descExpanded ? Integer.MAX_VALUE : 3);
            tvReadMore.setText(descExpanded ? "Show less" : "Read more");
        });

        if (btnViewOnMap != null) {
            btnViewOnMap.setOnClickListener(v -> {
                if (propertyLat != 0 || propertyLng != 0) {
                    Uri gmmUri = Uri.parse("geo:" + propertyLat + "," + propertyLng + "?q=" + propertyLat + "," + propertyLng + "(Property)");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {

                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://maps.google.com/?q=" + propertyLat + "," + propertyLng)));
                    }
                } else {
                    Toast.makeText(getContext(), "No location saved for this property", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateHeartIcon() {
        if (isLiked) {
            ivFav.setImageResource(R.drawable.ic_heart_filled_placeholder);
            ivFav.setColorFilter(0xFFE53935);
        } else {
            ivFav.setImageResource(R.drawable.ic_heart_placeholder);
            ivFav.setColorFilter(0xFFB0B6BD);
        }
    }

    private void loadPropertyFromFirebase() {
        FirebaseDatabase.getInstance().getReference("Properties").child(propertyId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Property p = snapshot.getValue(Property.class);
                        if (p == null || getContext() == null) return;

                        ownerPhone = p.getOwnerPhone();
                        ownerId = p.getOwnerId();
                        propertyLat = p.getLatitude();
                        propertyLng = p.getLongitude();

                        tvTitle.setText(p.getTitle());
                        tvAddress.setText(p.getCity() + ", " + p.getAddress());
                        tvPrice.setText("PKR " + String.format(Locale.getDefault(), "%,.0f", p.getPrice()));
                        tvOwnerName.setText(p.getOwnerName());
                        tvOwnerMeta.setText("Contact: " + (ownerPhone != null ? ownerPhone : "N/A"));

                        if (ownerId != null && !ownerId.isEmpty()) {
                            FirebaseDatabase.getInstance().getReference("Users").child(ownerId)
                                    .child("profilePic").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snap) {
                                            if (snap.exists() && getContext() != null) {
                                                String picPath = snap.getValue(String.class);
                                                if (picPath != null) {
                                                    java.io.File pf = new java.io.File(picPath);
                                                    if (pf.exists()) {
                                                        Glide.with(requireContext()).load(pf).circleCrop().into(ivOwnerPic);
                                                    }
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError e) { }
                                    });
                        }

                        tvDetailType.setText(p.getType() != null ? p.getType() : "N/A");
                        tvDetailYear.setText(p.getYearBuilt() > 0 ? String.valueOf(p.getYearBuilt()) : "N/A");
                        tvDetailArea.setText(String.format(Locale.getDefault(), "%.0f %s",
                                p.getArea(), p.getAreaUnit() != null ? p.getAreaUnit() : "Marla"));
                        tvDetailBeds.setText(String.valueOf(p.getBedrooms()));
                        tvDetailBaths.setText(String.valueOf(p.getBathrooms()));

                        if (p.isHasGarage()) {
                            ivFeatGarage.setVisibility(View.VISIBLE);
                            tvFeatGarage.setVisibility(View.VISIBLE);
                        }
                        if (p.isHasGarden()) {
                            ivFeatGarden.setVisibility(View.VISIBLE);
                            tvFeatGarden.setVisibility(View.VISIBLE);
                        }

                        String desc = p.getDescription();
                        if (desc != null && !desc.isEmpty()) {
                            tvDescription.setText(desc);

                            tvDescription.post(() -> {
                                if (tvDescription.getLineCount() > 3) {
                                    tvReadMore.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            tvDescription.setText("No description provided.");
                        }

                        allImagePaths.clear();
                        if (p.getImagePaths() != null && !p.getImagePaths().isEmpty()) {
                            allImagePaths.addAll(p.getImagePaths());
                        } else if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
                            allImagePaths.add(p.getImageUrl());
                        }

                        setupImagePager();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void setupImagePager() {
        if (allImagePaths.isEmpty()) {
            tvPageIndicator.setText("No images");
            return;
        }

        vpImages.setAdapter(new RecyclerView.Adapter<ImageVH>() {
            @NonNull
            @Override
            public ImageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ImageView iv = new ImageView(requireContext());
                iv.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return new ImageVH(iv);
            }

            @Override
            public void onBindViewHolder(@NonNull ImageVH holder, int position) {
                String path = allImagePaths.get(position);
                File f = new File(path);
                if (f.exists()) {
                    Glide.with(requireContext()).load(f).centerCrop().into((ImageView) holder.itemView);
                }
                holder.itemView.setOnClickListener(v -> showZoomDialog(path));
            }

            @Override
            public int getItemCount() { return allImagePaths.size(); }
        });

        tvPageIndicator.setText("1 / " + allImagePaths.size());

        vpImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tvPageIndicator.setText((position + 1) + " / " + allImagePaths.size());
            }
        });
    }

    static class ImageVH extends RecyclerView.ViewHolder {
        ImageVH(@NonNull View itemView) { super(itemView); }
    }

    private void showZoomDialog(String imagePath) {
        if (getContext() == null) return;
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(requireContext());
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setBackgroundColor(0xFF000000);
        File f = new File(imagePath);
        if (f.exists()) Glide.with(requireContext()).load(f).into(iv);
        iv.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(iv);
        dialog.show();
    }

    public static PropertyPreviewFragment newInstance(String propertyId) {
        PropertyPreviewFragment f = new PropertyPreviewFragment();
        Bundle b = new Bundle();
        b.putString("propertyId", propertyId);
        f.setArguments(b);
        return f;
    }
}
