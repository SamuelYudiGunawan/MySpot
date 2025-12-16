package com.example.myspot.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.myspot.model.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllCategories();
    
    @Query("SELECT * FROM categories WHERE isDefault = 1")
    List<Category> getDefaultCategories();
    
    @Query("SELECT * FROM categories WHERE name = :name")
    Category getCategoryByName(String name);
    
    @Query("SELECT * FROM categories WHERE id = :id")
    Category getCategoryById(long id);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(Category category);
    
    @Update
    void updateCategory(Category category);
    
    @Delete
    void deleteCategory(Category category);
    
    @Query("DELETE FROM categories WHERE id = :id")
    void deleteCategoryById(long id);
}

