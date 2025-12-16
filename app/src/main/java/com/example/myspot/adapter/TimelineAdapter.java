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

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {
    private List<Spot> spots;
    private OnTimelineItemClickListener listener;
    
    public interface OnTimelineItemClickListener {
        void onTimelineItemClick(Spot spot);
    }
    
    public TimelineAdapter(List<Spot> spots, OnTimelineItemClickListener listener) {
        this.spots = spots;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline_event, parent, false);
        return new TimelineViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
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
    
    class TimelineViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateTime;
        private TextView tvEventTitle;
        private TextView tvEventCategory;
        private TextView tvEventDescription;
        private ImageView ivEventImage;
        
        TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTimelineItemClick(spots.get(position));
                }
            });
        }
        
        void bind(Spot spot) {
            tvDateTime.setText(DateUtils.formatDateTime(spot.getCreatedAt()));
            tvEventTitle.setText(spot.getTitle());
            
            if (spot.getCategory() != null && !spot.getCategory().isEmpty()) {
                tvEventCategory.setText(spot.getCategory());
                tvEventCategory.setVisibility(android.view.View.VISIBLE);
            } else {
                tvEventCategory.setVisibility(android.view.View.GONE);
            }
            
            String journal = spot.getJournal();
            if (journal != null && !journal.isEmpty()) {
                tvEventDescription.setText(journal);
            } else {
                tvEventDescription.setText("No journal entry");
            }
            
            // Load and display image if available
            if (spot.getImageUri() != null && !spot.getImageUri().isEmpty()) {
                Bitmap bitmap = ImageUtils.loadBitmapFromPath(spot.getImageUri());
                if (bitmap != null) {
                    ivEventImage.setImageBitmap(bitmap);
                    ivEventImage.setBackground(null);
                } else {
                    // If image failed to load, show placeholder
                    ivEventImage.setImageBitmap(null);
                    ivEventImage.setBackgroundResource(R.color.purple_200);
                }
            } else {
                // No image, show placeholder
                ivEventImage.setImageBitmap(null);
                ivEventImage.setBackgroundResource(R.color.purple_200);
            }
        }
    }
}

