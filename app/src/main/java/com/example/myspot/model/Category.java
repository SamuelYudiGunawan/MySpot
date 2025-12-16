package com.example.myspot.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private boolean isDefault;
    
    // Default constructor
    public Category() {
        this.isDefault = false;
    }
    
    // Constructor with parameters
    public Category(String name, boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}

