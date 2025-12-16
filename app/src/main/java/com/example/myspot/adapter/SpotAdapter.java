package com.example.myspot.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myspot.R;
import com.example.myspot.model.Spot;
import com.example.myspot.util.DateUtils;
import com.example.myspot.util.ImageUtils;

import java.util.List;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.SpotViewHolder> {
    private List<Spot> spots;
    private OnSpotClickListener listener;
    
    public interface OnSpotClickListener {
        void onSpotClick(Spot spot);
    }
    
    public SpotAdapter(List<Spot> spots, OnSpotClickListener listener) {
        this.spots = spots;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spot_card, parent, false);
        return new SpotViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        Spot spot = spots.get(position);
        holder.bind(spot);
    }
    
    @Override
    public int getItemCount() {
        return spots != null ? spots.size() : 0;
    }
    
    public void updateSpots(List<Spot> newSpots) {
        this.spots = newSpots;
        notifyDataSetChanged();
    }
    
    class SpotViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvCategory;
        private TextView tvUpdated;
        private ImageView ivPlaceholder;
        
        SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvUpdated = itemView.findViewById(R.id.tvUpdated);
            ivPlaceholder = itemView.findViewById(R.id.ivPlaceholder);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSpotClick(spots.get(position));
                }
            });
        }
        
        void bind(Spot spot) {
            tvTitle.setText(spot.getTitle());
            if (spot.getCategory() != null && !spot.getCategory().isEmpty()) {
                tvCategory.setText(spot.getCategory());
                tvCategory.setVisibility(android.view.View.VISIBLE);
            } else {
                tvCategory.setVisibility(android.view.View.GONE);
            }
            tvUpdated.setText(DateUtils.getRelativeTimeString(itemView.getContext(), spot.getUpdatedAt()));
            
            // Load and display image if available
            if (spot.getImageUri() != null && !spot.getImageUri().isEmpty()) {
                Bitmap bitmap = ImageUtils.loadBitmapFromPath(spot.getImageUri());
                if (bitmap != null) {
                    ivPlaceholder.setImageBitmap(bitmap);
                    ivPlaceholder.setBackground(null);
                } else {
                    // If image failed to load, show placeholder
                    ivPlaceholder.setImageBitmap(null);
                    ivPlaceholder.setBackgroundResource(R.color.purple_200);
                }
            } else {
                // No image, show placeholder
                ivPlaceholder.setImageBitmap(null);
                ivPlaceholder.setBackgroundResource(R.color.purple_200);
            }
        }
    }
}

