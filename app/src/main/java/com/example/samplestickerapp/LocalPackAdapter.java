        package com.example.samplestickerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocalPackAdapter
        extends RecyclerView.Adapter<LocalPackAdapter.ViewHolder> {

    public interface OnPackClickListener {
        void onPackClick(StickerPackData pack);
    }

    private final List<StickerPackData> packs;
    private final OnPackClickListener listener;

    public LocalPackAdapter(
            List<StickerPackData> packs,
            OnPackClickListener listener) {

        this.packs = packs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                android.R.layout.simple_list_item_2,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        StickerPackData pack = packs.get(position);

        holder.title.setText(pack.name);

        holder.subtitle.setText(
                pack.publisher
                        + " • "
                        + pack.stickers.size()
                        + " stickers"
        );

        holder.itemView.setOnClickListener(
                v -> listener.onPackClick(pack)
        );
    }

    @Override
    public int getItemCount() {
        return packs.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView subtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            title =
                    itemView.findViewById(
                            android.R.id.text1
                    );

            subtitle =
                    itemView.findViewById(
                            android.R.id.text2
                    );
        }
    }
}