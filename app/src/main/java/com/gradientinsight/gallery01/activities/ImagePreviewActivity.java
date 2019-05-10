package com.gradientinsight.gallery01.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gradientinsight.gallery01.R;
import com.gradientinsight.gallery01.entities.Photo;

import java.io.File;

public class ImagePreviewActivity extends AppCompatActivity {


    Photo photo;
    String path;
    int type;
    LinearLayout footer;
    TextView tags;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
        getSupportActionBar().hide();

        Intent intent = getIntent();
        type = intent.getIntExtra("type", 0);

        ImageView imagePreview = findViewById(R.id.image);
        ImageView delete = findViewById(R.id.delete);
        ImageView share = findViewById(R.id.share);
        tags = findViewById(R.id.tags);
        footer = findViewById(R.id.footerLayout);


        photo = intent.getParcelableExtra("photo");
        position = intent.getIntExtra("position", 0);
        path = photo.getFile();
        footer.setVisibility(View.VISIBLE);
        String tag = photo.getTag();
        if (tag.equals("empty"))
            tag = "";
        tags.setText(tag);

        Glide.with(ImagePreviewActivity.this)
                .load(new File(path))
                .into(imagePreview);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                shareIntent.setType("image/*");

                Uri uri = Uri.fromFile(new File(photo.getFile()));
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(shareIntent, "Share Image Using"));
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(ImagePreviewActivity.this)
                        .setMessage("Do you want to delete Image ?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                File file = new File(photo.getFile());
                                if (file.exists()) {
                                    boolean isDeleted = file.delete();
                                    if (isDeleted) {
                                        MediaScannerConnection.scanFile(ImagePreviewActivity.this
                                                , new String[]{photo.getFile()}, null, null);
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("position", position);
                                        setResult(Activity.RESULT_OK, resultIntent);
                                        finish();
                                    }
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                alertDialog.show();
            }
        });
    }


}
