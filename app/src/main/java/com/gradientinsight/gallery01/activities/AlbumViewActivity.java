package com.gradientinsight.gallery01.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
import com.gradientinsight.gallery01.util.GridSpacingItemDecoration;
import com.gradientinsight.gallery01.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("deprecation")
public class AlbumViewActivity extends AppCompatActivity {

    public static String albumNameExtra = "albumNameExtra";
    private String albumName;
    private ArrayList<Photo> albumsPhotosList = new ArrayList<>();
    private ArrayList<Photo> originalPhotosList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private ViewAlbumAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<String> listOfTags = new ArrayList<>();
    private ArrayAdapter<String> mArrayAdapter;
    private List<String> tempTags = new ArrayList<>();
    private Spinner spinner;
    private String filteredTag = "";
    private boolean allLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);
        albumName = getIntent().getStringExtra(albumNameExtra);
        this.setTitle(albumName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = findViewById(R.id.swipe_container);
        mRecyclerView = findViewById(R.id.recycler_view);
        listOfTags.add("Search By Tag");
        mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listOfTags);
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(5), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ViewAlbumAdapter(this, albumsPhotosList);
        mRecyclerView.setAdapter(mAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (TextUtils.isEmpty(filteredTag)) {
                    if (allLoaded) {
                        executeLoadAlbumImagesTask();
                    }
                } else {
                    applyFilter(filteredTag);
                }
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        executeLoadAlbumImagesTask();
    }

    private synchronized void executeLoadAlbumImagesTask() {
        AppDatabase database = AppDatabase.getAppDatabase(AlbumViewActivity.this);
        new LoadAlbumImages(database.photoDao()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(mArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 && !TextUtils.isEmpty(filteredTag)) {
                    filteredTag = "";
                    applyFilter(filteredTag);
                } else if (position != 0) {
                    filteredTag = listOfTags.get(position);
                    applyFilter(filteredTag);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) {
            if (data != null) {
                int pos = data.getIntExtra("position", 0);
                Photo photo = albumsPhotosList.get(pos);
                originalPhotosList.remove(photo);
                mAdapter.deletePhoto(pos);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private class LoadAlbumImages extends AsyncTask<Void, Void, ArrayList<Photo>> {

        private PhotoDao photoDao;

        public LoadAlbumImages(PhotoDao photoDao) {
            this.photoDao = photoDao;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Photo> doInBackground(Void... voids) {
            ArrayList<Photo> photoArrayList = new ArrayList<>();
            Uri uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.DATE_ADDED};
            String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";
            String selection;
            if (albumName.equals("My Album")) {
                selection = null;
            } else {
                selection = "bucket_display_name = \"" + albumName + "\"";
            }

            Cursor cursor = getContentResolver().query(uriExternal, projection, selection,
                    null, null);
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
                        Long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                        if (date == -1) {
                            date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                        }
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
                        String tag = dbPhoto.getTag();
                        if (!TextUtils.isEmpty(tag)) {
                            updateTagList(tag.split(","));
                        }
                        photoArrayList.add(dbPhoto);
                    } else if (!isValidFile && fileExists) {
                        file.delete();
                        MediaScannerConnection.scanFile(AlbumViewActivity.this
                                , new String[]{path}, null, null);
                    }
                }
                cursor.close();
            }
            return photoArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Photo> photos) {
            super.onPostExecute(photos);
            albumsPhotosList.clear();
            originalPhotosList.clear();
            if (photos.size() > 0) {
                albumsPhotosList.addAll(photos);
                originalPhotosList.addAll(albumsPhotosList);
                sortPhotos();
                mAdapter.setData(albumsPhotosList);
                updateArrayAdapter();
                generateTags();
            }
            stopRefreshing();
        }
    }

    private synchronized void applyFilter(String tag) {
        if (tag.isEmpty()) {
            albumsPhotosList = originalPhotosList;
            sortPhotos();
            mAdapter.setData(albumsPhotosList);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRefreshing();
                }
            }, 1000);
        } else {
            ArrayList<Photo> filteredList = new ArrayList<>();
            for (Photo row : originalPhotosList) {
                if (row.getTag().toLowerCase().contains(tag.toLowerCase())) {
                    filteredList.add(row);
                }
            }
            albumsPhotosList = filteredList;
            sortPhotos();
            mAdapter.setData(albumsPhotosList);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRefreshing();
                }
            }, 1000);
        }
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    private class UpdatePhotosTask extends AsyncTask<Void, Void, Void> {

        private PhotoDao photoDao;
        private ArrayList<Photo> photos;
        private ArrayList<String> tagsList;

        public UpdatePhotosTask(PhotoDao photoDao, ArrayList<Photo> photos, ArrayList<String> tagsList) {
            this.photoDao = photoDao;
            this.photos = photos;
            this.tagsList = tagsList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int j = 0; j < photos.size(); j++) {
                photoDao.update(tagsList.get(j), photos.get(j).getId());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < photos.size(); i++) {
                        Photo ph = photos.get(i);
                        int index = originalPhotosList.indexOf(ph);
                        ph.setTag(tagsList.get(i));
                        originalPhotosList.set(index, ph);
                    }
                    applyFilter(filteredTag);
                    updateArrayAdapter();
                    generateTags();
                }
            });
        }
    }

    private void sortPhotos() {
        Collections.sort(albumsPhotosList, new Comparator<Photo>() {
            @Override
            public int compare(Photo o1, Photo o2) {
                return o2.getDateTaken().compareTo(o1.getDateTaken());
            }
        });
    }

    private void updateTagList(String[] tags) {
        for (String tag : tags) {
            if (!tag.equals("empty") && !tempTags.contains(tag))
                tempTags.add(tag);
        }
    }

    private void updateArrayAdapter() {
        if (mArrayAdapter != null) {
            if (tempTags.size() > 0) {
                tempTags.remove(0);
                Collections.sort(tempTags, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });
                tempTags.add(0, "Search by Tag");
                listOfTags.clear();
                listOfTags.addAll(tempTags);
                mArrayAdapter.notifyDataSetChanged();
                if (!TextUtils.isEmpty(filteredTag))
                    spinner.setSelection(listOfTags.indexOf(filteredTag));
                tempTags.clear();
                tempTags.addAll(listOfTags);
            }
        }
    }


    ArrayList<Photo> tempPhotos = new ArrayList<>();
    ArrayList<String> tagsList = new ArrayList<>();
    ArrayList<Photo> subList = new ArrayList<>();

    private void generateTags() {
        tempPhotos.clear();
        subList.clear();
        tagsList.clear();
        subList = getListOfPhotosWithoutTags();
        int subListSize = subList.size();
        allLoaded = subListSize == 0;
        if (subListSize > 0) {
            for (final Photo photo : subList) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeFile(photo.getFile());
                        getLabels(bitmap, photo, new OnTagGenerated() {
                            @Override
                            public void onTag(final StringBuilder tag, final Photo mPhoto) {
                                tempPhotos.add(mPhoto);
                                String tags = tag.toString();
                                if (TextUtils.isEmpty(tags))
                                    tags = "empty";
                                updateTagList(tags.split(","));
                                tagsList.add(tags);
                                if (tempPhotos.size() == subList.size()) {
                                    updatePhotos(tempPhotos, tagsList);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                e.getMessage();
                            }
                        });
                    }
                }).start();
            }
        }
    }

    private void updatePhotos(ArrayList<Photo> updatedPhotos, ArrayList<String> tagsList) {
        PhotoDao photoDao = AppDatabase.getAppDatabase(this).photoDao();
        new UpdatePhotosTask(photoDao, updatedPhotos, tagsList).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private static void getLabels(final Bitmap bitmap, final Photo photo,
                                  final OnTagGenerated callback) {
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
