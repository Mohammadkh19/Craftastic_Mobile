package com.example.craftastic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter {
    ArrayList<Craft> craftsData;
    Context context;

    View.OnClickListener onClickListener;


    public void setOnClickListener(View.OnClickListener listener){
        onClickListener = listener;
    }

    public FavoritesAdapter(ArrayList<Craft> crafts, Context context) {
        craftsData = crafts;
        this.context = context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_in_list, parent, false);
        FavoritesAdapter.FavoritesViewHolder holder = new FavoritesAdapter.FavoritesViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FavoritesAdapter.FavoritesViewHolder favoriteViewHolder = (FavoritesAdapter.FavoritesViewHolder) holder;
        favoriteViewHolder.getCraftTitleTextView().setText(craftsData.get(position).getCraftTitle());
        favoriteViewHolder.getCraftPriceTextView().setText(String.format("$%.2f", craftsData.get(position).getPrice()));
        String dateString = craftsData.get(position).getPostDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = sdf.parse(dateString);
            long dateMillis = date.getTime();
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    dateMillis,
                    System.currentTimeMillis(),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            favoriteViewHolder.getPostDateTextView().setText(relativeTime);
        } catch (Exception e) {
            favoriteViewHolder.getPostDateTextView().setText(R.string.some_time_ago);
        }
        String address = getAddressFromLatLng(holder.itemView.getContext(), craftsData.get(position).getLatitude(), craftsData.get(position).getLongitude());
        favoriteViewHolder.getLocationTextView().setText(address);
        byte[] imageData = craftsData.get(position).getFirstImage().getImageData();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        favoriteViewHolder.getCraftImageView().setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return craftsData.size();
    }

    private String getAddressFromLatLng(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            return address.getAddressLine(0) ;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class FavoritesViewHolder extends RecyclerView.ViewHolder{
        TextView craftTitleTextView, craftPriceTextView, postDateTextView, locationTextView;
        ImageView craftImageView, favoritesImageView;

        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            craftTitleTextView = itemView.findViewById(R.id.textViewFavoriteTitle);
            craftPriceTextView = itemView.findViewById(R.id.textViewFavoritePrice);
            postDateTextView = itemView.findViewById(R.id.textViewFavoritePostDate);
            craftImageView = itemView.findViewById(R.id.imageViewFavoritePost);
            favoritesImageView = itemView.findViewById(R.id.imageViewFavoriteIcon);
            locationTextView = itemView.findViewById(R.id.textViewFavoriteLocation);
            favoritesImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    ((Favorites) v.getContext()).removeFromFavorites(position);
                }
            });
            itemView.setTag(this);
            itemView.setOnClickListener(onClickListener);
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

        public TextView getLocationTextView() {
            return locationTextView;
        }

        public TextView getCraftTitleTextView() {
            return craftTitleTextView;
        }
    }
}
