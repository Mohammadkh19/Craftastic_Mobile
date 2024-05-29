package com.example.craftastic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageDetailsAdapter extends RecyclerView.Adapter<ImageDetailsAdapter.SliderViewHolder> {

    private List<byte[]> imagesData;

    public ImageDetailsAdapter(List<byte[]> imagesData) {
        this.imagesData = imagesData;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new SliderViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        if (imagesData.get(position) != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagesData.get(position), 0, imagesData.get(position).length);

            if (bitmap != null) {
                Glide.with(holder.imageView.getContext())
                        .load(bitmap)
                        .into(holder.imageView);
            } else {
                Log.e("ImageAdapter", "Bitmap decoding failed.");
            }
        } else {
            Log.e("ImageAdapter", "Image data is null.");
        }
    }

    @Override
    public int getItemCount() {
        return imagesData.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            this.imageView = itemView;
        }
    }



}

