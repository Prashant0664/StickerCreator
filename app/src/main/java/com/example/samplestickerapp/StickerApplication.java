/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.samplestickerapp;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.File;
import java.util.Arrays;

public class StickerApplication extends Application {

    @Override
    public void onCreate() {
        System.out.println("cjibekj ");
        super.onCreate();
        Fresco.initialize(this);
        StickerPackManager manager =
                new StickerPackManager(this);
        try {

            File source =
                    new StickerStorage(this)
                            .getStickerFile(
                                    "1",
                                    "sticker_01.webp"
                            );

            manager.addSticker(
                    "memes",
                    source,
                    Arrays.asList("😀")
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
