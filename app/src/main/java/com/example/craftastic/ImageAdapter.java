package com.example.craftastic;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter implements DragItemTouchHelper.ActionCompletionContract{
    private Context context;
    ArrayList<Uri> imageUris;
    private OnChangeListener onChangeListener;

    View.OnClickListener onClickListener;


    public ImageAdapter(Context context, ArrayList<Uri> imageUris, OnChangeListener listener) {
        this.context = context;
        this.imageUris = imageUris;
        this.onChangeListener = listener;
    }

    public interface OnChangeListener {
        void onChange();
    }

    public void setOnClickListener(View.OnClickListener listener){
        onClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        ImageViewHolder holder = new ImageViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {
        ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
        Uri uri = imageUris.get(position);
        imageViewHolder.imageView.setImageURI(uri);
    }

    @Override
    public int getItemCount() {
        return imageUris.size()<5 ? imageUris.size() : 5;
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        Uri temp = imageUris.get(oldPosition);
        imageUris.remove(oldPosition);
        imageUris.add(newPosition, temp);
        notifyItemMoved(oldPosition, newPosition);
        if (onChangeListener != null) {
            onChangeListener.onChange();
        }
    }



    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            float density = itemView.getResources().getDisplayMetrics().density;
            int dp = 80;
            int pixels = (int) ((dp * density) + 0.5);
            params.width = pixels;
            params.height = pixels;
            imageView.setLayoutParams(params);
            itemView.setOnClickListener(onClickListener);
            itemView.setTag(this);
        }
    }


}
