package com.example.craftastic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Favorites extends AppCompatActivity {
    ImageView homeIcon, favoritesIcon,adsIcon, accountIcon;
    FloatingActionButton postBtn;
    FirebaseUser currentUser;
    ArrayList<Craft> crafts;
    RecyclerView craftRecyclerView;
    FavoritesAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        if (LoginRedirecting.redirectToLogin(this)) {
            finish();
            return;
        }
        initLayouts();
        Navigation.initNavButtons(homeIcon, favoritesIcon, postBtn,adsIcon, accountIcon, this);
        initRecyclerView();
    }

    private void initLayouts() {
        accountIcon = findViewById(R.id.accountIcon);
        postBtn = findViewById(R.id.postBtn);
        favoritesIcon = findViewById(R.id.favoritesIcon);
        favoritesIcon.setColorFilter(ContextCompat.getColor(this, R.color.pink));
        favoritesIcon.setEnabled(false);
        adsIcon = findViewById(R.id.adsIcon);
        homeIcon = findViewById(R.id.homeIcon);
        craftRecyclerView = findViewById(R.id.favoritesList);
    }

    private void initRecyclerView() {
        try {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            CraftsDataSource ds = new CraftsDataSource(this);
            ds.open();
            crafts = ds.getUserFavorites(currentUser.getUid());
            RecyclerView.LayoutManager layout= new LinearLayoutManager(this);
            craftRecyclerView.setLayoutManager(layout);
            adapter = new FavoritesAdapter(crafts, this);
            initAdapterOnClickListener(adapter);
            craftRecyclerView.setAdapter(adapter);
            ds.close();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initAdapterOnClickListener(FavoritesAdapter adapter){
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoritesAdapter.FavoritesViewHolder holder = (FavoritesAdapter.FavoritesViewHolder) v.getTag();
                int position = holder.getAdapterPosition();
                Intent intent = new Intent(Favorites.this, PostDetails.class);
                int craftId = crafts.get(position).getCraftId();
                intent.putExtra("craftId", craftId);
                intent.putExtra("LAST_VISITED_ACTIVITY", Favorites.class.getSimpleName());
                startActivity(intent);
            }
        });
    }

    public void removeFromFavorites(int position){
        Craft craft = crafts.get(position);
        CraftsDataSource ds = new CraftsDataSource(this);
        try {
            ds.open();
            boolean didDelete = ds.removeFromFavorites(craft.getCraftId(), currentUser.getUid());
            ds.close();
            if(didDelete){
                crafts.remove(position);
                adapter.notifyItemRemoved(position);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
        }
    }

}