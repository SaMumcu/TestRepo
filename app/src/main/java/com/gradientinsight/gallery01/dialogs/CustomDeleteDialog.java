package com.gradientinsight.gallery01.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.gradientinsight.gallery01.R;
import com.gradientinsight.gallery01.interfaces.CustomListener;

public class CustomDeleteDialog extends Dialog {

    CustomListener customListener;
    Button cancel, delete;

    public CustomDeleteDialog(Context context,final CustomListener customListener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_dialog);
        this.customListener = customListener;
        cancel = findViewById(R.id.cancel_action);
        delete = findViewById(R.id.delete_action);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customListener.onCancel();
                dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customListener.onYes();
                dismiss();
            }
        });
    }
}
