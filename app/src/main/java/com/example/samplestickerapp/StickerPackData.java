package com.example.samplestickerapp;

import java.util.ArrayList;
import java.util.List;

public class StickerPackData {

    public String identifier;
    public String name;
    public String publisher;
    public String trayImageFile;

    public List<StickerData> stickers = new ArrayList<>();

    public StickerPackData(
            String identifier,
            String name,
            String publisher,
            String trayImageFile
    ) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.trayImageFile = trayImageFile;
    }

    public static class StickerData {

        public String imageFile;
        public List<String> emojis = new ArrayList<>();

        public StickerData(
                String imageFile,
                List<String> emojis
        ) {
            this.imageFile = imageFile;
            this.emojis.addAll(emojis);
        }
    }
}