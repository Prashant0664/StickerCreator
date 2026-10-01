package com.example.samplestickerapp;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class LocalStickerAdapter
        extends RecyclerView.Adapter<LocalStickerAdapter.ViewHolder> {

    private final List<File> stickers;

    private final OnStickerLongClickListener longClickListener;
    private final OnStickerClickListener clickListener;

//    private final OnStickerLongClickListener listener;
//    public LocalStickerAdapter(List<File> stickers) {
//        this.stickers = stickers;
//    }

    public LocalStickerAdapter(
            List<File> stickers,
            OnStickerClickListener clickListener,
            OnStickerLongClickListener longClickListener) {

        this.stickers = stickers;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }
//    public LocalStickerAdapter(
//            List<File> stickers,
//            OnStickerLongClickListener listener) {
//
//        this.stickers = stickers;
//        this.listener = listener;
//    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.local_sticker_item,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        File sticker = stickers.get(position);

        holder.imageView.setImageBitmap(
                BitmapFactory.decodeFile(
                        sticker.getAbsolutePath()
                )
        );

        holder.itemView.setOnClickListener(v -> {
            clickListener.onStickerClick(sticker);
        });

        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onStickerLongClick(sticker);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView =
                    itemView.findViewById(
                            R.id.sticker_image
                    );
        }
    }
    public interface OnStickerClickListener {
        void onStickerClick(File sticker);
    }
    public interface OnStickerLongClickListener {
        void onStickerLongClick(File sticker);

    }

}
