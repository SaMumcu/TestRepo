package com.gradientinsight.gallery01.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gradientinsight.gallery01.R;
import com.gradientinsight.gallery01.activities.ImagePreviewActivity;
import com.gradientinsight.gallery01.activities.ViewAlbumActivity;
import com.gradientinsight.gallery01.diffUtil.PhotoDiffCallBack;
import com.gradientinsight.gallery01.entities.Photo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ViewAlbumAdapter extends RecyclerView.Adapter<ViewAlbumAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<Photo> photosList;

    public ViewAlbumAdapter(Context mContext, ArrayList<Photo> photos) {
        this.mContext = mContext;
        this.photosList = photos;
    }

    public void setData(ArrayList<Photo> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PhotoDiffCallBack(newData, photosList));
        diffResult.dispatchUpdatesTo(this);
        this.photosList.clear();
        this.photosList = new ArrayList<>();
        this.photosList.addAll(newData);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView tag;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            tag = itemView.findViewById(R.id.tag);
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "font/Roboto-Medium.ttf");
            tag.setTypeface(typeface);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent intent = new Intent(mContext, ImagePreviewActivity.class);
                    intent.putExtra("type", 1);
                    intent.putExtra("photo", photosList.get(position));
                    intent.putExtra("position", position);
                    ((ViewAlbumActivity) mContext).startActivityForResult(intent, 100);
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Photo photo = this.photosList.get(position);
        Glide.with(mContext).load(Uri.fromFile(new File(photo.getFile())))
                .apply(new RequestOptions().centerCrop())
                .into(holder.imageView);
        String tag = photo.getTag();
        if (TextUtils.isEmpty(tag) || tag.equals("empty"))
            holder.tag.setText("No tags yet");
        else
            holder.tag.setText(tag);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            Bundle o = (Bundle) payloads.get(0);
            for (String key : o.keySet()) {
                if (key.equals("tag")) {
                    Photo photo = this.photosList.get(position);
                    String tag = photo.getTag();
                    if (TextUtils.isEmpty(tag) || tag.equals("empty"))
                        holder.tag.setText("No tags yet");
                    else
                        holder.tag.setText(tag);
                }
            }
        }
    }

    public void deletePhoto(int position) {
        photosList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_photo_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }
}