package com.example.samplestickerapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class EntryActivity extends BaseActivity {

    private static final int PICK_STICKER = 1001;

    private LoadListAsyncTask loadListAsyncTask;
    private View progressBar;
    private String selectedPackId = "memes";
    private RecyclerView stickerRecyclerView;
    private RecyclerView packRecyclerView;
    private LocalPackAdapter packAdapter;
    private List<StickerPackData> localPacks;
    private LocalStickerAdapter stickerAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry);

        stickerRecyclerView = findViewById(R.id.sticker_recycler_view);

        stickerRecyclerView.setLayoutManager(
                new GridLayoutManager(this, 3)
        );

        loadLocalStickers();

        overridePendingTransition(0, 0);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button addStickerButton =
                findViewById(R.id.add_sticker_button);

        addStickerButton.setOnClickListener(
                v -> pickSticker()
        );

        Button addToWhatsAppButton =
                findViewById(R.id.add_to_whatsapp_button);

        addToWhatsAppButton.setOnClickListener(
                v -> openStickerPackDetails()
        );
        Button createPackButton = findViewById(R.id.create_pack_button); createPackButton.setOnClickListener(v -> { Intent intent = new Intent( EntryActivity.this, CreateStickerPackActivity.class ); startActivity(intent); });
        packRecyclerView =
                findViewById(R.id.pack_recycler_view);

        packRecyclerView.setLayoutManager(
                new GridLayoutManager(this, 1)
        );

        loadLocalPacks();
        progressBar =
                findViewById(R.id.entry_activity_progress);
        createPackButton.setOnClickListener(
                v -> {
                    Intent intent =
                            new Intent(
                                    EntryActivity.this,
                                    CreatePackActivity.class
                            );

                    startActivity(intent);
                }
        );
    }
    private void loadLocalPacks() {

        try {

            StickerPackManager manager =
                    new StickerPackManager(this);

            localPacks =
                    manager.getAllPacks();

            packAdapter =
                    new LocalPackAdapter(
                            localPacks,
                            this::openPack
                    );

            packRecyclerView.setAdapter(
                    packAdapter
            );

        } catch (Exception e) {

            Log.e(
                    "LUCKY_STICKERS",
                    "Unable to load sticker packs",
                    e
            );

            showError(
                    e.getMessage()
            );
        }
    }
    private void openPack(StickerPackData pack) {

        try {
            selectedPackId = pack.identifier;
            ArrayList<StickerPack> packs =
                    StickerPackLoader.fetchStickerPacks(this);

            StickerPack selectedPack = null;

            for (StickerPack stickerPack : packs) {

                if (pack.identifier.equals(
                        stickerPack.identifier)) {

                    selectedPack = stickerPack;
                    break;
                }
            }

            if (selectedPack == null) {

                showError(
                        "Unable to find sticker pack: "
                                + pack.identifier
                );

                return;
            }

            Intent intent =
                    new Intent(
                            this,
                            StickerPackDetailsActivity.class
                    );

            intent.putExtra(
                    StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON,
                    true
            );

            intent.putExtra(
                    StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA,
                    selectedPack
            );

            startActivity(intent);

        } catch (Exception e) {

            Log.e(
                    "LUCKY_STICKERS",
                    "Unable to open sticker pack",
                    e
            );

            showError(
                    e.getMessage()
            );
        }
    }

    private void openStickerPackDetails() {
        loadListAsyncTask = new LoadListAsyncTask(this);
        loadListAsyncTask.execute();
    }
    private void showStickerPack(
            ArrayList<StickerPack> stickerPackList) {

        progressBar.setVisibility(View.GONE);

        if (stickerPackList == null ||
                stickerPackList.isEmpty()) {

            showError("No sticker packs found");
            return;
        }

        StickerPack stickerPack =
                stickerPackList.get(0);

        Intent intent =
                new Intent(
                        this,
                        StickerPackDetailsActivity.class
                );

        intent.putExtra(
                StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON,
                false
        );

        intent.putExtra(
                StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA,
                stickerPack
        );

        startActivity(intent);

        overridePendingTransition(0, 0);
    }
    private void loadLocalStickers() {

        StickerStorage storage =
                new StickerStorage(this);

        File packDirectory =
                storage.getPackDirectory(selectedPackId);

        File[] files =
                packDirectory.listFiles(
                        file ->
                                file.isFile()
                                        && file.getName()
                                        .endsWith(".webp")
                                        && !file.getName()
                                        .equals("tray.webp")
                );

        List<File> stickers =
                new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                stickers.add(file);
            }
        }

        stickerAdapter =
                new LocalStickerAdapter(
                        stickers,
                        this::openStickerPreview,
                        this::confirmDeleteSticker
                );

        stickerRecyclerView.setAdapter(
                stickerAdapter
        );
    }
    private void openStickerPreview(File sticker) {

        Intent intent =
                new Intent(
                        this,
                        StickerPreviewActivity.class
                );

        intent.putExtra(
                StickerPreviewActivity.EXTRA_STICKER_PATH,
                sticker.getAbsolutePath()
        );

        startActivity(intent);
    }

    private void deleteSticker(File sticker) {

        try {

            StickerPackManager manager =
                    new StickerPackManager(this);

            manager.deleteSticker(
                    selectedPackId,
                    sticker.getName()
            );

            loadLocalStickers();

        } catch (Exception e) {

            Log.e(
                    "LUCKY_STICKERS",
                    "Failed to delete sticker",
                    e
            );

            showError(
                    e.getMessage()
            );
        }

    }

    private void confirmDeleteSticker(File sticker) {

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Delete sticker?")
                        .setMessage(sticker.getName())
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Delete",
                                (dialogInterface, which) ->
                                        deleteSticker(sticker)
                        )
                        .create();

        dialog.setOnShowListener(dialogInterface -> {

            int textColor =
                    getResources().getColor(
                            android.R.color.black,
                            getTheme()
                    );

            int buttonColor =
                    getResources().getColor(
                            android.R.color.holo_blue_dark,
                            getTheme()
                    );

            dialog.findViewById(
                    android.R.id.message
            ).setAlpha(1.0f);

            android.widget.TextView message =
                    dialog.findViewById(
                            android.R.id.message
                    );

            if (message != null) {
                message.setTextColor(textColor);
            }

            int titleId =
                    getResources().getIdentifier(
                            "alertTitle",
                            "id",
                            getPackageName()
                    );

            android.widget.TextView title =
                    dialog.findViewById(titleId);

            if (title != null) {
                title.setTextColor(textColor);
            }

            dialog.getButton(
                    androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE
            ).setTextColor(buttonColor);

            dialog.getButton(
                    androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
            ).setTextColor(buttonColor);
        });

        dialog.show();

    }

    private void showErrorMessage(String errorMessage) {

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        Log.e(
                "EntryActivity",
                "error fetching sticker packs, " + errorMessage
        );

        TextView errorMessageTV =
                findViewById(R.id.error_message);

        errorMessageTV.setText(
                getString(
                        R.string.error_message,
                        errorMessage
                )
        );

        errorMessageTV.setVisibility(View.VISIBLE);
    }
    private void pickSticker() {

        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("image/webp");

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        startActivityForResult(
                intent,
                PICK_STICKER
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode != PICK_STICKER
                || resultCode != RESULT_OK
                || data == null) {
            return;
        }

        Uri uri = data.getData();

        if (uri == null) {
            return;
        }

        try {

            addStickerFromUri(uri);

        } catch (Exception e) {

            Log.e(
                    "LUCKY_STICKERS",
                    "Failed to add sticker",
                    e
            );

            showError(
                    e.getMessage()
            );
        }
    }

    private void addStickerFromUri(Uri uri)
            throws IOException {

        StickerPackManager manager =
                new StickerPackManager(this);

        String packId = selectedPackId;

        File packDirectory =
                manager.getPackDirectory(packId);

        File packJson =
                new File(
                        packDirectory,
                        "pack.json"
                );

        if (!packJson.exists()) {

            throw new IOException(
                    "Pack does not exist: "
                            + packId
            );
        }

        String fileName =
                "sticker_"
                        + System.currentTimeMillis()
                        + ".webp";

        File destination =
                new File(
                        packDirectory,
                        fileName
                );

        try (
                InputStream input =
                        getContentResolver()
                                .openInputStream(uri);

                FileOutputStream output =
                        new FileOutputStream(
                                destination
                        )
        ) {

            if (input == null) {

                throw new IOException(
                        "Unable to open selected file"
                );
            }

            byte[] buffer =
                    new byte[8192];

            int length;

            while (
                    (length = input.read(buffer)) != -1
            ) {

                output.write(
                        buffer,
                        0,
                        length
                );
            }
        }

        try {
            manager.addStickerMetadata(
                    packId,
                    fileName,
                    Arrays.asList("😀")
            );
            runOnUiThread(
                    this::loadLocalStickers
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Log.d(
                "LUCKY_STICKERS",
                "Sticker added: "
                        + destination
        );
    }

    private void showError(String message) {

        TextView errorMessage =
                findViewById(
                        R.id.error_message
                );

        errorMessage.setText(
                message != null
                        ? message
                        : "Unknown error"
        );

        errorMessage.setVisibility(
                View.VISIBLE
        );
    }
    static class LoadListAsyncTask
            extends AsyncTask<
            Void,
            Void,
            Pair<String, ArrayList<StickerPack>>> {

        private final WeakReference<EntryActivity>
                contextWeakReference;

        LoadListAsyncTask(EntryActivity activity) {
            this.contextWeakReference =
                    new WeakReference<>(activity);
        }

        @Override
        protected Pair<String, ArrayList<StickerPack>> doInBackground(Void... voids) {
            ArrayList<StickerPack> stickerPackList;
            try {
                final Context context = contextWeakReference.get();
                if (context != null) {
                    stickerPackList = StickerPackLoader.fetchStickerPacks(context);
                    if (stickerPackList.size() == 0) {
                        return new Pair<>("could not find any packs", null);
                    }
                    for (StickerPack stickerPack : stickerPackList) {
                        StickerPackValidator.verifyStickerPackValidity(context, stickerPack);
                    }
                    return new Pair<>(null, stickerPackList);
                } else {
                    return new Pair<>("could not fetch sticker packs", null);
                }
            } catch (Exception e) {
                Log.e("EntryActivity", "error fetching sticker packs", e);
                return new Pair<>(e.getMessage(), null);
            }
        }

        @Override
        protected void onPostExecute(Pair<String, ArrayList<StickerPack>> stringListPair) {

            final EntryActivity entryActivity = contextWeakReference.get();
            if (entryActivity != null) {
                if (stringListPair.first != null) {
                    entryActivity.showErrorMessage(stringListPair.first);
                } else {
                    entryActivity.showStickerPack(stringListPair.second);
                }
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (stickerRecyclerView != null) {
            loadLocalStickers();
        }
    }

}
