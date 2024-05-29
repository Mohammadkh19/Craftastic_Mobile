package com.example.craftastic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyPostsAdapter extends RecyclerView.Adapter {
    ArrayList<Craft> craftsData;
    Context context;

    View.OnClickListener onClickListener;

    public MyPostsAdapter(ArrayList<Craft> crafts, Context context) {
        craftsData = crafts;
        this.context = context;
    }

    public void setOnClickListener(View.OnClickListener listener){
        onClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mypost_in_list, parent, false);
        MyPostViewHolder holder = new MyPostViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyPostViewHolder postViewHolder = (MyPostViewHolder) holder;
        postViewHolder.getCraftTitleTextView().setText(craftsData.get(position).getCraftTitle());
        postViewHolder.getCraftPriceTextView().setText(String.format("$%.2f", craftsData.get(position).getPrice()));
        postViewHolder.getPostDateTextView().setText(String.valueOf(craftsData.get(position).getPostDate()));
        byte[] imageData = craftsData.get(position).getFirstImage().getImageData();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        postViewHolder.getCraftImageView().setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return craftsData.size();
    }

    public class MyPostViewHolder extends RecyclerView.ViewHolder{
        TextView craftTitleTextView, craftPriceTextView, postDateTextView;
        ImageView craftImageView, adViewOptions;

        public MyPostViewHolder(@NonNull View itemView) {
            super(itemView);
            craftTitleTextView = itemView.findViewById(R.id.adTitleTextView);
            craftPriceTextView = itemView.findViewById(R.id.adPriceTextView);
            postDateTextView = itemView.findViewById(R.id.adDateTextView);
            craftImageView = itemView.findViewById(R.id.adViewImage);
            adViewOptions = itemView.findViewById(R.id.adViewOptions);
            adViewOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu(v);
                }
            });
            itemView.setOnLongClickListener(v -> {
                showPopupMenu(v);
                return true;
            });
            itemView.setTag(this);
            itemView.setOnClickListener(onClickListener);
        }

        public TextView getCraftTitleTextView() {
            return craftTitleTextView;
        }

        public TextView getCraftPriceTextView() {
            return craftPriceTextView;
        }

        public TextView getPostDateTextView() {
            return postDateTextView;
        }

        public ImageView getCraftImageView() {
            return craftImageView;
        }

        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view, Gravity.END);
            popupMenu.inflate(R.menu.post_context_menu);

            popupMenu.setOnMenuItemClickListener(item -> {
                int position = getAdapterPosition();

                if(item.getItemId() == R.id.edit){
                    if (position != RecyclerView.NO_POSITION) {
                        ((My_posts) view.getContext()).editCraft(position);
                    }
                    return true;
                } else if (item.getItemId() == R.id.delete) {
                    if (position != RecyclerView.NO_POSITION) {
                        ((My_posts) view.getContext()).deleteCraft(position);
                    }
                    return true;
                }
                return false;

            });
            popupMenu.show();
        }
    }
}
