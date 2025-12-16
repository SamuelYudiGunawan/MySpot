package com.example.myspot.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.myspot.database.SpotDao;
import com.example.myspot.database.SpotDatabase;
import com.example.myspot.model.Spot;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpotRepository {
    private SpotDao spotDao;
    private ExecutorService executorService;
    
    public SpotRepository(Application application) {
        SpotDatabase database = SpotDatabase.getDatabase(application);
        spotDao = database.spotDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public void insertSpot(Spot spot, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = spotDao.insertSpot(spot);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void updateSpot(Spot spot, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                spot.setUpdatedAt(new Date());
                spotDao.updateSpot(spot);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void deleteSpot(Spot spot, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                spotDao.deleteSpot(spot);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getAllSpots(RepositoryCallback<List<Spot>> callback) {
        executorService.execute(() -> {
            try {
                List<Spot> spots = spotDao.getAllSpots();
                if (callback != null) {
                    callback.onSuccess(spots);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getSpotsByCategory(String category, RepositoryCallback<List<Spot>> callback) {
        executorService.execute(() -> {
            try {
                List<Spot> spots = spotDao.getSpotsByCategory(category);
                if (callback != null) {
                    callback.onSuccess(spots);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getSpotsNewToOld(RepositoryCallback<List<Spot>> callback) {
        executorService.execute(() -> {
            try {
                List<Spot> spots = spotDao.getSpotsNewToOld();
                if (callback != null) {
                    callback.onSuccess(spots);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getSpotById(long id, RepositoryCallback<Spot> callback) {
        executorService.execute(() -> {
            try {
                Spot spot = spotDao.getSpotById(id);
                if (callback != null) {
                    callback.onSuccess(spot);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}

