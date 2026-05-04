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
import android.widget.LinearLayout;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

    private TextView tvTitle, tvAddress, tvPrice, tvOwnerName, tvOwnerMeta;
    private TextView tvDetailType, tvDetailYear, tvDetailArea;
    private TextView tvFeatureBedrooms, tvFeatureBathrooms, tvFeatureLiving, tvFeatureKitchen;
    private ImageView ivFav, ivOwnerPic, ivCompactImage;
    private ImageView ivFeatGarage, ivFeatGarden;
    private TextView tvFeatGarage, tvFeatGarden;
    private TextView tvCompactTitle, tvCompactAddress;
    private ViewPager2 vpImages;
    private View btnCall, btnChat, btnViewProfile, btnBack, btnFav, btnViewOnMap;
    private View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private LinearLayout dotIndicator;
    private View compactCard;
    private View collapsedHeader;

    private String propertyId;
    private String ownerPhone = "";
    private String ownerId = "";
    private double propertyLat = 0, propertyLng = 0;
    private boolean isLiked = false;
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
        dotIndicator = view.findViewById(R.id.dot_indicator);
        tvTitle = view.findViewById(R.id.tv_title);
        tvAddress = view.findViewById(R.id.tv_address);
        tvPrice = view.findViewById(R.id.tv_price);
        ivCompactImage = view.findViewById(R.id.iv_compact_image);
        tvCompactTitle = view.findViewById(R.id.tv_compact_title);
        tvCompactAddress = view.findViewById(R.id.tv_compact_address);
        tvOwnerName = view.findViewById(R.id.tv_owner_name);
        tvOwnerMeta = view.findViewById(R.id.tv_owner_meta);
        tvDetailType = view.findViewById(R.id.tv_detail_type);
        tvDetailYear = view.findViewById(R.id.tv_detail_year);
        tvDetailArea = view.findViewById(R.id.tv_detail_area);
        tvFeatureBedrooms = view.findViewById(R.id.tv_feature_bedrooms);
        tvFeatureBathrooms = view.findViewById(R.id.tv_feature_bathrooms);
        tvFeatureLiving = view.findViewById(R.id.tv_feature_living);
        tvFeatureKitchen = view.findViewById(R.id.tv_feature_kitchen);
        ivFeatGarage = view.findViewById(R.id.iv_feat_garage);
        tvFeatGarage = view.findViewById(R.id.tv_feat_garage);
        ivFeatGarden = view.findViewById(R.id.iv_feat_garden);
        tvFeatGarden = view.findViewById(R.id.tv_feat_garden);
        btnCall = view.findViewById(R.id.btn_call);
        btnChat = view.findViewById(R.id.btn_chat);
        btnViewProfile = view.findViewById(R.id.btn_view_profile);
        ivOwnerPic = view.findViewById(R.id.iv_owner_pic);
        btnViewOnMap = view.findViewById(R.id.btn_view_on_map);
        bottomSheet = view.findViewById(R.id.bottom_sheet);
        compactCard = view.findViewById(R.id.compact_card);
        collapsedHeader = view.findViewById(R.id.collapsed_header);
        if (bottomSheet != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setFitToContents(true);
            bottomSheetBehavior.setPeekHeight(dpToPx(380));
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            updateSheetHeader(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    updateSheetHeader(newState == BottomSheetBehavior.STATE_EXPANDED);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        }

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

                        String compactAddress = p.getCity() + ", " + p.getAddress();
                        tvTitle.setText(p.getTitle());
                        tvAddress.setText(compactAddress);
                        if (tvCompactTitle != null) tvCompactTitle.setText(p.getTitle());
                        if (tvCompactAddress != null) tvCompactAddress.setText(compactAddress);
                        tvPrice.setText("PKR " + String.format(Locale.getDefault(), "%,.0f", p.getPrice()));
                        tvOwnerName.setText(p.getOwnerName());
                        tvOwnerMeta.setText("Contact: " + (ownerPhone != null ? ownerPhone : "N/A"));

                        if (ownerId != null && !ownerId.isEmpty()) {
                            FirebaseDatabase.getInstance().getReference("Users").child(ownerId)
                                    .child("profilePic").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snap) {
                                            if (getContext() == null) return;
                                            String picPath = snap.getValue(String.class);
                                            if (picPath != null && !picPath.isEmpty()) {
                                                java.io.File pf = new java.io.File(picPath);
                                                if (pf.exists()) {
                                                    Glide.with(requireContext())
                                                            .load(pf)
                                                            .circleCrop()
                                                            .error(R.drawable.default_profile)
                                                            .into(ivOwnerPic);
                                                } else {
                                                    Glide.with(requireContext())
                                                            .load(picPath)
                                                            .circleCrop()
                                                            .error(R.drawable.default_profile)
                                                            .into(ivOwnerPic);
                                                }
                                            } else {
                                                Glide.with(requireContext())
                                                        .load(R.drawable.default_profile)
                                                        .circleCrop()
                                                        .into(ivOwnerPic);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError e) { }
                                    });
                        } else {
                            Glide.with(requireContext())
                                    .load(R.drawable.default_profile)
                                    .circleCrop()
                                    .into(ivOwnerPic);
                        }

                        tvDetailType.setText(p.getType() != null ? p.getType() : "N/A");
                        tvDetailYear.setText(p.getYearBuilt() > 0 ? String.valueOf(p.getYearBuilt()) : "N/A");
                        tvDetailArea.setText(String.format(Locale.getDefault(), "%.0f %s",
                                p.getArea(), p.getAreaUnit() != null ? p.getAreaUnit() : "Marla"));
                        if (tvFeatureBedrooms != null) {
                            tvFeatureBedrooms.setText(p.getBedrooms() + " bedrooms");
                        }
                        if (tvFeatureBathrooms != null) {
                            tvFeatureBathrooms.setText(p.getBathrooms() + " bathrooms");
                        }
                        if (tvFeatureLiving != null) {
                            tvFeatureLiving.setText("N/A living rooms");
                        }
                        if (tvFeatureKitchen != null) {
                            tvFeatureKitchen.setText("Kitchen");
                        }

                        if (p.isHasGarage()) {
                            ivFeatGarage.setVisibility(View.VISIBLE);
                            tvFeatGarage.setVisibility(View.VISIBLE);
                        }
                        if (p.isHasGarden()) {
                            ivFeatGarden.setVisibility(View.VISIBLE);
                            tvFeatGarden.setVisibility(View.VISIBLE);
                        }


                        allImagePaths.clear();
                        if (p.getImagePaths() != null && !p.getImagePaths().isEmpty()) {
                            allImagePaths.addAll(p.getImagePaths());
                        } else if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
                            allImagePaths.add(p.getImageUrl());
                        }

                        setupImagePager();
                        bindCompactImage();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void setupImagePager() {
        if (allImagePaths.isEmpty()) {
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
                    Glide.with(requireContext())
                            .load(R.drawable.default_house)
                            .centerCrop()
                            .into((ImageView) holder.itemView);
                    holder.itemView.setOnClickListener(v -> showZoomDialog(R.drawable.default_house));
                }

                @Override
                public int getItemCount() { return 1; }
            });

            renderDots(1);
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
                if (path == null || path.isEmpty()) {
                    Glide.with(requireContext())
                            .load(R.drawable.default_house)
                            .centerCrop()
                            .into((ImageView) holder.itemView);
                } else {
                    File f = new File(path);
                    if (f.exists()) {
                        Glide.with(requireContext())
                                .load(f)
                                .centerCrop()
                                .error(R.drawable.default_house)
                                .into((ImageView) holder.itemView);
                    } else {
                        Glide.with(requireContext())
                                .load(path)
                                .centerCrop()
                                .error(R.drawable.default_house)
                                .into((ImageView) holder.itemView);
                    }
                }
                holder.itemView.setOnClickListener(v -> showZoomDialog(path));
            }

            @Override
            public int getItemCount() { return allImagePaths.size(); }
        });

        renderDots(allImagePaths.size());

        vpImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setActiveDot(position);
            }
        });
    }

    private void bindCompactImage() {
        if (ivCompactImage == null) return;
        if (allImagePaths.isEmpty()) {
            Glide.with(requireContext())
                    .load(R.drawable.default_house)
                    .centerCrop()
                    .into(ivCompactImage);
            return;
        }
        String path = allImagePaths.get(0);
        if (path == null || path.trim().isEmpty() || "null".equalsIgnoreCase(path.trim())) {
            Glide.with(requireContext())
                    .load(R.drawable.default_house)
                    .centerCrop()
                    .into(ivCompactImage);
            return;
        }
        File f = new File(path);
        if (f.exists()) {
            Glide.with(requireContext())
                    .load(f)
                    .centerCrop()
                    .error(R.drawable.default_house)
                    .into(ivCompactImage);
        } else {
            Glide.with(requireContext())
                    .load(path)
                    .centerCrop()
                    .error(R.drawable.default_house)
                    .into(ivCompactImage);
        }
    }

    static class ImageVH extends RecyclerView.ViewHolder {
        ImageVH(@NonNull View itemView) { super(itemView); }
    }

    private void showZoomDialog(String imagePath) {
        if (getContext() == null) return;
        if (imagePath == null || imagePath.isEmpty()) {
            showZoomDialog(R.drawable.default_house);
            return;
        }
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(requireContext());
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setBackgroundColor(0xFF000000);
        File f = new File(imagePath);
        if (f.exists()) {
            Glide.with(requireContext()).load(f).into(iv);
        } else {
            Glide.with(requireContext()).load(imagePath).error(R.drawable.default_house).into(iv);
        }
        iv.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(iv);
        dialog.show();
    }

    private void showZoomDialog(int resId) {
        if (getContext() == null) return;
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView iv = new ImageView(requireContext());
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setBackgroundColor(0xFF000000);
        Glide.with(requireContext()).load(resId).into(iv);
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

    private void renderDots(int count) {
        if (dotIndicator == null) return;
        dotIndicator.removeAllViews();
        int size = dpToPx(6);
        int margin = dpToPx(6);
        for (int i = 0; i < Math.max(count, 1); i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            if (i > 0) lp.setMarginStart(margin);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(i == 0 ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
            dotIndicator.addView(dot);
        }
    }

    private void setActiveDot(int index) {
        if (dotIndicator == null) return;
        for (int i = 0; i < dotIndicator.getChildCount(); i++) {
            View dot = dotIndicator.getChildAt(i);
            dot.setBackgroundResource(i == index ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void updateSheetHeader(boolean expanded) {
        if (compactCard != null) compactCard.setVisibility(expanded ? View.VISIBLE : View.GONE);
        if (collapsedHeader != null) collapsedHeader.setVisibility(expanded ? View.GONE : View.VISIBLE);
    }
}
