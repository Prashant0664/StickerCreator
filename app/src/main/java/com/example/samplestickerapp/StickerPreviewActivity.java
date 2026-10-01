package com.example.samplestickerapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

public class StickerPreviewActivity extends BaseActivity {

    public static final String EXTRA_STICKER_PATH =
            "sticker_path";

    private File stickerFile;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_sticker_preview
        );

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        String path =
                getIntent().getStringExtra(
                        EXTRA_STICKER_PATH
                );

        if (path == null || path.isEmpty()) {
            finish();
            return;
        }

        stickerFile = new File(path);

        if (!stickerFile.exists()) {
            finish();
            return;
        }

        ImageView previewImage =
                findViewById(
                        R.id.preview_image
                );

        previewImage.setImageBitmap(
                android.graphics.BitmapFactory
                        .decodeFile(
                                stickerFile
                                        .getAbsolutePath()
                        )
        );

        findViewById(
                R.id.back_button
        ).setOnClickListener(
                v -> finish()
        );

        Button deleteButton =
                findViewById(
                        R.id.delete_button
                );

        deleteButton.setOnClickListener(
                v -> confirmDelete()
        );
    }

    private void confirmDelete() {

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Delete sticker?")
                        .setMessage(
                                stickerFile.getName()
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Delete",
                                (dialogInterface, which) ->
                                        deleteSticker()
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    int textColor =
                            getResources().getColor(
                                    android.R.color.black,
                                    getTheme()
                            );

                    android.widget.TextView message =
                            dialog.findViewById(
                                    android.R.id.message
                            );

                    if (message != null) {
                        message.setTextColor(
                                textColor
                        );
                    }

                    dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                    ).setTextColor(textColor);

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setTextColor(textColor);
                }
        );

        dialog.show();
    }

    private void deleteSticker() {

        try {

            StickerPackManager manager =
                    new StickerPackManager(this);

            manager.deleteSticker(
                    "memes",
                    stickerFile.getName()
            );

            setResult(RESULT_OK);
            finish();

        } catch (Exception e) {

            android.util.Log.e(
                    "LUCKY_STICKERS",
                    "Failed to delete sticker",
                    e
            );
        }
    }
}