package com.gradientinsight.gallery01.diffUtil;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import com.gradientinsight.gallery01.model.Album;
import java.util.ArrayList;

public class AlbumDiffUtilCallBack extends DiffUtil.Callback {

    private ArrayList<Album> newList;
    private ArrayList<Album> oldList;

    public AlbumDiffUtilCallBack(ArrayList<Album> newList, ArrayList<Album> oldList) {
        this.newList = newList;
        this.oldList = oldList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return newList.get(newItemPosition).getAlbumName().equals(oldList.get(oldItemPosition).getAlbumName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        int result = newList.get(newItemPosition).compareTo(oldList.get(oldItemPosition));
        return result == 0;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Album newAlbum = newList.get(newItemPosition);
        Album oldAlbum = oldList.get(oldItemPosition);
        Bundle diff = new Bundle();
        if (!newAlbum.getPhotoCount().equals(oldAlbum.getPhotoCount())) {
            diff.putString("photoCount", newAlbum.getPhotoCount());
        }
        if (diff.size() == 0) {
            return null;
        }
        return diff;
    }
}
