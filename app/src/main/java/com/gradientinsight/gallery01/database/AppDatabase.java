package com.gradientinsight.gallery01.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.gradientinsight.gallery01.dao.PhotoDao;
import com.gradientinsight.gallery01.entities.Photo;

@Database(entities = {Photo.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String databaseName = "photo_gallery_db";
    private static volatile AppDatabase INSTANCE = null;

    public abstract PhotoDao photoDao();

    // Constructor
    public AppDatabase() {
        //Prevent form the reflection api.
        if (INSTANCE != null) {
            throw new RuntimeException("Use getAppDatabase(context) method to get the single instance of this class.");
        }
    }

    /**
     * Get Instance of Room Database
     *
     * @param context Context
     * @return instance
     */
    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            databaseName)
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
