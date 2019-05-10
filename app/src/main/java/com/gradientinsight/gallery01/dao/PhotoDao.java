package com.gradientinsight.gallery01.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.gradientinsight.gallery01.entities.Photo;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public interface PhotoDao {

    @Insert(onConflict = IGNORE)
    void insert(List<Photo> photoList);

    @Insert(onConflict =  IGNORE)
    void insert(Photo photo);

    @Query("UPDATE photo_table SET tag=:tag WHERE id = :id")
    void update(String tag, String id);

    @Query("delete from photo_table where id = :photo_id")
    void delete(String photo_id);

    @Delete
    void deletePhotos(List<Photo> photos);

    @Query("Select * from photo_table where name=:bucket_name")
    List<Photo> selectAllPhotos(String bucket_name);

    @Query("Select * from photo_table")
    List<Photo> selectAllPhotos();

    @Query("Select * from photo_table where id = :photo_id")
    Photo checkIfAlreadyExist(String photo_id);

    @Query("Select MIN(date_taken) from photo_table")
    String selectMaxDate();

    @Query("Select tag from photo_table where id =:id")
    String selectTagForPhotoId(String id);
}

