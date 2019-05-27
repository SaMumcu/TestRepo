package com.gradientinsight.gallery01.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.gradientinsight.gallery01.R;
import com.gradientinsight.gallery01.adapters.CustomListAdapter;
import com.gradientinsight.gallery01.adapters.ViewAlbumAdapter;
import com.gradientinsight.gallery01.dao.PhotoDao;
import com.gradientinsight.gallery01.database.AppDatabase;
import com.gradientinsight.gallery01.entities.Photo;
import com.gradientinsight.gallery01.util.ExternalPhotosUtil;
import com.gradientinsight.gallery01.util.GridSpacingItemDecoration;
import com.gradientinsight.gallery01.util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAlbumActivity extends AppCompatActivity {

    public static String albumNameExtra = "albumNameExtra";
    private String albumName = null;
    private boolean allLoaded = false;
    private String filteredTag = "";
    private ArrayList<Photo> originalPhotosList = new ArrayList<>();
    private List<String> listOfTags = new ArrayList<>();
    private List<String> tempTags = new ArrayList<>();
    private ArrayList<Photo> subList = new ArrayList<>();
    private HashMap<Object, String> mHashMap = new HashMap<>();

    private LinearLayout mLinearLayout;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewAlbumAdapter mAdapter;
    private LoadAlbumPhotosTask mLoadAlbumPhotosTask;
    private UpdatePhotosTask mUpdatePhotosTask;
    private GetBitmapTask mGetBitmapTask;
    private Spinner mSpinner = null;
    private Toolbar toolbar;
    private AutoCompleteTextView autoCompleteTextView;
    private CustomListAdapter mArrayAdapter = null;
    private ImageView dropDownTagIcon;
    private TextView noOfPhotos;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        /**
         * Get Extra Album Name, set Activity title and enable back button
         */
        albumName = getIntent().getStringExtra(albumNameExtra);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        autoCompleteTextView = toolbar.findViewById(R.id.autoCompleteTextView);
        dropDownTagIcon = toolbar.findViewById(R.id.dropDownIcon);
        noOfPhotos = findViewById(R.id.noOfPhotos);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/Roboto-Medium.ttf");
        autoCompleteTextView.setTypeface(typeface);
        noOfPhotos.setTypeface(typeface);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_back_icon));

        /**
         * Initialize Views
         */
        mLinearLayout = findViewById(R.id.loaderSection);
        swipeRefreshLayout = findViewById(R.id.swipe_container);
        mRecyclerView = findViewById(R.id.recycler_view);

        /**
         * Initialize RecyclerView
         */
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, Util.dpToPx(ViewAlbumActivity.this, 12), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ViewAlbumAdapter(this, new ArrayList<Photo>());
        mRecyclerView.setAdapter(mAdapter);

        /**
         * Add OnRefresh Listener
         */
        addOnRefreshListener();
        loadAlbumPhotos();

        mArrayAdapter = new CustomListAdapter(this, R.layout.tag_list_item, listOfTags);
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setAdapter(mArrayAdapter);
        autoCompleteTextView.setOnItemClickListener(onItemClickListener);

        dropDownTagIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCompleteTextView.showDropDown();
            }
        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0) {
                    filteredTag = "";
                    filterAlbumPhotosList(filteredTag);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    filteredTag = (String) adapterView.getItemAtPosition(position);
                    filterAlbumPhotosList(filteredTag);
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) {
            if (data != null) {
                int pos = data.getIntExtra("position", 0);
                Photo photo = originalPhotosList.get(pos);
                originalPhotosList.remove(photo);
                mAdapter.deletePhoto(pos);
            }
        }
    }

    private void addOnRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (TextUtils.isEmpty(filteredTag)) {
                    if (allLoaded) {
                        loadAlbumPhotos();
                    }
                } else {
                    filterAlbumPhotosList(filteredTag);
                }
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadAlbumPhotos();
    }

    private void loadAlbumPhotos() {
        mLoadAlbumPhotosTask = new LoadAlbumPhotosTask(this, albumName);
        mLoadAlbumPhotosTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        if (!swipeRefreshLayout.isRefreshing())
            showLoader();
    }

    private void showLoader() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLinearLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        mLinearLayout.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
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
        private HashMap<Object, String> mHashMap;

        public UpdatePhotosTask(ViewAlbumActivity context, HashMap<Object, String> mHashMap) {
            mAlbumActivityReference = new WeakReference<>(context);
            this.mHashMap = mHashMap;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context mContext = mAlbumActivityReference.get();
            PhotoDao photoDao = AppDatabase.getAppDatabase(mContext).photoDao();
            for (Map.Entry<Object, String> entry : mHashMap.entrySet()) {
                Photo photo = (Photo) entry.getKey();
                photoDao.update(entry.getValue(), photo.getId());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ViewAlbumActivity activity = mAlbumActivityReference.get();
            activity.updatePhotos(mHashMap);
        }
    }

    static class GetBitmapTask extends AsyncTask<Void, Void, Bitmap[]> {

        private WeakReference<ViewAlbumActivity> mAlbumActivityReference;
        private ArrayList<Photo> photos;

        public GetBitmapTask(ViewAlbumActivity context, ArrayList<Photo> photos) {
            mAlbumActivityReference = new WeakReference<>(context);
            this.photos = photos;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap[] doInBackground(Void... voids) {
            Bitmap[] bitmaps = new Bitmap[photos.size()];
            for (int i = 0; i < photos.size(); i++) {
                bitmaps[i] = BitmapFactory.decodeFile(photos.get(i).getFile());
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            super.onPostExecute(bitmaps);
            mAlbumActivityReference.get().getPhotosLabels(bitmaps, photos);
        }
    }

    /**
     * Update the list of Tags
     *
     * @param tags tags
     */
    private void updateTagList(String[] tags) {
        for (String tag : tags) {
            if (!tag.equals("empty") && !tempTags.contains(tag))
                tempTags.add(tag);
        }
    }

    /**
     * Update the ArrayAdapter of Spinner
     */
    private void updateArrayAdapter() {
        if (mArrayAdapter != null) {
            if (tempTags.size() > 0) {
                Collections.sort(tempTags, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return o1.compareTo(o2);
                    }
                });
                listOfTags.clear();
                listOfTags.addAll(tempTags);
                mArrayAdapter.notifyDataSetChanged();
                if (!TextUtils.isEmpty(filteredTag))
                    mSpinner.setSelection(listOfTags.indexOf(filteredTag));
                tempTags.clear();
                tempTags.addAll(listOfTags);
            }
        }
    }

    private void updateAlbumPhotosAdapter(ArrayList<Photo> photoArrayList) {
        if (photoArrayList.size() > 0) {
            ArrayList<Photo> mPhotos = new ArrayList<>();
            mPhotos.addAll(photoArrayList);
            Collections.sort(mPhotos, new Comparator<Photo>() {
                @Override
                public int compare(Photo o1, Photo o2) {
                    return o2.getDateTaken().compareTo(o1.getDateTaken());
                }
            });
            mAdapter.setData(mPhotos);
            originalPhotosList.clear();
            originalPhotosList.addAll(mPhotos);
            for (Photo photo : originalPhotosList) {
                String tag = photo.getTag();
                if (!TextUtils.isEmpty(tag))
                    updateTagList(tag.split(","));
            }
            updateArrayAdapter();
            noOfPhotos.setText(photoArrayList.size() + " Photos");
            generateTagsForAlbumPhotos();
        }
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
        hideLoader();
    }

    private void filterAlbumPhotosList(String tag) {
        ArrayList<Photo> filteredPhotos = new ArrayList<>();
        if (TextUtils.isEmpty(tag)) {
            filteredPhotos.addAll(originalPhotosList);
            Collections.sort(filteredPhotos, new Comparator<Photo>() {
                @Override
                public int compare(Photo o1, Photo o2) {
                    return o2.getDateTaken().compareTo(o1.getDateTaken());
                }
            });
            mAdapter.setData(filteredPhotos);
            stopRefreshing();
        } else {
            for (Photo row : originalPhotosList) {
                if (row.getTag().toLowerCase().contains(tag.toLowerCase())) {
                    filteredPhotos.add(row.clone());
                }
            }
            Collections.sort(filteredPhotos, new Comparator<Photo>() {
                @Override
                public int compare(Photo o1, Photo o2) {
                    return o2.getDateTaken().compareTo(o1.getDateTaken());
                }
            });
            mAdapter.setData(filteredPhotos);
            stopRefreshing();
        }
    }

    private void stopRefreshing() {
        if (swipeRefreshLayout.isRefreshing()) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
        }
    }

    private void updatePhotos(HashMap<Object, String> mHashMap) {
        ArrayList<Photo> newArrayList = new ArrayList<>();
        for (Photo photo : originalPhotosList) {
            Photo clone = photo.clone();
            if (mHashMap.containsKey(photo)) {
                clone.setTag(mHashMap.get(photo));
            }
            newArrayList.add(clone);
        }
        originalPhotosList.clear();
        originalPhotosList.addAll(newArrayList);
        filterAlbumPhotosList(filteredTag);
        updateArrayAdapter();
        generateTagsForAlbumPhotos();
    }

    private void generateTagsForAlbumPhotos() {
        subList.clear();
        subList = new ArrayList<>();
        mHashMap.clear();
        mHashMap = new HashMap<>();
        subList = getListOfPhotosWithoutTags();
        int subListSize = subList.size();
        allLoaded = subListSize == 0;
        if (subListSize > 0) {
            executeBitmapTask();
        }
    }

    private void executeBitmapTask() {
        mGetBitmapTask = new GetBitmapTask(ViewAlbumActivity.this, subList);
        mGetBitmapTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void getPhotosLabels(Bitmap[] bitmaps, ArrayList<Photo> photos) {
        for (int i = 0; i < photos.size(); i++) {
            Bitmap bitmap = bitmaps[i];
            Photo photo = photos.get(i);
            getLabels(bitmap, photo, new OnTagGenerated() {
                @Override
                public void onTag(StringBuilder tag, Photo mPhoto) {
                    String tags = tag.toString();
                    if (TextUtils.isEmpty(tags))
                        tags = "empty";
                    updateTagList(tags.split(","));
                    mHashMap.put(mPhoto, tags);
                    if (mHashMap.size() == subList.size()) {
                        executeUpdatePhotosTask(mHashMap);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        }
    }

    private void executeUpdatePhotosTask(HashMap<Object, String> mHashMap) {
        mUpdatePhotosTask = new UpdatePhotosTask(ViewAlbumActivity.this, mHashMap);
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
            if (listOfPhotos.size() == 5 || i + 1 == size) {
                return listOfPhotos;
            }
        }
        return listOfPhotos;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLoadAlbumPhotosTask != null) {
            mLoadAlbumPhotosTask.cancel(true);
        }
        if (mUpdatePhotosTask != null)
            mUpdatePhotosTask.cancel(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                            sbTags.append(", ");
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
        onBackPressed();
        return true;
    }
}
