package com.example.myspot.repository;

import android.app.Application;
import com.example.myspot.database.CategoryDao;
import com.example.myspot.database.SpotDatabase;
import com.example.myspot.model.Category;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {
    private CategoryDao categoryDao;
    private ExecutorService executorService;
    
    public CategoryRepository(Application application) {
        SpotDatabase database = SpotDatabase.getDatabase(application);
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public void insertCategory(Category category, SpotRepository.RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = categoryDao.insertCategory(category);
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
    
    public void updateCategory(Category category, SpotRepository.RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                categoryDao.updateCategory(category);
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
    
    public void deleteCategory(Category category, SpotRepository.RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                categoryDao.deleteCategory(category);
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
    
    public void getAllCategories(SpotRepository.RepositoryCallback<List<Category>> callback) {
        executorService.execute(() -> {
            try {
                List<Category> categories = categoryDao.getAllCategories();
                if (callback != null) {
                    callback.onSuccess(categories);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void getCategoryByName(String name, SpotRepository.RepositoryCallback<Category> callback) {
        executorService.execute(() -> {
            try {
                Category category = categoryDao.getCategoryByName(name);
                if (callback != null) {
                    callback.onSuccess(category);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    public void initializeDefaultCategories(SpotRepository.RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                // Expected default categories
                String[] expectedCategories = {
                    "Food",
                    "Cafe",
                    "Museum",
                    "Park",
                    "Shopping",
                    "Hotel",
                    "Restaurant",
                    "Work",
                    "Nature",
                    "Tourist Attraction",
                    "Other"
                };
                
                // Always ensure all expected categories exist
                for (String catName : expectedCategories) {
                    Category existingCat = categoryDao.getCategoryByName(catName);
                    if (existingCat == null) {
                        // Insert if doesn't exist
                        categoryDao.insertCategory(new Category(catName, true));
                    } else if (!existingCat.isDefault()) {
                        // Update to default if exists but not marked as default
                        existingCat.setDefault(true);
                        categoryDao.updateCategory(existingCat);
                    }
                }
                
                if (callback != null) {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}

