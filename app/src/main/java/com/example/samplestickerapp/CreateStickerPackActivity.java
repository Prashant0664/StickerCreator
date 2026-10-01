        package com.example.samplestickerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class CreateStickerPackActivity extends BaseActivity {

    private EditText identifierEditText;
    private EditText nameEditText;
    private EditText publisherEditText;
    private TextView errorMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_sticker_pack);

        identifierEditText =
                findViewById(R.id.pack_identifier);

        nameEditText =
                findViewById(R.id.pack_name);

        publisherEditText =
                findViewById(R.id.pack_publisher);

        errorMessage =
                findViewById(R.id.error_message);

        Button createButton =
                findViewById(R.id.create_pack_button);

        createButton.setOnClickListener(
                v -> createPack()
        );
    }

    private void createPack() {

        String identifier =
                identifierEditText
                        .getText()
                        .toString()
                        .trim();

        String name =
                nameEditText
                        .getText()
                        .toString()
                        .trim();

        String publisher =
                publisherEditText
                        .getText()
                        .toString()
                        .trim();

        errorMessage.setVisibility(
                TextView.GONE
        );

        if (TextUtils.isEmpty(identifier)) {
            showError("Pack ID is required");
            return;
        }

        if (TextUtils.isEmpty(name)) {
            showError("Pack name is required");
            return;
        }

        if (TextUtils.isEmpty(publisher)) {
            showError("Publisher is required");
            return;
        }

        if (identifier.contains("..")
                || identifier.contains("/")) {

            showError(
                    "Pack ID cannot contain '..' or '/'"
            );

            return;
        }

        try {

            StickerPackManager manager =
                    new StickerPackManager(this);

            manager.createPack(
                    identifier,
                    name,
                    publisher
            );

            Intent result =
                    new Intent();

            result.putExtra(
                    "pack_id",
                    identifier
            );

            setResult(
                    RESULT_OK,
                    result
            );

            finish();

        } catch (Exception e) {

            showError(
                    e.getMessage()
                            != null
                            ? e.getMessage()
                            : "Unable to create sticker pack"
            );
        }
    }

    private void showError(String message) {

        errorMessage.setText(message);

        errorMessage.setVisibility(
                TextView.VISIBLE
        );
    }
}
