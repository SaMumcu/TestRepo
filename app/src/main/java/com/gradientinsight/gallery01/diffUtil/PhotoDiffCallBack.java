package com.gradientinsight.gallery01.diffUtil;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.gradientinsight.gallery01.entities.Photo;

import java.util.ArrayList;

public class PhotoDiffCallBack extends DiffUtil.Callback {

    private ArrayList<Photo> newList;
    private ArrayList<Photo> oldList;

    public PhotoDiffCallBack(ArrayList<Photo> newList, ArrayList<Photo> oldList) {
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
        return newList.get(newItemPosition).getId().equals(oldList.get(oldItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        int result = newList.get(newItemPosition).compareTo(oldList.get(oldItemPosition));
        return result == 0;
    }

//    @Nullable
//    @Override
//    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
//        Photo newPhoto = newList.get(newItemPosition);
//        Photo oldPhoto = oldList.get(oldItemPosition);
//        Bundle diff = new Bundle();
//        if (!newPhoto.getTag().equals(oldPhoto.getTag())) {
//            diff.putString("tag", newPhoto.getTag());
//        }
//        if (diff.size() == 0) {
//            return null;
//        }
//        return diff;
//    }
}
