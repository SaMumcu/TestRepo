package com.gradientinsight.gallery01.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gradientinsight.gallery01.R;
import com.gradientinsight.gallery01.dialogs.CustomDeleteDialog;
import com.gradientinsight.gallery01.entities.Photo;
import com.gradientinsight.gallery01.interfaces.CustomListener;

import java.io.File;
import java.io.FileNotFoundException;

public class ImagePreviewActivity extends AppCompatActivity {


    Photo photo;
    String path;
    int type;
    int position;
    private static final int READ_REQUEST_CODE = 42;
    private Toolbar toolbar;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);

        toolbar = findViewById(R.id.toolbar);
        title = findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/Roboto-Regular.ttf");
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setMaxLines(2);
        title.setTextSize(18);
        title.setTypeface(typeface);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.ic_back_icon));

        Intent intent = getIntent();
        type = intent.getIntExtra("type", 0);

        ImageView imagePreview = findViewById(R.id.image);
        ImageView delete = findViewById(R.id.delete);
        ImageView share = findViewById(R.id.share);

        photo = intent.getParcelableExtra("photo");
        position = intent.getIntExtra("position", 0);
        path = photo.getFile();
        String tag = photo.getTag();
        if (tag.equals("empty"))
            tag = "";
        title.setText(tag);

        Glide.with(ImagePreviewActivity.this)
                .load(new File(path))
                .into(imagePreview);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(photo.getFile());
                Uri uri = FileProvider.getUriForFile(ImagePreviewActivity.this, getPackageName() + ".provider", file);
                Intent intent = ShareCompat.IntentBuilder.from(ImagePreviewActivity.this)
                        .setType("image/*")
                        .setStream(uri)
                        .createChooserIntent()
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                AlertDialog alertDialog = new AlertDialog.Builder(ImagePreviewActivity.this)
//                        .setMessage("Do you want to delete Image ?")
//                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                File file = new File(photo.getFile());
//                                if (file.exists()) {
//                                    if (!file.canWrite()) {
//                                        performFileSearch(file.getPath());
//                                    } else {
//                                        file.delete();
//                                        MediaScannerConnection.scanFile(ImagePreviewActivity.this, new String[]{photo.getFile()},
//                                                null, new MediaScannerConnection.OnScanCompletedListener() {
//                                                    public void onScanCompleted(String path, Uri uri) {
//                                                        Intent resultIntent = new Intent();
//                                                        resultIntent.putExtra("position", position);
//                                                        setResult(Activity.RESULT_OK, resultIntent);
//                                                        finish();
//                                                    }
//                                                });
//                                    }
//                                }
//                                dialog.dismiss();
//                            }
//                        })
//                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }).create();
//                alertDialog.show();

                CustomDeleteDialog customDeleteDialog = new CustomDeleteDialog(ImagePreviewActivity.this, new CustomListener() {
                    @Override
                    public void onCancel() {

                    }
                    @Override
                    public void onYes() {
                        File file = new File(photo.getFile());
                        if (file.exists()) {
                            if (!file.canWrite()) {
                                performFileSearch(file.getPath());
                            } else {
                                file.delete();
                                MediaScannerConnection.scanFile(ImagePreviewActivity.this, new String[]{photo.getFile()},
                                        null, new MediaScannerConnection.OnScanCompletedListener() {
                                            public void onScanCompleted(String path, Uri uri) {
                                                Intent resultIntent = new Intent();
                                                resultIntent.putExtra("position", position);
                                                setResult(Activity.RESULT_OK, resultIntent);
                                                finish();
                                            }
                                        });
                            }
                        }
                    }
                });
                customDeleteDialog.setCancelable(false);
                customDeleteDialog.setCanceledOnTouchOutside(false);
                customDeleteDialog.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("SAF Image ", "Uri: " + uri.toString());
                try {
                    DocumentsContract.deleteDocument(getContentResolver(), uri);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("position", position);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch(String path) {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Uri imageUriLcl = FileProvider.getUriForFile(ImagePreviewActivity.this, getPackageName() + ".provider", new File(path));
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, imageUriLcl);
        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
//        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
