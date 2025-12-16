package com.example.myspot.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.myspot.model.Spot;
import com.example.myspot.model.Category;

@Database(entities = {Spot.class, Category.class}, version = 4, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class SpotDatabase extends RoomDatabase {
    public abstract SpotDao spotDao();
    public abstract CategoryDao categoryDao();
    
    private static volatile SpotDatabase INSTANCE;
    
    public static SpotDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SpotDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SpotDatabase.class, "spot_database")
                            .fallbackToDestructiveMigration() // Rebuild database on version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

