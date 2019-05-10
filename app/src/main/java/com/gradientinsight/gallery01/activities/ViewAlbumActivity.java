package com.gradientinsight.gallery01.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.gradientinsight.gallery01.R;
import com.gradientinsight.gallery01.adapters.ViewAlbumAdapter;
import com.gradientinsight.gallery01.dao.PhotoDao;
import com.gradientinsight.gallery01.database.AppDatabase;
import com.gradientinsight.gallery01.entities.Photo;
import com.gradientinsight.gallery01.util.ExternalPhotosUtil;
import com.gradientinsight.gallery01.util.GridSpacingItemDecoration;
import com.gradientinsight.gallery01.util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ViewAlbumActivity extends AppCompatActivity {

    public static String albumNameExtra = "albumNameExtra";
    private String albumName = null;
    private boolean allLoaded = false;
    private ArrayList<Photo> originalPhotosList = new ArrayList<>();
    private ArrayList<Photo> tempPhotos = new ArrayList<>();
    private ArrayList<String> tagsList = new ArrayList<>();
    private ArrayList<Photo> subList = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewAlbumAdapter mAdapter;
    private LoadAlbumPhotosTask mLoadAlbumPhotosTask;
    private UpdatePhotosTask mUpdatePhotosTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        /**
         * Get Extra Album Name, set Activity title and enable back button
         */
        albumName = getIntent().getStringExtra(albumNameExtra);
        this.setTitle(albumName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**
         * Initialize Views
         */
        swipeRefreshLayout = findViewById(R.id.swipe_container);
        mRecyclerView = findViewById(R.id.recycler_view);

        /**
         * Initialize RecyclerView
         */
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, Util.dpToPx(ViewAlbumActivity.this, 5), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ViewAlbumAdapter(this, new ArrayList<Photo>());
        mRecyclerView.setAdapter(mAdapter);

        /**
         * Add OnRefresh Listener
         */
        addOnRefreshListener();
        loadAlbumPhotos();
    }

    private void addOnRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
    }

    private void loadAlbumPhotos() {
        mLoadAlbumPhotosTask = new LoadAlbumPhotosTask(this, albumName);
        mLoadAlbumPhotosTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    static class LoadAlbumPhotosTask extends AsyncTask<Void, Void, ArrayList<Photo>> {

        private WeakReference<ViewAlbumActivity> mAlbumActivityReference;
        private String albumName;

        private LoadAlbumPhotosTask(ViewAlbumActivity context, String albumName) {
            mAlbumActivityReference = new WeakReference<>(context);
            this.albumName = albumName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Photo> doInBackground(Void... voids) {
            Context mContext = mAlbumActivityReference.get();
            ArrayList<Photo> photoArrayList = ExternalPhotosUtil.getAlbumPhotosList(mContext, albumName);
            return photoArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Photo> photos) {
            super.onPostExecute(photos);
            ViewAlbumActivity activity = mAlbumActivityReference.get();
            activity.updateAlbumPhotosAdapter(photos);
        }
    }

    static class UpdatePhotosTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<ViewAlbumActivity> mAlbumActivityReference;
        private ArrayList<Photo> photos;
        private ArrayList<String> tags;

        public UpdatePhotosTask(ViewAlbumActivity context, ArrayList<Photo> photos, ArrayList<String> tags) {
            mAlbumActivityReference = new WeakReference<>(context);
            this.photos = photos;
            this.tags = tags;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context mContext = mAlbumActivityReference.get();
            PhotoDao photoDao = AppDatabase.getAppDatabase(mContext).photoDao();
            for (int i = 0; i < photos.size(); i++)
                photoDao.update(photos.get(i).getTag(), tags.get(i));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ViewAlbumActivity activity = mAlbumActivityReference.get();
            activity.updateAlbumPhotosAdapterWithUpdatedPhotosList(photos, tags);
        }
    }

    private void updateAlbumPhotosAdapter(ArrayList<Photo> photoArrayList) {
        if (photoArrayList.size() > 0) {
            ArrayList<Photo> mPhotos = new ArrayList<>();
            mPhotos.addAll(photoArrayList);
            mAdapter.setData(mPhotos);
            originalPhotosList.clear();
            originalPhotosList.addAll(mPhotos);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Photo> newArrayList = new ArrayList<>();
                    newArrayList.addAll(originalPhotosList);
                    for (int j = 0; j < originalPhotosList.size(); j++)
                        newArrayList.get(j).setTag("dddd" + j);
                    mAdapter.setData(newArrayList);
                }
            }, 5000);
