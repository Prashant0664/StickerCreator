package com.example.samplestickerapp;

import android.content.Context;

import java.io.File;

public class StickerStorage {

    private static final String ROOT_DIR = "sticker-packs";

    private final File rootDirectory;

    public StickerStorage(Context context) {
        rootDirectory = new File(context.getFilesDir(), ROOT_DIR);

        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
        }
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public File getPackDirectory(String packId) {
        File directory = new File(rootDirectory, packId);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directory;
    }

    public File getStickerFile(String packId, String fileName) {
        return new File(getPackDirectory(packId), fileName);
    }
    public void copyAssetPack(Context context, String packId) {
        File packDirectory = getPackDirectory(packId);

        try {
            String[] files = context.getAssets().list(packId);

            if (files == null) {
                return;
            }

            for (String fileName : files) {
                File destination = new File(packDirectory, fileName);

                if (destination.exists()) {
                    continue;
                }

                java.io.InputStream input =
                        context.getAssets().open(packId + "/" + fileName);

                java.io.FileOutputStream output =
                        new java.io.FileOutputStream(destination);

                byte[] buffer = new byte[8192];
                int length;

                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }

                input.close();
                output.close();
            }

        } catch (Exception e) {
            throw new RuntimeException("Unable to copy sticker pack", e);
        }
    }
    public File getContentsFile() {
        return new File(rootDirectory, "contents.json");
    }
    public void copyContentsFile(Context context) {
        File destination = getContentsFile();

        if (destination.exists()) {
            return;
        }

        try {
            java.io.InputStream input =
                    context.getAssets().open("contents.json");

            java.io.FileOutputStream output =
                    new java.io.FileOutputStream(destination);

            byte[] buffer = new byte[8192];
            int length;

            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            input.close();
            output.close();

        } catch (java.io.IOException e) {
            throw new RuntimeException(
                    "Unable to copy contents.json",
                    e
            );
        }
    }
    public void initializeFromAssets(Context context) {
        copyContentsFile(context);
        copyAssetPack(context, "1");
    }
}