package com.example.samplestickerapp;

import android.annotation.SuppressLint;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StickerPackManager {

    private final Context context;
    private final StickerStorage storage;

    public StickerPackManager(Context context) {
        this.context = context.getApplicationContext();
        this.storage = new StickerStorage(this.context);
    }

    public File getPackDirectory(String packId) {
        return storage.getPackDirectory(packId);
    }

    public File getStickerFile(
            String packId,
            String fileName
    ) {
        return storage.getStickerFile(packId, fileName);
    }

    public File getContentsFile() {
        return storage.getContentsFile();
    }

    public void createPack(
            String identifier,
            String name,
            String publisher
    ) throws IOException {

        File packDirectory =
                storage.getPackDirectory(identifier);

        File packJson =
                new File(packDirectory, "pack.json");

        if (packJson.exists()) {
            throw new IOException(
                    "Sticker pack already exists: " + identifier
            );
        }

        StickerPackData pack =
                new StickerPackData(
                        identifier,
                        name,
                        publisher,
                        "tray.webp"
                );

        savePack(pack);
    }

    @SuppressLint("NewApi")
    public void savePack(
            StickerPackData pack
    ) throws IOException {

        File packDirectory =
                storage.getPackDirectory(pack.identifier);

        File packJson =
                new File(packDirectory, "pack.json");

        JSONObject json = new JSONObject();

        try {
            json.put("identifier", pack.identifier);
            json.put("name", pack.name);
            json.put("publisher", pack.publisher);
            json.put("tray_image_file", pack.trayImageFile);

            JSONArray stickers = new JSONArray();

            for (StickerPackData.StickerData sticker :
                    pack.stickers) {

                JSONObject stickerJson = new JSONObject();

                stickerJson.put(
                        "image_file",
                        sticker.imageFile
                );

                JSONArray emojis = new JSONArray();

                for (String emoji : sticker.emojis) {
                    emojis.put(emoji);
                }

                stickerJson.put("emojis", emojis);

                stickers.put(stickerJson);
            }

            json.put("stickers", stickers);

        } catch (JSONException e) {
            throw new IOException(
                    "Unable to create pack JSON",
                    e
            );
        }

        try (
                FileOutputStream output =
                        new FileOutputStream(packJson)
        ) {
            try {
                output.write(
                        json.toString(2)
                                .getBytes(StandardCharsets.UTF_8)
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public List<StickerPackData> getAllPacks() throws Exception {
        List<StickerPackData> packs = new ArrayList<>();

        File root = storage.getRootDirectory();

        File[] directories = root.listFiles(File::isDirectory);

        if (directories == null) {
            return packs;
        }

        for (File directory : directories) {

            File packJson =
                    new File(directory, "pack.json");

            if (!packJson.exists()) {
                continue;
            }

            StickerPackData pack = loadPack(packJson);

            if (pack.stickers.isEmpty()) {
                continue;
            }

            packs.add(pack);
        }

        return packs;
    }
    private StickerPackData loadPack(
            File packJson
    ) throws Exception {

        String content;

        try {
            content = new String(
                    java.nio.file.Files.readAllBytes(
                            packJson.toPath()
                    ),
                    StandardCharsets.UTF_8
            );
            System.out.println(content);

        } catch (Exception e) {
            throw new Exception(
                    "Unable to read " + packJson,
                    e
            );
        }

        try {
            JSONObject json =
                    new JSONObject(content);

            StickerPackData pack =
                    new StickerPackData(
                            json.getString("identifier"),
                            json.getString("name"),
                            json.getString("publisher"),
                            json.getString("tray_image_file")
                    );

            JSONArray stickers =
                    json.optJSONArray("stickers");

            if (stickers != null) {

                for (int i = 0;
                     i < stickers.length();
                     i++) {

                    JSONObject stickerJson =
                            stickers.getJSONObject(i);

                    String imageFile =
                            stickerJson.getString("image_file");

                    List<String> emojis =
                            new ArrayList<>();

                    JSONArray emojiArray =
                            stickerJson.optJSONArray("emojis");

                    if (emojiArray != null) {

                        for (int j = 0;
                             j < emojiArray.length();
                             j++) {

                            emojis.add(
                                    emojiArray.getString(j)
                            );
                        }
                    }

                    pack.stickers.add(
                            new StickerPackData.StickerData(
                                    imageFile,
                                    emojis
                            )
                    );
                }
            }

            return pack;

        } catch (Exception e) {
            System.out.println(e);
            throw new IOException(
                    "Invalid pack.json: " + packJson,
                    e
            );
        }
    }
    public void deleteSticker(
            String packId,
            String fileName) throws IOException {

        File packDirectory =
                getPackDirectory(packId);

        File stickerFile =
                new File(packDirectory, fileName);

        if (stickerFile.exists()
                && !stickerFile.delete()) {

            throw new IOException(
                    "Unable to delete sticker: "
                            + fileName
            );
        }

        removeStickerMetadata(
                packId,
                fileName
        );
    }
    private void removeStickerMetadata(
            String packId,
            String fileName
    ) throws IOException {

        File packJson =
                new File(
                        storage.getPackDirectory(packId),
                        "pack.json"
                );

        StickerPackData pack;

        try {
            pack = loadPack(packJson);
        } catch (Exception e) {
            throw new IOException(
                    "Unable to load pack before deleting sticker",
                    e
            );
        }

        boolean removed = false;

        for (int i = pack.stickers.size() - 1; i >= 0; i--) {

            StickerPackData.StickerData sticker =
                    pack.stickers.get(i);

            if (fileName.equals(sticker.imageFile)) {
                pack.stickers.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) {
            throw new IOException(
                    "Sticker metadata not found: "
                            + fileName
            );
        }

        savePack(pack);

        try {
            rebuildContentsFile();
        } catch (Exception e) {
            throw new IOException(
                    "Unable to rebuild contents.json",
                    e
            );
        }

    }

    public void rebuildContentsFile()
            throws Exception {
        android.util.Log.d(
                "StickerPackManager",
                "rebuildContentsFile() CALLED"
        );

        List<StickerPackData> packs = getAllPacks();

        android.util.Log.d(
                "StickerPackManager",
                "Found packs: " + packs.size()
        );

        JSONArray stickerPacks =
                new JSONArray();

        for (StickerPackData pack : packs) {
            android.util.Log.d(
                    "StickerPackManager",
                    "Pack: " + pack.identifier
                            + " / " + pack.name
            );
            JSONObject json =
                    new JSONObject();

            try {
                json.put(
                        "identifier",
                        pack.identifier
                );

                json.put(
                        "name",
                        pack.name
                );

                json.put(
                        "publisher",
                        pack.publisher
                );

                json.put(
                        "tray_image_file",
                        pack.trayImageFile
                );

                json.put(
                        "image_data_version",
                        "1"
                );

                json.put(
                        "avoid_cache",
                        false
                );

                json.put(
                        "publisher_email",
                        ""
                );

                json.put(
                        "publisher_website",
                        ""
                );

                json.put(
                        "privacy_policy_website",
                        ""
                );

                json.put(
                        "license_agreement_website",
                        ""
                );

                JSONArray stickers =
                        new JSONArray();

                for (StickerPackData.StickerData sticker :
                        pack.stickers) {

                    JSONObject stickerJson =
                            new JSONObject();

                    stickerJson.put(
                            "image_file",
                            sticker.imageFile
                    );

                    JSONArray emojis =
                            new JSONArray();

                    for (String emoji :
                            sticker.emojis) {

                        emojis.put(emoji);
                    }

                    stickerJson.put(
                            "emojis",
                            emojis
                    );

                    stickers.put(stickerJson);
                }

                json.put(
                        "stickers",
                        stickers
                );

                stickerPacks.put(json);

            } catch (JSONException e) {

                throw new IOException(
                        "Unable to generate pack metadata",
                        e
                );
            }
        }

        JSONObject root =
                new JSONObject();

        try {
            root.put(
                    "android_play_store_link",
                    ""
            );

            root.put(
                    "ios_app_store_link",
                    ""
            );

            root.put(
                    "sticker_packs",
                    stickerPacks
            );

        } catch (JSONException e) {

            throw new IOException(
                    "Unable to generate contents.json",
                    e
            );
        }

        File contentsFile =
                storage.getContentsFile();

        android.util.Log.d(
                "StickerPackManager",
                "Writing contents to: "
                        + contentsFile.getAbsolutePath()
        );
        try (
                FileOutputStream output =
                        new FileOutputStream(contentsFile)
        ) {
            try {
                output.write(
                        root.toString(2)
                                .getBytes(StandardCharsets.UTF_8)
                );

            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }

        }
        android.util.Log.d(
                "StickerPackManager",
                "contents.json written successfully"
        );
    }
    public void addSticker(
            String packId,
            File sourceFile,
            List<String> emojis
    ) throws Exception {

        if (!sourceFile.exists()) {
            throw new IOException(
                    "Source sticker does not exist: "
                            + sourceFile.getAbsolutePath()
            );
        }

        StickerPackData pack = loadPack(
                new File(
                        storage.getPackDirectory(packId),
                        "pack.json"
                )
        );

        int stickerNumber = pack.stickers.size() + 1;

        String fileName = String.format(
                "%03d.webp",
                stickerNumber
        );

        File destination =
                storage.getStickerFile(
                        packId,
                        fileName
                );

        copyFile(sourceFile, destination);

        pack.stickers.add(
                new StickerPackData.StickerData(
                        fileName,
                        emojis
                )
        );

        savePack(pack);

        rebuildContentsFile();
    }
    private void copyFile(
            File source,
            File destination
    ) throws IOException {

        try (
                FileInputStream input =
                        new FileInputStream(source);

                FileOutputStream output =
                        new FileOutputStream(destination)
        ) {

            byte[] buffer = new byte[8192];

            int length;

            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        }
    }

    public void addStickerMetadata(
            String packId,
            String fileName,
            List<String> emojis
    ) throws Exception {

        android.util.Log.d(
                "StickerPackManager",
                "addStickerMetadata START: "
                        + packId + " / " + fileName
        );

        File packJson =
                new File(
                        storage.getPackDirectory(packId),
                        "pack.json"
                );

        android.util.Log.d(
                "StickerPackManager",
                "packJson: "
                        + packJson.getAbsolutePath()
                        + " exists="
                        + packJson.exists()
        );

        StickerPackData pack =
                loadPack(packJson);

        android.util.Log.d(
                "StickerPackManager",
                "Loaded stickers before add: "
                        + pack.stickers.size()
        );

        pack.stickers.add(
                new StickerPackData.StickerData(
                        fileName,
                        emojis
                )
        );

        android.util.Log.d(
                "StickerPackManager",
                "Stickers after add: "
                        + pack.stickers.size()
        );

        savePack(pack);

        android.util.Log.d(
                "StickerPackManager",
                "savePack DONE"
        );

        rebuildContentsFile();

        android.util.Log.d(
                "StickerPackManager",
                "rebuildContentsFile DONE"
        );
    }
    public void deletePack(String packId) throws IOException {

        File packDirectory =
                storage.getPackDirectory(packId);

        if (!packDirectory.exists()) {
            throw new IOException(
                    "Sticker pack does not exist: " + packId
            );
        }

        deleteDirectory(packDirectory);

        try {
            rebuildContentsFile();
        } catch (Exception e) {
            throw new IOException(
                    "Pack deleted, but contents.json could not be rebuilt",
                    e
            );
        }
    }

    private void deleteDirectory(File directory)
            throws IOException {

        File[] files = directory.listFiles();

        if (files != null) {

            for (File file : files) {

                if (file.isDirectory()) {

                    deleteDirectory(file);

                } else if (!file.delete()) {

                    throw new IOException(
                            "Unable to delete file: "
                                    + file.getAbsolutePath()
                    );
                }
            }
        }

        if (!directory.delete()) {

            throw new IOException(
                    "Unable to delete directory: "
                            + directory.getAbsolutePath()
            );
        }
    }



}