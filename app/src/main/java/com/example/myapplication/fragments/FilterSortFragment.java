package com.example.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.RangeSlider;

import java.util.List;
import java.util.Locale;

public class FilterSortFragment extends Fragment {

    private MaterialCardView chipAll, chipVilla, chipApartment, chipHouse;
    private MaterialCardView chipHighest, chipLowest;
    private MaterialCardView btnReset, btnApply;
    private TextView tvBedCount, tvBathCount, tvPriceRange, tvAreaRange;
    private View btnBedMinus, btnBedPlus, btnBathMinus, btnBathPlus;
    private View vDim, sheet;
    private RangeSlider sliderPrice, sliderArea;

    private int bedCount = 0, bathCount = 0;
    private int selectedCategory = 0;

    private int selectedSort = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_sort, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    private void init(View view) {
        vDim = view.findViewById(R.id.v_dim);
        sheet = view.findViewById(R.id.sheet);

        chipAll = view.findViewById(R.id.chip_all);
        chipVilla = view.findViewById(R.id.chip_villa);
        chipApartment = view.findViewById(R.id.chip_apartment);
        chipHouse = view.findViewById(R.id.chip_house);

        View chipNewest = view.findViewById(R.id.chip_newest);
        View chipRecommended = view.findViewById(R.id.chip_recommended);
        if (chipNewest != null) chipNewest.setVisibility(View.GONE);
        if (chipRecommended != null) chipRecommended.setVisibility(View.GONE);

        chipHighest = view.findViewById(R.id.chip_highest_price);
        chipLowest = view.findViewById(R.id.chip_lowest_price);

        btnReset = view.findViewById(R.id.btn_reset);
        btnApply = view.findViewById(R.id.btn_apply);

        tvBedCount = view.findViewById(R.id.tv_bed_count);
        tvBathCount = view.findViewById(R.id.tv_bath_count);
        tvPriceRange = view.findViewById(R.id.tv_price_range);
        tvAreaRange = view.findViewById(R.id.tv_area_range);

        btnBedMinus = view.findViewById(R.id.btn_bed_minus);
        btnBedPlus = view.findViewById(R.id.btn_bed_plus);
        btnBathMinus = view.findViewById(R.id.btn_bath_minus);
        btnBathPlus = view.findViewById(R.id.btn_bath_plus);

        sliderPrice = view.findViewById(R.id.slider_price);
        sliderArea = view.findViewById(R.id.slider_area);

        if (sheet != null) sheet.setClickable(true);
        if (vDim != null) vDim.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        view.findViewById(R.id.iv_close).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        chipAll.setOnClickListener(v -> setSelectedCategory(0));
        chipVilla.setOnClickListener(v -> setSelectedCategory(1));
        chipApartment.setOnClickListener(v -> setSelectedCategory(2));
        chipHouse.setOnClickListener(v -> setSelectedCategory(3));

        chipHighest.setOnClickListener(v -> setSelectedSort(0));
        chipLowest.setOnClickListener(v -> setSelectedSort(1));

        tvBedCount.setText(String.valueOf(bedCount));
        tvBathCount.setText(String.valueOf(bathCount));
        btnBedMinus.setOnClickListener(v -> { if (bedCount > 0) bedCount--; tvBedCount.setText(String.valueOf(bedCount)); });
        btnBedPlus.setOnClickListener(v -> { bedCount++; tvBedCount.setText(String.valueOf(bedCount)); });
        btnBathMinus.setOnClickListener(v -> { if (bathCount > 0) bathCount--; tvBathCount.setText(String.valueOf(bathCount)); });
        btnBathPlus.setOnClickListener(v -> { bathCount++; tvBathCount.setText(String.valueOf(bathCount)); });

        if (sliderPrice != null) {
            sliderPrice.setValues(50f, 350f);
            sliderPrice.addOnChangeListener((slider, value, fromUser) -> updatePriceLabel());
            updatePriceLabel();
        }

        if (sliderArea != null) {
            sliderArea.setValues(3f, 50f);
            sliderArea.addOnChangeListener((slider, value, fromUser) -> updateAreaLabel());
            updateAreaLabel();
        }

        btnReset.setOnClickListener(v -> {
            bedCount = 0; bathCount = 0;
            tvBedCount.setText("0"); tvBathCount.setText("0");
            setSelectedCategory(0);
            selectedSort = -1;
            setChipStyle(chipHighest, false);
            setChipStyle(chipLowest, false);
            if (sliderPrice != null) sliderPrice.setValues(50f, 350f);
            if (sliderArea != null) sliderArea.setValues(3f, 50f);
            updatePriceLabel(); updateAreaLabel();
        });

        btnApply.setOnClickListener(v -> {
            String[] types = {"All", "Villa", "Apartment", "House"};
            Bundle result = new Bundle();
            result.putString("type", types[selectedCategory]);

            if (selectedSort == 0) result.putString("sort", "highest");
            else if (selectedSort == 1) result.putString("sort", "lowest");
            else result.putString("sort", "");
            result.putInt("bedrooms", bedCount);
            result.putInt("bathrooms", bathCount);
            if (sliderPrice != null) {
                List<Float> pv = sliderPrice.getValues();
                result.putFloat("priceMin", pv.get(0));
                result.putFloat("priceMax", pv.get(1));
            }
            if (sliderArea != null) {
                List<Float> av = sliderArea.getValues();
                result.putFloat("areaMin", av.get(0));
                result.putFloat("areaMax", av.get(1));
            }
            getParentFragmentManager().setFragmentResult("filter_result", result);
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        setSelectedCategory(0);
    }

    private void updatePriceLabel() {
        if (sliderPrice == null || tvPriceRange == null) return;
        List<Float> v = sliderPrice.getValues();
        tvPriceRange.setText(String.format(Locale.getDefault(), "PKR %.0f Lac - PKR %.0f Lac", v.get(0), v.get(1)));
    }

    private void updateAreaLabel() {
        if (sliderArea == null || tvAreaRange == null) return;
        List<Float> v = sliderArea.getValues();
        tvAreaRange.setText(String.format(Locale.getDefault(), "%.0f - %.0f Marla", v.get(0), v.get(1)));
    }

    private void setSelectedCategory(int index) {
        selectedCategory = index;
        setChipStyle(chipAll, index == 0);
        setChipStyle(chipVilla, index == 1);
        setChipStyle(chipApartment, index == 2);
        setChipStyle(chipHouse, index == 3);
    }

    private void setSelectedSort(int index) {
        selectedSort = index;
        setChipStyle(chipHighest, index == 0);
        setChipStyle(chipLowest, index == 1);
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
