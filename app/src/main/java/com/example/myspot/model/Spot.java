package com.example.myspot.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.myspot.database.Converters;

import java.util.Date;

@Entity(tableName = "spots")
@TypeConverters(Converters.class)
public class Spot {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String title;
    private String category;
    private String journal;
    private double latitude;
    private double longitude;
    private String plusCode;
    private String imageUri; // Store image URI as string
    private Date createdAt;
    private Date updatedAt;
    
    // Default constructor
    public Spot() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // Constructor with parameters
    public Spot(String title, String category, String journal, double latitude, double longitude, String plusCode) {
        this.title = title;
        this.category = category;
        this.journal = journal;
        this.latitude = latitude;
        this.longitude = longitude;
        this.plusCode = plusCode;
        this.imageUri = null;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // Constructor with image
    public Spot(String title, String category, String journal, double latitude, double longitude, String plusCode, String imageUri) {
        this.title = title;
        this.category = category;
        this.journal = journal;
        this.latitude = latitude;
        this.longitude = longitude;
        this.plusCode = plusCode;
        this.imageUri = imageUri;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getJournal() {
        return journal;
    }
    
    public void setJournal(String journal) {
        this.journal = journal;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getPlusCode() {
        return plusCode;
    }
    
    public void setPlusCode(String plusCode) {
        this.plusCode = plusCode;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getImageUri() {
        return imageUri;
    }
    
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}

