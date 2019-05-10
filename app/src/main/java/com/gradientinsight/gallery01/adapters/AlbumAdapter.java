package com.gradientinsight.gallery01.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gradientinsight.gallery01.R;
import com.gradientinsight.gallery01.activities.AlbumViewActivity;
import com.gradientinsight.gallery01.activities.ViewAlbumActivity;
import com.gradientinsight.gallery01.diffUtil.AlbumDiffUtilCallBack;
import com.gradientinsight.gallery01.model.Album;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private Context mContext;
    private ArrayList<Album> albumList;

    public AlbumAdapter(Context mContext, ArrayList<Album> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    public class AlbumViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView count;
        private TextView albumName;

        public AlbumViewHolder(final View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            albumName = itemView.findViewById(R.id.albumName);
            count = itemView.findViewById(R.id.count);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String albumName = albumList.get(position).getAlbumName();
                    Intent intent = new Intent(mContext, ViewAlbumActivity.class);
                    intent.putExtra(AlbumViewActivity.albumNameExtra, albumName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    public ArrayList<Album> getData() {
        return albumList;
    }

    public void setData(ArrayList<Album> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new AlbumDiffUtilCallBack(newData, albumList));
        this.albumList.clear();
        this.albumList.addAll(newData);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);
        return new AlbumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);
        Glide.with(mContext).load(Uri.fromFile(new File(album.getPath())))
                .apply(new RequestOptions().centerCrop())
                .into(holder.imageView);
        holder.count.setText(album.getPhotoCount());
        holder.albumName.setText(album.getAlbumName());
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            Bundle o = (Bundle) payloads.get(0);
            for (String key : o.keySet()) {
                if (key.equals("photoCount")) {
                    Album album = albumList.get(position);
                    Glide.with(mContext).load(Uri.fromFile(new File(album.getPath())))
                            .apply(new RequestOptions().centerCrop())
                            .into(holder.imageView);
                    holder.count.setText(album.getPhotoCount());
                    holder.albumName.setText(album.getAlbumName());
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }
}
