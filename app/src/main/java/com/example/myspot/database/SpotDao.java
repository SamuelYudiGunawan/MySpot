package com.example.myspot.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.myspot.model.Spot;

import java.util.Date;
import java.util.List;

@Dao
public interface SpotDao {
    @Query("SELECT * FROM spots ORDER BY updatedAt DESC")
    List<Spot> getAllSpots();
    
    @Query("SELECT * FROM spots WHERE category = :category ORDER BY updatedAt DESC")
    List<Spot> getSpotsByCategory(String category);
    
    @Query("SELECT * FROM spots ORDER BY createdAt DESC")
    List<Spot> getSpotsNewToOld();
    
    @Query("SELECT * FROM spots ORDER BY createdAt ASC")
    List<Spot> getSpotsOldToNew();
    
    @Query("SELECT * FROM spots WHERE id = :id")
    Spot getSpotById(long id);
    
    @Query("SELECT * FROM spots WHERE createdAt >= :date ORDER BY createdAt DESC")
    List<Spot> getSpotsByDate(Date date);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSpot(Spot spot);
    
    @Update
    void updateSpot(Spot spot);
    
    @Delete
    void deleteSpot(Spot spot);
    
    @Query("DELETE FROM spots WHERE id = :id")
    void deleteSpotById(long id);
}