//            generateTagsForAlbumPhotos();
        }
    }

    private void updateAlbumPhotosAdapterWithUpdatedPhotosList(ArrayList<Photo> photoArrayList, ArrayList<String> tags) {
        ArrayList<Photo> newArrayList = new ArrayList<>();
        if (photoArrayList.size() > 0) {
            newArrayList.addAll(originalPhotosList);
            for (int i = 0; i < photoArrayList.size(); i++) {
                Photo mPhoto = photoArrayList.get(i);
                int index = newArrayList.indexOf(mPhoto);
                mPhoto.setTag(tags.get(i));
                newArrayList.set(index, mPhoto);
            }
            mAdapter.setData(newArrayList);
            originalPhotosList.clear();
            originalPhotosList.addAll(newArrayList);
        }
    }

    private void generateTagsForAlbumPhotos() {
        tempPhotos.clear();
        tempPhotos = new ArrayList<>();
        subList.clear();
        subList = new ArrayList<>();
        tagsList.clear();
        tagsList = new ArrayList<>();
        subList = getListOfPhotosWithoutTags();
        int subListSize = subList.size();
        allLoaded = subListSize == 0;
        if (subListSize > 0)
            getPhotosLabels();
    }

    private void getPhotosLabels() {
        for (final Photo photo : subList) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = BitmapFactory.decodeFile(photo.getFile());
                    getLabels(bitmap, photo, new OnTagGenerated() {
                        @Override
                        public void onTag(StringBuilder tag, Photo mPhoto) {
                            tempPhotos.add(mPhoto);
                            String tags = tag.toString();
                            if (TextUtils.isEmpty(tags))
                                tags = "empty";
                            tagsList.add(tags);
                            if (tempPhotos.size() == subList.size()) {
                                executeUpdatePhotosTask(tempPhotos, tagsList);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                        }
                    });
                }
            }).start();
        }
    }

    private void executeUpdatePhotosTask(ArrayList<Photo> photos, ArrayList<String> tags) {
        mUpdatePhotosTask = new UpdatePhotosTask(ViewAlbumActivity.this, photos, tags);
        mUpdatePhotosTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private ArrayList<Photo> getListOfPhotosWithoutTags() {
        ArrayList<Photo> listOfPhotos = new ArrayList<>();
        int size = originalPhotosList.size();
        for (int i = 0; i < size; i++) {
            Photo photo = originalPhotosList.get(i);
            String photoTag = photo.getTag();
            if (photoTag == null || TextUtils.isEmpty(photoTag))
                listOfPhotos.add(photo);
            if (listOfPhotos.size() == 10 || i + 1 == size) {
                return listOfPhotos;
            }
        }
        return listOfPhotos;
    }

    public interface OnTagGenerated {
        void onTag(StringBuilder tag, Photo photo);

        void onFailure(Exception e);
    }

    private static void getLabels(Bitmap bitmap, final Photo photo, final OnTagGenerated callback) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                StringBuilder sbTags = new StringBuilder();
                if (labels.size() > 0) {
                    String lastLabel = labels.get(labels.size() - 1).getText();
                    for (FirebaseVisionImageLabel label : labels) {
                        String text = label.getText();
                        sbTags.append(text);
                        if (!text.equals(lastLabel))
                            sbTags.append(",");
                    }
                }
                callback.onTag(sbTags, photo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return true;
    }
}
