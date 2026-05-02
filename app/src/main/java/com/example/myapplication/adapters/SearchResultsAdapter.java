package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.HomeProperty;

import java.io.File;
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.VH> {

    public interface Listener {
        void onPropertyClicked(HomeProperty item);
        void onLikeClicked(HomeProperty item, int position);
    }

    public interface DeleteListener {
        void onDeleteClicked(HomeProperty item, int position);
    }

    private final List<HomeProperty> items;
    private final Listener listener;
    private DeleteListener deleteListener = null;

    public void setDeleteListener(DeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public SearchResultsAdapter(List<HomeProperty> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HomeProperty item = items.get(position);

        h.tvTitle.setText(item.getTitle());
        h.tvLocation.setText(item.getLocation());
        h.tvPrice.setText(item.getPrice());

        if (item.isLiked()) {
            h.ivLike.setImageResource(R.drawable.ic_heart_filled_placeholder);
            h.ivLike.setColorFilter(0xFFE53935);
        } else {
            h.ivLike.setImageResource(R.drawable.ic_heart_placeholder);
            h.ivLike.setColorFilter(0xFFB0B6BD);
        }

        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            File imageFile = new File(imageUrl);
            if (imageFile.exists()) {
                Glide.with(h.itemView.getContext())
                        .load(imageFile)
                        .centerCrop()
                        .placeholder(R.drawable.default_house)
                        .error(R.drawable.default_house)
                        .into(h.ivPropertyImage);
            } else {
                Glide.with(h.itemView.getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.default_house)
                        .error(R.drawable.default_house)
                        .into(h.ivPropertyImage);
            }
        } else {
            Glide.with(h.itemView.getContext())
                    .load(R.drawable.default_house)
                    .centerCrop()
                    .into(h.ivPropertyImage);
        }

        h.itemView.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;
            if (listener != null) listener.onPropertyClicked(item);
        });
        h.ivLike.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;
            if (listener != null) listener.onLikeClicked(item, pos);
        });

        if (h.btnDelete != null) {
            if (deleteListener != null) {
                h.btnDelete.setVisibility(View.VISIBLE);
                h.btnDelete.setOnClickListener(v -> {
                    int pos = h.getAdapterPosition();
                    if (pos == RecyclerView.NO_ID) return;
                    deleteListener.onDeleteClicked(item, pos);
                });
            } else {
                h.btnDelete.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvPrice;
        ImageView ivLike, ivPropertyImage;
        ImageButton btnDelete;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle        = itemView.findViewById(R.id.tv_title);
            tvLocation     = itemView.findViewById(R.id.tv_location);
            tvPrice        = itemView.findViewById(R.id.tv_price);
            ivLike         = itemView.findViewById(R.id.iv_like);
            ivPropertyImage = itemView.findViewById(R.id.iv_property_image);
            btnDelete      = itemView.findViewById(R.id.btn_delete_listing);
        }
    }
}
