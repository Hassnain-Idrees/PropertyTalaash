package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.HomeProperty;

import java.io.File;
import java.util.List;

public class HomePropertyAdapter extends RecyclerView.Adapter<HomePropertyAdapter.VH> {

    public interface Listener {
        void onPropertyClicked(HomeProperty item);

        void onLikeClicked(HomeProperty item, int position);
    }

    private final List<HomeProperty> items;
    private final Listener listener;

    public HomePropertyAdapter(List<HomeProperty> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_property, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HomeProperty item = items.get(position);

        h.tvType.setText(item.getType());
        h.tvTitle.setText(item.getTitle());
        h.tvLocation.setText(item.getLocation());
        h.tvPrice.setText(item.getPrice());

        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            File imageFile = new File(imageUrl);
            if (imageFile.exists()) {

                Glide.with(h.itemView.getContext()).load(imageFile).centerCrop().into(h.ivPropertyImage);
            } else {

                Glide.with(h.itemView.getContext()).load(imageUrl).centerCrop().into(h.ivPropertyImage);
            }
        } else {
            h.ivPropertyImage.setImageResource(0);
        }

        if (item.isLiked()) {
            h.ivLike.setImageResource(R.drawable.ic_heart_filled_placeholder);
            h.ivLike.setColorFilter(0xFFE53935);
        } else {
            h.ivLike.setImageResource(R.drawable.ic_heart_placeholder);
            h.ivLike.setColorFilter(0xFFB0B6BD);
        }

        h.ivGarage.setVisibility(item.isHasGarage() ? View.VISIBLE : View.GONE);
        h.ivGarden.setVisibility(item.isHasGarden() ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPropertyClicked(item);
        });

        h.likeContainer.setOnClickListener(v -> {
            if (listener != null) listener.onLikeClicked(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvType, tvTitle, tvLocation, tvPrice;
        ImageView ivLike, ivPropertyImage, ivGarage, ivGarden;
        View likeContainer;

        VH(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_type);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvPrice = itemView.findViewById(R.id.tv_price);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivPropertyImage = itemView.findViewById(R.id.iv_property_image);
            likeContainer = itemView.findViewById(R.id.like_bg);
            ivGarage = itemView.findViewById(R.id.iv_garage);
            ivGarden = itemView.findViewById(R.id.iv_garden);
        }
    }
}
