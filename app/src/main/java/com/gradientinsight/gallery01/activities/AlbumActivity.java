package com.gradientinsight.gallery01.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gradientinsight.gallery01.R;
import com.gradientinsight.gallery01.adapters.AlbumAdapter;
import com.gradientinsight.gallery01.model.Album;
import com.gradientinsight.gallery01.util.ExternalPhotosUtil;
import com.gradientinsight.gallery01.util.GridSpacingItemDecoration;
import com.gradientinsight.gallery01.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AlbumActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;
    private SwipeRefreshLayout swipeRefreshLayout;
    @TargetApi(Build.VERSION_CODES.M)
    static String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private RecyclerView mRecyclerView;
    private LoadAlbumsTask loadAlbumsTask;
    private AlbumAdapter mAdapter;
    private ArrayList<Album> albumArrayList = new ArrayList<>();
    private LinearLayout mLoaderLayout;
    private Toolbar toolbar;
    private TextView toolBarTitle;
    private TextView totalAlbums;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        totalAlbums = findViewById(R.id.totalAlbums);
        toolbar = findViewById(R.id.toolbar);
        toolBarTitle = findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/Roboto-Medium.ttf");
        toolBarTitle.setTypeface(typeface);
        totalAlbums.setTypeface(typeface);

        swipeRefreshLayout = findViewById(R.id.swipe_container);
        mLoaderLayout = findViewById(R.id.loaderSection);
        mRecyclerView = findViewById(R.id.recyclerView);

        /**
         * Initialize RecyclerView
         */
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, Util.dpToPx(AlbumActivity.this, 12), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new AlbumAdapter(AlbumActivity.this, albumArrayList);
        mRecyclerView.setAdapter(mAdapter);

        /**
         * Request Runtime Permissions
         */
        requestRunTimePermissions();
        addOnSwipeRefreshListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (checkPermissionGranted())
            loadAlbums();
    }

    private boolean checkPermissionGranted() {
        return checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(permissions[1]) == PackageManager.PERMISSION_GRANTED;
    }

    private void addOnSwipeRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                loadAlbums();
            }
        });
    }

    private void showLoader() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoaderLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        mLoaderLayout.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void loadAlbums() {
        loadAlbumsTask = new LoadAlbumsTask(AlbumActivity.this);
        loadAlbumsTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        if (!swipeRefreshLayout.isRefreshing()) {
            showLoader();
        }
    }

    static class LoadAlbumsTask extends AsyncTask<Void, Void, ArrayList<Album>> {

        private WeakReference<AlbumActivity> mAlbumActivityReference;

        private LoadAlbumsTask(AlbumActivity context) {
            mAlbumActivityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Album> doInBackground(Void... voids) {
            Context mContext = mAlbumActivityReference.get();
            ArrayList<Album> albums = ExternalPhotosUtil.getListOfAlbums(mContext);
            Collections.sort(albums, new Comparator<Album>() {
                @Override
                public int compare(Album o1, Album o2) {
                    return o2.getTimeStamp().compareTo(o1.getTimeStamp());
                }
            });
            Album allPhotosAlbum = ExternalPhotosUtil.getAllPhotosAlbum(mContext);
            ArrayList<Album> newAlbumsList = new ArrayList<>();
            newAlbumsList.addAll(albums);
            if (allPhotosAlbum != null)
                newAlbumsList.add(0, allPhotosAlbum);
            return newAlbumsList;
        }

        @Override
        protected void onPostExecute(ArrayList<Album> albums) {
            super.onPostExecute(albums);
            AlbumActivity activity = mAlbumActivityReference.get();
            activity.updateAlbumAdapter(albums);
        }
    }

    private void updateAlbumAdapter(ArrayList<Album> albumList) {
        if (albumList.size() > 0) {
            ArrayList<Album> updateAlbums = new ArrayList<>();
            updateAlbums.addAll(albumList);
            mAdapter.setData(updateAlbums);
        }
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        hideLoader();
    }

    private void requestRunTimePermissions() {
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(AlbumActivity.this, permissions[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(AlbumActivity.this, permissions[1])
            ) {
                displayPermissionRequestDialog();
            } else {
                ActivityCompat.requestPermissions(AlbumActivity.this,
                        permissions
                        , REQUEST_PERMISSION);
            }
        } else {
            loadAlbums();
        }
    }

    public int checkSelfPermission(String permission) {
        return ActivityCompat.checkSelfPermission(AlbumActivity.this, permission);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                boolean allGranted = false;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        allGranted = true;
                    } else {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    loadAlbums();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(AlbumActivity.this, permissions[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(AlbumActivity.this, permissions[1])
                ) {
                    displayPermissionRequestDialog();
                }
        }
    }

    private void displayPermissionRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AlbumActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("Gallery App need Multiple Permissions." +
                "\nPlease Grant.");
        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ActivityCompat.requestPermissions(AlbumActivity.this, permissions, REQUEST_PERMISSION);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loadAlbumsTask != null) {
            loadAlbumsTask.cancel(true);
        }
    }
}
