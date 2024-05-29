package com.example.craftastic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter implements Filterable {
    ArrayList<Craft> craftsData;
    private ArrayList<Craft> craftsFull;
    Context context;

    View.OnClickListener onClickListener;

    public HomeAdapter(ArrayList<Craft> crafts, Context context) {
        craftsData = crafts;
        craftsFull = new ArrayList<>(crafts);
        this.context = context;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        onClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_in_list, parent, false);
        HomeAdapter.HomeViewHolder holder = new HomeAdapter.HomeViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HomeAdapter.HomeViewHolder postViewHolder = (HomeAdapter.HomeViewHolder) holder;
        postViewHolder.getCraftTitleTextView().setText(craftsData.get(position).getCraftTitle());
        postViewHolder.getCraftPriceTextView().setText(String.format("$%.2f", craftsData.get(position).getPrice()));
        boolean isFavorite = craftsData.get(position).isFavorite();
        updateFavIcon(postViewHolder.getFavIcon(), isFavorite);

        byte[] imageData = craftsData.get(position).getFirstImage().getImageData();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        postViewHolder.getCraftImageView().setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return craftsData.size();
    }

    public void updateFavIcon(ImageView favIcon, boolean isFavorite) {
        if (isFavorite) {
            favIcon.setImageResource(R.drawable.favorite_filled);
            favIcon.setColorFilter(context.getColor(R.color.pink));
        } else {
            favIcon.setImageResource(R.drawable.favorite);
            favIcon.setColorFilter(context.getColor(R.color.black));
        }
    }

    @Override
    public Filter getFilter() {
        return craftFilter;
    }

    private final Filter craftFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Craft> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(craftsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Craft item : craftsFull) {
                    if (item.getCraftTitle().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            craftsData.clear();
            craftsData.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public class HomeViewHolder extends RecyclerView.ViewHolder {
        TextView craftTitleTextView, craftPriceTextView;
        ImageView craftImageView, favIcon;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            craftTitleTextView = itemView.findViewById(R.id.textViewProductName);
            craftPriceTextView = itemView.findViewById(R.id.textViewProductPrice);
            craftImageView = itemView.findViewById(R.id.imageViewProduct);
            favIcon = itemView.findViewById(R.id.buttonFavorite);
            favIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    ((MainActivity) v.getContext()).addOrRemoveFavorite(position);
                }
            });
            itemView.setTag(this);
            itemView.setOnClickListener(onClickListener);
        }

        public ImageView getFavIcon() {
            return favIcon;
        }

        public TextView getCraftTitleTextView() {
            return craftTitleTextView;
        }

        public TextView getCraftPriceTextView() {
            return craftPriceTextView;
        }

        public ImageView getCraftImageView() {
            return craftImageView;
        }


    }
}
