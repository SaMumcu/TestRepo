package com.gradientinsight.gallery01.util;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import com.gradientinsight.gallery01.dao.PhotoDao;
import com.gradientinsight.gallery01.database.AppDatabase;
import com.gradientinsight.gallery01.entities.Photo;
import com.gradientinsight.gallery01.model.Album;

import java.io.File;
import java.util.ArrayList;

public class ExternalPhotosUtil {

    public static ArrayList<Album> getListOfAlbums(Context mContext) {
        ArrayList<Album> albums = new ArrayList<>();
        Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};
        Cursor cursor = mContext.getContentResolver().query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                    String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    String timeStamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    String countPhoto = Util.getBucketPhotosCount(mContext, albumName);
                    Album album = new Album(albumId, albumName, path, timeStamp, timeStamp, countPhoto);
                    albums.add(album);
                } catch (Exception e) {
                    e.getMessage();
                }
            }
            cursor.close();
        }
        return albums;
    }

    public static Album getAllPhotosAlbum(Context mContext) {
        Album album = null;
        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_MODIFIED};
        Cursor cursor = mContext.getContentResolver().query(uriExternal, projection, null,
                null, MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC Limit 1");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                try {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    String timeStamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    album = new Album(String.valueOf(System.currentTimeMillis()), "My Album", path, timeStamp, timeStamp,
                            Util.getTotalPhotosCount(mContext));
                } catch (Exception e) {
                    e.getMessage();
                }
            }
            cursor.close();
        }
        return album;
    }

    public static ArrayList<Photo> getAlbumPhotosList(Context mContext, String albumName) {
        PhotoDao photoDao = AppDatabase.getAppDatabase(mContext).photoDao();
        ArrayList<Photo> photoArrayList = new ArrayList<>();
        Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATE_MODIFIED};

        String selection;
        if (albumName.equals("My Album")) {
            selection = null;
        } else {
            selection = "bucket_display_name = \"" + albumName + "\"";
        }
        Cursor cursor = mContext.getContentResolver().query(uriExternal, projection, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                File file = new File(path);
                boolean fileExists = file.exists();
                boolean isValidFile = Util.validFileSize(file);
                Photo dbPhoto = photoDao.checkIfAlreadyExist(id);
                if (dbPhoto == null && fileExists && isValidFile) {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    if (name.lastIndexOf(".") != -1) {
                        name = name.substring(0, name.lastIndexOf("."));
                    }
                    String type = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE));
                    Long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
                    Photo photo = new Photo.PhotoBuilder()
                            .setId(id)
                            .setName(name)
                            .setFile(path)
                            .setDateTaken(String.valueOf(date))
                            .setType(type)
                            .setTag("")
                            .create();
                    photoArrayList.add(photo);
                    photoDao.insert(photo);
                } else if (dbPhoto != null && !fileExists) {
                    photoDao.delete(dbPhoto.getId());
                } else if (dbPhoto != null && isValidFile) {
                    photoArrayList.add(dbPhoto);
                } else if (!isValidFile && fileExists) {
                    file.delete();
                    MediaScannerConnection.scanFile(mContext,new String[]{path}, null, null);
                }
            }
            cursor.close();
        }
        return photoArrayList;
    }
}
